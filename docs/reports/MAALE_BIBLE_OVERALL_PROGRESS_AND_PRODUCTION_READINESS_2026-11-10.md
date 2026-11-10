# Maale Bible — Overall Project Progress and Production Readiness

**Report ID:** MBB-PROGRESS-2026-11-10-R1  
**Assessment date:** 10 November 2026  
**Canonical repository:** `akefadaka-git/maale-bible`  
**Default branch:** `main`  
**Main SHA at assessment:** `ecf4124bfcd2e2a471147d3ba2695cd6e28ba389`  
**Owner governance:** stage-gated; merge, signing, deployment and publication require separate approval.

## 1. Executive assessment

**Overall project status: IN DEVELOPMENT — NOT PRODUCTION READY.**

The repository contains substantial UI foundations, local study-note persistence, and an incremental authentication/backend implementation. The latest PostgreSQL concurrency slice has passed its backend integration suite on a real PostgreSQL 16 CI service. The authentication contract and Android authentication unit-test jobs also passed on the same exact PR head.

However, the implementation is distributed across draft, unmerged pull requests. The UI and authentication work assessed here has not been integrated into `main`. The scripture reader still needs verified scripture-content integration; search remains catalogue metadata rather than full-text scripture search; audio is a placeholder; and several study features remain inactive. Public registration, live SMS, production deployment, signing and publication have not been performed.

**Production readiness decision: NOT READY / NO-GO.** This is a technical readiness assessment, not a claim that the app is close to a store release.

## 2. Repository and integration baseline

- Canonical repository: `https://github.com/akefadaka-git/maale-bible`
- Default branch: `main`
- Main SHA checked: `ecf4124bfcd2e2a471147d3ba2695cd6e28ba389`
- Pull requests #20–#39: 20 PRs total; #38 is merged into the A3.8 feature branch; 19 remain open and draft.
- The assessed UI and authentication implementation PRs remain unmerged from `main`.
- PR #38 merged only into `feat/auth-a3-registration-otp-verification`, not into `main`.
- PR #39 is a draft concurrency-hardening PR targeting the A3.8 feature branch.

## 3. UI and study-workspace progress

| Slice | Scope | Evidence / current state | Assessment |
|---|---|---|---|
| UI-01 / PR #20 | Design foundation, Material 3 light/dark theme, palette | Draft PR; SHA `c7bfd143ffe62a2d56ddfe93080182f45fa5d93d` | IMPLEMENTED; integration/build verification outstanding |
| UI-02 / PR #21 | Smart Home shell and discovery | Draft PR; SHA `1c6112a620a2f7cd7ec88bc48c3ead34ec5699bd` | IMPLEMENTED; verified scripture not yet loaded |
| UI-03 / PR #22 | Immersive reader shell and typography/focus controls | Draft PR; SHA `73d71b571ccbb7fdc4d21011e4c443dd2ae50205` | PARTIAL; reader shell, no complete corpus integration |
| UI-04 / PR #23 | Smart Search | Draft PR; SHA `3d34bf4f42fcc5290ea2d1907bc85c479f8653fd` | PARTIAL; catalogue metadata search only, not full-text scripture search |
| UI-05 / PR #24 | Audio and offline listening foundation | Draft PR; SHA `aaed9bf082208782354f56cae14d2c43a2b19174` | PARTIAL; playback/download disabled; no verified audio assets or engine |
| UI-06 / PR #25 | Smart Study Workspace | Draft PR; SHA `c054d9785338c0d76ae2b78258a1bbcf19876732` | PARTIAL; workspace foundation |
| UI-07 / PR #26 | Persistent local journal notes data layer | Draft PR; SHA `adbf067db212c32fef66c9f58246d5b0d7157f3e` | IMPLEMENTED; schema v4 and SQLite tests recorded; emulator matrix had failures at the time of assessment |
| UI-08 / PR #27 | Persistent study-workspace integration | Draft PR; SHA `ed3313ccce69b820d0f9131543fd29e7b76a0a4e` | PARTIAL; note CRUD integration implemented; exact-head verification not recorded as complete |

UI-07 implements persistent journal-note CRUD and migration behavior. UI-08 connects that data layer to the workspace. Bookmarks, highlights and collections remain inactive pending verified canonical passage identity and related implementation.

### Android verification and reliability

PR #38 corrected deterministic Android compilation/test issues and improved emulator recovery/diagnostics. Its exact-head synthetic verification run #42679607142 passed host contracts and Android API 26, 28, 30, 33, 35 and 36. PR #38 was squash-merged into the A3.8 feature branch at `7deea2cdd8f11eb83223a2484a52f81b25a9733a`; it was not merged into `main`.

