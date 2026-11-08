# A4 P2 — Offline Architecture and Data Design

**Status:** P2 architecture proposal; implementation has not started  
**Approved product baseline:** P0/P1 requirements and screen inventory  
**Branch:** `feat/a4-offline-study-suite`  
**Content gate:** `content_intake_authorized=false`  
**Architecture principle:** local-first, offline-capable, modular, rights-aware and testable.

## 1. Architecture overview

Recommended dependency direction:

```text
Compose UI / Navigation
        ↓
Presentation state holders (ViewModels)
        ↓
Domain use cases and entities
        ↓
Repository interfaces
        ↓
Data implementations
  ├── Room / SQLite (user records and metadata)
  ├── Local resource store (authorized packages only)
  ├── Local search index
  ├── Android platform adapters (notifications, share, files)
  └── Import/export and backup adapters
```

Rules:
- Domain code must not depend on Android UI or a specific database implementation.
- UI reads and writes through use cases/repositories; no SQL or file parsing in Composables.
- Installed content is immutable and versioned; user annotations are mutable and independently stored.
- Scripture text is never fabricated to fill missing content.
- All core use cases must have deterministic local behavior and no network dependency.
- Keep server integration out of this release. Interfaces may be designed for future substitution, but no remote implementation or hidden network path is added.

## 2. Domain model and stable reference contract

### 2.1 Core entities

| Entity | Key fields | Invariants |
|---|---|---|
| Edition | editionId, languageTag, title, canonId, publisher, sourceUri, version, licenceId, attribution, contentHash, status | editionId is stable; each installed version has immutable provenance and integrity metadata |
| Book | bookId, canonId, canonicalOrder, nameKey, testament, chapterCount | Book identity is canonical within a canon; display names are localized metadata |
| Chapter | chapterId, bookId, chapterNumber | Unique within book |
| Verse | verseId, chapterId, verseNumber, segmentKey? | Verse identity follows canonical versification metadata; segment support must be explicit |
| EditionText | editionId, verseId, text, sourceSequence | Text belongs to one exact edition/version; source text is not silently rewritten |
| Resource | resourceId, resourceType, languageTag, version, licence, attribution, source, integrity, installState | Rights and integrity must pass before active installation |
| ResourceDependency | resourceId, dependsOnResourceId, versionConstraint | Dependency graph must be acyclic and resolvable |
| ReadingPosition | editionId, bookId, chapterNumber, verseNumber?, updatedAt | Last position is local and recoverable |
| Annotation | annotationId, kind, body, createdAt, updatedAt, archiveState | User-owned data is not deleted when an edition is removed |
| AnnotationReference | annotationId, canonicalReferenceId, editionId? | A record may link to multiple stable references; edition context is optional |
| Collection / CollectionItem | collectionId, title, itemType, targetId, sortOrder | User-defined ordering is stable |
| ReadingPlan / PlanDay / PlanAssignment | planId, date, passageReference, status | Schedule and completion history are separate |
| SearchIndexManifest | editionId/resourceId, contentVersion, indexVersion, languageStrategy, buildState | Index is bound to exact source version and rebuildable |

### 2.2 Stable passage reference

Use a canonical, edition-independent reference tuple:

`canonId + bookId + chapterNumber + verseNumber + optionalSegment`

Represent this as a typed value object (not a display string). Display strings such as “John 3:16” are formatted from localized metadata and are never primary keys.

Rules:
- Notes, highlights, bookmarks, collections and plans reference the canonical passage identity.
- Edition-specific text lookup resolves canonical reference → edition versification mapping → edition text.
- Editions with different versification require an explicit mapping table with source/provenance and mapping confidence/status.
- Do not assume all translations have one-to-one verse mapping.
- If mapping is unavailable or ambiguous, preserve the canonical reference and show “not available in this edition”; never attach an annotation to a guessed verse.
- Store the edition/version used when the user created a text-dependent note where context matters, but do not make that edition the sole identity of the note.

### 2.3 Identity and deletion

- IDs are stable opaque identifiers (UUID or deterministic canonical identifiers); do not use localized names as IDs.
- Removing an edition deletes only its installed text/index/package data after dependency confirmation.
- User notes and annotations remain; links to unavailable editions resolve to the canonical reference and show edition unavailable.
- Canon/canon-order metadata is versioned and provenance tracked.

## 3. Room / SQLite schema design

Recommended Room tables:
- `edition`, `canon`, `book`, `chapter`, `verse`, `edition_verse_text`
- `versification_mapping`
- `resource_manifest`, `resource_dependency`, `resource_installation`
- `reading_position`, `reading_history`, `bookmark`, `highlight`
- `annotation`, `annotation_reference`, `tag`, `annotation_tag`
- `collection`, `collection_item`
- `reading_plan`, `reading_plan_day`, `reading_completion`, `reminder_preference`
- `journal_entry`, `sermon`, `notebook`, `notebook_item`
- `saved_search`, `search_history`, `search_index_manifest`
- `app_preference`, `backup_manifest`

