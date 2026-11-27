from __future__ import annotations
import base64, binascii, os
from dataclasses import dataclass
from fastapi import HTTPException
from maale_bible_api.services.registration_abuse import RateLimitPolicy
from maale_bible_api.services.sessions import SessionPolicy

@dataclass(frozen=True, slots=True)
class AuthRuntime:
    otp_hmac_key: bytes
    rate_limit_hmac_key: bytes
    session_policy: SessionPolicy
    registration_rate_limit_policy: RateLimitPolicy

def _required_integer(name: str) -> int:
    value = os.environ.get(name)
    if value is None or not value.strip():
        raise RuntimeError(f"{name} must be configured.")
    try: return int(value)
    except ValueError as exc: raise RuntimeError(f"{name} must be an integer.") from exc

def _required_key(name: str) -> bytes:
    value = os.environ.get(name)
    if value is None or not value.strip():
        raise RuntimeError(f"{name} must be configured as URL-safe base64.")
    try:
        decoded = base64.b64decode(value.encode("ascii"), altchars=b"-_", validate=True)
    except (UnicodeEncodeError, binascii.Error) as exc:
        raise RuntimeError(f"{name} must be valid URL-safe base64.") from exc
    if len(decoded) < 32: raise RuntimeError(f"{name} must decode to at least 32 random bytes.")
    return decoded

def load_auth_runtime() -> AuthRuntime:
    try:
        return AuthRuntime(
            otp_hmac_key=_required_key("OTP_HMAC_KEY_B64"),
            rate_limit_hmac_key=_required_key("RATE_LIMIT_HMAC_KEY_B64"),
            session_policy=SessionPolicy(
                access_ttl_seconds=_required_integer("ACCESS_TOKEN_TTL_SECONDS"),
                refresh_ttl_seconds=_required_integer("REFRESH_TOKEN_TTL_SECONDS")),
            registration_rate_limit_policy=RateLimitPolicy(
                max_requests=_required_integer("REGISTRATION_RATE_LIMIT_MAX_REQUESTS"),
                window_seconds=_required_integer("REGISTRATION_RATE_LIMIT_WINDOW_SECONDS"),
                block_seconds=_required_integer("REGISTRATION_RATE_LIMIT_BLOCK_SECONDS")))
    except (RuntimeError, ValueError) as exc:
        raise HTTPException(503, detail="Authentication service is not configured.",
                            headers={"Cache-Control":"no-store"}) from exc
