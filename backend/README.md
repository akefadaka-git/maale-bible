# Maale Bible API

Server-side API for the offline-first Maale Bible Android application. Guest
reading and private on-device study notes do not require an account.

## Local development

Requires Python 3.12 or 3.13.

    python -m venv .venv
    # Activate the environment, then:
    python -m pip install -e ".[test]"
    pytest
    uvicorn maale_bible_api.main:app --app-dir src --host 127.0.0.1 --port 8000

GET /health/live is process liveness only. GET /health/ready requires a
migrated database, explicit authentication policy and an HTTPS SMS provider.

## Authentication API

- POST /auth/register reserves username/mobile identity, stores an Argon2id
  password hash and OTP HMAC digest, then sends a registration-only SMS code.
  The OTP is never returned by the API.
- POST /auth/register/verify atomically consumes the code, activates the account
  and issues its first opaque access/refresh token pair.
- POST /auth/login accepts username or E.164 mobile plus password; OTP is not
  used during ordinary login.
- POST /auth/refresh rotates a refresh token once; replay revokes its family.
- POST /auth/logout revokes the current device-session family.
- GET /auth/me validates an opaque bearer access token.

Only SHA-256 token hashes are persisted. Credential responses are marked
Cache-Control: no-store. Registration rate-limit subjects are stored as keyed
HMAC digests, not raw mobile/IP values.

## Required runtime configuration

Generate independent random secrets of at least 32 bytes and encode them as
URL-safe base64. Do not commit secrets.

- DATABASE_URL — PostgreSQL URL; postgres:// and postgresql:// are normalized
  to the psycopg 3 driver.
- OTP_HMAC_KEY_B64 and RATE_LIMIT_HMAC_KEY_B64 — separate random secrets.
- ACCESS_TOKEN_TTL_SECONDS and REFRESH_TOKEN_TTL_SECONDS — explicit lifetimes.
- REGISTRATION_RATE_LIMIT_MAX_REQUESTS,
  REGISTRATION_RATE_LIMIT_WINDOW_SECONDS and
  REGISTRATION_RATE_LIMIT_BLOCK_SECONDS — explicit quota policy.

Apply Alembic migrations before enabling authentication:

    alembic -c alembic.ini upgrade head

Configure SMS_GATEWAY_URL (HTTPS only), SMS_GATEWAY_BEARER_TOKEN and
SMS_GATEWAY_SENDER_ID. The generic adapter POSTs JSON fields `to`, `sender_id`
and `message`. If the selected gateway has a different contract, implement
RegistrationSmsProvider server-side. Never put SMS credentials in Android.

## Production security

Terminate TLS at a trusted reverse proxy. Run Uvicorn with proxy headers enabled
only for proxy addresses you control; never expose plain HTTP publicly. Store
secrets in the deployment platform secret manager, restrict database network
access, run migrations as a separate release step, and configure trusted proxy
addresses before relying on client-IP rate limits. SQLite is for isolated tests
only; PostgreSQL concurrency verification remains a production gate.

Authentication does not enable cloud note synchronization. Study notes remain
private and local by default. No account is required for guest reading.