Schema rules:
- Primary keys are stable opaque IDs; enforce uniqueness with composite indexes where needed.
- Add foreign keys and indexes for high-cardinality lookups, but avoid cascading deletes from edition/resource rows into personal annotations.
- Use explicit UTC instants for audit timestamps and local-date values for reading-plan calendar days; keep the user's chosen zone where reminder scheduling requires it.
- Use transactions for multi-row user actions and resource state transitions.
- Store large immutable resource payloads in app-private files, not SQLite BLOB columns; database stores manifest, relative storage key, hash, size and state.
- Never store secrets, signing keys or credentials in the database.
- Avoid destructive schema changes; migrations must preserve user records and have upgrade tests.
- Use Room migrations, not destructive fallback, for production schema upgrades.
- Development fallback may clear only synthetic test databases.

### Migration policy
1. Increment schema version for every schema change.
2. Provide forward migration from every supported released schema.
3. Test migration with representative synthetic fixtures and data-count/reference invariants.
4. Record migration failures without silently dropping user data.
5. Backup before any future destructive transformation; restore is user-confirmed.

## 4. Edition/resource manifest and import lifecycle

### 4.1 Manifest contract

A package manifest should include:
- manifest schema version and package type;
- resource ID, version and language tag;
- title, publisher/author, source URL/name and source revision;
- licence identifier, licence URL/text reference and required attribution;
- content format, canon/versification identifier where relevant;
- payload relative paths, byte sizes and SHA-256 digests;
- declared dependencies and minimum app/resource compatibility;
- optional signature metadata and signing-key identity.

A hash detects accidental modification; it does not prove publisher authenticity. Signature verification and trusted publisher/key policy are separate controls.

### 4.2 Fail-closed state machine

```text
DISCOVERED → PREFLIGHTED → RIGHTS_CLEARED → VERIFIED
    → STAGED → VALIDATED → ACTIVE
Any failure → REJECTED or ROLLED_BACK
ACTIVE → REMOVAL_PENDING → REMOVED
```

- The app must not transition a resource to ACTIVE unless rights, schema compatibility, integrity and required dependencies pass.
- Installation stages files under a temporary directory, verifies every declared payload, validates schema, then atomically switches the active manifest pointer in a database transaction.
- On process death or low storage, startup recovery removes abandoned staging data or resumes only from a verified checkpoint.
- Removal first checks dependencies and user-data references; personal annotations are preserved.
- An update is side-by-side staged; old active version remains usable until new version is fully validated.
- Resource import is disabled as a product operation until the Owner separately authorizes content intake and the exact package has passed rights review.

## 5. Offline search and indexing

### 5.1 Recommended approach

Use SQLite FTS5 through a tested Android-compatible integration if compatibility and packaging tests pass. Keep the search abstraction independent of FTS5 so the engine can be replaced if Android/device compatibility or language behavior is inadequate.

Do not assume the default SQLite tokenizer correctly handles Maale or Amharic. Build language adapters and evaluate:
- Unicode normalization strategy for matching only;
- punctuation and word-boundary rules;
- Ethiopic syllabic text handling;
- Maale-specific orthographic/tokenization behavior;
- prefix, exact phrase, all-term and any-term semantics;
- diacritics/combining marks and mixed-script queries.

Displayed scripture text must remain byte/code-point faithful to the approved source representation. A normalized search key may be stored separately with a documented transform version. Never normalize and overwrite the source text.

### 5.2 Index lifecycle

- Index key includes edition/resource ID, exact content version, tokenizer strategy version and index schema version.
- Build into a temporary index; activate atomically only after completeness checks.
- Partial indexes are marked PARTIAL and must not be presented as complete.
- Rebuild is deterministic and idempotent.
- Removing an edition removes its index but not user search history unless user chooses to clear it.
- Search results carry stable reference, edition, snippet offsets and index provenance.
- Synthetic benchmark fixtures only until content intake is separately authorized.

### 5.3 Search acceptance/benchmark contract

Measure cold index build time, warm query latency, memory, index storage overhead and result correctness on representative low/mid-range Android devices. Establish numerical budgets after a reproducible synthetic corpus and device baseline exist; do not invent performance claims now.

## 6. Personal data, privacy and backup/restore

### 6.1 Data classes

- Public app metadata: app version, generic catalogue metadata.
- Licensed immutable resources: installed edition/reference payloads and manifests.
- Personal records: notes, journals, sermons, bookmarks, highlights, collections and plan progress.
- Operational state: reading position, preferences, search history and index manifests.

No analytics, ads, telemetry, account service or cloud sync in this release.

### 6.2 Threat model

Protect against:
- accidental app data loss or uninstall;
- corrupted/incomplete backup files;
- malicious or malformed imported packages;
- path traversal, decompression bombs and oversized payloads;
- resource substitution/tampering;
- unintended export of private notes;
- stale or incompatible backup schema;
- device sharing and shoulder-surfing.

