# Authentication session lifecycle and registration activation

**Status:** Draft implementation; no public routes enabled.  
**Policy:** username OR E.164 mobile plus password; registration OTP only; guest reading remains available without account.

## Implemented service behavior

- Registration verification, active-account creation, and first session issuance are performed in one database transaction. The challenge is consumed only when the account and session record can be committed.
- Ordinary login accepts normalized username or E.164 mobile plus password. It does not request OTP.
- Unknown identifiers use a configured dummy Argon2id hash for password-verification work; user-facing callers must map all invalid cases to the same generic response.
- Access and refresh credentials are opaque, independently generated 256-bit bearer values. Only SHA-256 hashes are persisted.
- Access credentials have their own expiry and are invalidated when their refresh generation rotates.
- Refresh credentials rotate once. Reuse of a rotated credential revokes all records in the token family.
- Logout revokes the current device/session token family.
- Access-token validation checks expiry, rotation, revocation, and active account status.
- Session TTLs are explicit `SessionPolicy` inputs; no production duration is silently assumed.

## Database migration

Migration `0003_auth_access_tokens` adds access-token hash and expiry columns. Existing legacy rows may have NULL access fields and are not accepted as access-authenticated sessions. New service-issued rows populate both token hashes and expiries.

## Required runtime integration before public activation

- Load a valid dummy Argon2id hash, OTP HMAC key, rate-limit HMAC key, database URL, and explicit session TTL policy from server-side secret/configuration sources; fail closed if any required value is missing or malformed.
- Wire the registration abuse limiter before Argon2id hashing, OTP verification, and SMS delivery. Configure per-scope quotas and trusted proxy behavior explicitly.
- Wire FastAPI endpoints only after generic response mapping, request-size/timeouts, observability redaction, TLS, and rate-limit enforcement are reviewed.
- Android must store refresh credentials using Android Keystore-backed encrypted storage and serialize refresh operations to avoid benign concurrent refreshes being treated as token replay.
- Add deployment-specific key rotation, backup/restore, operational revocation, and monitoring procedures.

## Verification requirements

Unit tests cover registration activation, first session issuance, username/mobile login, generic login failure, access expiry/rotation, refresh replay family revocation, and logout. PostgreSQL integration coverage exercises simultaneous refresh use; the expected strict behavior is one rotation winner followed by family revocation when the stale credential is replayed concurrently.

This is a service-layer implementation only. No live API route, public registration, SMS provider, production secret, merge, signing, deployment, or publication is enabled.
