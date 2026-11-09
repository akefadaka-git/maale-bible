# Maale Bible (ጌኤዦ ማፃኣፖ) — Authoritative Implementation Plan

**Plan version:** 2.1  
**Date:** 2026-11-08  
**Status:** CURRENT GOVERNING PLAN — replaces earlier planning documents as the active roadmap  
**Repository:** akefadaka-git/maale-bible  
**Implementation branch:** feat/a4-offline-study-suite  
**Current A3 verified source:** fcb1d04d7a227bfda320cd18a06b12fbd37e4b6d  
**Current product direction:** comprehensive, advanced, offline-first Android Bible and study workspace  
**Content authorization:** content_intake_authorized=true — Owner authorization recorded 2026-11-08; per-source rights gates remain mandatory  
**Governance:** Owner-governed, stage-gated; no merge, content intake, signing, deployment or publication without the required separate approval.

> This document is the single authoritative implementation roadmap. Earlier plans and specifications are retained under docs/archive/superseded-plans/ for historical reference. Where an archived plan conflicts with this document, this plan governs future work; historical approvals and evidence remain valid unless explicitly superseded.

## 1. Executive direction

Build Maale Bible as a dependable, polished and advanced offline-first Bible reading and study environment, with Maale-first language priorities and carefully governed support for Amharic and English. The application should combine the strongest useful capabilities found across leading Bible reader, study, reference, productivity, mapping and audio products without copying proprietary content or claiming unsupported scholarly functionality.

The competitive position is not “the largest Bible catalogue” or “the biggest social network.” It is:

1. Best-in-class Maale-first reading and study experience.
2. Core reading, search, study and personal records that remain functional in airplane mode.
3. Edition-independent notes and highlights that survive translation changes.
4. A coherent passage-aware workspace joining reader, references, notes, maps and study tools.
5. Transparent source provenance, licence information and user ownership of local data.
6. High performance and accessibility on modest Android devices and constrained connectivity.

Success is measured through task completion, correctness, responsiveness, offline reliability, language quality, accessibility, data integrity and user trust—not feature count alone.

## 2. Product and release boundaries

### 2.1 Current offline release

Included:
- Bible edition catalogue and reader; book/chapter/verse navigation and reading history.
- Multiple separately approved editions and parallel reading, with explicit versification alignment.
- Bookmarks, highlights, verse-linked notes and personal collections.
- Local full-text search, concordance and cross-reference framework.
- Topical index, dictionaries, encyclopedias and commentaries only when exact resources are authorized and rights-cleared.
- Passage-aware Study Workspace and local Resource Guide.
- Personal journal, diary, study notebooks, sermon preparation and reading plans.
- Offline atlas, verified biblical places and scripture-linked maps, subject to resource approval.
- Optional offline audio, only for separately authorized audio packages.
- Verse cards, share sheet, document export, local backup/restore and storage management.
- Maale-first UX; Amharic and English interface/edition support only to the extent reviewed and approved.
- Accessibility, typography, privacy, help and detailed settings.

### 2.2 Explicitly deferred to a separately governed server release

- Account registration, authentication and cloud profiles.
- Cloud synchronization and cross-device shared state.
- Online discussion, forums, comments, groups and moderation.
- Collaborative notes and shared reading plans.
- News subscriptions, remote announcements and server content administration.
- Server push notifications and remote services.

The offline release may define clean integration interfaces and durable local models, but must not show server-dependent features as working.

### 2.3 Content and rights boundary — binding

