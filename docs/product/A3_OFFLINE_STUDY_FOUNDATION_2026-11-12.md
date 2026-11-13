# A3 offline study foundation — 2026-11-12

> **Authority history note (2026-11-14):** The statement below that scripture-content intake remains unauthorized records the authority state at the time this 2026-11-12 foundation slice was prepared. It is superseded as current project-level authority by the Owner's 2026-11-08 controlled content-intake authorization, reconciled in [Owner Content-Intake Authority Reconciliation](../licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md). No scripture content was added by this historical study-foundation change. Source-specific rights and distribution gates remain mandatory.

## Scope
- Coordinate-only reader navigation; chapter and verse bounds are not inferred.
- SQLite persistence for reading position, bookmarks, highlights, notes, and reading plans.
- Versioned JSON export and transactional restore of user-created study metadata.
- Non-destructive schema migration from database version 1 to version 2.
- JVM tests for navigation and model invariants.
- Android instrumentation tests for database close/reopen persistence and backup restore/rejection.

## Content boundary
No scripture text, verse counts, chapter counts, translation data, audio, or third-party assets are included. Coordinates are not asserted as authoritative scripture boundaries. Scripture-content intake remains separately gated and unauthorized.

## Verification
The A3 Android workflow runs JVM tests and assembles the debug APK, then starts an Android API 35 emulator and runs connected instrumentation tests. All results must be checked against the exact PR head SHA. A source change after a run requires a new exact-head run.

## Governance
This branch is based on the unmerged PR #53 head. It does not modify main or the closed A2 baseline. Merge, signing, deployment, publication, and branch deletion remain separately controlled.
