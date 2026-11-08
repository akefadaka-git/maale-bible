# P2 Verification Closure Readiness Record — 2026-11-08

**Repository:** `akefadaka-git/maale-bible`  
**Branch:** `feat/a4-offline-study-suite`  
**PR:** [#19 — P2 offline persistence and synthetic invariants](https://github.com/akefadaka-git/maale-bible/pull/19)  
**Implementation baseline:** `f582fa3ce3c0ee0e4777b4f7269b890ae6fbb567`  
**Current PR head before this report amendment:** `6a8932599f55c22caf3954403212c0f111481d53` (two commits after the implementation baseline: this record plus a one-line test comment; no functional code change).  
**Disposition:** **PARTIAL — NOT VERIFIED-CLOSED**  
**Content gate:** `content_intake_authorized=false`

## 1. Summary

The persistence architecture qualification has been explicitly resolved under the Owner's instruction: this offline release uses AndroidX SQLite with the bundled SQLite driver directly, behind data/repository boundaries. The earlier Room preference is superseded by the accepted amendment in ADR-A4-004 and the corresponding update to ADR-A4-005. The current persistence implementation is therefore no longer an undocumented architecture deviation.

New verification implementation has been added for encrypted backups, synthetic Unicode preservation, current-shell navigation, Android FTS5 performance, and repository content-boundary scanning. These implementations are not equivalent to passed tests. The current exact-HEAD GitHub Actions run is queued, and the older retry matrix has encountered repeated emulator/ADB failures. P2 cannot be closed until exact-HEAD test results and the remaining required evidence are available.

No merge, release signing, deployment, publication or content intake has occurred. No payment or top-up is required.

## 2. Architecture qualification — RESOLVED

- **Decision:** direct AndroidX SQLite + bundled SQLite driver; no Room in this release database.
- **ADR:** `docs/architecture/adr/ADR-A4-004-local-personal-data-and-backup.md`.
- **Navigation/data boundary ADR:** `docs/architecture/adr/ADR-A4-005-compose-navigation-and-state.md`.
- **Decision register:** `docs/architecture/A4_P2_DECISION_RESOLUTION_2026-11-08.md`.
- **Rationale:** explicit bundled FTS5 support, explicit SQL constraints and migrations, no dual persistence abstraction, and the current narrow schema. SQL remains behind data/repository boundaries. This is a release-specific architecture decision, not a general claim that direct SQLite is superior to Room.
- **Verification still required:** exact-HEAD Android migration, persistence, foreign-key, deletion and failure-path tests.

## 3. Gate-by-gate disposition

| Gate | Status | Current evidence and remaining work |
|---|---|---|
| P2-V1 — Requirement traceability | **PASS — mapping only** | 88 unique IDs; 82 active requirements mapped; 6 deferred server requirements excluded. Host validator was previously green. This does not mean every feature implementation exists. |
| P2-V2 — Data model, relationships, migration and deletion | **PARTIAL** | Direct bundled-SQLite schema and six instrumentation tests exist. API 26, 28, 30 and 33 passed in the latest completed retry snapshot; API 35 retry remained queued and API 36 failed with emulator/UTP split-APK/ADB infrastructure errors. Exact current-head rerun remains queued. |
| P2-V3 — FTS5 driver matrix | **PASS — original matrix** | Original six-API workflow #42461819934 passed. This validates bundled SQLite FTS5 operations, not linguistic search correctness. |
| P2-V4 — Unicode/language contracts | **PARTIAL** | Host and Android synthetic Ethiopic/mixed-script/NFC preservation tests added. They do not validate Maale tokenization, morphology or orthography. Qualified specialist review remains deferred. Exact-HEAD execution pending. |
| P2-V5 — Backup security | **IMPLEMENTED PROTOTYPE — NOT VERIFIED** | AES-256-GCM envelope, PBKDF2-HMAC-SHA256 (310,000 iterations), random salt/nonce, versioned KDF metadata, authenticated header, size/count/path limits, manifest and per-entry SHA-256, plus Android tests for round-trip, wrong password, tamper, unsupported version/KDF, traversal-on-create and truncation. Tests have not passed in CI. Staged restore preview, merge/replace workflow, rollback and independent security review remain outstanding. |
| P2-V6 — Navigation/state | **PARTIAL** | 42 screen IDs/routes and six state labels are structurally validated. An Android Compose smoke test covers the implemented Home → Books → Reader → Books → Search journey. The current application has only three top-level destinations and a reader placeholder; the full 42-route graph, process recreation, deep links and accessibility tests remain outstanding. |
| P2-V7 — Performance | **IMPLEMENTED TEST — NOT VERIFIED** | Android instrumentation benchmark seeds 100,000 synthetic FTS5 rows and measures 30 warm queries against the 300 ms p95 budget. No Android result exists yet. Cold launch, navigation, frame jank, main-thread I/O and ANR SLOs are not yet evidenced. |
| P2-V8 — Content boundary | **PARTIAL** | Host validator now checks protected payload extensions/directories and requires the explicit false intake flag. Exact-HEAD host workflow is queued. Static path scanning does not prove historical non-acquisition or inspect external content. |

## 4. Implementation and evidence references

- Persistence implementation: `app/src/main/java/org/maale/bible/data/LocalStudyDatabase.kt`.
- Persistence instrumentation: `app/src/androidTest/java/org/maale/bible/P2V2LocalStudyPersistenceTest.kt`.
- Backup implementation: `app/src/main/java/org/maale/bible/data/backup/EncryptedBackup.kt`.
- Backup instrumentation: `app/src/androidTest/java/org/maale/bible/P2V5EncryptedBackupTest.kt`.
- Unicode instrumentation: `app/src/androidTest/java/org/maale/bible/P2V4UnicodeContractTest.kt`.
- Navigation smoke test: `app/src/androidTest/java/org/maale/bible/P2V6NavigationSmokeTest.kt`.
- Android benchmark: `app/src/androidTest/java/org/maale/bible/P2V7AndroidSearchBenchmarkTest.kt`.
- Host contracts and content boundary scan: `verification/p2/p2_verify.py`.
- Route contracts: `verification/p2/screen-contracts.json`.

Important implementation commits:
- Backup container: `120485c2b05d3e6e9b3c942a0d1b1a1b016d542e`.
- Backup tests: `a9f6ac5f8f6ea693de70c06475cfc586c65b1891`.
- Android FTS5 benchmark: `3bab7b554910df63e5fa201ecfadc034d3a1a96a`.
- Host Unicode/content-boundary checks: `f395acf99183c7e5e744ac0a5c8f7f1d799a5bce`.
- Unicode Android tests: `e63958193514a2e1f9d5c4c452c0a20b795ff4d3`.
- Navigation smoke test correction: `9cb3189e81f7d28736c95a05bb115a18d4414a6f`.
- Architecture decision amendment: `40d83875063ff63fcf28d215ccd8c4f4a5007288`, `e5b8cb81d23185e36cbcdd78e55c8cc03d6f2276`, `81a7c02a0ecf709205f04f083b7cccfbe1462711`.
- Backup KDF/header/manifest hardening: `d45e513da021fcb897443305dd9e6be23c608317`, `40534fde75c34acfd3501074b372f8bdb64b22f2`, `484379e7bf3845368d1202a953d949c761a39b0b`, `b6a1b08c56444f7d97afef5cf66dc1d898f6251e`.
- CI stabilization: `ef4f1d62f7c7cffb9517e22743bd66758c87e061`.

## 5. CI execution status

- Earlier P2-V2 retry workflow: [#42473816923](https://github.com/akefadaka-git/maale-bible/actions/runs/42473816923).
  - API 26, 28, 30 and 33: PASS.
  - API 35: emulator ADB daemon failures; a later retry remained queued.
  - API 36: emulator shell unresponsive / split-APK installation failure.
  - These are infrastructure failures; they are not persistence assertion failures, but they do not count as passing tests.
- Feature implementation workflow #42481662218: all jobs remained queued at the last retrieval, then were superseded/cancelled by the newer-run policy.
- Workflow #42482139112 (head `ef4f1d62f7c7cffb9517e22743bd66758c87e061`): all jobs ended **cancelled**.
- Workflow #42482316194 (head `f582fa3ce3c0ee0e4777b4f7269b890ae6fbb567`): all jobs ended **cancelled** after later branch updates.
- The latest current PR head has no completed exact-head run result available through the GitHub status/run queries at this assessment.
- Workflow/CI changes add Gradle dependency caching, emulator animation disablement, a longer timeout and latest-run concurrency policy. They use free GitHub-hosted runners only.

A queued or cancelled run is not verification evidence. Do not claim the consolidated exact-HEAD matrix passed.

## 6. Closure blockers

1. Obtain exact-head successful instrumentation results for P2-V2 and the new V4/V5/V6/V7 tests. API 35/36 emulator failures must be resolved or replaced with a documented free verification route.
2. Obtain independent security review of the backup cryptographic envelope and complete staged restore, merge/replace, rollback and malformed-archive failure tests.
3. Complete actual 42-route navigation/state contracts and automated route/restoration/accessibility coverage; the current shell smoke test is deliberately reported as partial.
4. Capture Android performance measurements for the 100k-record search and the remaining approved SLOs.
5. Run the host Unicode/content-boundary validator on the exact final SHA and retain its artifact and SHA-256.
6. Re-run all required checks against one final exact HEAD after any remediation, then prepare an evidence-linked final closure record.

## 7. Governance

- P2 overall: **PARTIAL — NOT VERIFIED-CLOSED**.
- P3: **NOT STARTED** pending P2 evidence and applicable stage approval.
- `content_intake_authorized=false`: unchanged.
- No scripture or licensed reference content was added.
- PR #19 remains open, draft and unmerged.
- Merge, signing, deployment and publication remain separate approvals.
- No paid CI, top-up, deposit or subscription is required.