- content_intake_authorized=true by explicit Owner authorization dated 2026-11-08; see docs/governance/OWNER_CONTENT_INTAKE_AUTHORIZATION_2026-11-08.md.
- Intake work may cover Maale, Amharic (am), English (en), and other necessary Bible/study resources within the approved offline product scope.
- This is not a blanket licence: every edition/resource must independently pass provenance, rights/permission, attribution, integrity, structure, quality and language-review gates before integration or distribution.
- The primary Maale edition remains the 2008 Full Bible MDYTWF / mdyeth. Do not substitute the distinct 2007 MDYBSE Maale New Testament.
- Existing A2 verification remains valid for the exact archive and evidence already examined; recheck archive identity and hashes before new processing.
- Commentaries, dictionaries, concordances, maps, atlas data, images, audio, fonts and derived indexes require source-specific rights and quality review.
- Public-repository commits of raw scripture or protected assets are permitted only where the exact licence allows public redistribution and the repository's distribution model has been approved; otherwise use controlled content packaging outside the public source tree.
- Never fabricate scripture, commentary, maps, historical facts, morphology or citations.
- Maale language/Ethiopic-script specialist review remains a deferred residual risk; do not claim linguistic sign-off.

## 3. Competitive benchmark translated into product priorities

| Competitor benchmark | Strength to learn from | Maale Bible response |
|---|---|---|
| YouVersion | Daily engagement, plans, audio, language reach, sharing | Excellent local reading plans, habit tools and share/export; differentiate on deep Maale-first offline study |
| Logos | Research workflows, notebooks, sermon tools, Resource Guide, atlas | Build a coherent passage workspace and local resource guide; do not promise desktop-scale library parity |
| Olive Tree | Offline resource library, split-screen study, linked resources | Treat offline reliability, parallel panes and resource management as core architecture |
| Accordance | Original-language search, interlinear and specialist research | Provide extensible word-study data structures; only expose scholarly tools when licensed and verified |
| Blue Letter Bible | Concordance, lexicons and reference lookup | Build traceable local reference tools and prioritize verified Ethiopian-language resources |
| Bible Gateway | Translation discovery and comparison | Implement correct multi-edition comparison with explicit mapping and edition provenance |
| Bible.is / Dwell | Listening and audio experience | Plan a modular audio subsystem; distribute only rights-cleared recordings |

The app must not claim universal superiority. The intended competitive advantage is measurable superiority for target users in Maale-language usefulness, offline task completion, local privacy, cross-edition annotation integrity and performance on constrained devices.

## 4. Architecture baseline and binding technical decisions

The P2 decision record is authoritative for technical detail. The following choices govern implementation unless changed through an explicit architecture decision record (ADR):

1. Android minimum API 26 (Android 8.0); retain target SDK 36 from A3 for current engineering work and recheck current platform/store policy before release.
2. Kotlin, Jetpack Compose and Material 3; retain the verified A3 toolchain unless a tested compatibility change is necessary.
3. Room/local SQLite with versioned, tested migrations.
4. Preferred search path: Room + AndroidX SQLite bundled driver + SQLite FTS5; do not depend on OEM SQLite FTS5. Keep a SearchEngine abstraction and evaluate fallback only if a supported-device gate fails.
5. Language adapters are versioned separately from FTS. Preserve display/source text; search normalization must not mutate it. Maale and Amharic correctness requires qualified language review against separately authorized samples.
6. Do not encrypt the entire Room database in v1. Keep personal data app-private; encrypt portable backup/export archives and review local-data threat controls.
7. Backup v1: versioned ZIP, canonical JSON manifest, NDJSON record sets, per-entry SHA-256, strict path/size/count limits, AES-256-GCM authenticated encryption and a vetted password KDF with per-backup salt. Restore is staged, previewed and merge-first; replace requires explicit confirmation. Implementation library and parameters require security review.
8. Canonical passage identity is edition-independent. Edition alignment is explicit many-to-many mapping with EXACT, SPLIT, MERGED, PARTIAL and UNMAPPED states. Never silently guess a verse mapping or orphan annotations.
9. Start with feature-oriented packages in the existing app module. Extract Gradle modules only when dependency boundaries and build/test benefits are demonstrated.
10. Resource installation is staged and atomic. Rights clearance, publisher authenticity, package integrity and content correctness are independent checks.
11. No account, analytics, ads, telemetry, forced network dependency or cloud sync in the offline release.
12. Initial performance SLOs: cold launch to usable reader ≤2.5 s; navigation p95 ≤200 ms; warm search p95 ≤300 ms on a 100,000-record synthetic corpus; scroll jank <5% frames over 16.7 ms at 60 Hz; no main-thread disk I/O; no ANR in scripted journeys. These are targets until benchmarked.

