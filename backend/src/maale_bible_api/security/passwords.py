from argon2 import PasswordHasher
from argon2.exceptions import InvalidHashError, VerificationError, VerifyMismatchError

# Argon2id parameters: 64 MiB memory, 3 iterations, 2 lanes.
_password_hasher = PasswordHasher(
    time_cost=3,
    memory_cost=65_536,
    parallelism=2,
    hash_len=32,
    salt_len=16,
)


def hash_password(password: str) -> str:
    """Return an Argon2id PHC string; callers must never log the input."""
    if not isinstance(password, str) or not password:
        raise ValueError("Password must not be empty.")
    if len(password.encode("utf-8")) > 1024:
        raise ValueError("Password exceeds the maximum supported byte length.")
    return _password_hasher.hash(password)


def verify_password(password_hash: str, candidate: str) -> bool:
    """Verify without exposing mismatch or malformed-hash details to callers."""
    if not isinstance(candidate, str) or not candidate:
        return False
    try:
        return _password_hasher.verify(password_hash, candidate)
    except (VerifyMismatchError, VerificationError, InvalidHashError, TypeError):
        return False


def password_hash_needs_rehash(password_hash: str) -> bool:
    try:
        return _password_hasher.check_needs_rehash(password_hash)
    except (VerificationError, InvalidHashError, TypeError):
        return True
