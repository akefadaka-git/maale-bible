# UI-08 — Persistent Study Workspace Integration

## Scope

Connect the existing Compose Study workspace to the schema-v4 local journal-note API delivered in UI-07. This stage covers journal note loading, creation, editing, deletion, refresh, and user-visible failure handling.

## Implementation

- Reuses the existing `LocalStudyDatabase` and `StudyNote` APIs; no duplicate schema or database is introduced.
- Opens the private SQLite database lazily on a single serial worker thread.
- Performs database open, query, insert, update, delete, and close off the Compose main thread.
- Loads persisted journal notes when the Study screen is entered.
- Supports create, edit, delete, and explicit refresh. UI state changes only after successful database operations.
- Uses UUID note IDs and millisecond timestamps. Journal notes remain unlinked (`canonical_id IS NULL`).
- Displays database exceptions rather than silently presenting failed writes as successful.
- Closes the database connection on the worker during screen disposal.
- Keeps bookmarks, highlights, and collections inactive until verified passage identities and corresponding persistence workflows exist.

## Privacy and content integrity

Notes remain in the app-private on-device database. This stage adds no network sync, cloud backup, analytics, scripture text, audio, or fabricated passage references. Android app-private storage semantics apply.

## Verification

- UI-07 instrumentation tests cover schema-v4 journal CRUD, persistence across close/reopen, and v3 migration preservation. The UI-07 CI matrix had API 26 and API 28 emulator jobs fail with runner exit code 1; logs show emulator/system activity but do not expose a specific test assertion failure. Other matrix jobs were not all complete at the last poll.
- UI-08 exact-HEAD Android build and emulator/instrumentation results are pending.
- TalkBack, manual device UX, process-death UI restoration, and broader accessibility checks are not claimed as verified.

## Governance

Branch: `feat/ui-08-persistent-study-integration`

Base: UI-07 exact HEAD `adbf067db212c32fef66c9f58246d5b0d7157f3e`.

Draft PR only. No merge, signing, deployment, or publication is authorized by this change.
