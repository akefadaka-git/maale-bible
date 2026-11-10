# A3.8 PostgreSQL concurrency hardening

**Status:** implementation submitted for exact-HEAD CI; draft PR only  
**Parent:** `feat/auth-a3-registration-otp-verification` at `7deea2cdd8f11eb83223a2484a52f81b25a9733a`

## Registration challenge reservation

PostgreSQL registration challenge creation now acquires transaction-scoped advisory locks for both normalized identifiers before checking accounts and active challenges. The lock keys are namespaced and sorted before acquisition to avoid lock-order deadlocks when two requests overlap on different identifiers. Locks are released automatically at transaction end.

A 64-bit hash collision can only cause unnecessary serialization; it cannot allow a duplicate reservation. Existing unique constraints on account username and mobile remain the final account-integrity boundary. The advisory-lock protection applies to challenge creation paths that use this service; any future alternate registration writer must use the same lock protocol or an equivalent database-enforced reservation design.

Non-PostgreSQL test dialects retain their existing behavior. PostgreSQL-specific correctness is exercised by dedicated integration tests, not inferred from SQLite.

## OTP verification concurrency

The existing verification service uses PostgreSQL `SELECT FOR UPDATE` and a conditional update that requires the challenge to remain unconsumed, unexpired, and within its attempt budget. The new PostgreSQL integration test runs two valid verifications concurrently and requires exactly one successful consumption, with the attempt budget unchanged.

## CI coverage

The backend authentication workflow provisions PostgreSQL 16 as a disposable GitHub Actions service and sets `POSTGRES_TEST_URL`. The new integration suite tests:
- two simultaneous correct OTP submissions: exactly one succeeds and the challenge is consumed once;
- two simultaneous registrations sharing a username: exactly one active challenge is created;
- two simultaneous registrations sharing a mobile number: exactly one active challenge is created.

The tests use independent SQLAlchemy sessions and concurrent worker threads. They are skipped locally when `POSTGRES_TEST_URL` is absent; CI provisions the database and must report them as executed.

## Boundaries and remaining work

- No HTTP registration endpoint, SMS provider, or production activation is added.
- No merge to the parent feature branch or `main`, signing, deployment, or publication is performed here.
- Database-level account unique constraints remain mandatory; the challenge reservation lock is not a substitute for them.
- Independent security review, abuse/rate limiting, resend controls, and end-to-end registration activation remain separate gates.
