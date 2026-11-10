from __future__ import annotations

from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.orm import Session

from maale_bible_api.db.models import Account
from maale_bible_api.security.passwords import verify_password
from maale_bible_api.services.registration import (
    normalize_registration_mobile,
    normalize_registration_username,
)
from maale_bible_api.services.sessions import IssuedSession, SessionPolicy, issue_session_for_active_account


def login_with_password(
    session: Session,
    *,
    identifier: str,
    password: str,
    dummy_password_hash: str,
    policy: SessionPolicy,
    now: datetime | None = None,
) -> IssuedSession | None:
    """Authenticate username-or-E.164 plus password with a generic failure result.

    The caller must supply a valid server-generated dummy Argon2id hash so an
    unknown identifier still performs password verification. No OTP is used
    during ordinary login.
    """
    if not isinstance(password, str) or not password or not isinstance(dummy_password_hash, str):
        return None

    account = None
    try:
        if not isinstance(identifier, str):
            raise ValueError("Invalid identifier.")
        normalized = (
            normalize_registration_mobile(identifier)
            if identifier.strip().startswith("+")
            else normalize_registration_username(identifier)
        )
        with session.begin():
            if identifier.strip().startswith("+"):
                account = session.scalar(
                    select(Account).where(Account.mobile_e164 == normalized)
                )
            else:
                account = session.scalar(
                    select(Account).where(Account.username_normalized == normalized)
                )
    except ValueError:
        account = None

    candidate_hash = account.password_hash if account is not None else dummy_password_hash
    password_valid = verify_password(candidate_hash, password)
    if account is None or account.status != "active" or not password_valid:
        return None

    return issue_session_for_active_account(
        session, account_id=account.id, policy=policy, now=now
    )
