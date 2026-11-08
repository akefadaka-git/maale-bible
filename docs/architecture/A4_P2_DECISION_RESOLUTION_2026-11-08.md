# A4 P2 — Decision Resolution and Technical Closure Plan

**Date:** 2026-11-08  
**Branch:** feat/a4-offline-study-suite  
**Baseline:** P0/P1 accepted; P2 architecture prepared  
**Content gate:** content_intake_authorized=false  
**Status:** Architecture decisions dispositioned; runtime verification remains open. This record does not claim P2 VERIFIED-CLOSED.

## 1. Decision register

| ID | Question | Decision | Rationale / constraint | Verification still required |
|---|---|---|---|---|
| P2-D01 | Android API/device matrix | Retain **minSdk 26 (Android 8.0)**; retain targetSdk 36 from A3, subject to current policy at release. Test API 26, 28, 30, 33, 35 and 36; representative low-memory 2 GB and mid-range 4 GB physical devices, plus emulator coverage. | Avoid raising the minimum without demonstrated feature need; API 26 is the A3 baseline. Physical-device availability is a release risk, not compatibility evidence. | Build/instrumentation matrix, API-26 SQLite/runtime checks, low-memory and storage-pressure tests. Reconfirm target SDK policy before publication. |
| P2-D02 | FTS5 availability / packaging | Adopt **Room + AndroidX SQLite bundled driver + SQLite FTS5** as the preferred path. Do not depend on OEM-provided SQLite FTS5. Keep a SearchEngine interface and tested fallback only if bundled FTS5 fails a supported-device gate. | AndroidX documents that FTS5 availability depends on the driver and that the bundled SQLite driver supports it. Room's FTS5 annotation belongs to Room 3; do not assume Room 2 exposes the same annotation. | Pin compatible Room/SQLite versions against A3 AGP/Kotlin; build a synthetic instrumentation probe on API 26/28/30/33/35/36; verify create/insert/query/rank/rebuild/upgrade. |
| P2-D03 | Maale / Amharic tokenizer | FTS5 is the indexing engine, **not** the linguistic tokenizer decision. Use versioned language adapters: Unicode-aware baseline tokenizer plus Maale and Amharic adapters with separately versioned normalization/search-key transforms. Preserve source text unchanged; do not claim linguistic correctness until reviewed against authorized language samples by a qualified specialist. | General Unicode tokenization does not establish Maale morphology or orthographic behavior. Avoid a JNI custom tokenizer until test corpus and language specialist define requirements. | Synthetic Unicode/Ethiopic tests now; after separate content authorization, language evaluation and specialist sign-off. Linguistic coverage remains PARTIAL until then. |
| P2-D04 | Full database encryption | **Do not encrypt the entire Room database in v1.** Keep personal data in app-private internal storage, rely on Android application sandbox and device encryption, minimize exported data, and encrypt backup/export archives. Do not promise app lock. | Full-database encryption adds native dependency, key lifecycle, migration, performance and recovery risk. A device-bound key can make recovery impossible after reinstall/device loss. Present threat model does not establish sufficient need. | Security review of local-data threat model; verify no sensitive data in logs, external storage or exported diagnostics. Revisit only if documented threat or user requirement changes. |
| P2-D05 | Backup archive / compatibility | Define **Maale Bible Backup v1** as a versioned ZIP container with UTF-8 canonical JSON manifest, NDJSON records by category, attachment files, per-entry SHA-256 and strict size/count/path limits. Encrypt the complete archive using AES-256-GCM. Derive a portable key from a user-supplied backup passphrase using a vetted password KDF with per-backup random salt and recorded parameters; never rely solely on a device-bound Keystore key. Restore is staged, previewed and non-destructive merge by default; replace requires separate confirmation. | ZIP is broadly inspectable and streamable; manifest/schema versioning supports compatibility. A portable passphrase avoids making backups decryptable only on the originating device. Scripture/resource payloads are excluded by default. | Select and pin maintained archive/KDF implementation; security review KDF parameters and nonce handling; fuzz malformed archives; test wrong password, tampering, truncation, path traversal, oversized entries, schema compatibility, merge conflict and rollback. |
| P2-D06 | Stable versification mapping | Keep canonical passage identity independent of edition. Add a **many-to-many mapping model**: canonical passage range ↔ edition passage/segment range, with mapping source, version, status (EXACT, SPLIT, MERGED, PARTIAL, UNMAPPED), and review provenance. Never silently select a nearest verse. | Edition segmentation is not guaranteed one-to-one. Annotation references remain canonical; text lookup resolves through explicit mapping. Ambiguous/unmapped references display unavailable/ambiguous state. | Schema and DAO tests for exact, split, merged, partial and unmapped cases; round-trip invariants; synthetic mapping fixtures. Real mapping data waits for separately authorized sources. |
| P2-D07 | Gradle modules | Begin with **package-level boundaries in the existing app module**. Add small core:model and core:testing modules only when extraction reduces dependency cycles or enables independent tests. Extract database/search/resource and feature modules incrementally when contracts stabilize. | Avoid premature module proliferation, build configuration churn and AGP/Kotlin plugin complexity. Enforce dependency direction with package/API tests and Gradle dependency checks. | Record module dependency graph; verify incremental build and test times before each extraction. |
| P2-D08 | Performance budgets | Adopt initial measurable SLOs on reference low-end device: cold launch to first usable reader frame ≤2.5 s; book/chapter navigation p95 ≤200 ms; warm local search p95 ≤300 ms on a 100,000-record synthetic corpus; reader scroll jank <5% of frames over 16.7 ms at 60 Hz; no main-thread disk I/O; no ANR in scripted journeys. Measure memory and index size before setting hard caps. | Budgets are engineering targets, not observed results. Baseline Profiles and Startup Profiles are required for release-critical journeys after baseline measurements. | Publish benchmark environment, corpus generator/seed, device/build details, raw results and repeated-run statistics. Recalibrate only with documented device evidence. |

