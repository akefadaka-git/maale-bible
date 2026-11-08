# Maale Bible — Phased Implementation, Deployment and Post-Deployment Plan

**Plan revision:** 2026-11-08  
**Repository:** `akefadaka-git/maale-bible`  
**Active A4 branch:** `feat/a4-offline-study-suite`  
**A4 baseline:** `fcb1d04d7a227bfda320cd18a06b12fbd37e4b6d`  
**A4 scope record:** `docs/product/A4_OFFLINE_BIBLE_PRODUCT_SCOPE.md`  
**Plan status:** PROVISIONAL — phased estimates; no committed delivery date  
**Release target:** Comprehensive, advanced, offline-first Android Bible and study application  
**Server-side boundary:** Accounts, cloud sync, online community/forum, news subscriptions and remote administration are deferred to a later release.

---

## 1. Executive delivery strategy

Delivery is organized from preparation through architecture, implementation, content authorization, verification, release preparation, deployment and post-deployment support. Each phase has an explicit entry condition, deliverables, exit evidence and approval gate.

The A3 Android foundation has a successful SHA-pinned remote build. The latest A3 source baseline is `fcb1d04d7a227bfda320cd18a06b12fbd37e4b6d`; GitHub Actions run [#42454704758](https://github.com/akefadaka-git/maale-bible/actions/runs/42454704758) completed successfully, including unit tests and debug APK assembly. The generated artifact was a debug APK, not a signed production release. A3 PR #3 remains open, draft and unmerged.

A4 is the comprehensive offline product implementation. It is not complete merely because feature screens or placeholder navigation exist. Each feature must work, persist correctly, pass automated and manual verification, and operate without a network connection.

**Planning estimate:** approximately 12–16 weeks from authorization of the detailed A4 architecture and availability of all required rights-cleared content/resource packages. This is an estimate, not a promise. Content-rights delays, specialist review, device defects, and store requirements can extend the schedule. Work can proceed in parallel where dependencies allow, but no gate may be skipped to preserve a target date.

## 2. Phase roadmap

| Phase | Indicative duration | Purpose and principal deliverables | Exit gate |
|---|---:|---|---|
| P0 — Governance and project preparation | 3–5 working days | Confirm release boundary, A4 scope, acceptance criteria, risk register, issue breakdown, branch/PR strategy and verification matrix | Owner accepts baseline, exclusions, dependencies and stage gates |
| P1 — Product research and detailed specification | 1–2 weeks | Feature-level requirements, user journeys, screen inventory, offline workflows, accessibility, multilingual UX, content/resource inventory and traceability matrix | Every committed feature maps to a requirement and acceptance test |
| P2 — Architecture and technical design | 1–2 weeks | Domain model, Room/SQLite schema, migrations, edition/resource registry, import/export design, local search architecture, security/privacy model, navigation and design system | Architecture review; migration and data-boundary decisions documented |
| P3 — Offline platform foundation | 1–2 weeks | Persistence layer, repository interfaces, local settings, resource manager, backup foundation, error/recovery handling, test fixtures and dependency wiring | Persistence, migration, integrity and offline platform tests pass |
| P4 — Bible library and reader | 2–3 weeks | Edition library, functional reader, book/chapter/verse navigation, reading position, typography, themes, bookmarks, highlights, parallel reading and verse-linked notes | Reader acceptance passes against an authorized installed edition; airplane-mode test passes |
| P5 — Search and Bible study reference suite | 2–3 weeks | Offline full-text search, concordance, cross-references, topical index, dictionaries/encyclopedias/commentaries where licensed, people/places and reference navigation | Search/reference correctness and provenance tests pass using rights-cleared resources |
| P6 — Personal study and productivity suite | 1–2 weeks | Daily journal, diary, sermon notes, study notebooks, collections, reading plans, reminders, progress and local export | CRUD, persistence, recovery, reminder and export tests pass |
| P7 — Atlas, maps and media/export tools | 1–2 weeks | Offline atlas/map package support, place profiles, scripture links, verse-image composer, share sheet and document export | Offline map/resource tests and exact-text/attribution export tests pass |
| P8 — Integration, quality and release hardening | 2 weeks | Feature integration, regression, accessibility, security, privacy, performance, low-storage, process-death, migration, backup/restore and device matrix | No release-blocking defects; all required gates pass; known risks accepted explicitly |
| P9 — Release candidate and deployment preparation | 1 week | Versioning, app identity, icon/branding, signing plan, reproducible release build, licence notices, privacy disclosures, store listing, support/rollback plan and release notes | Release candidate reviewed; signing and publication separately approved |
| P10 — Production deployment and staged rollout | 3–7 days | Build/sign final artifact, verify checksums, publish through approved channel, staged rollout, smoke tests and rollout monitoring | Explicit Owner deployment approval; production evidence and rollback readiness recorded |
| P11 — Post-deployment stabilization | First 30 days | Monitor crash/ANR reports from permitted sources, support issues, offline failures, data-loss reports, device compatibility and critical defects; issue controlled patches | 30-day review completed; critical issues resolved or mitigated |
| P12 — Post-deployment improvement | Days 31–90 and ongoing | Prioritize user feedback, performance improvements, accessibility refinements, content/resource updates, compatibility maintenance and next-release server-side discovery | 60/90-day reviews; next-release backlog and roadmap accepted |

Durations are planning ranges and may overlap. The full product schedule must be reforecast after P1/P2 and again when content/resource rights and technical feasibility are confirmed.

## 3. Preparation phase — P0

### Activities
- Confirm the offline-only release boundary and the server-side features deferred to the next release.
- Confirm the A4 product scope in `docs/product/A4_OFFLINE_BIBLE_PRODUCT_SCOPE.md`.
- Break every feature into small implementation issues with dependencies and test criteria.
- Establish a risk/dependency register covering scripture source intake, translation rights, reference resources, maps, localization, device performance and release requirements.
- Confirm protected branch, pull-request and review approach; preserve A2/A3 history and unrelated changes.
- Define supported Android versions/devices and a representative low/mid-range reference-device test matrix.
- Set quality gates and required evidence before implementation begins.

### Deliverables
- Product requirements and feature traceability matrix.
- Prioritized work breakdown and dependency graph.
- Risk, assumption, issue and decision registers.
- Test strategy and device matrix.
- Updated delivery estimate.

### Exit gate
Owner-reviewed A4 baseline and scope. This plan itself does not authorize merge, content intake, signing or deployment.

## 4. Specification and architecture — P1/P2

### Product specification
For each feature, define user stories, offline behavior, empty/error states, data retention, accessibility, localization, permissions, acceptance criteria and required content assets.

### Architecture decisions
- Kotlin + Jetpack Compose + Material 3, retaining the existing Android foundation.
- Feature-oriented packages with presentation/domain/data boundaries.
- Room/SQLite local database with versioned migrations.
- Stable edition and scripture-reference identifiers.
- Local full-text search and rebuildable indexes.
- Local resource registry for Bible editions, dictionaries, maps and other installed datasets.
- Atomic resource installation with manifest, licence, attribution, hash and schema validation.
- User-controlled export and backup/restore with security assessment.
- Optional future server adapters, not required by core functionality.
- No analytics, advertising, cloud tracking or account requirement in this release.

### Exit gate
Architecture and schema reviewed; data lifecycle, import boundaries, rights metadata, security model and test contracts documented.

## 5. Implementation sequence — P3 through P7

Implementation proceeds in vertical, testable slices. Build working features, not empty screens.

1. Establish local persistence and migration framework.
2. Build the resource/edition registry and verified installation boundary.
3. Implement Bible library and reader against synthetic fixtures until content intake is approved.
4. Implement local bookmarks, highlights, annotations and reading position.
5. Add local full-text search, concordance and cross-reference services.
6. Add personal journals, diary, sermon notes, study notebooks and reading plans.
7. Add offline maps/atlas and licensed reference resources.
8. Add verse-card generation, export/share, help, settings and accessibility refinements.
9. Integrate and verify every feature in airplane mode.

A feature is not marked complete until its data path, UI, error handling, tests and offline behavior are all verified.

## 6. Scripture and third-party content authorization gate

The following restrictions remain binding:

- `content_intake_authorized=false` until the Owner separately approves the exact source package and intake action.
- The 2008 Maale Full Bible `MDYTWF / mdyeth` remains the intended primary Maale edition; do not substitute the 2007 `MDYBSE` New Testament.
- Do not download, acquire, unpack, inspect, hash, ingest, transform, store or commit scripture text/audio/scans before the separate content-intake authorization.
- A2 source verification and documentary rights evidence do not themselves authorize content intake.
- Amharic and English editions need their own exact edition/source/rights approval.
- Commentaries, dictionaries, concordances, maps, atlas datasets, images, audio, fonts and derived indexes each need documented provenance, licence, attribution, integrity and quality checks.
- Specialist Maale-language/Ethiopic-script review remains deferred as an accepted residual risk; no linguistic sign-off may be claimed.
- Until approvals are granted, implementation uses synthetic fixtures and metadata-only catalogues. The application must clearly identify unavailable content and must never fabricate scripture or reference material.

**Schedule consequence:** content-dependent acceptance and final release cannot pass while required content is unavailable or rights remain unresolved. Do not bypass this gate to meet a date.

## 7. Verification and release-quality gates — P8

Required automated and manual verification:

- Unit tests for domain logic, references, search, reading plans and personal records.
- Database migration tests, data integrity and persistence across process death/restart.
- Instrumented/UI tests for core reader and study workflows.
- Import/export integrity and interrupted-operation recovery tests.
- Search correctness, indexing and performance on approved corpora.
- Airplane-mode tests for all core features and installed reference resources.
- Accessibility tests, screen-reader behavior, text scaling and touch targets.
- Maale, Amharic and English rendering and localization review.
- Security/privacy review of local records, app storage, import packages, exports and backups.
- Low-storage, low-memory, slow-device and supported Android-version tests.
- Reproducible build and dependency/security checks.
- Regression testing against the exact reviewed source SHA.

Every result must be recorded as PASS, VERIFIED-CLOSED, PARTIAL, BLOCKED, NOT RUN, UNKNOWN or UNRESOLVED, with source SHA and evidence. A successful debug build alone is not production readiness.

## 8. Release preparation — P9

### Deliverables
- Final release version and changelog.
- Application icon, name, package identity and store metadata.
- Release signing configuration and secure key custody procedure.
- Reproducible signed release artifact and SHA-256 checksum.
- Licence/attribution and third-party notices.
- Privacy policy and data-safety/store disclosures appropriate to actual behavior.
- Supported-device and minimum-OS statement.
- Store screenshots and feature descriptions that accurately reflect shipped functionality.
- Crash/support contact process, known-issues list and rollback/withdrawal plan.
- Final acceptance report and Owner release decision.

### Exit gate
All required quality and rights gates pass. Owner explicitly approves release signing and deployment. No signing key or production credential is committed to the public repository.

## 9. Deployment — P10

1. Freeze the release candidate and record its exact source SHA.
2. Build and sign from the approved source and controlled environment.
3. Verify package identity, signature, checksum, version, permissions and offline behavior.
4. Publish only through the Owner-approved distribution channel.
5. Start a staged rollout where the distribution channel supports it.
6. Run installation, upgrade, first-launch, reader, search, notes, export and airplane-mode smoke tests.
7. Monitor rollout and apply documented pause/rollback criteria.
8. Record release URL/version, artifact checksum, publication time, rollout percentage and verification evidence.

**Deployment is a separate Owner approval gate.** This plan does not authorize signing, Play Store submission, production publication or rollout.

## 10. Post-deployment — P11/P12

### First 72 hours
- Confirm installation and upgrade success on representative devices.
- Validate core offline workflows and local data persistence.
- Review crash/ANR and support reports from sources explicitly approved for use.
- Triage critical defects and pause rollout if data loss, security or core-reading failures are detected.
- Publish a concise operational status and known-issues update.

### Days 4–30
- Resolve critical/high-severity defects.
- Review performance, battery, storage and search responsiveness.
- Verify backup/restore guidance and user support instructions.
- Issue signed, versioned patches only through the approved release process.
- Complete a 30-day stability and user-feedback report.

### Days 31–90
- Prioritize usability, localization, accessibility and content/resource improvements.
- Review device/OS compatibility and update dependencies safely.
- Plan lawful, integrity-checked offline content/resource updates.
- Prepare next-release discovery for accounts, cloud sync, online discussion groups, moderation, news subscriptions and server-delivered announcements.
- Keep the next-release server scope separate from this offline release and subject to its own architecture, privacy and security approvals.

### Ongoing maintenance
- Maintain a security and dependency update process.
- Retest migrations, backup/restore and offline guarantees for every material update.
- Maintain source/licence/attribution records for every distributed resource.
- Track support, defects, compatibility and release evidence.
- Preserve rollback capability and user data safety.

## 11. Deferred server-side features

These are expressly out of scope for this release:
- Account registration/authentication and cloud profiles.
- Cloud synchronization and cross-device shared state.
- Online group discussion, forums, comments and moderation.
- News subscriptions and publisher/server announcements.
- Collaborative notes and shared reading plans.
- Server push notifications and remote content administration.

The offline release may prepare clean interfaces and stable local models for future integration. It must not present server-dependent features as functional when no server exists.

## 12. Governance, reporting and schedule control

- Keep the A2 baseline and its evidence closed; reopen only for material authoritative contradiction.
- Preserve the A3 source history and successful build evidence.
- Implement A4 on `feat/a4-offline-study-suite`; do not modify `main` or merge PRs without separate approval.
- Preserve unrelated changes and keep all implementation within the approved offline scope.
- Do not request paid CI top-ups or deposits; use available remote verification paths and report unavailable gates honestly.
- Each phase report must include exact source SHA, changed files, commits/PRs, workflow run links, tests, artifact hashes, unresolved issues and next gate.
- No milestone is closed solely because its planned date has passed.
- Reforecast whenever a critical dependency changes; schedule pressure never overrides rights, security, quality or Owner approval.
- A5 release preparation, P10 deployment and P11/P12 post-deployment are distinct phases with separate evidence and approval requirements.

## 13. Immediate next actions

1. Complete P0 by converting the A4 scope into a feature-level requirements and acceptance matrix.
2. Execute P1/P2: detailed user journeys, screen inventory, data model, database schema, resource registry and architecture decision records.
3. Establish the A4 issue sequence and implementation/test dependencies.
4. Begin P3 with local persistence, migrations and synthetic fixtures.
5. Maintain the content authorization gate; do not import scripture or third-party assets without separate approval.
6. Reforecast the 12–16 week estimate after architecture review and content-rights dependency assessment.

**Current status:** A4 scope and phased plan are recorded. Detailed architecture and A4 implementation remain NOT STARTED unless separately evidenced by commits and verification. No merge, release signing, deployment or publication has been performed.
