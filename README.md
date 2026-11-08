# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build a comprehensive offline-first Bible reading and study application.

## Current work: A4 — comprehensive offline Bible and study application

The Owner has directed that this release become a full-featured offline Bible application combining advanced reading, study, reference, atlas, personal journaling, reading plans, verse-card export, sharing, help and accessibility capabilities. Server-dependent capabilities (accounts, cloud sync, online forums, group discussions, news subscriptions and remote administration) are deferred to a later release.

- **A4 product scope and architecture:** [docs/product/A4_OFFLINE_BIBLE_PRODUCT_SCOPE.md](docs/product/A4_OFFLINE_BIBLE_PRODUCT_SCOPE.md)
- **A4 implementation branch:** `feat/a4-offline-study-suite`
- **A3 foundation PR:** https://github.com/akefadaka-git/maale-bible/pull/3 (open, draft, unmerged)

A4 is not complete merely when screens are present. Features must work, persist correctly, pass verification and remain usable offline. No A4 completion or release-readiness claim is made at this stage.

## Accepted A2 source baseline and content controls

A2 was closed by Owner acceptance on 2026-11-07 with the named Maale-language/Ethiopic-script specialist review explicitly deferred as an accepted residual risk. The exact 2008 MDYTWF / mdyeth source baseline passed archive, signature, file-hash, structural, encoding and independent second-pass checks. The source archive and all scripture text remain outside this public repository. Production content intake remains separately unauthorized.

The Owner supplied written rights-holder clarification stating that the 2008 Maale Full Bible (MDYTWF / mdyeth) is licensed under CC BY-SA 4.0 and is distinct from the 2007 MDYBSE New Testament. The substantive edition/licence reconciliation is recorded as PASS — DOCUMENTARY. Source-package acquisition and content intake remain separately unauthorized.

### A2 records and tooling

- Product scope and A2 acceptance gates: docs/product/A2_SCOPE.md
- Rights and permissions decision: docs/licensing/RIGHTS_GATE.md
- CC BY-SA 4.0 implementation and release controls: docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md
- Detailed rights/edition evidence: docs/source-verification/RIGHTS_EDITION_RECONCILIATION_2026-11-07.md
- Owner-accepted real-source validation report: [docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md](docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md)
- Candidate source register: content/source-register/README.md
- Source manifest schema: content/manifests/source-manifest.schema.json
- Source verification procedure: docs/source-verification/README.md
- Source package readiness and safety checklist: docs/source-verification/SOURCE_READINESS_CHECKLIST.md
- ZIP metadata-only preflight: tools/archive_preflight.py
- Local USFM structural audit: tools/usfm_audit.py
- Synthetic ZIP preflight tests: tools/tests/test_archive_preflight.py
- Synthetic USFM regression tests: tools/tests/test_usfm_audit.py
- Audit engineering findings and verification status: docs/source-verification/USFM_AUDIT_ENGINEERING_NOTE_2026-11-05.md

## A3 Android foundation

- Implementation scope: docs/product/A3_ANDROID_IMPLEMENTATION_SCOPE.md
- Android app module: app/
- A3 baseline: metadata-only canonical 66-book catalogue, Compose shell, Home/Books/Search/Reader placeholder screens and catalogue unit tests.
- Verified debug build source SHA: `fcb1d04d7a227bfda320cd18a06b12fbd37e4b6d`
- Successful SHA-pinned GitHub Actions run: https://github.com/akefadaka-git/maale-bible/actions/runs/42454704758
- Scripture content is intentionally not bundled. Production ingestion remains separately gated.

## Run synthetic tests

Requires Python 3.10+:

    python -m unittest discover -s tools/tests -v

## Content restrictions

Do not download, acquire, unpack, inspect, hash, ingest, or commit USFM, USX, scripture excerpts, audio, scans, maps, commentaries, dictionaries or other source-content assets until the Owner separately authorizes the exact source-package acquisition and intake. Keep `content_intake_authorized=false`.

All additional Bible editions, reference works, maps, images, audio, fonts and derived datasets require their own source, licence, attribution, integrity and quality review. A4 scope authorization does not waive these gates.

## Release governance

A4 implementation and verification are separate from A5 release signing/deployment preparation and A6 publication. No merge, signing, deployment or publication is authorized by this scope update.
