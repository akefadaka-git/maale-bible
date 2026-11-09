# Maale Bible UI-07 — Persistent Study Notes Data Layer

## Scope

Introduces schema v4 and durable CRUD operations for independent personal journal notes while preserving the existing passage-linked notes and canonical/source-mapping schema.

## Implemented

- Added an atomic v3-to-v4 migration.
- Rebuilds the existing study_notes table with nullable canonical_id and updated_at; copies every existing record with its original ID, passage reference, body and creation timestamp.
- Retains the existing canonical passage foreign key and ON DELETE CASCADE behavior for passage-linked notes.
- Adds indexes for canonical reference and most-recently-updated ordering.
- Adds add/list/update/delete operations for independent journal notes.
- Keeps existing addNote behavior for passage-linked notes, now initializing updated_at from created_at.
- Adds instrumentation coverage for fresh schema, v3 migration preservation, durable journal CRUD, existing foreign-key behavior and cascade behavior.

## Integrity boundaries

- Journal notes do not require fabricated or guessed scripture references.
- Passage-linked notes remain subject to the existing canonical_passages foreign key.
- Bookmarks and highlights are not implemented here because validated chapter/verse rows are not yet populated in canonical_passage.
- No scripture content, source mapping, or new dependencies are introduced.

## Migration behavior

Database schema version: 3 → 4. The migration runs inside BEGIN IMMEDIATE / COMMIT with rollback on failure. It preserves legacy passage-linked note records. Fresh databases continue through the existing v1, v2 and v3 migrations before v4.

## Verification plan

- Execute the exact-HEAD Android instrumentation matrix and inspect all device API jobs.
- Verify all legacy migration, foreign-key and cascade tests.
- Confirm journal note CRUD survives closing and reopening the database.
- Run Android compilation and full unit/instrumentation suites.
- UI integration and accessibility checks are separate follow-up work.

## Governance

- Base: feat/ui-06-smart-study-workspace
- Base SHA: c054d9785338c0d76ae2b78258a1bbcf19876732
- Branch: feat/ui-07-persistent-study-notes
- Draft PR only; no merge, deployment, signing or publication authorized.
