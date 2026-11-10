# Registration abuse-control primitives

**Status:** Draft implementation; endpoint integration and policy approval remain outstanding.  
**Branch:** `feat/auth-registration-abuse-controls`

## What is implemented

- Persistent quota buckets for registration-related request scopes.
- Atomic PostgreSQL and SQLite UPSERT-based quota consumption, avoiding a read-then-write race between concurrent requests.
- HMAC-SHA-256 keyed subject digests; raw mobile numbers, IP addresses, device identifiers, and challenge references are not stored in the quota table.
- Configurable `RateLimitPolicy(max_requests, window_seconds, block_seconds)`; no unapproved production thresholds are hard-coded.
- A generic `RateLimitExceeded` outcome with a calculated `Retry-After` duration.
- A dedicated Alembic migration, SQLite unit tests, and PostgreSQL concurrency coverage.

## Intended enforcement points

The future HTTP layer must consume separate scopes before expensive work:
- registration start by source IP;
- registration start by normalized mobile number;
- registration verification by challenge and source IP;
- registration resend by destination mobile number.

A resend policy can use the existing 60-second cooldown as a minimum, but any broader per-hour/day destination quotas and IP/device quotas require an explicit operational policy. Login OTP is not introduced; OTP remains registration-only.

## Security and operational boundaries

- This is a service primitive, not an enabled API route. The FastAPI application still exposes only liveness.
- The caller must apply limits before Argon2id hashing and before SMS delivery.
- A dedicated rate-limit HMAC secret (minimum 32 random bytes) must be loaded from a server-side secret manager/environment and validated at startup before any route is enabled. Do not reuse the OTP HMAC key by default.
- Proxy-derived client IPs must be trusted only when the API is behind explicitly configured trusted proxies. Never trust arbitrary `X-Forwarded-For` input.
- Rate-limit thresholds must be configured per scope and tuned using capacity and abuse testing; missing policy/configuration must prevent public route activation.
- Expired bucket cleanup/retention, distributed proxy topology, and production capacity testing remain outstanding.
- This slice does not implement SMS provider delivery, registration endpoint behavior, login throttling, or account enumeration response handling.

## Verification

The migration test must create `registration_rate_limit_buckets`. Unit tests cover quota enforcement, reset behavior, keyed subject hashing, malformed configuration, and timestamps. PostgreSQL CI tests exercise simultaneous requests against a shared quota with independent database sessions.

No live service, paid provider, production secret, merge, deployment, signing, or publication is involved.
