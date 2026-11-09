# A3.7 — Registration challenge creation and password hashing

**Repository:** akefadaka-git/maale-bible  
**Branch:** feat/auth-a3-registration-challenge-creation  
**Base:** A3.6 security primitives head 2fb3e1f2a423fed8210435d99b20de05cca844d8  
**Status:** Service implementation slice; no public endpoint.

## Implemented

- Backend normalization mirrors the existing Android registration contract: username NFKC + casefold, 3–30 allowed ASCII characters; mobile number requires explicit E.164 country code and strips common visual separators.
- Registration password is passed directly to the existing Argon2id password hasher; only the encoded hash is persisted in the challenge row.
- Generates a cryptographically random six-digit OTP and stores only its HMAC-SHA-256 digest bound to the challenge UUID and normalized mobile number.
- Challenge expires after five minutes and starts with five attempts.
- Rejects an identifier already associated with an account or an unconsumed, unexpired challenge, using one generic service exception.
- Uses a database transaction and returns an internal-only result containing the OTP for a trusted delivery adapter. The OTP must never be serialized into an API response, persisted, or logged.

## Explicitly not included

- No HTTP registration route is exposed and no SMS provider is called. No live OTP is delivered.
- No resend endpoint, resend cooldown enforcement, OTP verification/consumption, attempt decrement, IP/device rate limiting, or abuse controls are implemented here.
- The active-challenge pre-check is not a concurrency lock. Concurrent registration attempts require database-specific locking or another atomic reservation strategy before public exposure.
- No account is created or activated by challenge creation.
- No login, password recovery, phone change, account deletion, guest-note migration or cloud sync is added.

## Verification

The test suite covers Argon2id password persistence, digest-only OTP storage, normalized identifiers, duplicate account/challenge rejection, expired challenge behavior, and invalid key/timestamp input. GitHub Actions exact-HEAD evidence is required before this slice can be marked verified.
