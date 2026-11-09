# A3.5 — Authentication database migration

**Branch:** feat/auth-a3-database-migrations  
**Base:** A3.4 persistence-model head fbd015e3b8880413453d868a742d108ea7bf1bf8  
**Status:** Initial migration authored; PostgreSQL execution remains pending.

## Migration contents

- Creates accounts with unique normalized username and E.164 mobile constraints, opaque UUID primary key, explicit status constraint and fail-closed pending default.
- Creates registration challenges with digest-only OTP storage, expiry and non-negative attempt budget constraints.
- Creates sessions with unique refresh-token digest, account foreign key with cascade behavior, token-family index, expiry, rotation and revocation fields.
- Adds reverse-order downgrade operations.
- Reads DATABASE_URL only from the process environment. No fallback production URL or credential is committed.

## Verification boundary

A synthetic SQLite migration test checks that the migration creates all expected tables. This is not proof of PostgreSQL compatibility, locking, isolation, concurrent registration safety or production readiness. A PostgreSQL-backed CI service and migration-upgrade/downgrade verification are required before the schema is accepted.

No production database has been provisioned or modified. No user records, live OTPs, SMS provider, secrets, deployment or activation are included.
