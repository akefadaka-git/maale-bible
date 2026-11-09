# A3 — Authentication domain contract foundation

**Branch:** `feat/auth-a3-domain-contracts`  
**A2 parent:** `301f64fca25fa82055a3a14180e5b23a1d2567d2`  
**Policy:** mobile number OR username + password; guest reading enabled; OTP for registration only.

## Implemented in this slice

- Added a dependency-free `LoginIdentifier` domain type with username and E.164 mobile-number variants.
- Username normalization uses Unicode NFKC and locale-independent case folding; syntax is constrained to 3–30 ASCII characters, starts with a letter, and allows letters, digits, dot, underscore and hyphen.
- Mobile normalization strips common display separators but requires an explicit international country code and E.164 digit bounds. The client never infers a country.
- Added `RegistrationIdentity` as the typed pair of normalized username and mobile number.
- Added unit tests for normalization, accepted formats, invalid formats and registration identity construction.

## Security boundaries

- These types validate input syntax only. They do not prove mobile ownership, reserve identifiers, create an account, verify a password, issue a session or perform OTP.
- Backend unique constraints remain authoritative for normalized username and mobile number.
- No password or OTP is represented in a persistable model or written to local storage.
- No authentication state is asserted from local client state.
- Guest reading and the existing study database are unchanged.
- No network library, INTERNET permission, SMS provider, API endpoint, account table, database migration, credential store or session store is introduced in this slice.

## Deliberately deferred

A server/API implementation is required for registration challenges, OTP generation and consumption, password hashing, login, refresh-token rotation, logout/revocation, rate limiting and abuse monitoring. No backend currently exists in this repository. The server location, contract, hosting, database and SMS provider must be selected before those operations can be implemented safely.

Password recovery, phone-number changes, account deletion, cloud sync and guest-data migration remain separate policy decisions. The approved OTP scope is registration only.

## Verification

- Unit tests added: 6.
- Execution status: NOT RUN in this commit record.
- Android build, emulator, instrumentation, security testing and end-to-end authentication: NOT RUN.
- No merge, signing, deployment, publication or live SMS integration.