## 5. Delivery roadmap and stage gates

Durations are indicative working estimates, not committed deadlines. They are reforecast after P2 technical verification, resource-rights decisions and device testing. Phases may overlap only where dependencies permit.

| Phase | Indicative duration | Work and deliverables | Exit evidence |
|---|---:|---|---|
| P0 — Governance and preparation | Accepted | Scope, risk register, branch strategy, governance and issue framework | Owner-accepted scope and constraints |
| P1 — Requirements and product specification | Accepted | Feature acceptance matrix, 42-screen inventory, journeys and issue breakdown | Requirements traceability and accepted baseline |
| P2 — Architecture and technical verification | 1–2 weeks | Close technical decisions with runtime evidence; schema/data model, FTS5 probe, route mapping, backup threat/failure tests and performance baselines | P2-V1…V8 evidence; closure report against exact SHA |
| P3 — Offline platform foundation | 1–2 weeks | Room schema/migrations, repository contracts, resource manifest/registry, settings, local data lifecycle, search abstraction, backup primitives, synthetic fixtures | Persistence, migration, resource-integrity, offline and recovery tests |
| P4 — Bible library and reader | 2–3 weeks | Functional catalogue, reader, navigation, typography, history, annotations, parallel editions and passage mapping | End-to-end reader acceptance; airplane-mode and annotation integrity |
| P5 — Search and reference engine | 2–3 weeks | FTS5 local search, language adapters, concordance framework, cross-references, topical index, Resource Guide and authorized reference resources | Search correctness, provenance, performance and offline tests |
| P6 — Personal study workspace | 1–2 weeks | Journal, diary, notes, notebooks, sermon builder, collections, reading plans, progress and local reminders | Persistence, recovery, usability, reminders and export tests |
| P7 — Atlas, audio and output tools | 1–2 weeks | Offline atlas/maps, place records, audio subsystem, verse cards, sharing, document export and backup/restore | Resource rights, offline map/audio, export and restore tests |
| P8 — Integration and release-quality hardening | 2 weeks | Full regression, accessibility, privacy/security, performance, low-memory/storage, process death, migrations and device matrix | No unresolved release blockers; full evidence and residual-risk acceptance |
| P9 — Release candidate preparation | 1 week | Versioning, branding, notices, privacy/store disclosures, reproducible release build and rollback plan | Release candidate accepted; separate signing approval |
| P10 — Production deployment | 3–7 days | Sign and verify approved artifact, publish through approved channel, staged rollout and smoke tests | Separate explicit Owner deployment approval and release evidence |
| P11 — Stabilization | First 30 days | Defect triage, crash/ANR review from approved sources, offline reliability and controlled patches | 30-day stability report |
| P12 — Improvement and next release | Days 31–90 and ongoing | Usability, language, accessibility, performance and resource improvements; server-release discovery | 60/90-day reviews and separately accepted next-release plan |

### Dependency order

- P2 verification precedes P3 implementation.
- Local persistence and canonical identity precede annotations and edition mapping.
- Resource registry and rights/provenance lifecycle precede third-party resource distribution.
- Reader and passage references precede passage-aware study tools.
- Search engine precedes concordance, topical discovery and Resource Guide.
- Backup schema and local record model precede final backup/restore acceptance.
- Atlas/audio packages remain separate resource-rights gates.
- P8 acceptance precedes P9 release candidate.
- P9 approval precedes signing; signing approval precedes P10 deployment.
- P11/P12 are not substitutes for pre-release quality gates.

## 6. P2 verification work package — immediate priority

