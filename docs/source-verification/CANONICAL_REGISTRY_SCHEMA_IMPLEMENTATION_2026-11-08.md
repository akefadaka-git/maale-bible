# Canonical Registry and SQLite Schema Implementation

**Date:** 2026-11-08  
**Branch:** `feat/a4-offline-study-suite`  
**ADR:** ADR-A4-001 — Stable Passage Identity  
**Canon:** `maale-bible:canon:66-v1`

## Implemented in source

- `content/canon/66-v1/registry.json`: frozen ordered 66-book registry fixture, with 39 OT and 27 NT entries and standard uppercase USFM IDs.
- `app/src/main/java/org/maale/bible/data/CanonicalCanonRegistry.kt`: typed in-app registry, duplicate/order/count invariants and ID membership validation.
- `database/schema/canonical-identity-v1.sql`: standalone SQLite DDL for the normalized identity model.
- `app/src/main/java/org/maale/bible/data/LocalStudyDatabase.kt`: additive schema migration from v2 to v3, seeding the canonical registry and preserving the existing v1/v2 study tables and user records.
- `app/src/androidTest/java/org/maale/bible/P2V2LocalStudyPersistenceTest.kt`: updated schema-version expectations and added registry-count, membership and canonical-key validation tests.

## Data model

1. `canon_registry`: versioned, frozen canon identity.
2. `canonical_book`: immutable ordered book IDs and canonical labels.
3. `canonical_passage`: composite coordinate (canon, book, chapter, verse, segment) and deterministic serialized key.
4. `edition`: immutable edition metadata, rights, attribution, source hash and versification profile.
5. `edition_passage`: edition-native reference and source-file/parser provenance.
6. `passage_mapping`: mapping state, provenance, review status and mapping version.
7. `passage_mapping_source` / `passage_mapping_target`: explicit many-to-many source/target cardinality for EXACT, SPLIT, MERGED, PARTIAL and UNMAPPED mappings.

Canonical registry and book rows are protected by SQLite immutability triggers. Foreign keys and composite uniqueness prevent invalid or duplicate coordinates. Positive chapter/verse checks and bounded segment syntax are enforced. Source text is deliberately absent from these identity tables.

## Migration and compatibility

- Database schema advances from v2 to v3.
- Fresh databases execute the existing v1 and v2 setup, then the v3 migration.
- Existing v1 databases migrate through v2 and v3.
- Existing v2 databases migrate directly to v3.
- Existing legacy study tables and note/mapping rows are retained; no destructive table replacement is performed.
- The canonical registry is seeded transactionally from the typed frozen registry.
- Unsupported future schema versions still fail closed.

## Verification status

- Source files and commits: recorded in the implementation commits on this branch.
- Android instrumentation tests: **NOT RUN in this implementation turn**.
- SQLite DDL execution/parity check: **NOT RUN**.
- Exact 66-ID comparison against the privately staged Maale archive: **NOT RUN**; the fixture is the approved standard 66-book registry and must be reconciled with the source manifest before importing source passages.
- Mapping cardinality finalization and importer enforcement: **NOT YET IMPLEMENTED**.
- FTS5, content database generation, source parser and Android content integration: **NOT RUN / NOT IMPLEMENTED**.
- No scripture text, raw source archive, generated content database or search index is included in these commits.

This is an implementation milestone, not P2 closure or production readiness. No merge, signing, release or deployment is authorized by this record.
