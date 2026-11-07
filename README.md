# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build an offline-first reading experience.

## Current phase: A3 — offline-first Android implementation

A2 was closed by Owner acceptance on 2026-11-07 with the named Maale-language/Ethiopic-script specialist review explicitly deferred as an accepted residual risk. The exact 2008 MDYTWF / mdyeth source baseline passed archive, signature, file-hash, structural, encoding and independent second-pass checks. The source archive and all scripture text remain outside this public repository. Production content intake remains separately unauthorized.

This repository contains project documentation, source-audit tooling and the Android application foundation. It does not contain scripture text or source archives. The Owner supplied written rights-holder clarification stating that the 2008 Maale Full Bible (MDYTWF / mdyeth) is licensed under CC BY-SA 4.0 and is distinct from the 2007 MDYBSE New Testament. The substantive edition/licence reconciliation is recorded as PASS — DOCUMENTARY. Source-package acquisition and content intake remain separately unauthorized.

### A2 contents

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

## Run synthetic tests

Requires Python 3.10+:

    python -m unittest discover -s tools/tests -v

## Preflight an authorized ZIP package

Only after the Owner separately authorizes acquisition:

    python tools/archive_preflight.py SOURCE_PACKAGE.zip --output preflight-report.json

This inspects ZIP metadata and hashes the archive; it does not extract entries or authorize content intake.

## Audit an authorized local source package

Only after the Owner separately authorizes acquisition and intake, and the approved rights evidence and source manifest are available:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --output audit-report.json

The audit reports structural metadata and file hashes; it does not print scripture text. A passing audit is not proof of canon completeness, translation accuracy, linguistic correctness, legal permission, or publication readiness.

## Content restrictions

Do not download, acquire, unpack, inspect, hash, ingest, or commit USFM, USX, scripture excerpts, audio, scans, or other source-content files until the Owner separately authorizes the exact source-package acquisition and intake. Keep content_intake_authorized=false. Android and backend implementation are not part of the current A2 authorization.


## A3 Android foundation

- Implementation scope: docs/product/A3_ANDROID_IMPLEMENTATION_SCOPE.md
- Android app module: app/
- Current slice: metadata-only canonical 66-book catalogue, Compose shell, Home/Books/Search/Reader placeholder screens and catalogue unit tests.
- Scripture content is intentionally not bundled. Production ingestion remains separately gated.
- A3 pull request: https://github.com/akefadaka-git/maale-bible/pull/3