## 2. Canonical data model refinements

### 2.1 Stable reference and edition mapping
- Canonical identity: canonId + bookId + chapterNumber + verseNumber + optional canonicalSegmentId.
- Edition mapping is range-based and many-to-many, not a single nullable verse number.
- Mapping rows carry editionId, edition version, source reference range, canonical range, mapping status, provenance, reviewer/status and mapping schema version.
- A canonical reference may resolve to zero, one or multiple edition segments. Multiple segments render as a range/group; zero renders as unavailable. No heuristic auto-attachment of notes.
- Annotation links store canonical reference IDs. Edition/version context may be retained as provenance, not primary identity.

### 2.2 Search index
- Search index is disposable/rebuildable and never the source of truth.
- Store original display text separately from versioned normalized search keys.
- Index manifest binds edition/resource ID, exact content version, tokenizer adapter version, normalization transform version, SQLite/FTS schema version and completeness state.
- Search reports partial/unavailable index states honestly.
- Search adapters are tested independently of FTS5 using deterministic synthetic fixtures.

### 2.3 Backup envelope
- Outer encrypted envelope records format version, KDF identifier/parameters, salt, nonce, authentication tag and ciphertext.
- Inner ZIP manifest records schema version, app version, creation time, record categories/counts, per-entry lengths and hashes.
- Reject duplicate paths, absolute paths, traversal segments, symlinks, excessive entry count, excessive expansion ratio and size overflow.
- Never auto-restore on startup. User explicitly selects, previews and confirms.
- Backup excludes installed licensed scripture, maps, audio and third-party reference payloads by default.

## 3. Screen and route contract

The 42-screen inventory is authoritative. Before P2 closure, each screen must have a stable screen ID and typed route; one owning feature/state holder; input arguments and restoration behavior; repository/use-case dependencies; loading, empty, error, offline and permission states; deep-link/back-stack behavior; accessibility semantics and an automated navigation test. Reader, parallel reader, search, resource detail, notes, sermon editor, journal, plans, atlas, backup/restore and settings are first-class journeys. A composable placeholder is not a completed screen.

## 4. Security and operational guardrails
- No account, remote sync, analytics, ads, telemetry or network dependency in this release.
- User-created records remain app-private by default.
- Backup/export requires explicit user action and destination confirmation.
- No automatic sharing or export of private notes.
- Resource package handling remains disabled until Owner content-intake authorization; hashes/signatures do not replace rights clearance.
- Resource integrity, publisher authenticity, licence clearance and content correctness are separate gates.
- No content source, text, audio, map, scan, font or third-party reference payload is acquired or inspected under this record.

## 5. P2 verification gates

| Gate | Required evidence | Current status |
|---|---|---|
| P2-V1 Architecture traceability | Every requirement maps to architecture component, screen/route and test ID | NOT RUN |
| P2-V2 Data model | ERD, constraints, mapping cardinality, deletion invariants and synthetic tests | DESIGN RESOLVED; NOT RUN |
| P2-V3 FTS5 packaging | Version-pinned Android instrumentation probe on API matrix | DECISION RESOLVED; NOT RUN |
| P2-V4 Language adapters | Synthetic Unicode/Ethiopic tests; later specialist/content evaluation | PARTIAL |
| P2-V5 Backup security | Threat review, fuzzing, crypto review and restore failure injection | DESIGN RESOLVED; NOT RUN |
| P2-V6 Navigation | All 42 screen IDs mapped to typed route/state contracts and tested | NOT RUN |
| P2-V7 Performance | Reproducible benchmark report against SLOs | TARGETS SET; NOT RUN |
| P2-V8 Content boundary | Confirm no protected assets acquired/inspected/ingested/committed | PASS by scope record; keep gate closed |

## 6. Closure rule

P2 can be marked **VERIFIED-CLOSED** only after P2-V1 through P2-V8 have evidence, with linguistic validation explicitly recorded as a deferred residual risk if authorized source samples remain unavailable. Architecture decisions are dispositioned; implementation/runtime evidence is not implied.

**No P3 feature implementation, content intake, merge, signing, deployment or publication is authorized by this decision record.**
