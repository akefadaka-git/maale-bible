# Source Verification Procedure

## 1. Current authorization boundary

The selected candidate is the 2008 Maale Full Bible, MDYTWF / mdyeth. The Owner supplied a written rights-holder response stating that the exact edition and its eBible USFM package are covered by CC BY-SA 4.0. The related DBL MDYBSE RESERVED record is stated to concern a separate 2007 New Testament.

See:
- [Rights and Edition Reconciliation](RIGHTS_EDITION_RECONCILIATION_2026-11-07.md)
- [Rights Gate](../licensing/RIGHTS_GATE.md)
- [Licence Compliance Controls](../licensing/LICENCE_COMPLIANCE_CONTROLS.md)
- [Source Readiness Checklist](SOURCE_READINESS_CHECKLIST.md)

The documentary rights discrepancy is reconciled. However, **source-package acquisition and content intake are not authorized**. Keep content_intake_authorized=false. Do not download, acquire, unpack, hash, inspect, or ingest mdyeth_usfm.zip until the Owner separately authorizes that exact operation.

## 2. Preconditions for a future authorized intake

Before any package is acquired, the evidence register must contain:

- Original rights-holder email retained with sender, date, headers and attachments where available.
- Reviewer record of the signatory, organizational role and the scope of the written confirmation; unresolved authentication limits must be stated.
- Exact edition identity: 2008 Full Bible, MDYTWF / mdyeth, Maale / Male (mdy), and full-Bible scope.
- Licence: CC BY-SA 4.0, with explicit package applicability recorded.
- Owner's separate, explicit authorization identifying the package, acquisition purpose, permitted environment and whether inspection/intake is authorized.
- Approved expected book inventory and canon reference, with provenance and Owner approval.
- Named reviewers for Maale language and Ethiopic-script review.

## 3. Intake provenance and chain of custody

For a separately authorized package, record before processing:

- Exact source URL, retrieval timestamp (UTC), retrieval method and operator.
- Original filename, media type, byte size and HTTP/source metadata available.
- SHA-256 of the untouched archive, computed before extraction.
- Storage location and access controls; source archives and extracted text remain outside the public repository.
- Tool/runtime versions and exact commands used.
- Every extracted path, file size, format and SHA-256.
- Reviewer identity, custody transfers, timestamps and any errors or retries.
- Immutable link between source archive hash, extracted-file manifest, audit report and final approved baseline.

Never rewrite or normalize the original archive. Perform validation on a separate working copy and preserve the original bytes.

## 4. Safe archive handling requirements

Before extraction, inspect archive metadata only after acquisition has been authorized. Extraction tooling must fail closed on:

- Absolute paths, parent-directory traversal, drive-letter or UNC paths.
- Symlinks, hard links, device files, or unsupported entry types.
- Duplicate normalized paths, case-folding collisions, and Unicode-normalization collisions.
- Nested archives unless explicitly approved.
- Excessive file count, individual file size, total expanded size, or compression ratio.
- Unexpected file extensions, executable content, or files outside the declared package inventory.

Extract only into a new isolated directory with no overwrite behavior. Do not execute package contents. Record rejected entries and extraction-tool version. These are readiness requirements; no archive has been acquired or tested.

## 5. Structural validation

The existing synthetic audit utility checks UTF-8 readability, USFM book/chapter/verse markers, duplicate book IDs and chapter numbers within each file, positive chapter and verse numbers, verses after chapter markers, replacement characters, Ethiopic-range code points, and manifest SHA-256 equality.

The utility remains intentionally incomplete. It does not establish expected canon, complete book order, chapter/verse completeness, cross-file consistency, verse sequence continuity, full USFM grammar, footnote/table/poetry correctness, Unicode normalization policy, orthographic correctness, translation accuracy, or legal permission.

Validation must be layered:
1. Archive integrity and safe extraction.
2. Per-file and package structural checks.
3. Expected book inventory and canon comparison against an approved reference.
4. Expected chapter/verse inventory comparison where a trusted reference exists.
5. Unicode, Ethiopic-script and encoding review.
6. Maale-language specialist review.
7. Independent reproducibility and Owner baseline acceptance.

## 6. Reproducible synthetic verification

Use Python 3.10+ and the standard library:

    python -m unittest discover -s tools/tests -v

The corrective PR #1 head was previously reconstructed from exact GitHub source blobs and its complete synthetic suite passed 12/12 with exit code 0. That evidence applies to the pinned corrective head recorded in PR #1; it does not validate a real source package or the current documentation changes. No CI run is required for this documentation-only A2 work.

## 7. Acceptance gates

A2 source readiness requires:
- rights/edition reconciliation: PASS — DOCUMENTARY;
- rights evidence retention and authentication review: PENDING;
- explicit Owner authorization to acquire the exact package: PENDING;
- approved expected book/canon reference: PENDING;
- safe archive-handling procedure: SPECIFIED; execution NOT RUN;
- complete synthetic tooling verification: PASS on pinned PR #1 head;
- actual package validation: NOT RUN / NOT AUTHORIZED;
- Maale-language and Ethiopic-script specialist acceptance: NOT RUN;
- immutable source baseline: BLOCKED.

A green structural audit is not proof of canon completeness, translation accuracy, linguistic correctness, legal permission, or publication readiness.
