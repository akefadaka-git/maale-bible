from __future__ import annotations

import re
import unicodedata
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from uuid import UUID, uuid4

from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from maale_bible_api.db.models import Account, RegistrationChallenge
from maale_bible_api.security.passwords import hash_password
from maale_bible_api.security.registration_otp import (
    OTP_MAX_ATTEMPTS,
    OTP_TTL_SECONDS,
    digest_registration_otp,
    generate_registration_otp,
)


_USERNAME_PATTERN = re.compile(r"[a-z][a-z0-9._-]{2,29}", re.ASCII)
_MOBILE_FORMATTING = re.compile(r"[\s().-]")
_E164_PATTERN = re.compile(r"\+[1-9][0-9]{7,14}", re.ASCII)


class RegistrationNotAvailable(Exception):
    """Generic outcome when an identifier cannot start a new registration."""


@dataclass(frozen=True, slots=True)
class CreatedRegistrationChallenge:
    """Internal-only delivery material; never serialize or log otp_code."""

    challenge_id: UUID
    otp_code: str
    expires_at: datetime


def normalize_registration_username(raw: str) -> str:
    if not isinstance(raw, str):
        raise ValueError("Invalid registration username.")
    normalized = unicodedata.normalize("NFKC", raw.strip()).casefold()
    if not _USERNAME_PATTERN.fullmatch(normalized):
        raise ValueError("Invalid registration username.")
    return normalized


def normalize_registration_mobile(raw: str) -> str:
    if not isinstance(raw, str):
        raise ValueError("Invalid registration mobile number.")
    normalized = _MOBILE_FORMATTING.sub("", raw.strip())
    if not _E164_PATTERN.fullmatch(normalized):
        raise ValueError("Mobile number must include an international country code.")
    return normalized


def create_registration_challenge(
    session: Session,
    *,
    username: str,
    mobile_number: str,
    password: str,
    otp_hmac_key: bytes,
    now: datetime | None = None,
) -> CreatedRegistrationChallenge:
    """Persist a pending challenge and return OTP delivery material to trusted code.

    The caller must deliver otp_code through a server-side provider and must
    never return it in an HTTP response, persist it, or write it to logs.
    This function owns the SQLAlchemy transaction and requires a clean Session.
    """
    normalized_username = normalize_registration_username(username)
    normalized_mobile = normalize_registration_mobile(mobile_number)

    if not isinstance(password, str) or not password:
        raise ValueError("Password must not be empty.")
    if not isinstance(otp_hmac_key, bytes) or len(otp_hmac_key) < 32:
        raise ValueError("Registration OTP HMAC key must contain at least 32 random bytes.")

    # hash_password applies the UTF-8 byte-length bound and Argon2id hashing.
    password_hash = hash_password(password)

    issued_at = now or datetime.now(timezone.utc)
    if issued_at.tzinfo is None or issued_at.utcoffset() is None:
        raise ValueError("Challenge timestamp must be timezone-aware.")
    issued_at = issued_at.astimezone(timezone.utc)

    challenge_id = uuid4()
    otp_code = generate_registration_otp()
    otp_digest = digest_registration_otp(
        otp_hmac_key, challenge_id, normalized_mobile, otp_code
    )
    expires_at = issued_at + timedelta(seconds=OTP_TTL_SECONDS)

    # The pre-check avoids routine duplicate challenges. Database account
    # constraints remain authoritative; this pre-check is not a concurrency lock.
    with session.begin():
        existing_account = session.scalar(
            select(Account.id).where(
                or_(
                    Account.username_normalized == normalized_username,
                    Account.mobile_e164 == normalized_mobile,
                )
            ).limit(1)
        )
        existing_challenge = session.scalar(
            select(RegistrationChallenge.id).where(
                RegistrationChallenge.consumed_at.is_(None),
                RegistrationChallenge.expires_at > issued_at,
                or_(
                    RegistrationChallenge.username_normalized == normalized_username,
                    RegistrationChallenge.mobile_e164 == normalized_mobile,
                ),
            ).limit(1)
        )
        if existing_account is not None or existing_challenge is not None:
            raise RegistrationNotAvailable("Registration cannot be started.")

        session.add(
            RegistrationChallenge(
                id=challenge_id,
                username_normalized=normalized_username,
                mobile_e164=normalized_mobile,
                password_hash=password_hash,
                otp_digest=otp_digest,
                expires_at=expires_at,
                attempts_remaining=OTP_MAX_ATTEMPTS,
                consumed_at=None,
            )
        )
        session.flush()

    return CreatedRegistrationChallenge(
        challenge_id=challenge_id,
        otp_code=otp_code,
        expires_at=expires_at,
    )
