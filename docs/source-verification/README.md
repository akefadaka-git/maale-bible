# Source Verification Procedure

## 1. Current authorization boundary

The selected candidate is the 2008 Maale Full Bible, MDYTWF / mdyeth. The Owner supplied a written rights-holder response stating that the exact edition and its eBible USFM package are covered by CC BY-SA 4.0. The related DBL MDYBSE RESERVED record is stated to concern a separate 2007 New Testament.

See:
- [Rights and Edition Reconciliation](RIGHTS_EDITION_RECONCILIATION_2026-11-07.md)
- [Rights Gate](../licensing/RIGHTS_GATE.md)
- [Licence Compliance Controls](../licensing/LICENCE_COMPLIANCE_CONTROLS.md)
- [Source Readiness Checklist](SOURCE_READINESS_CHECKLIST.md)
- [A2 Completion and Blocker Register](A2_COMPLETION_AND_BLOCKER_REGISTER.md)
- [Provisional Canon Inventory](CANDIDATE_CANON_INVENTORY.md)
- [Language and Script Review Protocol](LANGUAGE_SCRIPT_REVIEW_PROTOCOL.md)

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

The 66-book candidate inventory is a preparation artifact only. Its JSON companion is a book-ID/order register, not the USFM auditor's expected-inventory input: chapter counts, a trusted source reference and Owner approval are deliberately absent. It is not the expected inventory for audit until approved.

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

## 4. ZIP metadata preflight

After separate Owner authorization to acquire the exact package, run the metadata-only preflight:

    python tools/archive_preflight.py SOURCE_PACKAGE.zip --output preflight-report.json

The utility reads the ZIP central directory and hashes the archive. It does not extract entries or emit member contents. It fails closed on:
- Absolute, drive-qualified, UNC, backslash, and parent-traversal paths.
- Duplicate paths after Unicode NFC normalization and case folding.
- Symlinks and special filesystem entries.
- Encrypted entries and nested archives.
- Configured entry-count, per-file size, total uncompressed size, and compression-ratio limits.
- Invalid ZIP archives.

Default limits are 2,000 entries, 50 MiB per file, 250 MiB total uncompressed size and 100:1 compression ratio. Record the exact command and report. Any limit change must be reviewed and documented.

A PASS is only a metadata preflight result. It does not validate member content, completeness, USFM structure, rights, or authorize extraction. Do not execute files from the archive.

## 5. Safe extraction and file inventory

After a passing preflight and separate approval of the extraction step, extract only into a new empty isolated directory with path containment checks and no overwrite behavior. Do not extract into a repository checkout. Preserve the original archive unchanged.

For every extracted file record normalized relative path, byte size, detected and declared format, SHA-256, USFM book ID where applicable, encoding result, and expected/unexpected/duplicate status. Recompute hashes independently. The manifest must cover every file in the approved package scope; no missing, extra, or silently ignored files.

## 6. Structural and specialist validation

The existing synthetic USFM audit utility checks UTF-8 readability, USFM book/chapter/verse markers, duplicate book IDs and chapter numbers within each file, positive chapter and verse numbers, verses after chapter markers, replacement characters, Ethiopic-range code points, and manifest SHA-256 equality.

The utility remains intentionally incomplete. It does not establish expected canon, complete book order, chapter/verse completeness, cross-file consistency, verse sequence continuity, full USFM grammar, footnote/table/poetry correctness, Unicode normalization policy, orthographic correctness, translation accuracy, or legal permission.

Validation must be layered:
1. Archive integrity and safe extraction.
2. Per-file and package structural checks.
3. Expected book inventory and canon comparison against an Owner-approved reference.
4. Expected chapter/verse inventory comparison where a trusted reference exists.
5. Unicode, Ethiopic-script and encoding review under the [Language and Script Review Protocol](LANGUAGE_SCRIPT_REVIEW_PROTOCOL.md).
6. Maale-language specialist review.
7. Independent reproducibility and Owner baseline acceptance.

## 7. Reproducible synthetic verification

Use Python 3.10+ and the standard library:

    python -m unittest discover -s tools/tests -v

Evidence:
- ZIP preflight suite: 14/14 synthetic tests passed, exit code 0, against exact GitHub source blobs recorded in the A2 branch.
- USFM audit suite: 12/12 synthetic tests passed, exit code 0, against pinned PR #1 corrective head 7008e827c43dd5f3a611f99882d67b4e5635ade4.
- Candidate inventory JSON: valid JSON; 66 unique USFM IDs; contiguous order 1–66; approval flags false.
- No CI/workflow was run.
- No real Maale source package was acquired or inspected.

## 8. Acceptance gates

A2 source readiness requires:
- rights/edition reconciliation: PASS — DOCUMENTARY;
- product permissions: PASS — DOCUMENTARY, with audio and third-party assets separately gated;
- original email/signatory authentication: PARTIAL — pending independent review or Owner acceptance of limitations;
- canon and expected inventory: PARTIAL — candidate prepared, primary-edition verification and Owner approval pending;
- explicit Owner authorization to acquire the exact package: BLOCKED;
- ZIP metadata preflight utility and synthetic suite: PASS — synthetic only;
- real archive preflight, extraction and package inventory: NOT RUN / NOT AUTHORIZED;
- actual USFM structural validation: NOT RUN;
- Maale-language and Ethiopic-script specialist acceptance: NOT RUN;
- immutable source baseline: BLOCKED.

A green structural audit is not proof of canon completeness, translation accuracy, linguistic correctness, legal permission, or publication readiness. See the [A2 Completion and Blocker Register](A2_COMPLETION_AND_BLOCKER_REGISTER.md) for the formal closure disposition.
