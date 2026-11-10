# Owner Decision — Independent Authentication Security Review Waiver

**Decision date:** 10 November 2026  
**Project:** Maale Bible (ጌኤዦ ማፃኣፖ)  
**Repository:** `akefadaka-git/maale-bible`  
**Decision authority:** Owner  
**Decision:** INDEPENDENT REVIEW NOT REQUIRED AS A PROJECT-PROGRESSION GATE

## Owner instruction

The Owner explicitly directed:

> “No need for independent security reviewer, just go to next step and record it”

This decision waives the requirement to obtain an independent external security reviewer before proceeding to the next planned implementation stage. The project shall not pause or reopen that requirement unless the Owner later requests it or a material incident, vulnerability, or authoritative requirement makes it necessary.

## Accurate security status

- The waiver is a governance decision, not a technical security finding.
- It does not convert the earlier AI-assisted internal assessment into an independent review, penetration test, certification, or security approval.
- Existing findings, test gaps, and implementation limitations remain recorded and must be addressed through engineering, automated verification, and release-readiness controls.
- No claim of independently certified security is authorized.

## Next sequential stage

Proceed with the next planned authentication implementation stage: registration abuse controls and account/session lifecycle.

The implementation sequence is:
1. Registration abuse/rate-limit enforcement and resend controls, preserving registration-only OTP.
2. Atomic registration verification, account activation and first-session issuance.
3. Username-or-mobile password login, access-token validation, refresh rotation/replay handling and logout revocation.
4. Exact-head PostgreSQL, backend, OpenAPI and Android contract verification.
5. Record the authentication stage outcome before proceeding to licensed corpus integration and full-text search.

## Boundaries preserved

- Login identifier remains username OR mobile number plus password.
- OTP remains registration-only.
- Guest scripture reading remains available without an account.
- No password-recovery OTP, phone-change OTP or every-login OTP is added.
- No public registration route, live SMS provider, production secret, merge to `main`, signing, deployment or publication is authorized by this decision.

**Disposition:** WAIVED FOR SEQUENCING — NOT A SECURITY CERTIFICATION.
