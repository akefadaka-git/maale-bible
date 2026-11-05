# USFM Audit Engineering Note — 2026-11-05

Status: IMPLEMENTED IN REPOSITORY — EXECUTION EVIDENCE PENDING  
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

The report now exposes `chapters_by_book` and `verse_marker_counts_by_book_and_chapter`. Because this is a report-shape change, the report version is advanced to `1.1.0`. This changes the report's structural detail; consumers must use the new per-book fields rather than assume a global chapter sequence.

## Regression fixtures added

Synthetic-only tests cover:
1. A single-book file and Ethiopic codepoint counting.
2. Verse markers before a chapter.
3. Repeated chapter 1 across two different books (valid).
4. Duplicate chapter number within one book (invalid).
5. Chapter marker before a book ID (invalid).
6. Manifest hash handling and the rights gate remaining blocked.

All fixtures use placeholder text, not scripture content.

## Verification status

- Source files re-fetched from GitHub after commits: VERIFIED.
- Python unit-test execution: NOT RUN.
- CI/workflow execution: NOT RUN.
- Real Maale source validation: NOT RUN; source acquisition remains blocked.
- Rights gate: BLOCKED / UNRESOLVED.
- Content intake authorization: FALSE.

Do not describe these regression tests as passing until they have actually been executed in an authorized environment. No test or workflow run was initiated as part of this change.

## Remaining A2 engineering work

- Add optional expected book/chapter inventory validation from an Owner-approved reference, without embedding scripture text.
- Add manifest-wide duplicate book-ID detection and package-level inventory checks.
- Add report schema and stable machine-readable error codes.
- Execute synthetic tests in an available local environment after Owner approval; avoid CI spending.
- Keep all source-content intake blocked pending written rights reconciliation and separate Owner approval.
