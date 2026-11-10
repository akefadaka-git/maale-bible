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


def test_simultaneous_registration_rate_limit_allows_only_quota(postgres_sessions):
    from maale_bible_api.services.registration_abuse import (
        RateLimitExceeded,
        RateLimitPolicy,
        consume_registration_rate_limit,
    )

    barrier = Barrier(3)
    policy = RateLimitPolicy(max_requests=2, window_seconds=60, block_seconds=120)

    def consume(_index):
        with postgres_sessions() as session:
            barrier.wait(timeout=10)
            try:
                consume_registration_rate_limit(
                    session,
                    scope="registration_start_ip",
                    subject="203.0.113.9",
                    secret=OTP_KEY,
                    policy=policy,
                    now=NOW,
                )
                return "allowed"
            except RateLimitExceeded:
                return "blocked"

    with ThreadPoolExecutor(max_workers=3) as pool:
        outcomes = list(pool.map(consume, range(3)))

    assert sorted(outcomes) == ["allowed", "allowed", "blocked"]


def test_simultaneous_refresh_reuse_revokes_token_family(postgres_sessions):
    import hashlib

    from sqlalchemy import select
    from maale_bible_api.db.models import Account, AuthSession
    from maale_bible_api.security.passwords import hash_password
    from maale_bible_api.services.sessions import (
        SessionPolicy,
        account_id_for_access_token,
        issue_session_for_active_account,
        rotate_refresh_token,
    )

    policy = SessionPolicy(access_ttl_seconds=900, refresh_ttl_seconds=86400)
    with postgres_sessions() as session:
        account = Account(
            username_normalized=f"refresh{uuid4().hex[:8]}",
            mobile_e164=f"+2519{uuid4().int % 10**8:08d}",
            password_hash=hash_password("synthetic refresh concurrency password"),
            status="active",
        )
        session.add(account)
        session.commit()
        issued = issue_session_for_active_account(
            session, account_id=account.id, policy=policy, now=NOW
        )
        assert issued is not None
        family_id = session.scalar(
            select(AuthSession.token_family_id).where(
                AuthSession.refresh_token_hash == hashlib.sha256(
                    issued.refresh_token.encode("ascii")
                ).hexdigest()
            )
        )
        assert family_id is not None

    barrier = Barrier(2)

    def refresh():
        with postgres_sessions() as session:
            barrier.wait(timeout=10)
            return rotate_refresh_token(
                session,
                refresh_token=issued.refresh_token,
                policy=policy,
                now=NOW + timedelta(seconds=1),
            )

    with ThreadPoolExecutor(max_workers=2) as pool:
        outcomes = list(pool.map(lambda _: refresh(), range(2)))

    successes = [value for value in outcomes if value is not None]
    assert len(successes) == 1
    rotated = successes[0]
    with postgres_sessions() as session:
        assert account_id_for_access_token(
            session, access_token=rotated.access_token, now=NOW + timedelta(seconds=2)
        ) is None
        family_rows = session.scalars(
            select(AuthSession).where(AuthSession.token_family_id == family_id)
        ).all()
        assert family_rows
        assert all(row.revoked_at is not None for row in family_rows)
