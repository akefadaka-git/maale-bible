# A4 — P0/P1 and Implementation Issue Breakdown

**Status:** Proposed backlog baseline; issue creation tracked separately  
**Branch:** `feat/a4-offline-study-suite`  
**Priority model:** P0 governance/specification; P1 product/UX; P2 architecture; P3–P8 implementation and verification.  
**Rule:** No issue authorizes content intake, merge, signing or deployment.

## 1. Dependency sequence

```text
P0 governance and risk baseline
  └── P1 requirements, journeys, screen inventory, traceability
        └── P2 architecture and data contracts
              └── P3 local persistence/resource framework
                    ├── P4 library, reader and annotations
                    │     └── P5 search and reference suite
                    ├── P6 personal study and reading plans
                    └── P7 atlas, cards, export and backup
                          └── P8 integrated QA and hardening
                                └── P9 release candidate (separate gate)
                                      └── P10 deployment (separate Owner approval)
                                            └── P11/P12 post-release
```

Content-dependent implementation remains blocked on separate Owner content-intake approval and per-resource rights clearance. Synthetic fixtures and metadata-only catalogue work can proceed.

## 2. Issue breakdown

| Key | Proposed issue | Phase | Priority | Dependencies | Deliverable / exit evidence |
|---|---|---|---|---|---|
| A4-P0-01 | Freeze offline release boundary, governance and exclusions | P0 | Critical | A4 scope | Signed-off scope checklist; server features explicitly deferred |
| A4-P0-02 | Establish risk, assumptions and content-rights dependency register | P0 | Critical | A4 scope | Risk owner/status/evidence; content gate retained |
| A4-P0-03 | Define verification strategy, device matrix and quality gates | P0 | High | A3 baseline | Test matrix for unit, DB, UI, offline, accessibility, security and performance |
| A4-P1-01 | Define feature-level requirements and acceptance traceability | P1 | Critical | P0-01 | Requirements IDs, acceptance criteria and test mapping |
| A4-P1-02 | Complete screen inventory, route model and user journeys | P1 | High | P1-01 | Screen IDs, states, navigation and journey acceptance |
| A4-P1-03 | Define localization, accessibility and text-rendering requirements | P1 | High | P1-01 | Maale/Amharic/English and script validation plan |
| A4-P1-04 | Inventory content/resource classes and rights dependencies | P1 | Critical | P0-02 | Edition/resource register template; no source asset intake |
| A4-P1-05 | Prioritize release scope and map requirement-to-issue dependencies | P1 | High | P1-01..04 | Traceable backlog, critical path and provisional reforecast |
| A4-P2-01 | Define domain model and stable scripture reference contract | P2 | Critical | P1-01 | Edition/book/chapter/verse identity and migration-safe contracts |
| A4-P2-02 | Design Room schema, migrations and repository boundaries | P2 | Critical | P2-01 | ER model, migration plan and persistence test contracts |
| A4-P2-03 | Design edition/resource registry and atomic import lifecycle | P2 | Critical | P1-04, P2-01 | Manifest, provenance, rights gate, integrity and rollback contract |
| A4-P2-04 | Select local search/index architecture and language strategy | P2 | High | P2-01, P2-03 | Search design, tokenizer evaluation and benchmark plan |
| A4-P2-05 | Define personal-data privacy, backup and restore threat model | P2 | High | P2-02 | Data lifecycle, export format, encryption decision and restore safety |
| A4-P2-06 | Define navigation, design system and Compose state contracts | P2 | High | P1-02, P1-03 | Route graph, design tokens and screen state contracts |
| A4-P3-01 | Implement Room database and versioned migration foundation | P3 | Critical | P2-02 | Migration tests from supported schema versions |
| A4-P3-02 | Implement local repositories, settings and persistence test fixtures | P3 | Critical | P3-01 | Repository contract and process-restart tests |
| A4-P3-03 | Implement resource registry and integrity metadata framework | P3 | Critical | P2-03 | Metadata-only resources and integrity status tests |
| A4-P3-04 | Implement atomic resource import/remove transaction framework | P3 | High | P3-03, rights gate | Synthetic package tests, rollback and low-storage recovery |
| A4-P4-01 | Implement functional edition library and book/chapter navigation | P4 | Critical | P3-01..03 | Navigation tests using metadata/synthetic content |
| A4-P4-02 | Implement reader rendering, position and typography settings | P4 | Critical | P4-01 | Reader UI/domain tests and state restoration |
| A4-P4-03 | Implement bookmarks, highlights, tags and verse-linked notes | P4 | Critical | P3-02, P2-01 | CRUD, stable-reference and persistence tests |
| A4-P4-04 | Implement parallel reader and cross-reference navigation | P4 | High | P4-02, P4-03 | Alignment and navigation tests |
| A4-P5-01 | Implement offline full-text search and index lifecycle | P5 | Critical | P2-04, P3-03, content gate | Search correctness and rebuild tests |
| A4-P5-02 | Implement language-aware Maale/Amharic search adapters | P5 | High | P5-01, specialist review dependency | Tokenization/normalization tests without rewriting source text |
| A4-P5-03 | Implement concordance and topical index framework | P5 | High | P5-01, rights gate | Validated derived-resource tests |
| A4-P5-04 | Implement cross-reference and reference-work viewer | P5 | High | P4-04, P3-03, rights gate | Provenance display and navigation tests |
| A4-P5-05 | Implement people/place/timeline reference directory | P5 | Medium | P3-03, rights gate | Verified-source and citation tests |
| A4-P6-01 | Implement journal, diary and personal note workspace | P6 | High | P3-02 | CRUD, search, recovery and privacy tests |
| A4-P6-02 | Implement sermon notes and study notebooks | P6 | High | P6-01, P4-03 | Multi-reference, outline and persistence tests |
| A4-P6-03 | Implement passage collections and personal organization | P6 | Medium | P4-03, P6-01 | Tags, filters and stable reference tests |
| A4-P6-04 | Implement reading plan engine, progress and local reminders | P6 | High | P3-02 | Schedule, date/time, reminder and restart tests |
| A4-P7-01 | Implement offline atlas/map package framework | P7 | High | P2-03, rights gate | Synthetic map package and airplane-mode tests |
| A4-P7-02 | Implement place profiles and scripture links | P7 | Medium | P7-01, P4-04 | Source and reference navigation tests |
| A4-P7-03 | Implement verse-card composer and exact-text rendering | P7 | High | P4-02, rights gate | Golden/image and attribution tests |
| A4-P7-04 | Implement personal document export and Android share integration | P7 | High | P6-01..04, P7-03 | Export correctness and system chooser tests |
| A4-P7-05 | Implement local backup/restore and recovery workflow | P7 | Critical | P2-05, P3-02 | Round-trip, corruption, version and interrupted restore tests |
| A4-P8-01 | Execute integrated airplane-mode and regression suite | P8 | Critical | P4–P7 | Exact-SHA report; core workflows verified offline |
| A4-P8-02 | Execute accessibility and multilingual device matrix | P8 | Critical | P1-03, P4–P7 | Screen-reader, scaling, shaping and device evidence |
| A4-P8-03 | Execute privacy/security and import/export review | P8 | Critical | P2-05, P3–P7 | Threat review and remediation evidence |
| A4-P8-04 | Execute performance, storage and resilience benchmarks | P8 | High | P3–P7 | Reference-device benchmark and recovery report |
| A4-P8-05 | Complete release acceptance and unresolved-risk register | P8 | Critical | P8-01..04 | Requirement traceability; no unsupported closure claims |
| A4-P9-01 | Prepare release candidate and signing readiness package | P9 | Critical | P8 complete | Versioned candidate, notices, privacy disclosures, rollback plan |
| A4-P10-01 | Execute approved staged deployment | P10 | Critical | P9 and separate Owner approval | Publication URL, checksum, rollout and smoke-test evidence |
| A4-P11-01 | Stabilize first 30 days and publish operational review | P11 | High | P10 | Defect, crash, support and offline reliability review |
| A4-P12-01 | Prioritize 31–90 day improvements and next-release discovery | P12 | Medium | P11 | Accepted improvement and server-side discovery backlog |