A prior Android UI baseline run on PR #31 passed host unit tests, but earlier emulator failures and the pending verification history for UI-05 through UI-08 mean the entire UI suite is not yet verified as one integrated release candidate.

## 4. Authentication and backend progress

User-approved policy:
- Login identifier: username **or** mobile number plus password.
- Guest scripture reading: allowed without an account.
- OTP: registration only.
- System-generated immutable account identity.
- No unapproved expansion to password-recovery, phone-change, every-login or sensitive-action OTP.

| Stage | Scope | Current evidence | Status |
|---|---|---|---|
| A2 / PR #28 | Authentication architecture reconciliation | Draft, unmerged; SHA `301f64fca25fa82055a3a14180e5b23a1d2567d2` | IMPLEMENTED as architecture baseline; not integrated |
| A3.1 / PR #29 | Domain contracts | Draft, unmerged; SHA `16511d4d98eae1813abad28606bde21f5c6f44b0` | IMPLEMENTED; unit tests were not recorded as run at initial handoff |
| A3.2 / PR #30 | Registration/session API contract | Draft, unmerged; SHA `149079bbfce0fe9a543a95efeeb091ac8a8c49a4` | CONTRACT DEFINED; public route not activated |
| A3.3 / PR #32 | Isolated FastAPI backend foundation | Draft, unmerged; SHA `b1e644107a09cbd4186d8b22d45e2806d52c21c9` | IMPLEMENTED; source-equivalent test previously 1/1 PASS |
| A3.4 / PR #33 | Account, challenge and session persistence models | Draft, unmerged; SHA `fbd015e3b8880413453d868a742d108ea7bf1bf8` | IMPLEMENTED; model tests previously 5/5 PASS |
| A3.5 / PR #34 | Initial Alembic migration | Draft, unmerged; SHA `2aae397fe5c09ab5df230d94bcee20bb085ffb45` | IMPLEMENTED; isolated SQLite migration test previously 1/1 PASS |
| A3.6 / PR #35 | Argon2id, OTP/HMAC and SMS provider abstraction | Draft, unmerged; SHA `2fb3e1f2a423fed8210435d99b20de05cca844d8` | IMPLEMENTED; primitive tests previously 5/5 PASS |
| A3.7 / PR #36 | Registration challenge creation and password hashing | Draft, unmerged; SHA `7f8a677c72d222192dc2937fbd6b2f164dac3dfe` | PASS on exact-head backend run #42611177018: 20 tests |
| A3.8 / PR #37 | Atomic OTP verification and challenge consumption | Draft, unmerged; head updated by PR #38 merge to `7deea2cdd8f11eb83223a2484a52f81b25a9733a` | Backend/OpenAPI/Android host checks and Android API matrix passed on updated head |
| A3.8 hardening / PR #39 | PostgreSQL concurrency and duplicate-registration race protection | Draft, unmerged; exact HEAD `4e6e9d552fe6e9f992451aa56f0f382597f4edcd` | Backend and contract checks PASS; see section 5 |

PR #31 is a separate Android Kotlin compilation repair branch; it remains open/draft and was not merged to `main`.

## 5. Latest exact-HEAD verification — PR #39

PR: `https://github.com/akefadaka-git/maale-bible/pull/39`  
Branch: `feat/auth-a3-postgres-concurrency`  
Base: `feat/auth-a3-registration-otp-verification`  
Exact tested HEAD: `4e6e9d552fe6e9f992451aa56f0f382597f4edcd`

