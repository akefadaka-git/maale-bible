from datetime import datetime, timedelta, timezone
from uuid import uuid4

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import Account, AuthSession, RegistrationChallenge


@pytest.fixture
def db_session():
    engine = create_engine("sqlite+pysqlite:///:memory:")
    Base.metadata.create_all(engine)
    with Session(engine) as session:
        yield session
    engine.dispose()


def test_account_has_server_uuid_and_unique_normalized_identifiers(db_session):
    account = Account(
        username_normalized="reader.one",
        mobile_e164="+251911223344",
        password_hash="$argon2id$synthetic-test-hash",
    )
    db_session.add(account)
    db_session.commit()

    assert account.id is not None
    assert account.status == "active"
    assert db_session.scalar(select(Account).where(Account.id == account.id)) is account

    db_session.add(Account(
        username_normalized="reader.one",
        mobile_e164="+251922334455",
        password_hash="$argon2id$synthetic-test-hash",
    ))
    with pytest.raises(IntegrityError):
        db_session.commit()
    db_session.rollback()


def test_registration_challenge_stores_digest_not_plain_otp(db_session):
    challenge = RegistrationChallenge(
        username_normalized="reader.two",
        mobile_e164="+251911223355",
        password_hash="$argon2id$synthetic-test-hash",
        otp_digest="a" * 64,
        expires_at=datetime.now(timezone.utc) + timedelta(minutes=5),
        attempts_remaining=5,
    )
    db_session.add(challenge)
    db_session.commit()

    assert challenge.otp_digest == "a" * 64
    assert not hasattr(challenge, "otp_code")
    assert challenge.consumed_at is None


def test_session_refresh_hash_is_unique_and_bound_to_account(db_session):
    account = Account(
        username_normalized="reader.three",
        mobile_e164="+251911223366",
        password_hash="$argon2id$synthetic-test-hash",
    )
    db_session.add(account)
    db_session.flush()
    session = AuthSession(
        account_id=account.id,
        token_family_id=uuid4(),
        refresh_token_hash="b" * 64,
        expires_at=datetime.now(timezone.utc) + timedelta(days=14),
    )
    db_session.add(session)
    db_session.commit()

    assert session.account_id == account.id
    assert session.revoked_at is None
