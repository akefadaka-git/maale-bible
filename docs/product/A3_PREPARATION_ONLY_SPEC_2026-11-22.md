# Maale Bible — A3 Preparation-Only Specification

**Document ID:** MB-A3-PREP-2026-11-22  
**Date:** 2026-11-22  
**Repository:** `akefadaka-git/maale-bible`  
**Controlled working branch:** `fix/a2-remediation-complete`  
**Authority:** Owner approval dated 2026-11-22  
**Status:** PREPARATION ONLY — A3 IMPLEMENTATION NOT STARTED

## 1. Authority and scope

The Owner authorized A2 remediation plus preparation of A3 specifications, using one existing controlled integration line and reusing existing pull-request work.

This document is a planning and acceptance specification only. It does not authorize A3 implementation, a phase transition, merge, main-branch modification, release signing, deployment, publication, paid-provider activation, or production content ingestion.

For the current execution, A2 remains PARTIAL/OPEN until all six A2 remediation gates have adequate exact-head implementation and test evidence and an A2 closure record is prepared and accepted. No older branch document or historical authorization is to be interpreted as permission to begin new A3 implementation under this current approval.

## 2. Entry conditions for future A3 implementation

A3 implementation must not begin until all of the following are true:

1. A2.1 Android CI/build baseline is reliable and exact-head evidence is recorded.
2. A2.2 Maale-default localization and Amharic/English selection meet their defined language, rendering, persistence and accessibility acceptance criteria.
3. A2.3 guest reading and the approved login/registration experience are verified; username-or-mobile plus password is used for login and OTP is registration-only.
4. A2.4 at least one legally authorized edition has a verified source/rights record, trusted package validation, installation evidence and exact-passage reader navigation.
5. A2.5 private local notes and every supported database migration/recovery path pass.
6. A2.6 the required Android API matrix has recorded results and unresolved failures are dispositioned.
7. The A2 closure record is complete and explicitly accepted by the Owner.
8. The Owner separately authorizes commencement of A3 implementation.

A passing build alone does not satisfy these conditions.

## 3. Product invariants carried forward

- Maale is the default interface language; Amharic and English remain selectable.
- Guest reading is supported and must not be blocked by account creation.
- Every edition that the platform is legally permitted to provide for free reading remains free to read.
- Login identifier is username or mobile number plus password.
- OTP is used for registration verification only, not routine login.
- Personal study notes remain local and private by default; no automatic account synchronization.
- No Scripture, Maale translation, source provenance, licence or editorial approval may be invented.
- The identified 2008 Maale Full Bible MDYTWF/mdyeth source and Owner-confirmed CC BY-SA 4.0 status do not automatically establish rights for any additional edition or supplementary content.
- Raw Scripture or derived Scripture datasets must not be added to the public repository unless a separate content-intake authorization and source-specific rights gate permit it.
- Audio, maps, third-party images, commentary and supplementary learning material require their own provenance and rights acceptance.

## 4. Proposed A3 workstreams — planning baseline only

### A3-W1 — Application architecture and design system

Specify stable presentation, domain and data boundaries; reusable Material 3 components; accessible navigation; responsive phone/tablet layouts; loading, empty, offline and error states; and design-token governance.

Acceptance to define: architecture decision records, screen inventory, navigation contracts, design-token catalogue, accessibility requirements and module dependency rules.

### A3-W2 — Verified Scripture package and edition lifecycle

Specify source registry, edition identity, manifest schema, publisher/source trust model, package download, archive safety, integrity verification, atomic installation, rollback, installed-edition selection, upgrade and removal.

Acceptance to define: source-specific rights record, pinned trust roots or equivalent authenticated provenance, signed or otherwise authenticated manifests, deterministic package validation, installation/recovery tests and audit evidence.

No unsigned hash-only package is to be described as publisher-authenticated. No real source package is to be ingested or published without the separate content-intake authorization.

### A3-W3 — Reader, passage identity and search

Specify canonical book/chapter/verse identity, edition-to-canonical passage mapping, exact search-result navigation, chapter navigation, reading position, accessibility, font/script rendering, offline search and edition switching.

Acceptance to define: deterministic passage-resolution contract, mapping confidence/status model, exact-result-to-reader tests, offline search tests, locale/script tests and explicit unavailable-content states.

### A3-W4 — Personal study and local data protection

Specify private notes, bookmarks, highlights, collections, reading history and user-controlled export. Keep personal notes local/private by default and prohibit automatic account sync.