| Check | Workflow evidence | Result |
|---|---|---|
| Backend suite with PostgreSQL 16 service | [Run #42748595152](https://github.com/akefadaka-git/maale-bible/actions/runs/42748595152) | PASS — 29 passed, 1 warning |
| PostgreSQL concurrent OTP verification | Included in #42748595152; test module executed | PASS — exactly one of two simultaneous valid submissions consumes the challenge |
| PostgreSQL duplicate username race | Included in #42748595152; test module executed | PASS — one active challenge created |
| PostgreSQL duplicate mobile race | Included in #42748595152; test module executed | PASS — one active challenge created |
| OpenAPI contract | [Run #42748594817](https://github.com/akefadaka-git/maale-bible/actions/runs/42748594817) | PASS |
| Android authentication unit tests | [Run #42748594817](https://github.com/akefadaka-git/maale-bible/actions/runs/42748594817) | PASS |

The PostgreSQL integration suite ran all three concurrency tests with no skips. The backend workflow completed 29 tests with one warning. PR #39 remains draft and unmerged.

The challenge reservation uses transaction-scoped PostgreSQL advisory locks on normalized username and mobile identifiers, acquired in sorted order. Existing unique account constraints remain the final account-identity integrity boundary. This protection assumes all registration challenge writers use this service/lock protocol; any alternate writer must use equivalent enforcement.

## 6. Content, licensing and feature readiness

- Verified Maale scripture source baseline: 2008 Maale Full Bible MDYTWF/mdyeth, confirmed by the Owner as CC BY-SA 4.0, attributed to The Word for the World International and Bible Society of Ethiopia.
- The 2007 DBL MDYBSE Maale New Testament is a distinct source and must not be substituted for the verified full Bible.
- Expanded Amharic, English and study-resource intake is authorized only after source-by-source provenance, licence/permission, attribution, integrity and compatibility checks.
- This report does not claim that the full Maale corpus or other language corpora have been imported, validated and integrated into the Android reader.
- Search is currently a 66-book metadata catalogue, not full-text scripture search.
- Audio playback and offline downloads are not implemented.
- Bookmarks/highlights/collections are not yet functional end-to-end.
- Guest reading remains a required product behavior; account creation must not become a prerequisite for reading.

## 7. Production-readiness gates

| Gate | Status | Evidence / gap |
|---|---|---|
| Canonical repository governance | PASS | Canonical public repository and Owner stage-gate rules established |
| Approved authentication policy | PASS | Username/mobile login, guest reading, registration-only OTP |
| UI design and navigation foundations | PARTIAL | PRs #20–#27 remain unmerged; integrated release build not established |
| Verified scripture corpus integrated | MISSING / UNVERIFIED | Source verified; complete app import, attribution display and integrity verification not evidenced here |
| Full-text scripture search | MISSING | Current search is catalogue metadata only |
| Audio engine and licensed audio content | MISSING | Placeholder only |
| Persistent notes | PARTIAL | Journal CRUD/schema implemented; integration PR #27 still unmerged |
| Bookmarks/highlights/collections | MISSING / INACTIVE | Requires canonical passage identity and end-to-end persistence |
| Backend auth models and migration | PARTIAL | Implemented on stacked draft PRs; no integrated production database rollout |
| Registration challenge security | PASS within tested slice | Argon2id, OTP digest, expiry and attempt budget |
| PostgreSQL concurrency | PASS within tested slice | 3 integration tests pass on PostgreSQL 16 |
| Public API endpoint security | NOT RUN / NOT IMPLEMENTED | No public registration route enabled |
| SMS delivery provider | NOT IMPLEMENTED | Provider abstraction only; no selected provider or credentials |
| Abuse controls / IP-device rate limits / resend enforcement | MISSING | Must be implemented and tested before public exposure |
| Login/session lifecycle and revocation | PARTIAL / NOT VERIFIED END-TO-END | Persistence model exists; full production route and lifecycle evidence absent |
| Password recovery / phone change | OUT OF SCOPE | Not approved by current OTP policy |
| Independent application security review | NOT RUN | Required before production authentication exposure |
| Full Android release build and device QA | NOT VERIFIED | Emulator matrix evidence exists for PR #38; no integrated signed release candidate |
| Accessibility / TalkBack validation | NOT RUN | Requires full integrated UI/device verification |
| Production infrastructure, secrets, backups, monitoring and incident response | NOT RUN | No production environment deployment evidence |
| Signing, store submission and publication | NOT AUTHORIZED / NOT RUN | Separate Owner approval required |

## 8. Progress measurement

A single percentage would be misleading because the project has multiple independent workstreams and many implementation branches are not merged or integrated.

A reliable count-based snapshot is:
- UI foundation slices UI-01 through UI-08: 8 implementation slices exist; all remain in draft/unmerged PRs.
- Authentication architecture/backend slices A2 and A3.1–A3.8: implementation work exists through A3.8; all relevant PRs remain unmerged, with PR #38 merged only into the feature branch and PR #39 still draft.
- PR #39 PostgreSQL concurrency hardening: exact-head backend, OpenAPI and Android auth unit checks all PASS.
- Production deployment gates: not complete; no production readiness claim is justified.

Therefore, implementation progress is substantial, but integrated-product and production readiness remain materially lower than the count of completed code slices.

## 9. Recommended next sequence

1. Finish the A3.8 concurrency PR review and obtain an independent security review of registration, OTP verification, concurrency and identifier reservation.
2. Reconcile the stacked draft PR chain into a coherent integration plan; do not merge to `main` until the Owner approves the integration gate.
3. Complete authentication abuse controls, resend policy enforcement, account activation transaction, login/session lifecycle, revocation and safe runtime configuration.
4. Integrate and validate the licensed Maale scripture corpus, then implement full-text search.
5. Complete audio and study features, including passage-linked bookmarks/highlights/collections.
6. Run integrated Android build, emulator/device, TalkBack/accessibility, migration and PostgreSQL tests on the final integration head.
7. Conduct production infrastructure/security/privacy review and prepare a release candidate.
8. Obtain separate Owner authorization for signing, deployment and publication.

## 10. Governance and change record

This report is a repository progress record only. It does not authorize PR merges, changes to `main`, public registration, live SMS, signing, deployment, publication or production activation. Unrelated work must be preserved. Reopen closed gates only for material contradictory evidence or an authoritative change.


---

## 11. Owner decision and sequential progress update — 10 November 2026

### Independent-review gate

The Owner waived the need for an independent external security reviewer as a prerequisite to continuing implementation. See [Owner decision record](../governance/OWNER_DECISION_INDEPENDENT_SECURITY_REVIEW_WAIVER_2026-11-10.md).

This is **WAIVED FOR SEQUENCING**, not a security certification. The AI-assisted internal assessment remains accurately classified as internal; it is not represented as an independent review or penetration test. No external-review blocker will stop the next engineering stage unless the Owner changes this decision or material new evidence requires escalation.

### Next sequential authentication stage — exact-head evidence

- **PR #40 — registration abuse controls:** [PR #40](https://github.com/akefadaka-git/maale-bible/pull/40), tested code SHA `0f74f2469092e50ea82b0c2c3b9418987a10fe8e`.
  - Backend run [#42755062928](https://github.com/akefadaka-git/maale-bible/actions/runs/42755062928): **PASS — 37 tests, 1 warning**.
  - Authentication contract run [#42755062794](https://github.com/akefadaka-git/maale-bible/actions/runs/42755062794): OpenAPI contract and Android authentication unit tests **PASS**.
- **PR #41 — account activation and session lifecycle:** [PR #41](https://github.com/akefadaka-git/maale-bible/pull/41), tested code SHA `620781c2c60cb19f5737c1be95e82b12ce03630a`.
  - Backend run [#42755780898](https://github.com/akefadaka-git/maale-bible/actions/runs/42755780898): **PASS — 48 tests, 1 warning**, including PostgreSQL-backed backend test job.
  - Authentication contract run [#42755780900](https://github.com/akefadaka-git/maale-bible/actions/runs/42755780900): OpenAPI contract **PASS**; Android authentication unit tests **PASS**.
  - Implemented service scope: atomic OTP verification/account activation/first session; username-or-mobile password login; hashed opaque access/refresh tokens; access validation; refresh rotation and replay-triggered token-family revocation; logout revocation.
  - No public auth route, SMS delivery, production secrets or production activation is enabled.

### Updated gate status

| Gate | Updated status | Qualification |
|---|---|---|
| Independent external security review | **WAIVED BY OWNER FOR SEQUENCING** | Not a security pass or certification |
| Registration abuse-control service slice | **PASS — exact-head CI** | Runtime/API wiring and deployment configuration remain separate |
| Account activation/session lifecycle service slice | **PASS — exact-head CI** | Service-layer and CI evidence; not production endpoint/device integration |
| Licensed Maale corpus and full-text search | **NEXT** | Source archive was previously structurally verified; re-hash/revalidation and parser/index work remain required |
| Production readiness | **NOT READY / NO-GO** | Integration, content, Android release QA, operational security and release gates remain open |

### Updated next sequence

1. Complete the authentication service-stage reconciliation and document any remaining runtime wiring boundaries; preserve the current registration-only OTP policy.
2. Proceed to controlled processing of the verified 2008 MDYTWF / mdyeth Maale Full Bible source: reacquire/reconfirm the archive hash, validate staged file hashes, qualify marker-aware USFM parsing using synthetic tests, then process the licensed source in private staging.
3. Build and validate the deterministic offline content database and FTS5 index outside the public repository; retain source and edition provenance and qualify Maale/Ethiopic and Amharic tokenization separately.
4. Integrate the private content package through the Android offline resource contract, then test reader, search, attribution, migration, rollback, offline operation and repository content-boundary controls.
5. Continue other language editions and study resources only through source-specific rights and quality gates.
6. Complete audio and passage-linked study features, integrated Android regression/accessibility/device checks, and production operations.
7. Obtain separate Owner authorization before merge to `main`, signing, deployment or publication.

**Current decision:** continue implementation; production remains **NO-GO**. PRs #39–#41 remain draft/unmerged and `main` remains unchanged.
