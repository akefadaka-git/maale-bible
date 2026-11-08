# P2-V3 and P2-V2 Execution Record — 2026-11-08

## Scope and governance

Feature branch only: `feat/a4-offline-study-suite`. All test records are synthetic. No scripture text, licensed reference content, audio, scans, or source archives were acquired, inspected, imported, or committed. No merge, signing, deployment, or publication was performed.

## P2-V3 — Bundled SQLite FTS5 Android API matrix

**Result: PASS on the original complete matrix; retry of API 36 is being repeated after a separate emulator installation failure.**

Original complete workflow: https://github.com/akefadaka-git/maale-bible/actions/runs/42461819934

All six Android emulator jobs completed successfully in that run:

| API | Job ID | Job conclusion | Test output |
|---:|---:|---|---|
| 26 | 128672539183 | success | Gradle build and connected instrumentation task successful |
| 28 | 128672539113 | success | Gradle build and connected instrumentation task successful |
| 30 | 128672539085 | success | 11 tests, 0 skipped, 0 failed |
| 33 | 128672539157 | success | 13 tests, 0 skipped, 0 failed |
| 35 | 128672539312 | success | 15 tests, 0 skipped, 0 failed |
| 36 | 128672539319 | success | 16 tests, 0 skipped, 0 failed |

The test exercises the bundled SQLite driver with FTS5 virtual-table creation, synthetic insert, MATCH query, rank ordering, index rebuild, and a schema-addition compatibility check. This is SQLite/FTS5 driver compatibility evidence; it does not establish linguistic tokenization correctness or application search relevance.

A second run (#42462238507) also passed API 26, 28, 30, 33 and 35. API 36 failed during test APK installation with a split-APK installation error (UTP plugin error), not an FTS5 assertion failure. The failed job was re-run. Latest job snapshot: API 36 job `128703942953` is in progress; its log endpoint has not yet yielded a downloadable log. No compatibility code change is justified by an APK-installation infrastructure error.

**Disposition:** The six-API compatibility gate has a complete successful original run. Preserve the API 36 retry as additional confidence, not as evidence of an FTS5 defect. Do not claim the retry passed until its job concludes.

## P2-V2 — Synthetic persistence boundary and executable invariants

**Implementation status: CODE AND TESTS COMMITTED — EXECUTION NOT YET VERIFIED.**

Commits:
- Persistence boundary: `a7b58be7b504a2d65364748a785901e00a8573d2`
- SQLite statement completion correction: `a9cea6633e978bb18da0937c7931a311f632b41e`
- Instrumentation tests: `961c1a70bb8dbe6a3005fbdb553be85d841839ba`

Files:
- `app/src/main/java/org/maale/bible/data/LocalStudyDatabase.kt`
- `app/src/androidTest/java/org/maale/bible/P2V2LocalStudyPersistenceTest.kt`

The implementation uses the already-pinned AndroidX bundled SQLite driver. It creates a local metadata/user-data schema:
- `canonical_passages`: edition-independent canonical passage identity and checked verse ranges.
- `editions`: edition metadata only.
- `edition_passage_map`: edition references, canonical relationship and constrained mapping status; unique edition/reference pair.
- `study_notes`: user-authored local notes linked to canonical passage identity.

The database enables SQLite foreign-key enforcement and uses transactional schema creation and v1-to-v2 migration. The v1-to-v2 migration adds the notes table and index while preserving existing passage and edition mapping rows.

Six instrumentation tests were added:
1. Fresh database schema version and synthetic record persistence.
2. v1-to-v2 migration preserving prior passage/mapping rows and allowing post-migration notes.
3. Foreign-key rejection of orphan mappings and notes.
4. Unique edition/reference constraint.
5. Canonical passage deletion cascades to related mappings and personal notes.
6. Invalid verse-range rejection before persistence.

**Important architecture qualification:** This slice currently uses the AndroidX bundled SQLite driver directly, not Room. The approved P2 architecture decision preferred Room over the bundled SQLite driver. This implementation is a narrow persistence verification slice, but the Room-vs-direct-driver choice must be reconciled against ADR-A4-003/ADR-A4-004 before treating it as the final production persistence architecture. No silent ADR change is claimed.

**Verification status:** The tests are committed on the feature branch and the workflow is configured to run on pushes affecting `app/**`. The exact post-commit Actions run and result for SHA `961c1a70bb8dbe6a3005fbdb553be85d841839ba` have not yet been retrieved. Therefore P2-V2 remains **NOT VERIFIED** until compile and instrumentation tests pass against that exact SHA (or a later corrected SHA).

## Exact latest implementation SHA

`961c1a70bb8dbe6a3005fbdb553be85d841839ba`

## Next verification actions

1. Collect the API 36 retry result from run #42462238507.
2. Retrieve the push-triggered workflow for implementation SHA `961c1a70bb8dbe6a3005fbdb553be85d841839ba`; verify compile, instrumentation test counts, and all six API jobs.
3. If compilation or instrumentation fails, fix only the evidenced defect and rerun on the new exact commit.
4. Resolve the direct bundled-SQLite vs Room architecture qualification before marking P2-V2 complete.
5. Add any missing transactional rollback and concurrency invariants after the base suite passes.

## Gate disposition

- P2-V3: **PASS** on original six-API matrix; supplemental API 36 retry pending.
- P2-V2: **IMPLEMENTED / NOT YET VERIFIED**.
- P3: **NOT STARTED** and remains gated on P2 verification and applicable stage approval.
- `content_intake_authorized=false`: unchanged.
- Merge, release signing, deployment and publication: not performed.
