from __future__ import annotations

import hashlib
import hmac
import math
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone

from sqlalchemy import case, or_
from sqlalchemy.dialects.postgresql import insert as pg_insert
from sqlalchemy.dialects.sqlite import insert as sqlite_insert
from sqlalchemy.orm import Session

from maale_bible_api.db.models import RegistrationRateLimitBucket


@dataclass(frozen=True, slots=True)
class RateLimitPolicy:
    """Caller-supplied quota; no production thresholds are silently assumed."""

    max_requests: int
    window_seconds: int
    block_seconds: int

    def __post_init__(self) -> None:
        if self.max_requests < 1 or self.window_seconds < 1 or self.block_seconds < 1:
            raise ValueError("Rate-limit policy values must be positive.")


class RateLimitExceeded(Exception):
    def __init__(self, retry_after_seconds: int):
        self.retry_after_seconds = max(1, retry_after_seconds)
        super().__init__("Request rate limit exceeded.")


def _subject_digest(secret: bytes, scope: str, subject: str) -> str:
    if not isinstance(secret, bytes) or len(secret) < 32:
        raise ValueError("Rate-limit HMAC key must contain at least 32 random bytes.")
    if not isinstance(scope, str) or not scope or len(scope) > 64:
        raise ValueError("Rate-limit scope is invalid.")
    if not isinstance(subject, str) or not subject:
        raise ValueError("Rate-limit subject is invalid.")
    message = f"maale-bible:registration-rate-limit:v1:{scope}:{subject}".encode("utf-8")
    return hmac.new(secret, message, hashlib.sha256).hexdigest()


def consume_registration_rate_limit(
    session: Session,
    *,
    scope: str,
    subject: str,
    secret: bytes,
    policy: RateLimitPolicy,
    now: datetime | None = None,
) -> None:
    """Atomically consume one quota unit or raise RateLimitExceeded.

    PostgreSQL and SQLite use native UPSERTs, so independent requests cannot
    both pass a quota check based on stale read-then-write state. The caller
    must supply a clean SQLAlchemy Session; this function owns its transaction.
    Only a keyed digest of the subject is persisted, never the raw phone/IP/device.
    """
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Rate-limit timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)
    subject_hash = _subject_digest(secret, scope, subject)
    table = RegistrationRateLimitBucket.__table__
    dialect = session.get_bind().dialect.name
    if dialect == "postgresql":
        insert_statement = pg_insert(table)
    elif dialect == "sqlite":
        insert_statement = sqlite_insert(table)
    else:
        raise RuntimeError("Registration rate limiting requires PostgreSQL or SQLite.")

    reset_window = or_(
        table.c.window_started_at <= instant - timedelta(seconds=policy.window_seconds),
        table.c.blocked_until.is_not(None) & (table.c.blocked_until <= instant),
    )
    next_count = case((reset_window, 1), else_=table.c.request_count + 1)
    next_start = case((reset_window, instant), else_=table.c.window_started_at)
    next_block = case(
        (reset_window, None),
        (table.c.blocked_until.is_not(None), table.c.blocked_until),
        (table.c.request_count >= policy.max_requests,
         instant + timedelta(seconds=policy.block_seconds)),
        else_=None,
    )

    statement = insert_statement.values(
        scope=scope,
        subject_hash=subject_hash,
        window_started_at=instant,
        request_count=1,
        blocked_until=None,
        updated_at=instant,
    ).on_conflict_do_update(
        index_elements=[table.c.scope, table.c.subject_hash],
        set_={
            "window_started_at": next_start,
            "request_count": next_count,
            "blocked_until": next_block,
            "updated_at": instant,
        },
    ).returning(table.c.request_count, table.c.blocked_until)

    exceeded = False
    retry_after = policy.window_seconds
    with session.begin():
        row = session.execute(statement).one()
        blocked_until = row.blocked_until
        # SQLite drops timezone metadata for DateTime(timezone=True); restore
        # the UTC contract before comparing with the aware request timestamp.
        if blocked_until is not None and blocked_until.tzinfo is None:
            blocked_until = blocked_until.replace(tzinfo=timezone.utc)
        exceeded = row.request_count > policy.max_requests or (
            blocked_until is not None and blocked_until > instant
        )
        if exceeded:
            retry_after = (
                math.ceil((blocked_until - instant).total_seconds())
                if blocked_until is not None
                else policy.window_seconds
            )
    # Commit the block state before raising; raising inside session.begin()
    # would roll back the very quota update that must reject the next request.
    if exceeded:
        raise RateLimitExceeded(retry_after)
