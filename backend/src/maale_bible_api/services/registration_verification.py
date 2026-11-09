from __future__ import annotations

from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import select, update
from sqlalchemy.orm import Session

from maale_bible_api.db.models import RegistrationChallenge
from maale_bible_api.security.registration_otp import verify_registration_otp


def verify_and_consume_registration_challenge(
    session: Session,
    *,
    challenge_id: UUID,
    otp_code: str,
    otp_hmac_key: bytes,
    now: datetime | None = None,
) -> bool:
    """Verify one registration OTP and atomically consume its challenge.

    Returns False for an unknown, expired, exhausted, consumed, or incorrect
    challenge. Invalid codes consume one attempt. A valid code consumes the
    challenge exactly once. This function never creates or activates an account.

    The caller must supply a clean SQLAlchemy Session. PostgreSQL obtains a
    row lock with SELECT FOR UPDATE; the conditional UPDATE is also the
    authoritative compare-and-set for databases whose dialect ignores it
    (including SQLite). The update predicates prevent replay and prevent a
    stale concurrent verifier from changing an already-consumed challenge.
    """
    if not isinstance(otp_hmac_key, bytes) or len(otp_hmac_key) < 32:
        raise ValueError("Registration OTP HMAC key must contain at least 32 random bytes.")

    verified_at = now or datetime.now(timezone.utc)
    if verified_at.tzinfo is None or verified_at.utcoffset() is None:
        raise ValueError("Verification timestamp must be timezone-aware.")
    verified_at = verified_at.astimezone(timezone.utc)

    with session.begin():
        challenge = session.scalar(
            select(RegistrationChallenge)
            .where(RegistrationChallenge.id == challenge_id)
            .with_for_update()
        )
        if challenge is None:
            return False

        valid_code = (
            isinstance(otp_code, str)
            and verify_registration_otp(
                otp_hmac_key,
                challenge.id,
                challenge.mobile_e164,
                otp_code,
                challenge.otp_digest,
            )
        )

        eligible = (
            update(RegistrationChallenge)
            .where(
                RegistrationChallenge.id == challenge_id,
                RegistrationChallenge.otp_digest == challenge.otp_digest,
                RegistrationChallenge.consumed_at.is_(None),
                RegistrationChallenge.expires_at > verified_at,
                RegistrationChallenge.attempts_remaining > 0,
            )
        )
        if valid_code:
            result = session.execute(
                eligible.values(consumed_at=verified_at).execution_options(
                    synchronize_session=False
                )
            )
            return result.rowcount == 1

        result = session.execute(
            eligible.values(
                attempts_remaining=RegistrationChallenge.attempts_remaining - 1
            ).execution_options(synchronize_session=False)
        )
        return False
