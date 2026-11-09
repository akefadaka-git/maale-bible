from datetime import datetime, timedelta, timezone

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import RegistrationChallenge
from maale_bible_api.services.registration import create_registration_challenge
from maale_bible_api.services.registration_verification import (
    verify_and_consume_registration_challenge,
)

OTP_KEY = b"test-only-registration-key-material-32-bytes"
NOW = datetime(2026, 11, 9, 12, 0, tzinfo=timezone.utc)


@pytest.fixture
def db_session():
    engine = create_engine("sqlite+pysqlite:///:memory:")
    Base.metadata.create_all(engine)
    with Session(engine) as session:
        yield session
    engine.dispose()


def make_challenge(session):
    return create_registration_challenge(
        session,
        username="reader.one",
        mobile_number="+251911223344",
        password="synthetic password for tests",
        otp_hmac_key=OTP_KEY,
        now=NOW,
    )


def stored_challenge(session, challenge_id):
    session.expire_all()
    return session.scalar(
        select(RegistrationChallenge).where(RegistrationChallenge.id == challenge_id)
    )


def test_valid_code_consumes_challenge_exactly_once(db_session):
    created = make_challenge(db_session)

    assert verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code=created.otp_code,
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(seconds=1),
    )
    record = stored_challenge(db_session, created.challenge_id)
    assert record.consumed_at.replace(tzinfo=timezone.utc) == NOW + timedelta(seconds=1)
    assert record.attempts_remaining == 5

    assert not verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code=created.otp_code,
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(seconds=2),
    )


def test_invalid_code_decrements_attempts_atomically(db_session):
    created = make_challenge(db_session)

    assert not verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code="000000" if created.otp_code != "000000" else "000001",
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(seconds=1),
    )
    record = stored_challenge(db_session, created.challenge_id)
    assert record.attempts_remaining == 4
    assert record.consumed_at is None


def test_fifth_invalid_attempt_exhausts_challenge(db_session):
    created = make_challenge(db_session)

    for attempt in range(5):
        assert not verify_and_consume_registration_challenge(
            db_session,
            challenge_id=created.challenge_id,
            otp_code="000000" if created.otp_code != "000000" else "000001",
            otp_hmac_key=OTP_KEY,
            now=NOW + timedelta(seconds=attempt + 1),
        )

    record = stored_challenge(db_session, created.challenge_id)
    assert record.attempts_remaining == 0
    assert record.consumed_at is None
    assert not verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code=created.otp_code,
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(seconds=10),
    )


def test_expired_challenge_is_not_consumed_or_decremented(db_session):
    created = make_challenge(db_session)

    assert not verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code=created.otp_code,
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(minutes=5),
    )
    record = stored_challenge(db_session, created.challenge_id)
    assert record.attempts_remaining == 5
    assert record.consumed_at is None


def test_unknown_challenge_and_malformed_code_fail_closed(db_session):
    created = make_challenge(db_session)

    assert not verify_and_consume_registration_challenge(
        db_session,
        challenge_id=created.challenge_id,
        otp_code=None,
        otp_hmac_key=OTP_KEY,
        now=NOW + timedelta(seconds=1),
    )
    assert stored_challenge(db_session, created.challenge_id).attempts_remaining == 4


def test_weak_key_and_naive_timestamp_are_rejected(db_session):
    created = make_challenge(db_session)

    with pytest.raises(ValueError):
        verify_and_consume_registration_challenge(
            db_session,
            challenge_id=created.challenge_id,
            otp_code=created.otp_code,
            otp_hmac_key=b"short",
            now=NOW,
        )

    with pytest.raises(ValueError):
        verify_and_consume_registration_challenge(
            db_session,
            challenge_id=created.challenge_id,
            otp_code=created.otp_code,
            otp_hmac_key=OTP_KEY,
            now=datetime(2026, 11, 9, 12, 0),
        )