Architecture decisions are dispositioned, but P2 is not yet VERIFIED-CLOSED. Complete:

| Gate | Required task | Evidence / acceptance |
|---|---|---|
| P2-V1 Traceability | Map every requirement to architecture component, screen/route and test ID | No unowned or untestable requirement |
| P2-V2 Data model | ERD/schema constraints, migration design, canonical references, split/merged mapping fixtures and deletion invariants | Synthetic DAO/property tests pass |
| P2-V3 FTS5 | Pin compatible Room/AndroidX SQLite dependencies and instrument bundled FTS5 on API 26/28/30/33/35/36 | Create, insert, query, rank, rebuild and upgrade pass |
| P2-V4 Language | Synthetic Unicode/Ethiopic normalization, mixed-script and round-trip tests | Source text immutable; language limitations explicit; specialist validation deferred |
| P2-V5 Backup | Review crypto/KDF library and parameters; test tampering, wrong passphrase, truncation, path traversal, oversized archive, schema mismatch, merge conflict and rollback | Negative/failure-path tests pass; no silent overwrite |
| P2-V6 Navigation | Map all 42 screens to typed routes, state owners, arguments, restoration and accessibility semantics | Route inventory traceable; navigation tests defined/run |
| P2-V7 Performance | Establish repeatable synthetic corpus and benchmark harness on API/device matrix | Publish raw runs and percentile results against SLOs |
| P2-V8 Content boundary | Review changed files, dependencies and fixtures for protected content | No unauthorized content intake or protected asset in repository |

P2 exit: all gates have evidence and a signed-off report. Linguistic review may remain a named residual risk, not falsely reported as complete. Do not close P2 based only on a written decision register.

## 7. Feature acceptance standards

Every implemented feature must satisfy all applicable criteria:

- Functional: real data path and user action; no placeholder presented as complete.
- Persistence: data survives process death, restart and supported upgrade/migration.
- Offline: release-critical behavior works in airplane mode after local setup.
- Correctness: references, edition alignment, search results and resource relationships are deterministic and traceable.
- Recovery: interrupted operations do not corrupt user data; backup restore is staged and recoverable.
- Privacy: no undisclosed network traffic, analytics, advertising or remote dependency; personal records remain app-private by default.
- Accessibility: screen reader semantics, scalable text, contrast, touch targets and keyboard/focus behavior where relevant.
- Localization: Maale/Amharic/English UI behavior is tested separately from scripture edition availability; Ethiopic rendering is checked on real devices.
- Performance: measured on documented reference devices; no target is claimed passed without results.
- Content governance: exact source, edition, rights, licence, attribution, integrity and review status are recorded before distribution.

## 8. Competitive quality scorecard

Release readiness requires measurable evidence:

| Dimension | Target | Verification |
|---|---|---|
| Offline completeness | 100% release-critical journeys pass in airplane mode after setup | Automated airplane-mode E2E suite |
| Search | p95 ≤300 ms on 100k synthetic records; later repeat on authorized content | Reproducible benchmark |
| Reader | Navigation p95 ≤200 ms; scroll jank <5% frames >16.7 ms | Macrobenchmark / frame metrics |
| Annotation integrity | No orphaned notes on edition switch, migration, backup or restore | Property and migration tests |
| Resource trust | Every installed resource has source, rights, attribution, integrity and review status | Resource audit and negative tests |
| Accessibility | Core journeys pass WCAG 2.2 AA-informed mobile checks and screen-reader review | Accessibility audit |
| Data ownership | User can export and restore personal records without account/network | Restore drill and data inventory |
| Language quality | Maale/Amharic rendering and search reviewed by qualified specialists | Recorded language QA; no unsupported claims |
| Reliability | No data loss under process death, low storage or interrupted import/restore | Fault-injection tests |
| Usability | New and experienced users complete key tasks efficiently | Moderated usability study |

## 9. Branch, repository and governance controls