Acceptance to define: data ownership and visibility model, database schema/migration plan, encryption/key-management decision, backup/restore and rollback tests, data deletion/export behaviour and privacy threat model.

### A3-W5 — Identity and optional account services

Specify guest-to-account transition, username/mobile identity, password lifecycle, registration-only OTP, session lifecycle, logout, recovery and abuse controls. Any server-side service must have an approved API contract and deployment/security decision before connection.

Acceptance to define: threat model, API contract, password and OTP policy, rate limits, session rotation/revocation, secure local token storage, privacy disclosures and end-to-end tests.

### A3-W6 — Learning and study resources

Specify book-learning packages, introductions, study guides, quizzes and source attribution. Draft content remains unavailable for public release until language, editorial, theological, reference and rights reviews are complete.

Acceptance to define: content schema, source register, language-review workflow, editorial/theological approval, reference validation, licence/attribution record and per-package publication gate.

### A3-W7 — Reading plans and progress

Specify local reading plans, reminders, progress calculations, calendar handoff and offline reconciliation. No cloud sync is implied.

Acceptance to define: plan data model, time-zone/calendar behaviour, accessibility, offline state and deterministic progress tests.

### A3-W8 — Audio, maps and external integrations

Treat audio, geographic data, cloud services, social publishing and external integrations as separately gated capabilities. Use official APIs and user-authorized handoff only; no credential scraping.

Acceptance to define per capability: named source/provider, supported API and permission scopes, rights, data flows, retention, revocation, cost, offline behaviour, security review and fallback. No provider or paid service is selected or activated by this specification.

### A3-W9 — Collaboration, organisations and membership

Plan groups, roles, moderation, courses, creator workflows, organisation workspaces and membership entitlements in accordance with the frozen A1 product specification.

Acceptance to define: least-privilege authorization, safeguarding and moderation operations, privacy boundaries, audit events, capacity/load tests, data portability, billing/legal requirements and abuse controls. Public communities, youth groups, payment processing and paid entitlements remain disabled until their separate operational and legal gates pass.

## 5. Cross-cutting engineering requirements

- Kotlin, Android Gradle Plugin, Jetpack Compose and Material 3 remain the approved Android foundation unless an Owner-approved architecture change is recorded.
- Preserve minimum supported Android API 26 and validate the agreed API matrix.
- Keep source and licence metadata separate from application presentation.
- Use deterministic, reproducible builds and dependency/version controls.
- Require unit, integration, instrumentation, migration, accessibility, localization, offline and security tests appropriate to each workstream.
- Record exact commit SHA, workflow run, individual job, test report and artifact checksum for acceptance.
- Distinguish product/code failures from emulator, runner, ADB, SDK, network and other infrastructure failures.
- Do not mark an item PASS from source inspection alone when runtime or test evidence is required.
- No secrets, production credentials or raw protected Scripture content in the public repository.

## 6. Required A3 planning deliverables before implementation authorization

1. Architecture decision record set and dependency map.
2. Screen and user-journey inventory with acceptance criteria.
3. Data model and migration plan.
4. Source/edition trust and rights model.
5. Authentication and privacy threat model.
6. Localization and language-quality workflow.
7. Test strategy and device/API matrix.
8. Security, accessibility, performance and offline acceptance plan.
9. Phased work breakdown with dependencies, risks and rollback strategy.
10. Explicit non-authorizations and Owner decision log.

These are specification deliverables, not implementation tasks.

## 7. Phase control

| Item | Current disposition |
|---|---|
| A2 remediation | ACTIVE — PARTIAL/OPEN |
| A3 specification preparation | AUTHORIZED |
| A3 implementation | NOT AUTHORIZED UNDER CURRENT EXECUTION |
| A4 independent verification/security review | NOT STARTED BY THIS DOCUMENT |
| A5 deployment/release | NOT AUTHORIZED |

The A3 specification may be refined as A2 evidence reveals verified constraints. Such refinement must not alter the frozen A1 product decisions without a documented Owner-approved change.

## 8. Change control and closure

Any material scope, architecture, source, privacy, licensing or commercial change requires a written change request stating the reason, affected requirements, compatibility impact, evidence and revised acceptance criteria.

This document is a preparation baseline only. It does not declare A2 closed, start A3 implementation, authorize production ingestion, or authorize merge, signing, deployment or publication.
