# Maale Bible UI-06 — Smart Study Workspace

## Scope

Adds a dedicated Study destination and a responsive workspace foundation for reflection notes, bookmarks, highlights and themed study collections.

## Implemented

- Added a Study item to the primary bottom navigation.
- Added responsive workspace layout with Notes, Bookmarks, Highlights and Collections sections.
- Added a working session-only reflection composer and in-memory note list with removal.
- Added explicit inactive states for bookmarks, highlights and collections.
- Preserved the existing reader, search, catalogue and listening destinations.
- Added no new dependencies, permissions, network calls or scripture content.

## Data integrity and persistence boundary

The existing `LocalStudyDatabase` includes a `study_notes` table, but its current API exposes note insertion and count only; it does not provide complete note retrieval/update/delete operations. Its note rows require a canonical passage foreign key. The current reader does not yet expose verified chapter/verse passage identities because the authorized scripture corpus and validated passage structure are not integrated.

For those reasons this UI stage does not write synthetic or unverified references into the database and does not claim durable note persistence. Draft reflections are held in Compose session state and are lost when the screen/app process is recreated. Bookmarks, highlights and collections remain inactive until verified passage identity and durable CRUD workflows are implemented.

## Verification requirements

- Run the repository's exact-HEAD Android workflow and inspect every matrix job.
- Verify Compose compilation and unit/instrumentation tests.
- Exercise phone and tablet layouts, TalkBack labels/focus order, large font scaling and dark/light themes.
- Before durable study implementation, reconcile the existing database schema and migrations; add tested CRUD/repository APIs without destructive migration or duplicate tables.
- Integrate notes/bookmarks/highlights only against verified canonical passage identifiers and authorized source mappings.

## Governance

- Base: `feat/ui-05-audio-offline-foundation`
- Base SHA: `aaed9bf082208782354f56cae14d2c43a2b19174`
- Branch: `feat/ui-06-smart-study-workspace`
- Draft PR only; no merge, deployment, signing or publication authorized.
