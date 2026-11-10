from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timedelta, timezone

import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

from maale_bible_api.db.base import Base
from maale_bible_api.db.models import RegistrationRateLimitBucket
from maale_bible_api.services.registration_abuse import (
    RateLimitExceeded,
    RateLimitPolicy,
    consume_registration_rate_limit,
)

KEY = b"test-only-rate-limit-hmac-key-material-32-bytes"
NOW = datetime(2026, 11, 10, 12, 0, tzinfo=timezone.utc)
POLICY = RateLimitPolicy(max_requests=2, window_seconds=60, block_seconds=120)


@pytest.fixture
def db():
    engine = create_engine("sqlite+pysqlite:///:memory:")
    Base.metadata.create_all(engine)
    # Use one shared connection for deterministic sequential SQLite tests.
    with Session(engine) as session:
        yield session
    engine.dispose()


def test_allows_quota_then_blocks_and_returns_retry_after(db):
    consume_registration_rate_limit(
        db, scope="registration_start_ip", subject="192.0.2.10",
        secret=KEY, policy=POLICY, now=NOW,
    )
    consume_registration_rate_limit(
        db, scope="registration_start_ip", subject="192.0.2.10",
        secret=KEY, policy=POLICY, now=NOW + timedelta(seconds=1),
    )
    with pytest.raises(RateLimitExceeded) as exc:
        consume_registration_rate_limit(
            db, scope="registration_start_ip", subject="192.0.2.10",
            secret=KEY, policy=POLICY, now=NOW + timedelta(seconds=2),
        )
    assert exc.value.retry_after_seconds == 120


def test_expired_window_resets_quota(db):
    for second in (0, 1):
        consume_registration_rate_limit(
            db, scope="registration_start_mobile", subject="+251911223344",
            secret=KEY, policy=POLICY, now=NOW + timedelta(seconds=second),
        )
    with pytest.raises(RateLimitExceeded):
        consume_registration_rate_limit(
            db, scope="registration_start_mobile", subject="+251911223344",
            secret=KEY, policy=POLICY, now=NOW + timedelta(seconds=2),
        )
    consume_registration_rate_limit(
        db, scope="registration_start_mobile", subject="+251911223344",
        secret=KEY, policy=POLICY, now=NOW + timedelta(seconds=123),
    )


def test_subjects_are_stored_only_as_keyed_digests(db):
    raw_subject = "+251911223344"
    consume_registration_rate_limit(
        db, scope="registration_resend_mobile", subject=raw_subject,
        secret=KEY, policy=RateLimitPolicy(1, 60, 60), now=NOW,
    )
    row = db.scalar(select(RegistrationRateLimitBucket))
    assert row is not None
    assert raw_subject not in row.subject_hash
    assert len(row.subject_hash) == 64


@pytest.mark.parametrize(
    "values",
    [(0, 60, 60), (1, 0, 60), (1, 60, 0)],
)
def test_policy_rejects_nonpositive_values(values):
    with pytest.raises(ValueError):
        RateLimitPolicy(*values)


def test_naive_timestamp_and_weak_key_fail_closed(db):
    with pytest.raises(ValueError):
        consume_registration_rate_limit(
            db, scope="registration_verify", subject="challenge-1",
            secret=KEY, policy=POLICY, now=datetime(2026, 11, 10, 12, 0),
        )
    with pytest.raises(ValueError):
        consume_registration_rate_limit(
            db, scope="registration_verify", subject="challenge-1",
            secret=b"short", policy=POLICY, now=NOW,
        )
