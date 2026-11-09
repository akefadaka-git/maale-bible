# A3.6 — Registration security primitives

**Branch:** feat/auth-a3-registration-security-primitives  
**Base:** A3.5 migration head 2aae397fe5c09ab5df230d94bcee20bb085ffb45  
**Status:** Primitive layer only; no live registration route or provider.

## Implemented

- Argon2id password hashing with explicit 64 MiB memory, three iterations and two lanes.
- Password verification returns a generic boolean for mismatch/malformed hash; no raw password is persisted by these helpers.
- Six-digit registration OTP generation uses the operating-system cryptographic random source.
- HMAC-SHA-256 OTP digest binds the code to a specific challenge UUID and E.164 destination; digest comparison uses constant-time comparison.
- Proposed registration OTP defaults are encoded: 300-second TTL, five attempts, 60-second resend cooldown. Transactional enforcement is not yet implemented.
- SMS delivery is represented only by a server-side protocol port. No provider or credentials are configured.
- Synthetic tests cover password hashing/verification, OTP format, challenge/destination binding and malformed input.

## Critical limits

These primitives do not implement challenge persistence transactions, expiry enforcement, attempt decrement, resend throttling, account uniqueness race handling, generic HTTP responses, IP/device rate limits, SMS delivery, login, refresh rotation or session revocation. They must not be exposed through an API route until the service layer implements those controls atomically and is reviewed.

No OTP is sent, no account is created, no password or OTP is stored from a real user, and no production secret is committed.
