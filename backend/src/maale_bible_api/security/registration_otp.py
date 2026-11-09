import hashlib
import hmac
import secrets
from uuid import UUID

OTP_DIGITS = 6
OTP_TTL_SECONDS = 300
OTP_MAX_ATTEMPTS = 5
OTP_RESEND_COOLDOWN_SECONDS = 60


def generate_registration_otp() -> str:
    """Generate a zero-padded six-digit registration code using OS randomness."""
    return f"{secrets.randbelow(10 ** OTP_DIGITS):0{OTP_DIGITS}d}"


def digest_registration_otp(
    secret: bytes,
    challenge_id: UUID,
    destination_e164: str,
    code: str,
) -> str:
    """Bind an OTP verifier to the HMAC key, challenge and destination number."""
    if not isinstance(secret, bytes) or len(secret) < 32:
        raise ValueError("OTP HMAC key must contain at least 32 random bytes.")
    if len(code) != OTP_DIGITS or not code.isascii() or not code.isdigit():
        raise ValueError("Registration code must contain exactly six ASCII digits.")
    message = b"maale-bible:registration:v1:" + str(challenge_id).encode("ascii")
    message += b":" + destination_e164.encode("ascii") + b":" + code.encode("ascii")
    return hmac.new(secret, message, hashlib.sha256).hexdigest()


def verify_registration_otp(
    secret: bytes,
    challenge_id: UUID,
    destination_e164: str,
    code: str,
    expected_digest: str,
) -> bool:
    """Constant-time digest comparison; expiry and attempts are enforced by service logic."""
    try:
        candidate = digest_registration_otp(secret, challenge_id, destination_e164, code)
    except (ValueError, UnicodeEncodeError, AttributeError):
        return False
    if (
        not isinstance(expected_digest, str)
        or len(expected_digest) != 64
        or any(character not in "0123456789abcdef" for character in expected_digest)
    ):
        return False
    try:
        return hmac.compare_digest(candidate, expected_digest)
    except (TypeError, UnicodeEncodeError):
        return False
