# A3.3 — Backend foundation

**Repository:** akefadaka-git/maale-bible  
**Branch:** feat/auth-a3-backend-foundation  
**Base:** A3.2 contract head 149079bbfce0fe9a543a95efeeb091ac8a8c49a4  
**Status:** Implementation slice; draft review required.

## Architecture decision

The Owner approved the authentication policy and confirmed that a separate
repository is not required. The backend therefore lives in the existing
canonical repository under backend/, isolated from the Android app module
and the existing scripture-audit tooling.

The backend foundation uses Python 3.12+ and FastAPI for HTTP, Pydantic for
request/response validation, SQLAlchemy 2 and Alembic for future persistence
and migrations, PostgreSQL as the intended authoritative relational database,
and Argon2id through argon2-cffi for future password hashing. A server-side
SMS adapter boundary will be added later; no provider is selected.

This is a technology selection for the new backend slice, not a claim that
these components are already configured or deployed. Dependencies are
version-bounded but not yet locked; a reproducible lock and dependency audit
are required before release.

## Implemented in A3.3

- Added an isolated Python package under backend/.
- Added a FastAPI entry point and a minimal liveness route.
- Added a synthetic HTTP test for the liveness contract.
- Added backend-only CI so service checks do not require Android SDK or
  treat unrelated Android compile errors as backend failures.
- Kept the liveness route free of database details and secrets.
- Did not create authentication endpoints, database tables, credentials,
  OTP delivery, sessions, or production configuration.

## Security and product boundaries

- Guest reading remains available without authentication.
- Existing local study SQLite storage is not reused for account identity.
- The server will generate opaque account IDs.
- Login remains username OR E.164 mobile number plus password.
- Registration OTP remains the only approved OTP use.
- No password recovery, phone changes, account deletion, sync or guest-note
  migration is added by this slice.
- No real user data or source scripture content is included.

## Next slices

1. A3.4 persistence model and migration design, including normalized unique
   username/mobile constraints and pending registration challenge state.
2. A3.5 registration challenge + OTP provider interface and fake test adapter.
3. A3.6 Argon2id login, generic failures and rate limiting.
4. A3.7 refresh rotation, reuse detection and revocation.
5. A3.8 Android API integration and guest-first UI.
6. Independent security review and exact-HEAD CI before any closure request.

No merge, signing, deployment, publication or live SMS provider activation is
authorized by this branch.
