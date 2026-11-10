from datetime import datetime, timedelta, timezone

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import Account, AuthSession
from maale_bible_api.security.passwords import hash_password
from maale_bible_api.security.registration_otp import verify_registration_otp
from maale_bible_api.services.login import login_with_password
from maale_bible_api.services.registration import create_registration_challenge
from maale_bible_api.services.registration_activation import verify_and_activate_registration
from maale_bible_api.services.sessions import (
    SessionPolicy,
    account_id_for_access_token,
    issue_session_for_active_account,
    revoke_session_for_access_token,
    rotate_refresh_token,
)

KEY = b"test-only-registration-key-material-32-bytes"
NOW = datetime(2026, 11, 10, 12, 0, tzinfo=timezone.utc)
POLICY = SessionPolicy(access_ttl_seconds=900, refresh_ttl_seconds=86400)
DUMMY_HASH = hash_password("synthetic dummy password used for timing equalization")


@pytest.fixture
def db():
    engine = create_engine("sqlite+pysqlite:///:memory:")
    Base.metadata.create_all(engine)
    from sqlalchemy.orm import sessionmaker
    factory = sessionmaker(engine, expire_on_commit=False)
    with factory() as session:
        yield session
    engine.dispose()


def active_account(session):
    account = Account(
        username_normalized="reader.one",
        mobile_e164="+251911223344",
        password_hash=hash_password("correct synthetic password"),
        status="active",
    )
    session.add(account)
    session.commit()
    return account


def test_session_issue_access_validation_refresh_rotation_and_replay_revocation(db):
    account = active_account(db)
    first = issue_session_for_active_account(
        db, account_id=account.id, policy=POLICY, now=NOW
    )
    assert first is not None
    assert account_id_for_access_token(db, access_token=first.access_token, now=NOW) == account.id

    rotated = rotate_refresh_token(
        db, refresh_token=first.refresh_token, policy=POLICY, now=NOW + timedelta(minutes=1)
    )
    assert rotated is not None
    assert account_id_for_access_token(
        db, access_token=first.access_token, now=NOW + timedelta(minutes=1)
    ) is None
    assert account_id_for_access_token(
        db, access_token=rotated.access_token, now=NOW + timedelta(minutes=1)
    ) == account.id

    # Reusing the old refresh credential revokes the entire family, including
    # the just-issued generation.
    assert rotate_refresh_token(
        db, refresh_token=first.refresh_token, policy=POLICY, now=NOW + timedelta(minutes=2)
    ) is None
    assert account_id_for_access_token(
        db, access_token=rotated.access_token, now=NOW + timedelta(minutes=2)
    ) is None


def test_logout_revokes_current_device_session_family(db):
    account = active_account(db)
    issued = issue_session_for_active_account(
        db, account_id=account.id, policy=POLICY, now=NOW
    )
    assert issued is not None
    assert revoke_session_for_access_token(
        db, access_token=issued.access_token, now=NOW + timedelta(seconds=1)
    )
    assert account_id_for_access_token(
        db, access_token=issued.access_token, now=NOW + timedelta(seconds=2)
    ) is None
    assert rotate_refresh_token(
        db, refresh_token=issued.refresh_token, policy=POLICY, now=NOW + timedelta(seconds=2)
    ) is None


def test_login_accepts_username_or_mobile_and_never_uses_otp(db):
    account = active_account(db)
    by_username = login_with_password(
        db, identifier="Reader.One", password="correct synthetic password",
        dummy_password_hash=DUMMY_HASH, policy=POLICY, now=NOW,
    )
    assert by_username is not None and by_username.account_id == account.id

    by_mobile = login_with_password(
        db, identifier="+251 911-223-344", password="correct synthetic password",
        dummy_password_hash=DUMMY_HASH, policy=POLICY, now=NOW + timedelta(seconds=1),
    )
    assert by_mobile is not None and by_mobile.account_id == account.id


def test_login_returns_same_failure_for_unknown_and_wrong_password(db):
    active_account(db)
    unknown = login_with_password(
        db, identifier="unknown.user", password="incorrect password",
        dummy_password_hash=DUMMY_HASH, policy=POLICY, now=NOW,
    )
    wrong = login_with_password(
        db, identifier="reader.one", password="incorrect password",
        dummy_password_hash=DUMMY_HASH, policy=POLICY, now=NOW,
    )
    assert unknown is None
    assert wrong is None


def test_registration_otp_activation_and_first_session_are_atomic(db):
    challenge = create_registration_challenge(
        db, username="new.reader", mobile_number="+251922334455",
        password="synthetic registration password", otp_hmac_key=KEY, now=NOW,
    )
    activated = verify_and_activate_registration(
        db, challenge_id=challenge.challenge_id, otp_code=challenge.otp_code,
        otp_hmac_key=KEY, session_policy=POLICY, now=NOW + timedelta(seconds=1),
    )
    assert activated is not None
    assert activated.session.account_id == activated.account_id
    account = db.get(Account, activated.account_id)
    assert account is not None and account.status == "active"
    assert account_id_for_access_token(
        db, access_token=activated.session.access_token, now=NOW + timedelta(seconds=2)
    ) == activated.account_id

    # The challenge is single-use and the second verification cannot create
    # another account or session.
    assert verify_and_activate_registration(
        db, challenge_id=challenge.challenge_id, otp_code=challenge.otp_code,
        otp_hmac_key=KEY, session_policy=POLICY, now=NOW + timedelta(seconds=3),
    ) is None
    assert db.scalar(select(Account.id).where(Account.username_normalized == "new.reader")) == activated.account_id


def test_pending_account_cannot_receive_a_session(db):
    account = Account(
        username_normalized="pending.reader",
        mobile_e164="+251933445566",
        password_hash=hash_password("synthetic password"),
        status="pending",
    )
    db.add(account)
    db.commit()
    assert issue_session_for_active_account(
        db, account_id=account.id, policy=POLICY, now=NOW
    ) is None
