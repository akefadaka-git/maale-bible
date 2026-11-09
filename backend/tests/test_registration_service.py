from datetime import datetime, timedelta, timezone

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import Account, RegistrationChallenge
from maale_bible_api.security.passwords import verify_password
from maale_bible_api.security.registration_otp import verify_registration_otp
from maale_bible_api.services.registration import (
    RegistrationNotAvailable,
    create_registration_challenge,
)


@pytest.fixture
def db_session():
    engine = create_engine("sqlite+pysqlite:///:memory:")
    Base.metadata.create_all(engine)
    with Session(engine) as session:
        yield session
    engine.dispose()


def test_create_challenge_hashes_password_and_persists_only_otp_digest(db_session):
    key = b"test-only-registration-key-material-32-bytes"
    now = datetime(2026, 11, 9, 12, 0, tzinfo=timezone.utc)

    created = create_registration_challenge(
        db_session,
        username=" Reader.One ",
        mobile_number="+251 911-223-344",
        password="synthetic password for tests",
        otp_hmac_key=key,
        now=now,
    )

    record = db_session.get(RegistrationChallenge, created.challenge_id)
    assert record is not None
    assert record.username_normalized == "reader.one"
    assert record.mobile_e164 == "+251911223344"
    assert record.password_hash.startswith("$argon2id$")
    assert verify_password(record.password_hash, "synthetic password for tests")
    assert record.password_hash != "synthetic password for tests"
    assert record.otp_digest != created.otp_code
    assert verify_registration_otp(
        key, created.challenge_id, "+251911223344", created.otp_code, record.otp_digest
    )
    assert record.attempts_remaining == 5
    assert record.consumed_at is None
    assert record.expires_at.replace(tzinfo=timezone.utc) == now + timedelta(minutes=5)


def test_duplicate_existing_account_is_rejected_without_new_challenge(db_session):
    db_session.add(
        Account(
            username_normalized="reader.one",
            mobile_e164="+251911223344",
            password_hash="$argon2id$synthetic-placeholder",
            status="pending",
        )
    )
    db_session.commit()

    with pytest.raises(RegistrationNotAvailable):
        create_registration_challenge(
            db_session,
            username="reader.one",
            mobile_number="+251922334455",
            password="synthetic password",
            otp_hmac_key=b"test-only-registration-key-material-32-bytes",
        )

    assert db_session.scalar(select(RegistrationChallenge.id)) is None


def test_active_unconsumed_challenge_prevents_second_challenge(db_session):
    first = create_registration_challenge(
        db_session,
        username="reader.one",
        mobile_number="+251911223344",
        password="synthetic password",
        otp_hmac_key=b"test-only-registration-key-material-32-bytes",
    )

    with pytest.raises(RegistrationNotAvailable):
        create_registration_challenge(
            db_session,
            username="reader.two",
            mobile_number="+251911223344",
            password="another synthetic password",
            otp_hmac_key=b"test-only-registration-key-material-32-bytes",
        )

    assert db_session.get(RegistrationChallenge, first.challenge_id) is not None


def test_expired_challenge_does_not_block_new_registration(db_session):
    old = datetime(2026, 11, 9, 10, 0, tzinfo=timezone.utc)
    create_registration_challenge(
        db_session,
        username="reader.one",
        mobile_number="+251911223344",
        password="synthetic password",
        otp_hmac_key=b"test-only-registration-key-material-32-bytes",
        now=old,
    )

    created = create_registration_challenge(
        db_session,
        username="reader.two",
        mobile_number="+251922334455",
        password="another synthetic password",
        otp_hmac_key=b"test-only-registration-key-material-32-bytes",
        now=old + timedelta(minutes=6),
    )
    assert db_session.get(RegistrationChallenge, created.challenge_id) is not None


@pytest.mark.parametrize(
    ("username", "mobile"),
    [
        ("ab", "+251911223344"),
        ("valid.user", "0911223344"),
        ("valid.user", "+251123"),
    ],
)
def test_invalid_registration_identifiers_are_rejected(db_session, username, mobile):
    with pytest.raises(ValueError):
        create_registration_challenge(
            db_session,
            username=username,
            mobile_number=mobile,
            password="synthetic password",
            otp_hmac_key=b"test-only-registration-key-material-32-bytes",
        )


def test_bad_otp_key_and_naive_timestamp_are_rejected(db_session):
    with pytest.raises(ValueError):
        create_registration_challenge(
            db_session,
            username="reader.one",
            mobile_number="+251911223344",
            password="synthetic password",
            otp_hmac_key=b"short",
        )

    with pytest.raises(ValueError):
        create_registration_challenge(
            db_session,
            username="reader.one",
            mobile_number="+251911223344",
            password="synthetic password",
            otp_hmac_key=b"test-only-registration-key-material-32-bytes",
            now=datetime(2026, 11, 9, 12, 0),
        )
