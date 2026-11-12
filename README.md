# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build an offline-first reading experience.

## Current phase: A4 — offline study and verified-content foundation

A2 source verification for the 2008 Maale Full Bible (MDYTWF / mdyeth) was closed by Owner acceptance on 2026-11-07, with the named Maale-language/Ethiopic-script specialist review explicitly deferred as an accepted residual risk. The exact archive's integrity and structural baseline is recorded in the A2 reports.

On 2026-11-08, the Owner authorized controlled content intake for Maale, Amharic, English and other necessary Bible/study resources. This authorization supersedes the earlier blanket `content_intake_authorized=false` restriction, but does not approve every source. Each edition/resource must pass its own provenance, licence/permission, integrity, quality, language and architecture gates before integration or distribution.

The selected Maale source is the 2008 Full Bible, MDYTWF / mdyeth, distinct from the 2007 MDYBSE New Testament. The Owner-supplied rights-holder clarification states CC BY-SA 4.0 for the 2008 Full Bible. The original email/signatory authentication remains a caveat; no independent legal opinion is claimed.

## Current implementation

- A3 Android foundation merged to `main` through PR #50.
- A4 P2 offline study suite is being integrated through a current-main reconciliation PR.
- The P2 foundation adds stable canonical book/passage identity, local study persistence, encrypted backup/restore planning, verified-USFM import contracts, source manifests and offline search verification.
- The Android app remains a development foundation. Passing build/tests do not constitute source approval, language approval, product integration approval or release readiness.

## A2 contents and source controls

- Product scope and A2 acceptance gates: docs/product/A2_SCOPE.md
- Rights and permissions decision: docs/licensing/RIGHTS_GATE.md
- CC BY-SA 4.0 implementation and release controls: docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md
- Owner content-intake authorization: docs/governance/OWNER_CONTENT_INTAKE_AUTHORIZATION_2026-11-08.md
- Detailed rights/edition evidence: docs/source-verification/RIGHTS_EDITION_RECONCILIATION_2026-11-07.md
- Owner-accepted real-source validation report: docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md
- Source register: content/source-register/README.md
- 2008 Maale source manifest (metadata and hashes only): content/manifests/maale-2008-source-manifest.json
- Candidate source register: content/source-register/README.md
- Source manifest schema: content/manifests/source-manifest.schema.json
- Optional expected book/chapter inventory schema: content/manifests/expected-inventory.schema.json
- Versioned USFM audit report schema (v1.2.0): content/manifests/usfm-audit-report.schema.json
- Source verification procedure: docs/source-verification/README.md
- Source package readiness and safety checklist: docs/source-verification/SOURCE_READINESS_CHECKLIST.md
- ZIP metadata-only preflight: tools/archive_preflight.py
- Local USFM structural audit: tools/usfm_audit.py
- Synthetic ZIP preflight tests: tools/tests/test_archive_preflight.py
- Synthetic USFM regression tests: tools/tests/test_usfm_audit.py

## Run synthetic source-tool tests

Requires Python 3.10+:

    python -m unittest discover -s tools/tests -v

## Preflight an authorized ZIP package

Only after source-specific Owner intake authorization and rights review:

    python tools/archive_preflight.py SOURCE_PACKAGE.zip --output preflight-report.json

This inspects ZIP metadata and hashes the archive; it does not extract entries or authorize product integration.

## Audit an authorized local source package

Only after the exact source has passed its source-specific rights and intake gates:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --output audit-report.json

If an independently reviewed and Owner-approved expected inventory is available, it may be supplied explicitly:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --expected-inventory EXPECTED_INVENTORY.json --output audit-report.json

The expected inventory contains book IDs and chapter numbers only. It is optional and requires a source reference and an explicit Owner approval reference. Duplicate `books[].book_id` values are rejected by the audit engine at runtime; JSON Schema validation alone cannot enforce uniqueness by that property, so consumers must run the audit engine. When a package includes unsupported formats, the inventory comparison is marked not verifiable rather than treating uninspected books as missing. It must not be treated as a canon assertion unless its reference has been independently established and approved.

The audit reports structural metadata, stable issue codes, and file hashes; it does not print scripture text. A passing audit is not proof of canon completeness, translation accuracy, linguistic correctness, legal permission, or publication readiness.

## Content and distribution boundaries

- Controlled source intake is authorized only within the 2026-11-08 Owner authorization and per-source gates.
- Do not commit raw scripture text, source archives, audio, scans, or restricted reference assets to this public repository unless the exact licence expressly permits public redistribution and the repository distribution model is approved.
- Do not infer that the Maale CC BY-SA 4.0 licence covers Amharic/English editions or third-party audio, maps, fonts, images, logos or commentary.
- Preserve required attribution, modification notices and ShareAlike obligations.
- Specialist Maale-language/Ethiopic-script review remains an accepted residual-risk item; no specialist sign-off is claimed.
- Merge, signing, deployment, store publication and public release remain separate stage-gated decisions.
