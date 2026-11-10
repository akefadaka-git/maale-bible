from __future__ import annotations

import os
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timedelta, timezone
from threading import Barrier
from uuid import uuid4

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session, sessionmaker

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import RegistrationChallenge
from maale_bible_api.services.registration import (
    RegistrationNotAvailable,
    create_registration_challenge,
)
from maale_bible_api.services.registration_verification import (
    verify_and_consume_registration_challenge,
)

POSTGRES_TEST_URL = os.environ.get("POSTGRES_TEST_URL")
pytestmark = pytest.mark.skipif(
    not POSTGRES_TEST_URL,
    reason="POSTGRES_TEST_URL is required for PostgreSQL concurrency integration tests",
)
OTP_KEY = b"postgres-concurrency-test-key-material-32-bytes"
NOW = datetime(2026, 11, 10, 12, 0, tzinfo=timezone.utc)


@pytest.fixture
def postgres_sessions():
    engine = create_engine(POSTGRES_TEST_URL, pool_size=6, max_overflow=2)
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)
    factory = sessionmaker(engine, expire_on_commit=False)
    try:
        yield factory
    finally:
        Base.metadata.drop_all(engine)
        engine.dispose()


def test_simultaneous_valid_otp_verification_consumes_once(postgres_sessions):
    with postgres_sessions() as session:
        created = create_registration_challenge(
            session,
            username=f"reader{uuid4().hex[:8]}",
            mobile_number="+251911223344",
            password="synthetic concurrency test password",
            otp_hmac_key=OTP_KEY,
            now=NOW,
        )

    barrier = Barrier(2)

    def verify():
        with postgres_sessions() as session:
            barrier.wait(timeout=10)
            return verify_and_consume_registration_challenge(
                session,
                challenge_id=created.challenge_id,
                otp_code=created.otp_code,
                otp_hmac_key=OTP_KEY,
                now=NOW + timedelta(seconds=1),
            )

    with ThreadPoolExecutor(max_workers=2) as pool:
        outcomes = list(pool.map(lambda _: verify(), range(2)))

    assert sorted(outcomes) == [False, True]
    with postgres_sessions() as session:
        record = session.get(RegistrationChallenge, created.challenge_id)
        assert record is not None
        assert record.consumed_at is not None
        assert record.attempts_remaining == 5


@pytest.mark.parametrize("shared_identifier", ["username", "mobile"])
def test_simultaneous_duplicate_registration_reservation_is_serialized(
    postgres_sessions, shared_identifier
):
    suffix = uuid4().hex[:8]
    barrier = Barrier(2)

    def reserve(index: int):
        username = "shared.user" if shared_identifier == "username" else f"reader{index}{suffix}"
        mobile = "+251911223344" if shared_identifier == "mobile" else f"+2519112233{index}0"
        with postgres_sessions() as session:
            barrier.wait(timeout=10)
            try:
                result = create_registration_challenge(
                    session,
                    username=username,
                    mobile_number=mobile,
                    password="synthetic concurrency test password",
                    otp_hmac_key=OTP_KEY,
                    now=NOW,
                )
                return ("created", result.challenge_id)
            except RegistrationNotAvailable:
                return ("rejected", None)

    with ThreadPoolExecutor(max_workers=2) as pool:
        outcomes = list(pool.map(reserve, (1, 2)))

    assert sorted(outcome[0] for outcome in outcomes) == ["created", "rejected"]
    with postgres_sessions() as session:
        active = session.scalars(
            select(RegistrationChallenge).where(
                RegistrationChallenge.consumed_at.is_(None),
                RegistrationChallenge.expires_at > NOW,
            )
        ).all()
        assert len(active) == 1