## 3. Work sequencing and parallelism

- P0-01/P0-02/P0-03 can proceed in parallel.
- P1-01 is the parent of the screen, localization, content inventory and prioritization work.
- P2 data contracts and UX architecture may proceed in parallel after P1 baseline; database implementation must wait for schema decisions.
- P3 persistence and resource registry are foundational and should be completed before feature modules depend on them.
- P4 reader and P6 personal workspace can proceed in parallel after their shared data contracts are stable.
- P5 depends on stable references, resource registry and a defined search architecture.
- P7 map/resource work depends on the resource framework; export/backup work depends on P2 privacy/data decisions.
- P8 is integration, not a substitute for feature-level tests.
- P9/P10 are separate from A4 implementation and require separate Owner approvals.

## 4. Issue creation and tracking rules

- Each issue must include requirement IDs, scope, explicit non-goals, dependencies, acceptance checklist and test evidence expected.
- Issues must not be marked complete from documentation-only work if they describe implementation.
- Do not create duplicate issues for existing A3 work.
- Keep A2/A3 records closed and preserve unrelated changes.
- No scripture/source assets in issues, test fixtures or public commits.
- Keep labels/milestones optional if the repository does not already define suitable values; do not create taxonomy churn solely for this backlog.
- Each implementation PR must reference issue keys and include exact source SHA and verification evidence.
