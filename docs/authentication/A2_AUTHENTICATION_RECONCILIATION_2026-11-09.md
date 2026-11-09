# Authentication A2 — Read-only reconciliation and architecture proposal

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Canonical repository:** `akefadaka-git/maale-bible`  
**Reconciliation ref:** `feat/ui-08-persistent-study-integration`  
**Exact inspected HEAD:** `ed3313ccce69b820d0f9131543fd29e7b76a0a4e`  
**A2 branch:** `architecture/auth-a2-reconciliation`  
**Owner-approved policy:** mobile number OR username + password; guest reading enabled; OTP for registration only.

## 1. A2 result

**A2 — READ-ONLY RECONCILIATION COMPLETE.** The inspected UI-08 tree contains no application authentication or account subsystem to extend. No auth implementation, backend API client, identity provider, account/session table, registration flow, login flow, OTP flow, or auth-related dependency was found in the inspected source tree.

This is consistent with the existing A3 Android scope, which explicitly deferred backend, accounts, analytics, and sync. The current application is an offline-first Android client. The existing private SQLite database is a study-data store, not an identity store and must not be repurposed as one.

## 2. Evidence inspected

| Evidence | Exact observation | Status |
|---|---|---|
| Git tree at `ed3313ccce69b820d0f9131543fd29e7b76a0a4e` | Complete recursive tree returned with `truncated=false`; no auth/account/identity package or backend service directory | PASS |
| `app/src/main/AndroidManifest.xml` | One launcher activity; no INTERNET permission or authentication/deep-link declarations | PASS |
| `app/build.gradle.kts` | Compose, AndroidX SQLite and test dependencies; no networking, auth, OTP, or secure-session library | PASS |
| `app/src/main/java/org/maale/bible/ui/MaaleBibleApp.kt` | Home, Books, Search, Listen and Study navigation; no login/account route | PASS |
| `app/src/main/java/org/maale/bible/data/LocalStudyDatabase.kt` | Schema v4 supports canonical passage and private study-note data; no user/account/session tables | PASS |
| `docs/product/A3_ANDROID_IMPLEMENTATION_SCOPE.md` | Backend, account/authentication and cloud sync explicitly deferred | PASS |
| Open PR #27 | Draft UI-08, based on UI-07; no auth work in its stated scope; remains unmerged | VERIFIED |

The inspected file blob SHAs are recorded by the repository at the exact ref; this report does not modify those files. Existing study-note schema, migrations, UI, and navigation are preserved.

## 3. Owner-approved policy

1. **Login identifier:** accept either a verified mobile number or a username, with a password.
2. **Registration:** collect a username and mobile number, establish uniqueness for each, and verify the mobile number using a one-time code before activating the account.
3. **Guest reading:** remains available without registration, network access, or an account.
4. **OTP:** registration verification only. Do not silently extend OTP to every login, password recovery, phone changes, or other actions.
5. **Internal identity:** assign an immutable server-generated opaque user ID. It is never a user-entered login identifier or a password.
6. **No fabricated identity:** guest mode must not create a server account or claim that a guest has a verified identity.

## 4. Proposed secure architecture

### 4.1 Separation of concerns

- **Android presentation:** guest/registration/login/account screens and explicit authentication state.
- **Domain:** typed account identity, login identifier, registration and authentication use cases; no Android or network dependencies.
- **Data/API adapter:** typed HTTPS API client and repository implementation, introduced only with an approved backend contract.
- **Backend identity service:** authoritative account creation, username/mobile uniqueness, password verification, OTP lifecycle, session issuance and revocation.
- **SMS provider adapter:** server-side provider interface. Provider, credentials and delivery policy remain configuration, never embedded in the APK.
- **Existing local study store:** remains separate. Do not add user IDs or migrate study-note ownership until a separately reviewed account-sync/data-ownership design exists.

### 4.2 Account identity and registration

- Server generates an immutable, non-guessable UUID (or equivalent opaque identifier) for each account.
- Normalize usernames consistently (Unicode normalization and a documented case-folding policy); enforce a database unique constraint on the normalized value.
- Normalize mobile numbers to E.164; enforce a unique constraint on the normalized value. Support international numbers rather than hard-coding one country.
- Registration collects username, mobile number and password. A pending account is not active until its registration OTP is verified.
- Create/activate the account and consume the OTP atomically. A code is single-use and bound to the pending registration and destination number.
- Do not disclose whether a username or mobile number is already registered in unauthenticated responses.

