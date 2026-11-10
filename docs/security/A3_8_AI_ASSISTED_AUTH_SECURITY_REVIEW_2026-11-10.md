# A3.8 Authentication Security Review — AI-Assisted Internal Assessment

**Assessment date:** 10 November 2026  
**Repository:** `akefadaka-git/maale-bible`  
**Reviewed code baseline:** `4e6e9d552fe6e9f992451aa56f0f382597f4edcd`  
**PR context:** #39, branch `feat/auth-a3-postgres-concurrency`  
**Review type:** AI-assisted internal code review; **not independent third-party review, penetration test, or certification**  
**Disposition:** **NOT APPROVED FOR PRODUCTION AUTHENTICATION**

## 1. Scope and method

Reviewed the registration challenge creation and verification services, PostgreSQL advisory-lock and row-lock behavior, OTP/HMAC and password primitives, SQLAlchemy account/challenge/session models, API bootstrap, SMS provider port, OpenAPI contract, Android login-identifier parsing, tests, and CI workflow configuration available at the reviewed baseline.

This assessment is limited to repository-visible source and CI evidence. No production environment, live SMS provider, deployed API, real user data, secret store, or externally operated PostgreSQL service was accessed. No dynamic penetration test or independent human review was performed.

## 2. Findings

### F-01 — Authentication lifecycle is not implemented end to end
**Severity: HIGH | Status: OPEN**

The API application currently exposes only process liveness. There are no live registration, verification, login, refresh, or logout routes. The verification service consumes a valid registration challenge but does not create/activate an account or issue the first session. The OpenAPI contract describes successful verification as atomically activating an account and issuing a session, which is not implemented by the service code reviewed.

**Risk:** Treating the contract or challenge-consumption primitive as a working authentication system could create a false authenticated state or leave a consumed challenge without a usable account/session.

**Required remediation:** Implement account creation/activation in the same transaction as successful OTP consumption; implement login, session issuance, refresh rotation/reuse detection, and revocation; ensure the Android client never treats local state as authentication. Add end-to-end service and API tests. Keep public routes disabled until these are verified.

### F-02 — Abuse controls are incomplete at the request boundary
**Severity: HIGH | Status: OPEN**

The challenge has a five-attempt verification budget and a five-minute expiry, and the security module declares a 60-second resend cooldown constant. However, no resend service or API exists, and no request-level quotas are implemented for registration starts, verification attempts across multiple challenges, source IPs, devices, or SMS destinations. The Argon2id password hash is computed before the database duplicate checks, making request throttling important for resource-exhaustion resistance.

**Risk:** Attackers could create repeated challenges, consume SMS resources once delivery is enabled, or use high-volume requests to consume CPU/database capacity. Per-challenge attempt limits alone do not constrain creation of new challenges or distributed abuse.

**Required remediation:** Add configurable server-enforced limits at the API/service boundary, destination cooldown and resend accounting, generic responses, bounded request sizes/timeouts, and tests for parallel and distributed request patterns. Do not add OTP to login, password recovery, or phone changes; current policy is registration-only.

### F-03 — OTP secret lifecycle and runtime configuration are not yet governed
**Severity: HIGH | Status: OPEN**

The registration services accept an HMAC key as a function argument. The reviewed backend has no production secret-loading/validation boundary, key identifier/versioning strategy, rotation procedure, or deployment configuration. This is appropriate for isolated primitives but insufficient for a live service.

**Risk:** Misconfiguration, accidental use of weak/test keys, or key rotation without a controlled verification window could make registration unverifiable or weaken OTP protection.

**Required remediation:** Add a server-only secret provider with startup fail-closed validation, explicit environment separation, documented rotation/versioning, and tests proving secrets never enter API responses or logs. Never commit real keys.

### F-04 — Registration reservation locks depend on one service path
**Severity: MEDIUM | Status: OPEN**

PostgreSQL transaction-scoped advisory locks serialize duplicate challenge reservations when all writers call `create_registration_challenge`. The schema does not independently enforce uniqueness of active (unconsumed, unexpired) challenges. The code documentation acknowledges that alternate writers can bypass the advisory-lock protocol.

**Risk:** A future alternate registration path or maintenance script could bypass the lock and create duplicate active challenges.

