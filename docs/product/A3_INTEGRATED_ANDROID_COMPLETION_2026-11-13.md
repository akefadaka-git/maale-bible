# A3 integrated Android completion work — 2026-11-13

## Branch and governance

- Branch: `feat/a3-integrated-android-completion-2026-11-13`
- Starting point: `7555f33ce66d2225bd8efdeb1bc167750ce1a06b`
- Base: existing A3 offline-study implementation branch.
- This work does not change the closed A2 baseline or add scripture source files.
- Draft PR only; merge, signing, deployment and publication remain separate approvals.

## Implemented in this increment

- Expanded Android instrumentation coverage for full-field migration preservation across reading position, bookmark, annotation (including nullable colour), and reading plan records.
- Added migration rejection test for invalid legacy coordinates; asserts the original plaintext source remains queryable after migration abort.
- Added transactional restore regression test: duplicate primary keys must fail without deleting existing study data.
- Added UI instrumentation coverage for Home → Books → Reader → Search → Reader navigation and search empty state.
- Added Settings navigation with persisted light/dark theme preference.
- Added settings disclosures for accessibility, localization completeness, local encrypted study storage and unavailable scripture content.
- Study database is closed when the app composition is disposed.
- Renamed placeholder routes to explicit `ReaderScreen` and `CatalogueSearchScreen`; catalogue search remains metadata-only and never searches scripture text.

## Verification state

- Earlier exact-head workflow `43244923660` passed on its own SHA.
- Workflow `43270697262` was cancelled while emulator instrumentation was running. Its logs show 11/12 tests completed with zero failures before cancellation; this is not a passing terminal result.
- New fault-injection and UI tests have been committed on this branch but have not yet been executed by GitHub Actions.
- No claim of A3 closure or production readiness is made by this increment.

## Remaining A3 blockers

1. Run exact-head CI including the newly added instrumentation tests; diagnose runner/emulator timeouts separately from test assertions.
2. Add deterministic cutover fault injection for each migration rename boundary and WAL checkpoint failure. Current tests cover invalid-data abort and data preservation, not process-kill simulation at every filesystem boundary.
3. Verify backup restore on a separate physical device and exercise interruption after DEK installation, before database commit.
4. Complete reader content integration only after a separately authorized, integrity-checked scripture package is available. Current reader provides coordinates and personal study tools, not scripture text.
5. Complete end-to-end Amharic and English localization, real-device accessibility review, font scaling acceptance, and lifecycle/process-death testing.
6. Complete independent external security assessment and A4 acceptance.

## Content and security boundaries

- No scripture text, inferred verse bounds, translation, audio, or unverified assets are introduced.
- Personal study metadata remains in the SQLCipher-backed local database.
- The portable backup uses the existing Argon2id/AES-GCM envelope. A clean Keystore recovery simulation on one emulator is not proof of cross-device recovery.
- The focused code review is not an independent auditor's sign-off.
