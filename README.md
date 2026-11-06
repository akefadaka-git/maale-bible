# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build an offline-first reading experience.

## Current phase: A2 — source and rights verification

This repository currently contains project documentation and source-audit tooling only. Scripture content is intentionally excluded until the exact edition, rights holder, licensing authority, and written product permissions have been verified and approved.

### A2 contents

- Product scope and A2 acceptance gates: docs/product/A2_SCOPE.md
- Rights and permissions gate: docs/licensing/RIGHTS_GATE.md
- Candidate source register: content/source-register/README.md
- Source manifest schema: content/manifests/source-manifest.schema.json
- Optional expected book/chapter inventory schema: content/manifests/expected-inventory.schema.json
- Versioned USFM audit report schema (v1.2.0): content/manifests/usfm-audit-report.schema.json
- Source verification procedure: docs/source-verification/README.md
- Local USFM structural audit: tools/usfm_audit.py
- Synthetic-only regression tests: tools/tests/test_usfm_audit.py
- Audit engineering findings and verification status: docs/source-verification/USFM_AUDIT_ENGINEERING_NOTE_2026-11-05.md

## Run synthetic tests

Requires Python 3.10+:

    python -m unittest discover -s tools/tests -v

## Audit an authorized local source package

Only after written permission is verified and Owner approval is recorded:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --output audit-report.json

If an independently reviewed and Owner-approved expected inventory is available, it may be supplied explicitly:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --expected-inventory EXPECTED_INVENTORY.json --output audit-report.json

The expected inventory contains book IDs and chapter numbers only. It is optional and requires both a source reference and an explicit Owner approval reference. It must not be treated as a canon assertion unless its reference has been independently established and approved.

The audit reports structural metadata, stable issue codes, and file hashes; it does not print scripture text. A passing audit is not proof of canon completeness, translation accuracy, linguistic correctness, legal permission, or publication readiness. Structural success never authorizes content intake.

## Content restrictions

Do not commit USFM, USX, scripture excerpts, audio, scans, or other source-content files until the rights gate is closed and a separate content-baseline approval is granted. Android and backend implementation are not part of the current A2 authorization.