### 4.3 Password and login security

- Passwords are processed and verified only by the backend. Store only a modern adaptive password hash (Argon2id preferred; a reviewed platform-supported alternative only if necessary). Never store or log plaintext passwords.
- Login accepts one identifier field; the backend resolves it against the normalized username or E.164 mobile namespace, then verifies the password.
- Rate-limit by account/identifier, source IP and device signals; apply bounded progressive delays and abuse monitoring without permanent lockout denial-of-service.
- Return generic invalid-credential responses that do not reveal account existence.
- Require TLS for all network traffic; disable cleartext traffic. Do not embed provider secrets, server secrets, OTP signing keys or password peppers in the Android client.

### 4.4 Registration OTP controls

- Generate codes with a cryptographically secure random generator on the server.
- Store a keyed digest or appropriately protected verifier, not a reusable plaintext code.
- Proposed initial controls: 6-digit code, 5-minute expiry, one successful use, maximum 5 verification attempts, and a 60-second resend cooldown; enforce per-number, per-IP and per-device quotas.
- Invalidate superseded codes, bind codes to a registration challenge, and use constant-time comparison where applicable.
- Keep provider delivery behind an adapter; use a deterministic fake provider only in tests. No live SMS credentials or paid provider are introduced in this client-only repository stage.
- OTP is not used at ordinary login. Recovery and phone-change policy remain unresolved and require separate Owner approval.

### 4.5 Session lifecycle

- Backend issues short-lived access credentials and rotating refresh credentials; refresh-token reuse triggers revocation of the affected token family/session.
- Store refresh credentials only in Android Keystore-backed encrypted storage; keep access credentials in memory where practical. Never store passwords or OTPs in preferences, SQLite, logs or backups.
- Support logout and server-side session revocation. Clear local session material on logout and account removal.
- Do not implement a local-only “authenticated” flag as proof of server identity.

### 4.6 Guest and offline behavior

- App starts in guest mode and can access available offline reading without an account.
- Account registration/login is optional and must not block reading.
- Guest study notes remain in the existing app-private database. Do not silently upload, merge, delete or reassign guest notes after login.
- Cross-device sync, guest-to-account migration, backup ownership and account deletion effects on local study data require a separate design and approval.

## 5. Explicit non-goals and unresolved decisions

- No backend/API service currently exists in this repository; the backend host, API contract, hosting, database, operational ownership and SMS provider are not selected.
- Password recovery is not authorized by the registration-only OTP policy; define and approve it separately before implementation.
- Phone-number change, username change, account deletion, multi-device session management, account sync and guest-data migration require detailed policy and privacy decisions.
- No third-party identity provider, analytics SDK, advertising SDK, paid SMS integration, or cloud database is approved by this A2 record.
- No auth implementation, dependency, manifest permission, schema migration or user-data mutation is part of A2.

## 6. A3 implementation sequence proposed

1. Add dependency-free domain contracts and validation tests for normalized login identifiers, registration request shape and authentication states.
2. Define a versioned API contract for registration challenge, OTP verification, login, refresh and logout; keep the server implementation as a separate deliverable/repository until an authoritative backend is selected.
3. Add Android networking and Keystore-backed session storage only after the API contract and backend trust boundary are reviewed.
4. Add registration/login UI while preserving guest-first navigation.
5. Implement the server identity service, unique constraints, password hashing, OTP controls, rate limits, audit events and session revocation in its own controlled backend project.
6. Verify the full end-to-end lifecycle, abuse controls, migration/data isolation and offline guest behavior on exact commit SHAs.

## 7. Governance and verification

- A2 was read-only against the inspected UI-08 source ref.
- No existing file was changed or removed by reconciliation.
- No authentication feature is claimed implemented.
- No workflow/build/test evidence is claimed for authentication.
- A3 work must remain on a separate branch and draft PR. Do not merge, sign, deploy, publish, or activate a live SMS provider without separate authorization.
