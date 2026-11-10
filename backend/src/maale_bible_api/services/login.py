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
    if not isinstance(password, str) or not password or len(password.encode("utf-8")) > 1024:
        return None
    if not isinstance(dummy_password_hash, str) or not dummy_password_hash:
        return None

    account_id = None
    account_status = None
    stored_hash = None
    try:
        if not isinstance(identifier, str):
            raise ValueError("Invalid identifier.")
        is_mobile = identifier.strip().startswith("+")
        normalized = (
            normalize_registration_mobile(identifier)
            if is_mobile
            else normalize_registration_username(identifier)
        )
        with session.begin():
            account = session.scalar(
                select(Account).where(
                    Account.mobile_e164 == normalized if is_mobile
                    else Account.username_normalized == normalized
                )
            )
            if account is not None:
                account_id = account.id
                account_status = account.status
                stored_hash = account.password_hash
    except ValueError:
        account_id = None

    # Unknown and disabled identities still incur a password-hash verification.
    password_valid = verify_password(stored_hash or dummy_password_hash, password)
    if account_id is None or account_status != "active" or not password_valid:
        return None

    return issue_session_for_active_account(
        session, account_id=account_id, policy=policy, now=now
    )