- Work on feat/a4-offline-study-suite; preserve A2/A3 history and unrelated work.
- Do not modify main or merge PRs without separate explicit approval.
- Content intake is authorized under the Owner's 2026-11-08 decision; source-specific rights and quality gates remain mandatory.
- No release signing, store submission, deployment or publication without separate explicit approval.
- Do not ask for payment, top-ups or deposits to enable CI. Use available remote/manual verification routes and report unavailable checks honestly.
- Keep public-repository contents free of scripture text, private keys, signing credentials and restricted third-party assets.
- Every phase report includes exact source SHA, commits, changed files, PR/branch, workflow run links, test commands/results, artifact hashes, issues, risks and next gate.
- Use status labels precisely: PASS, VERIFIED-CLOSED, PARTIAL, BLOCKED, NOT RUN, UNKNOWN, UNRESOLVED.
- A planned date passing does not close a phase. A build passing does not prove product readiness.
- Preserve completed baselines; reopen only for material authoritative contradiction or new evidence.

## 10. Project reporting and schedule policy

Each phase closure package must include:
1. Exact base and head SHAs.
2. Commit and PR disposition.
3. Files changed and scope check.
4. Workflow evidence, logs and artifact checksums where applicable.
5. Unit, integration, instrumentation, UI, security, accessibility and performance results.
6. Requirements-to-tests traceability.
7. Defect register and residual risks.
8. Phase status and overall readiness rating.
9. Explicit next stage and any required Owner decision.

Planning estimate: the previous 12–16 week estimate is retained only as a historical provisional estimate. It is not a current commitment. Reforecast after P2 verification, P3 implementation discovery, rights-cleared resource availability and device-matrix evidence. Content-rights and specialist-review delays may extend delivery; no quality or rights gate may be bypassed to preserve a date.

## 11. Current baseline and next action

- P0/P1: Owner accepted; planning issues #4 and #5 were closed as completed.
- P2 architecture: design and ADRs prepared; decision register recorded.
- P2 technical verification: NOT RUN / PARTIAL as recorded in the P2 decision document; P2 is not VERIFIED-CLOSED.
- P3 implementation: not yet started under this plan.
- A3: SHA-pinned remote debug build, unit-test and APK assembly evidence exists at workflow run https://github.com/akefadaka-git/maale-bible/actions/runs/42454704758; this was a debug APK, not a signed release.
- PR #3 remains open, draft and unmerged per the last verified project record; refresh live state before making a new status claim.
- Owner content-intake authorization is recorded; no source may be integrated until its own rights, provenance and quality gates pass.

**Immediate next action:** execute P2-V1 through P2-V8 using synthetic data and exact-HEAD evidence, then publish the P2 closure report. After P2 verification and the applicable stage approval, begin P3 in small, testable vertical slices.

## 12. Document precedence and historical records

This file is the sole current implementation roadmap. It supersedes earlier active planning documents, including:
- IMPLEMENTATION_AND_DEPLOYMENT_TIMELINE_2026-11-07.md
- A4_OFFLINE_BIBLE_PRODUCT_SCOPE.md as the active roadmap (its approved scope remains a reference)
- A4_REQUIREMENTS_AND_ACCEPTANCE_MATRIX.md
- A4_SCREEN_INVENTORY_AND_USER_JOURNEYS.md
- A4_P0_P1_IMPLEMENTATION_ISSUE_BREAKDOWN.md
- A4_P2_ARCHITECTURE_AND_DATA_DESIGN.md as the active roadmap (architecture decisions remain binding through the ADRs and P2 decision register)

Historical approval/completion records, ADRs, A2/A3 source-verification evidence and the global competitor benchmark are retained as reference and evidence. They are not deleted. The benchmark remains a supporting product strategy document; this implementation plan controls sequencing and gates.

**Authority:** If any archived planning estimate, feature sequence or status conflicts with this plan, this plan is current. Historical facts and approvals are not rewritten by supersession.
