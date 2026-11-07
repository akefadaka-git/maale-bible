# USFM Audit Engineering Note — 2026-11-05

> **STATUS UPDATE (2026-11-07):** The existing USFM audit suite was executed against PR #1 corrective head `7008e827c43dd5f3a611f99882d67b4e5635ade4`: 12/12 tests passed, exit code 0. Exact source Git blob hashes and execution details are recorded in PR #1. The new ZIP metadata preflight utility was executed against exact committed GitHub blobs: 14/14 synthetic tests passed, exit code 0. Its source blob is `e0498027c7384cac3a4b0096bdd4b869580ed9fa`; test blob is `8242c29cfc95dbcfcad5864c2bb6e63917203983`. The substantive rights/edition reconciliation is PASS — DOCUMENTARY; package acquisition remains NOT AUTHORIZED.

Status at note creation: IMPLEMENTED IN REPOSITORY — EXECUTION EVIDENCE PENDING  
Phase: A2 only  
Scope: Synthetic structural tooling; no scripture source package was acquired or inspected.

## Finding

Code inspection found that the initial audit implementation tracked chapter numbers and verse counts globally within a file. In a combined USFM file containing multiple books, chapter 1 in a second book could therefore be reported as a duplicate of chapter 1 in the first book, and verse counts could be conflated across books.

## USFM audit change

The audit now resets the current chapter on each new book marker, requires a book ID before a chapter marker, requires book and chapter before verse markers, tracks chapter numbers independently per book, tracks verse counts per BOOK:CHAPTER, and detects duplicate chapter numbers within the same book.

## Synthetic verification

USFM synthetic suite:
- Exact pinned corrective head: `7008e827c43dd5f3a611f99882d67b4e5635ade4`.
- Command: `/opt/pyvenv/bin/python -S -m unittest discover -s tools/tests -v`.
- Result: 12 tests, all passed, exit code 0.
- Fixtures contain placeholder text only.

ZIP metadata preflight synthetic suite:
- Exact module Git blob: `e0498027c7384cac3a4b0096bdd4b869580ed9fa`.
- Exact test Git blob: `8242c29cfc95dbcfcad5864c2bb6e63917203983`.
- Command: `/opt/pyvenv/bin/python -S -m unittest discover -s tools/tests -v` in an isolated reconstructed workspace containing the exact module and test bytes.
- Result: 14 tests, all passed, exit code 0.
- Cases cover path traversal, absolute/drive/backslash paths, case-fold and Unicode normalization collisions, symlinks, nested archives, file/total size and compression-ratio limits, entry count, corrupt archives, and a valid placeholder ZIP.
- The utility reads ZIP central-directory metadata and hashes the archive; it never extracts entries or prints their contents.

## Current verification status

- USFM synthetic suite on pinned PR #1 head: PASS — 12/12, exit code 0.
- ZIP preflight synthetic suite on exact committed source blobs: PASS — 14/14, exit code 0.
- Schema Draft 2020-12 meta-validation and conditional approval-reference behavior: PASS in isolated validation.
- CI/workflow execution: NOT RUN.
- Real Maale source validation: NOT RUN; source acquisition remains unauthorized.
- Rights/edition reconciliation: PASS — DOCUMENTARY, based on Owner-supplied written rights-holder response.
- Original email/signatory authentication review: PENDING.
- Licence compliance controls: SPECIFIED; application implementation NOT STARTED.
- Content intake authorization: FALSE.

## Remaining A2 engineering work

- Execute archive preflight only after separate Owner authorization for the exact package.
- Add/verify expected book inventory validation against an Owner-approved reference, without embedding scripture text.
- Complete independent Maale-language and Ethiopic-script review.
- Keep source-content intake blocked pending separate Owner authorization.
