# USFM Audit Engineering Note — 2026-11-06

Status: IMPLEMENTED — SYNTHETIC USFM AND ZIP TESTS VERIFIED AT PINNED SOURCES  
Phase: A2 only  
USFM audit branch: `feat/a2-manifest-inventory-audit`  
ZIP preflight implementation: A2 source-readiness branch  
No scripture source package was acquired or inspected in these implementation increments.

## Implemented USFM audit increment

The synthetic audit tooling includes:

- Package-level duplicate book-ID detection across distinct USFM files. Repeated chapter numbers remain valid across books, and a multi-book file is not misclassified as a cross-file duplicate.
- Manifest-declared `book_id` consistency checking against the USFM file.
- Optional expected book/chapter inventory validation through `--expected-inventory`. The inventory schema requires a source reference and an explicit Owner approval reference, and lists book IDs and positive chapter numbers only. The tool does not invent or embed a canon inventory; the reference must be independently reviewed and Owner-approved before use.
- Stable machine-readable issue codes with optional path and line context. Existing human-readable error messages remain available.
- Versioned JSON schemas for expected inventory and audit report output. Report version is `1.2.0`.
- Explicit documentation that JSON Schema draft 2020-12 cannot enforce uniqueness of `books[].book_id` by projecting that property with `uniqueItems`; the audit engine enforces it at runtime and a dedicated synthetic regression test verifies fail-closed behavior.

## Report and rights behavior

A structural pass never grants rights or authorizes content intake. The report always sets `content_intake_authorized` to false. Missing or invalid rights evidence keeps `rights_gate` at `BLOCKED`; complete evidence fields only move it to `DOCUMENTED_REQUIRES_OWNER_REVIEW`.

The optional inventory is an independently supplied structural comparison, not a canon, translation-completeness, or theological determination. An inventory mismatch or invalid inventory fails the structural audit. An inventory without a recorded Owner approval reference is invalid. If any manifest file uses an unsupported format, expected inventory comparison is marked NOT_VERIFIABLE rather than producing false missing-book findings.

## ZIP metadata-only preflight

The ZIP preflight utility reads archive central-directory metadata and hashes the archive; it does not extract entries or print their contents. It is a safety preflight, not source approval or content-intake authorization.

Pinned source evidence:
- Module Git blob: `e0498027c7384cac3a4b0096bdd4b869580ed9fa`.
- Test Git blob: `8242c29cfc95dbcfcad5864c2bb6e63917203983`.
- Synthetic suite result: 14/14 tests passed, exit code 0, using exact committed GitHub blobs in an isolated reconstructed workspace.
- Covered cases include path traversal, absolute/drive/backslash paths, case-fold and Unicode normalization collisions, symlinks, nested archives, file/total size and compression-ratio limits, entry count, corrupt archives, and a valid placeholder ZIP.

## Synthetic regression verification

USFM audit:
- Exact corrective head: `7008e827c43dd5f3a611f99882d67b4e5635ade4`.
- Command: `/opt/pyvenv/bin/python -S -m unittest discover -s tools/tests -v`.
- Result: 12 tests passed, exit code 0.
- Fixtures contain placeholder text only.

ZIP preflight:
- Exact module and test Git blob hashes are recorded above.
- Result: 14 tests passed, exit code 0.
- No archive extraction or scripture inspection was performed.

## Current verification status

- USFM synthetic suite on pinned PR #1 head: PASS — 12/12.
- ZIP preflight synthetic suite on exact committed source blobs: PASS — 14/14.
- Expected-inventory/report schema validation: PASS in isolated validation; runtime remains authoritative for uniqueness by object property.
- GitHub Actions/CI for these A2 tooling tests: NOT RUN.
- Real Maale source validation in these implementation increments: NOT RUN.
- Rights/edition reconciliation: PASS — DOCUMENTARY, based on Owner-supplied written rights-holder clarification.
- Original email/signatory authentication review: PENDING.
- Licence compliance controls: SPECIFIED; application implementation NOT STARTED.
- Content intake authorization: FALSE.

## Remaining A2 gates

- Execute archive preflight only after separate Owner authorization for the exact package.
- Use an expected inventory only after its reference and approval are independently established.
- Complete independent Maale-language and Ethiopic-script review.
- Keep source-content intake blocked pending separate Owner authorization.
