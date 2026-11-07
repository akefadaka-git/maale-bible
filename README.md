# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build an offline-first reading experience.

## Current phase: A2 — source and rights verification

This repository contains project documentation and synthetic source-audit tooling only. The Owner supplied written rights-holder clarification stating that the 2008 Maale Full Bible (MDYTWF / mdyeth) is licensed under CC BY-SA 4.0 and is distinct from the 2007 MDYBSE New Testament. The substantive edition/licence reconciliation is recorded as PASS — DOCUMENTARY. Source-package acquisition and content intake remain separately unauthorized.

### A2 contents

- Product scope and A2 acceptance gates: docs/product/A2_SCOPE.md
- Rights and permissions decision: docs/licensing/RIGHTS_GATE.md
- CC BY-SA 4.0 implementation and release controls: docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md
- Detailed rights/edition evidence: docs/source-verification/RIGHTS_EDITION_RECONCILIATION_2026-11-07.md
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
