from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from maale_bible_api.db.models import Account, RegistrationChallenge
from maale_bible_api.security.registration_otp import verify_registration_otp
from maale_bible_api.services.sessions import (
    IssuedSession,
    SessionPolicy,
    _issue_session_record,
)


@dataclass(frozen=True, slots=True)
class ActivatedRegistration:
    account_id: UUID
    session: IssuedSession


def verify_and_activate_registration(
    session: Session,
    *,
    challenge_id: UUID,
    otp_code: str,
    otp_hmac_key: bytes,
    session_policy: SessionPolicy,
    now: datetime | None = None,
) -> ActivatedRegistration | None:
    """Atomically consume OTP, activate the account, and issue its first session.

    Returns the same None outcome for unknown, invalid, expired, exhausted,
    consumed, or duplicate-identity challenges. The caller must provide a clean
    SQLAlchemy Session. This service does not send SMS or expose an HTTP route.
    """
    if not isinstance(otp_hmac_key, bytes) or len(otp_hmac_key) < 32:
        raise ValueError("Registration OTP HMAC key must contain at least 32 random bytes.")
    instant = now or datetime.now(timezone.utc)
    if instant.tzinfo is None or instant.utcoffset() is None:
        raise ValueError("Registration timestamp must be timezone-aware.")
    instant = instant.astimezone(timezone.utc)

    try:
        with session.begin():
            challenge = session.scalar(
                select(RegistrationChallenge)
                .where(RegistrationChallenge.id == challenge_id)
                .with_for_update()
            )
            if (
                challenge is None
                or challenge.consumed_at is not None
                or challenge.expires_at <= instant
                or challenge.attempts_remaining <= 0
            ):
                return None

            valid = (
                isinstance(otp_code, str)
                and verify_registration_otp(
                    otp_hmac_key,
                    challenge.id,
                    challenge.mobile_e164,
                    otp_code,
                    challenge.otp_digest,
                )
            )
            if not valid:
                challenge.attempts_remaining -= 1
                session.flush()
                return None

            account = Account(
                username_normalized=challenge.username_normalized,
                mobile_e164=challenge.mobile_e164,
                password_hash=challenge.password_hash,
                status="active",
            )
            session.add(account)
            session.flush()
            challenge.consumed_at = instant
            session.flush()
            issued = _issue_session_record(
                session,
                account_id=account.id,
                policy=session_policy,
                now=instant,
            )
            return ActivatedRegistration(account_id=account.id, session=issued)
    except IntegrityError:
        # Unique username/mobile constraints are authoritative. The transaction
        # rolls back both account activation and challenge consumption.
        return None