Mitigations:
- app-private storage, strict path canonicalization and package size/file-count limits;
- hash verification, safe archive extraction and allow-listed formats;
- explicit user action and preview for export/restore;
- no automatic sharing or network upload;
- integrity-checked backup manifest and per-file digest;
- restore into staging, validate, then transactionally activate;
- document the limits of protection if device is rooted, compromised or unlocked.

### 6.3 Encryption decision

**Provisional recommendation:** use Android Keystore-backed encryption for user-created backup archives and sensitive exported personal records. Do not add an app lock or encrypt the whole Room database until a threat-model and usability assessment confirms the requirement and key-recovery behavior. If database encryption is adopted, it must not strand user data after key loss or app reinstall; recovery and migration must be specified first.

### 6.4 Backup format

Versioned archive with:
- manifest (format version, app version, createdAt, record counts, included categories);
- serialized personal-data records and schema version;
- attachment files if supported;
- per-entry size and SHA-256;
- optional encrypted payload envelope and key-wrapping metadata.

Do not include licensed scripture/resource payloads by default. The backup should contain user records and stable references, not redistribute source content. Restore resolves references against installed resources and reports missing targets.

### 6.5 Restore algorithm

1. User selects file through Android Storage Access Framework.
2. Preflight file type, archive limits, manifest and integrity.
3. Show backup version, date, categories, counts and compatibility.
4. Validate into isolated staging database/files.
5. Preview merge/replace policy and conflict counts.
6. User confirms; commit transactionally.
7. Verify record counts and reference resolution; report any unresolved references.
8. On failure, keep original active data unchanged.

Default should be non-destructive merge with deterministic conflict rules; full replace requires explicit separate confirmation.

## 7. Compose navigation and state ownership

- Single-activity Compose application with typed navigation routes.
- Each destination has a screen-level ViewModel/state holder; composables remain stateless where practical.
- ViewModels call domain use cases; repository flows drive observable state.
- Persist durable user data in Room; use SavedStateHandle for small transient navigation/query state only.
- Never persist full scripture text or large result sets in saved instance state.
- Global navigation state: current tab, selected edition and current canonical reference.
- Search state: query, filters, sort and scroll anchor.
- Reader state: selected edition, canonical reference, scroll/verse anchor and display preferences.
- Parallel reader state: edition pair/list and shared canonical reference.
- Modal action sheets must return focus and preserve reader position.
- Deep links are internal typed routes; no remote link resolution is required.
- Navigation restoration is tested across rotation and process recreation.

## 8. Accessibility, localization and typography architecture

- Resource strings for UI; scripture text is stored and displayed as immutable source content.
- UI locale is independent of edition language.
- Use locale-aware formatting for dates/numbers and metadata labels.
- Text shaping and line breaking must be tested on actual Maale and Amharic samples after lawful content intake; synthetic script fixtures may test rendering mechanics but cannot validate linguistic correctness.
- Support dynamic font scaling, semantic roles/state, screen-reader descriptions, focus order, contrast and reduced motion.
- RTL direction is determined by language/script metadata and verified; do not assume all supported languages are RTL.
- Specialist Maale linguistic review remains a deferred residual risk.

## 9. Module boundaries (proposed)

- `core:model`: stable IDs, canonical references, result/error types.
- `core:database`: Room entities, DAOs and migrations.
- `core:datastore`: preferences.
- `core:resource`: manifest, rights/integrity gates and local package lifecycle.
- `core:search`: query model, tokenizer adapters and index abstraction.
- `core:designsystem`: typography, themes, components and semantics.
- `feature:home`, `feature:bible`, `feature:reader`, `feature:study`, `feature:library`, `feature:personal`, `feature:plans`, `feature:atlas`, `feature:settings`, `feature:backup`.
- `core:testing`: synthetic fixtures, fake repositories and test utilities.

Do not split into Gradle modules solely for appearance. Start with packages if build complexity outweighs isolation benefits; preserve the dependency direction and stable interfaces.

## 10. P2 verification contracts

Before P2 can close:
- Domain reference invariants have unit tests and documented edge cases.
- ERD/schema has key, index, FK, deletion and migration review.
- Resource state machine has transition table and failure-injection tests.
- Search engine choice has Android compatibility evidence and language evaluation plan.
- Backup threat model and restore algorithm have security review and test vectors.
- Route graph maps every screen ID to destination and state owner.
- Requirements-to-architecture traceability is complete.
- No content assets are used; no A4 feature implementation is claimed.

## 11. Decisions still requiring evidence or explicit disposition

1. Exact minimum Android API/device matrix, to be reconciled with A3 min API 26 / target SDK 36.
2. SQLite FTS5 availability/packaging and tokenizer feasibility on supported devices.
3. Whether SQLCipher or equivalent full database encryption is warranted.
4. Exact backup archive library/format and compatibility policy.
5. Stable versification mapping data model for editions with non-identical verse segmentation.
6. Whether separate Gradle modules are justified by actual build/test needs.
7. Numerical performance budgets after synthetic benchmark and target-device selection.

No decision above should be silently treated as an Owner-approved product or security policy where tradeoffs materially affect user data or compatibility.
