# USFM Audit Engineering Note — 2026-11-06

Status: IMPLEMENTED ON REVIEW BRANCH — SYNTHETIC TEST EXECUTION VERIFIED AT PRIOR HEAD  
Phase: A2 only  
Branch: `feat/a2-manifest-inventory-audit`  
Base: `a955203a249257e7dbf7d83f5077b43e434498e3`

## Implemented increment

The synthetic audit tooling now includes:

- Package-level duplicate book-ID detection across distinct USFM files. Repeated chapter numbers remain valid across books, and a multi-book file is not misclassified as a cross-file duplicate.
- Manifest-declared `book_id` consistency checking against the USFM file.
- Optional expected book/chapter inventory validation through `--expected-inventory`. The inventory schema requires a source reference and an explicit Owner approval reference, and lists book IDs and positive chapter numbers only. The tool does not invent or embed a canon inventory; the reference must be independently reviewed and Owner-approved before use.
- Stable machine-readable issue codes with optional path and line context. Existing human-readable error messages remain available.
- Versioned JSON schemas for expected inventory and audit report output. Report version is `1.2.0`.
- Explicitly document that JSON Schema draft 2020-12 cannot enforce uniqueness of `books[].book_id` by projecting that property with `uniqueItems`; the audit engine enforces it at runtime and a dedicated synthetic regression test verifies fail-closed behavior.

## Report and rights behavior

A structural pass never grants rights or authorizes content intake. The report always sets `content_intake_authorized` to false. Missing or invalid rights evidence keeps `rights_gate` at `BLOCKED`; complete evidence fields only move it to `DOCUMENTED_REQUIRES_OWNER_REVIEW`.

The optional inventory is an independently supplied structural comparison, not a canon, translation-completeness, or theological determination. An inventory mismatch or invalid inventory fails the structural audit. An inventory without a recorded Owner approval reference is invalid. If any manifest file uses an unsupported format, expected inventory comparison is marked NOT_VERIFIABLE rather than producing false missing-book findings.

## Synthetic regression coverage added

The tests now cover:

1. Existing per-book chapter and verse behavior.
2. Stable USFM error codes for malformed marker order and duplicate chapters.
3. Same chapter number across distinct books in one multi-book file.
4. Duplicate book ID across separate package files.
5. Manifest hash and rights-gate behavior.
6. A matching expected inventory.
7. An expected inventory chapter mismatch.
8. An invalid inventory reference.
9. Duplicate expected inventory book IDs are rejected with `INVENTORY_BOOK_ID_DUPLICATE`.

All fixtures use placeholder text. No scripture source package was acquired or inspected.

## Verification status — superseded by A2 closure

The historical implementation status below is superseded by the exact-head and real-source evidence recorded in the A2 closure documents.

- Corrective manifest/book-ID regression: merged through PR #49.
- Current A2 tooling suite: 27 tests passed on run [#43199212983](https://github.com/akefadaka-git/maale-bible/actions/runs/43199212983), tested SHA `b54146c62b9a6e1fc9a530eab9eaed16535800e5`.
- Real Maale source validation: PASS; see the 2026-11-07 report and 2026-11-12 independent re-verification.
- Rights/edition gate: PASS — DOCUMENTARY, with email/signatory authentication caveat.
- Content intake authorization: FALSE and remains binding.
- Specialist language/script review: deferred and Owner-accepted; not passed.

## Schema/runtime contract

The expected-inventory schema explicitly documents that duplicate `books[].book_id` values are an application-level constraint. JSON Schema `uniqueItems` compares whole array items and cannot enforce uniqueness by a selected object property in this schema. The runtime validator rejects duplicate IDs with `INVENTORY_BOOK_ID_DUPLICATE`; the synthetic suite contains a regression test proving the fail-closed behavior. Consumers must run the audit engine in addition to schema validation.

## Current disposition

A2 source-readiness is CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION. The historical implementation blockers above have been superseded by the A2 final closure record. The specialist review and application licence controls remain later release risks; content intake remains false.
