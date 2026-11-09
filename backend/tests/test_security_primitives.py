from uuid import uuid4

import pytest

from maale_bible_api.security.passwords import hash_password, verify_password, password_hash_needs_rehash
from maale_bible_api.security.registration_otp import (
    OTP_DIGITS,
    digest_registration_otp,
    generate_registration_otp,
    verify_registration_otp,
)


def test_password_hash_uses_argon2id_and_verifies_only_matching_password():
    encoded = hash_password("synthetic test password")
    assert encoded.startswith("$argon2id$")
    assert verify_password(encoded, "synthetic test password")
    assert not verify_password(encoded, "incorrect password")
    assert not verify_password("not-a-valid-hash", "synthetic test password")
    assert password_hash_needs_rehash(encoded) is False


def test_password_hash_rejects_empty_and_oversized_values():
    with pytest.raises(ValueError):
        hash_password("")
    with pytest.raises(ValueError):
        hash_password("x" * 1025)


def test_registration_otp_is_six_ascii_digits():
    for _ in range(100):
        code = generate_registration_otp()
        assert len(code) == OTP_DIGITS
        assert code.isascii() and code.isdigit()


def test_otp_digest_is_bound_to_challenge_and_destination():
    key = b"k" * 32
    challenge = uuid4()
    code = "004271"
    digest = digest_registration_otp(key, challenge, "+251911223344", code)

    assert verify_registration_otp(key, challenge, "+251911223344", code, digest)
    assert not verify_registration_otp(key, uuid4(), "+251911223344", code, digest)
    assert not verify_registration_otp(key, challenge, "+251922334455", code, digest)
    assert not verify_registration_otp(key, challenge, "+251911223344", "999999", digest)


def test_otp_digest_rejects_weak_key_and_malformed_code():
    with pytest.raises(ValueError):
        digest_registration_otp(b"short", uuid4(), "+251911223344", "123456")
    with pytest.raises(ValueError):
        digest_registration_otp(b"k" * 32, uuid4(), "+251911223344", "12ab56")
