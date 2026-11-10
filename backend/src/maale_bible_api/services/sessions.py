from __future__ import annotations

import hashlib
import secrets
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from uuid import UUID, uuid4

from sqlalchemy import select, update
from sqlalchemy.orm import Session

from maale_bible_api.db.models import Account, AuthSession


@dataclass(frozen=True, slots=True)
class SessionPolicy:
    """Explicit operator-supplied token lifetimes; no silent production defaults."""

    access_ttl_seconds: int
    refresh_ttl_seconds: int

    def __post_init__(self) -> None:
        if self.access_ttl_seconds < 60 or self.refresh_ttl_seconds < self.access_ttl_seconds:
            raise ValueError("Session token lifetimes are invalid.")


@dataclass(frozen=True, slots=True)
class IssuedSession:
    account_id: UUID
    session_id: UUID
    access_token: str
    refresh_token: str
    access_expires_at: datetime
    refresh_expires_at: datetime


def _token_hash(token: str) -> str:
    return hashlib.sha256(token.encode("ascii")).hexdigest()


def _new_tokens() -> tuple[str, str]:
    # 256 bits of OS-backed entropy per opaque bearer credential.
    return secrets.token_urlsafe(32), secrets.token_urlsafe(32)


def _issue_session_record(
    session: Session,
    *,
    account_id: UUID,
    policy: SessionPolicy,
    now: datetime,
    token_family_id: UUID | None = None,
) -> IssuedSession:
    access_token, refresh_token = _new_tokens()
    session_id = uuid4()
    access_expires_at = now + timedelta(seconds=policy.access_ttl_seconds)
    refresh_expires_at = now + timedelta(seconds=policy.refresh_ttl_seconds)
    session.add(
        AuthSession(
            id=session_id,
            account_id=account_id,
            token_family_id=token_family_id or uuid4(),
            access_token_hash=_token_hash(access_token),
            refresh_token_hash=_token_hash(refresh_token),
            access_expires_at=access_expires_at,
            expires_at=refresh_expires_at,
            rotated_at=None,
            revoked_at=None,
        )
    )
    session.flush()
    return IssuedSession(
        account_id=account_id,
        session_id=session_id,
        access_token=access_token,
        refresh_token=refresh_token,
        access_expires_at=access_expires_at,
        refresh_expires_at=refresh_expires_at,
    )


def issue_session_for_active_account(
    session: Session,
    *,
    account_id: UUID,
    policy: SessionPolicy,
    now: datetime | None = None,
) -> IssuedSession | None:
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Session timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)
    with session.begin():
        account = session.scalar(
            select(Account).where(Account.id == account_id).with_for_update()
        )
        if account is None or account.status != "active":
            return None
        return _issue_session_record(
            session, account_id=account.id, policy=policy, now=instant
        )


def rotate_refresh_token(
    session: Session,
    *,
    refresh_token: str,
    policy: SessionPolicy,
    now: datetime | None = None,
) -> IssuedSession | None:
    """Rotate once; reuse of a rotated credential revokes its entire token family."""
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Session timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)
    if not isinstance(refresh_token, str) or not refresh_token.isascii():
        return None

    with session.begin():
        record = session.scalar(
            select(AuthSession)
            .where(AuthSession.refresh_token_hash == _token_hash(refresh_token))
            .with_for_update()
        )
        if record is None:
            return None

        if record.rotated_at is not None:
            session.execute(
                update(AuthSession)
                .where(
                    AuthSession.token_family_id == record.token_family_id,
                    AuthSession.revoked_at.is_(None),
                )
                .values(revoked_at=instant)
            )
            return None

        if record.revoked_at is not None or record.expires_at <= instant:
            return None

        account = session.scalar(
            select(Account).where(Account.id == record.account_id).with_for_update()
        )
        if account is None or account.status != "active":
            session.execute(
                update(AuthSession)
                .where(
                    AuthSession.token_family_id == record.token_family_id,
                    AuthSession.revoked_at.is_(None),
                )
                .values(revoked_at=instant)
            )
            return None

        record.rotated_at = instant
        return _issue_session_record(
            session,
            account_id=record.account_id,
            policy=policy,
            now=instant,
            token_family_id=record.token_family_id,
        )


def account_id_for_access_token(
    session: Session,
    *,
    access_token: str,
    now: datetime | None = None,
) -> UUID | None:
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Session timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)
    if not isinstance(access_token, str) or not access_token.isascii():
        return None
    with session.begin():
        return session.scalar(
            select(AuthSession.account_id)
            .join(Account, Account.id == AuthSession.account_id)
            .where(
                AuthSession.access_token_hash == _token_hash(access_token),
                AuthSession.access_expires_at > instant,
                AuthSession.rotated_at.is_(None),
                AuthSession.revoked_at.is_(None),
                Account.status == "active",
            )
        )


def revoke_session_for_access_token(
    session: Session,
    *,
    access_token: str,
    now: datetime | None = None,
) -> bool:
    """Logout revokes the complete refresh-token family for this device session."""
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Session timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)
    if not isinstance(access_token, str) or not access_token.isascii():
        return False
    with session.begin():
        record = session.scalar(
            select(AuthSession)
            .where(AuthSession.access_token_hash == _token_hash(access_token))
            .with_for_update()
        )
        if record is None or record.revoked_at is not None:
            return False
        result = session.execute(
            update(AuthSession)
            .where(
                AuthSession.token_family_id == record.token_family_id,
                AuthSession.revoked_at.is_(None),
            )
            .values(revoked_at=instant)
        )
        return result.rowcount > 0
