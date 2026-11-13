# Maale Bible — ጌኤዦ ማፃኣፖ

An initiative to establish an authoritative, rights-cleared Maale Bible source and build an offline-first reading experience.

## Governance status

- **A1:** CLOSED — FINAL.
- **A2 source readiness:** CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION (Owner acceptance 2026-11-07; independent technical re-verification 2026-11-12).
- **A3 Android foundation:** separately authorized and integrated on `main`; it is not part of the A2 source-readiness gate.
- **A4 and release:** separately governed; no release, signing, deployment or publication is authorized by A2 closure.

The A2 closure is limited to edition identity, rights/permission documentation, source acquisition, source integrity, structural/encoding validation and the immutable metadata-only baseline. Maale-language/Ethiopic-script specialist review remains deferred and is not claimed as passed.

## A2 source baseline

- Selected edition: 2008 Maale Full Bible, `MDYTWF / mdyeth`.
- Licence: CC BY-SA 4.0, as documented in the Owner-supplied rights-holder response and official edition record.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Archive: 70 entries; 66 USFM files; signed checksum list 68/68 verified.
- Structure: 66/66 book IDs, 1,189 chapters, 30,506 verse-marker lines.
- Encoding: 66/66 strict UTF-8; zero replacement characters and zero non-NFC files.
- Signing-key trust state: UNDEFINED; cryptographic validity is not represented as independent identity certification.

See [A2 final closure record](docs/source-verification/A2_FINAL_CLOSURE_RECORD_2026-11-12.md), [A2 completion register](docs/source-verification/A2_COMPLETION_AND_BLOCKER_REGISTER.md), and [source verification procedure](docs/source-verification/README.md).

## Repository contents

- Product and A2 scope: `docs/product/A2_SCOPE.md`
- Rights and licence gate: `docs/licensing/RIGHTS_GATE.md`
- Licence implementation controls for later product/release work: `docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md`
- Candidate source register: `content/source-register/README.md`
- Owner-approved 66-book inventory: `content/manifests/candidate-book-inventory.json`
- Metadata-only immutable source baseline: `content/manifests/maale-source-baseline-manifest.json`
- Per-file SHA-256 inventory: `content/manifests/maale-usfm-file-hashes.json`
- Source manifest schema: `content/manifests/source-manifest.schema.json`
- Expected inventory schema: `content/manifests/expected-inventory.schema.json`
- USFM audit report schema: `content/manifests/usfm-audit-report.schema.json`
- ZIP metadata preflight: `tools/archive_preflight.py`
- USFM structural audit: `tools/usfm_audit.py`
- Synthetic and baseline metadata regression tests: `tools/tests/`

## Run A2 tests

Requires Python 3.10+:

    python -m unittest discover -s tools/tests -v

The GitHub Actions workflow runs on relevant pull requests, changes pushed to `main`, and manual dispatch. CI uses synthetic fixtures and metadata-only evidence; the real scripture archive is not stored in or processed by public CI.

## Current content-intake authority

- The Owner's 2026-11-08 authorization for controlled intake of Maale, Amharic, English and necessary Bible/study sources is reconciled in [Owner Content-Intake Authority Reconciliation](docs/licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md).
- The older `content_intake_authorized=false` wording is historical and superseded for current project authority by that later, source-specific gated decision. Closed A2 records remain unchanged.
- Every source requires provenance, rights/licence/permission, attribution, integrity, compatibility and quality review. Candidate evaluation does not imply approval for packaging or distribution.
- Raw scripture, source archives, protected assets and derived datasets remain outside the public repository unless the exact licence permits publication and the Owner separately approves it.
- Audio and third-party assets remain individually rights-gated. A2 closure does not authorize merge, signing, release, deployment or publication.

## Current product requirements

- [Owner-approved Product Requirements Baseline v1.0](docs/product/PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md) — P0 frozen 2026-11-14.
- [A3 Product-Gap Reassessment](docs/product/A3_PRODUCT_GAP_REASSESSMENT_2026-11-14.md).
