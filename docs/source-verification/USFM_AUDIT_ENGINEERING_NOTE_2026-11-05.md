# USFM Audit Engineering Note — 2026-11-05

> **STATUS UPDATE (2026-11-07):** This note records the engineering state at its original date. The synthetic suite was subsequently executed against PR #1 corrective head `7008e827c43dd5f3a611f99882d67b4e5635ade4`: 12/12 tests passed, exit code 0. Exact source Git blob hashes and execution details are recorded in PR #1. The substantive rights/edition reconciliation is now PASS — DOCUMENTARY; package acquisition remains NOT AUTHORIZED. See [current source verification procedure](README.md) and [rights/edition reconciliation](RIGHTS_EDITION_RECONCILIATION_2026-11-07.md).

Status at note creation: IMPLEMENTED IN REPOSITORY — EXECUTION EVIDENCE PENDING  
Phase: A2 only  
Scope: Synthetic structural tooling; no scripture source package was acquired or inspected.

## Finding

Code inspection found that the initial audit implementation tracked chapter numbers and verse counts globally within a file. In a combined USFM file containing multiple books, chapter 1 in a second book could therefore be reported as a duplicate of chapter 1 in the first book, and verse counts could be conflated across books.

## Change committed

The audit now:
- Resets the current chapter when a new `\\id` book marker is encountered.
- Requires a book ID before accepting a chapter marker.
- Requires both a valid book and chapter before accepting a verse marker.
- Tracks chapter numbers independently for each book.
- Tracks verse-marker counts using a `BOOK:CHAPTER` key.
- Detects duplicate chapter numbers within the same book while allowing the same chapter number in different books.

The report now exposes `chapters_by_book` and `verse_marker_counts_by_book_and_chapter`. Because this is a report-shape change, the report version is advanced to `1.1.0`.

## Regression fixtures

Synthetic-only tests cover:
1. A single-book file and Ethiopic codepoint counting.
2. Verse markers before a chapter.
3. Repeated chapter 1 across two different books (valid).
4. Duplicate chapter number within one book (invalid).
5. Chapter marker before a book ID (invalid).
6. Manifest hash handling and the rights gate remaining blocked.
7. Duplicate book IDs across package files.
8. Expected inventory mismatch and duplicate expected inventory IDs.
9. Unsupported package formats fail closed for inventory comparison.

All fixtures use placeholder text, not scripture content.

## Verification status — updated

- Corrective source re-fetched from GitHub: VERIFIED.
- Exact Git blob identity for audit source and test source: VERIFIED; details in PR #1.
- Python synthetic unit suite on pinned corrective head: PASS — 12/12, exit code 0.
- CI/workflow execution: NOT RUN.
- Real Maale source validation: NOT RUN; source acquisition remains unauthorized.
- Rights/edition reconciliation: PASS — DOCUMENTARY, based on Owner-supplied written rights-holder response.
- Original email/signatory authentication review: PENDING.
- Licence compliance controls: SPECIFIED; application implementation NOT STARTED.
- Content intake authorization: FALSE.

## Remaining A2 engineering work

- Add or verify schema meta-validation and conditional approval-evidence requirements.
- Add archive-safety tests using synthetic ZIP fixtures before any real package is authorized.
- Add/verify expected inventory validation against an Owner-approved reference, without embedding scripture text.
- Execute synthetic archive-safety tests in an isolated environment; avoid CI spending unless separately authorized.
- Keep source-content intake blocked pending separate Owner authorization.
