# A4 — P2 Architecture Design Completion Record

**Date:** 2026-11-08  
**Status:** Architecture design prepared; technical evidence and Owner decisions remain pending.  
**P0/P1 approval:** Accepted by Owner in conversation on 2026-11-08.  
**Branch:** `feat/a4-offline-study-suite`

## Deliverables

- `docs/architecture/A4_P2_ARCHITECTURE_AND_DATA_DESIGN.md`: layered architecture, domain model, stable passage identity, Room schema plan, resource lifecycle, search/index design, privacy/backup, Compose state ownership, localization/accessibility, module boundaries and verification contracts.
- `docs/architecture/adr/ADR-A4-001-stable-passage-identity.md`
- `docs/architecture/adr/ADR-A4-002-resource-integrity-and-rights-gate.md`
- `docs/architecture/adr/ADR-A4-003-offline-search-index.md`
- `docs/architecture/adr/ADR-A4-004-local-personal-data-and-backup.md`
- `docs/architecture/adr/ADR-A4-005-compose-navigation-and-state.md`

## Assessment

| Workstream | Result | Qualification |
|---|---|---|
| Domain model/reference identity | PREPARED | Canonical identity proposed; mapping standard remains open |
| Room schema/migrations | PREPARED | Entity/table plan; schema not implemented |
| Resource manifest/import lifecycle | PREPARED | Fail-closed state machine; signature policy open |
| Search/index architecture | PARTIAL | FTS5 candidate only; Android and language evaluation pending |
| Privacy/backup/restore | PREPARED | Threat model and contract; encryption decision needs evidence |
| Compose navigation/state | PREPARED | Typed-route architecture; route implementation not started |
| Architecture verification | NOT RUN | No feature implementation or runtime verification claimed |

## Governance

- Content intake remains unauthorized.
- No scripture text, archive, maps, audio, scans or third-party content assets were acquired or ingested.
- Synthetic fixtures and metadata-only records only.
- No production schema, Kotlin implementation, dependency change, merge, signing or deployment was performed by this design record.
- Existing A2/A3 baselines remain unchanged.

## Required before P2 closure

1. Review and resolve the open architecture decisions listed in the design document.
2. Verify FTS5/SQLite and tokenizer feasibility on supported Android devices.
3. Validate backup encryption/key recovery and database protection requirements.
4. Finalize stable versification mapping semantics and schema.
5. Map all 42 screen IDs to typed routes and state contracts.
6. Record Owner disposition of decisions that materially affect compatibility, user-data security or product behavior.

**P2 status:** DESIGN PREPARED — NOT VERIFIED-CLOSED. The next step is evidence-backed decision resolution, then implementation planning; this record does not authorize A3/P3 code implementation by itself.