**Required remediation:** Keep challenge creation behind one service/repository boundary, restrict direct writes, and add architecture tests/code review checks. Consider a database-enforced reservation design if the chosen PostgreSQL schema can represent expiry/consumption semantics safely.

### F-05 — Session persistence exists without session lifecycle services
**Severity: HIGH | Status: OPEN**

The `auth_sessions` model stores a refresh-token hash, family identifier, expiry, rotation and revocation timestamps, but no reviewed service implements token issuance, refresh rotation, replay detection, family revocation, logout, or access-token verification.

**Risk:** The presence of a session table may be mistaken for secure session management. Refresh-token replay and revocation guarantees are not established by the model alone.

**Required remediation:** Implement and test the full session state machine, including one-time refresh rotation, family-wide revocation on reuse, expiry checks, concurrent refresh races, logout, and access-token validation. Store only token hashes server-side and use high-entropy opaque refresh credentials or a rigorously validated token format.

### F-06 — Password policy and operational cost controls need explicit validation
**Severity: MEDIUM | Status: OPEN**

Argon2id is configured with a strong memory/time cost and a 1024-byte upper bound. Registration currently rejects only an empty password; no documented password-strength policy, request throttling, or capacity test is present. The exact production concurrency/capacity profile has not been measured.

**Risk:** Weak user-selected passwords and unbounded concurrent password hashing can increase account-compromise and resource-exhaustion exposure.

**Required remediation:** Define a password policy consistent with user experience and threat model, retain breached/common-password screening if an appropriate privacy-preserving source is selected, and load-test bounded concurrent hashing behind rate limits. Do not silently invent policy requirements.

### F-07 — Delivery and end-to-end registration remain unverified
**Severity: MEDIUM | Status: OPEN**

Only an SMS provider Protocol is present. There is no selected provider, delivery adapter, retry/idempotency policy, delivery-status handling, or verified destination masking at a live endpoint.

**Risk:** A correct OTP primitive does not demonstrate reliable or safe code delivery.

**Required remediation:** Select and approve a provider and operational controls before activation; test failures, retries, provider timeouts, destination formatting, and non-disclosure of OTP values. No provider or paid service is selected by this review.

## 3. Positive controls observed

- Passwords are hashed with Argon2id; plaintext password is not stored by the registration service.
- OTPs are generated using an operating-system cryptographic random source, six digits, and stored as a keyed HMAC-SHA-256 digest bound to challenge ID and E.164 destination.
- OTP digest comparison uses constant-time comparison.
- Challenge expiry and per-challenge attempt budget are enforced in the verification service.
- PostgreSQL OTP consumption uses row locking plus a conditional update; duplicate registration reservations use sorted transaction-scoped advisory locks.
- The PostgreSQL CI suite executed three concurrency tests (simultaneous OTP verification and username/mobile duplicate-registration races) with no skips; backend suite recorded 29 passing tests.
- Android guest reading remains independent of account authentication in the approved architecture.
- Authentication routes are intentionally not exposed by the current FastAPI app.

## 4. Verification evidence

- Backend authentication workflow: [run #42748595152](https://github.com/akefadaka-git/maale-bible/actions/runs/42748595152) — PASS, 29 tests, one warning; PostgreSQL integration tests executed.
- Authentication contract workflow: [run #42748594817](https://github.com/akefadaka-git/maale-bible/actions/runs/42748594817) — OpenAPI contract PASS and Android authentication unit tests PASS.
- These results apply to code/test HEAD `4e6e9d552fe6e9f992451aa56f0f382597f4edcd`. The later PR #39 HEAD `374c339594807138a0a4a22b845d6179b638923a` added documentation only.

## 5. Gate decision

**A3.8 implementation evidence: PARTIAL / PASS for the tested primitives only.**  
**Independent security review: MISSING.**  
**End-to-end authentication: NOT IMPLEMENTED.**  
**Production authentication: NO-GO.**

The findings above are actionable internal review findings, not independent sign-off. A volunteer reviewer should be a separate person with sufficient application-security and backend/PostgreSQL experience. A GitHub account is useful for a formal PR review but not strictly required for an offline review; the reviewer can provide a signed report or written findings that are then attached to the PR.

No merge, deployment, signing, publication, live SMS, public registration endpoint, or production activation is authorized by this assessment.
