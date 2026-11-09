# Authentication API v1 — contract notes

**Status:** A3 contract draft; not a deployed service.  
**OpenAPI:** [openapi-auth-v1.yaml](./openapi-auth-v1.yaml)  
**Policy:** username OR mobile number + password; guest reading remains available; OTP is for registration verification only.

## Account and identifier rules

- The server generates an immutable UUID account ID. It is an internal identifier, not a login option.
- Registration supplies a username, E.164 mobile number and password.
- Usernames are normalized with Unicode NFKC and locale-independent case folding before uniqueness checks. The current client contract accepts 3–30 ASCII characters, beginning with a letter and followed by letters, digits, dot, underscore or hyphen.
- Mobile numbers are normalized to E.164. The client never infers a country code; the server owns uniqueness and verified-ownership state.
- The database must enforce unique constraints on normalized username and normalized mobile number. Client validation is not an authority for uniqueness or ownership.

## Registration lifecycle

1. The app submits username, mobile number and password to create a pending registration challenge.
2. The server sends a one-time code through a server-controlled SMS adapter.
3. The app submits the code against the opaque challenge ID.
4. The server validates and consumes the code exactly once, atomically activates the account, and issues the first session.
5. Duplicate identifiers must not be disclosed through unauthenticated responses. Challenge creation uses generic acceptance responses and server-side abuse controls.

The OTP format, expiry, attempt budget and resend interval are server policy and must be finalized before backend implementation. They are intentionally not hard-coded in the client contract.

## Login and sessions

- Login accepts a single `identifier` field: E.164 mobile number (leading `+`) or username, plus password.
- Ordinary login does not request OTP.
- Unknown identifier and incorrect password produce the same generic authentication error.
- The backend verifies passwords using an adaptive password hash (Argon2id preferred); plaintext passwords are never stored or logged.
- Access credentials are short-lived. Refresh credentials rotate on every successful refresh; reuse of an already-rotated refresh credential revokes the related token family/session.
- The Android client stores refresh credentials only using Android Keystore-backed encrypted storage. Passwords and OTPs are never persisted.
- Logout revokes the current server session. Client-side flags alone never establish authentication.

## Guest and data boundaries

- Reading remains available without registration, network access or an account.
- Existing local study data remains separate from account identity.
- This contract does not authorize cloud sync, guest-note migration, cross-device merge, backup ownership or account deletion behavior.

## Explicitly out of scope

No backend host, database, SMS provider, credentials, paid service, live endpoint, account migration or production session service is selected or implemented by this document. Password recovery, phone-number changes and account deletion require separate policy approval. The app must not show a successful authenticated state until a real backend verifies credentials and issues a session.

## Verification boundary

The companion CI workflow validates the OpenAPI document and runs Android host unit tests on pull requests. Passing those checks validates the contract and client-side tests only; it does not prove SMS delivery, backend security, live authentication or production readiness.
