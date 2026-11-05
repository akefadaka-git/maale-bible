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

## A2 package-inventory increment — implemented on review branch

The next engineering increment adds:
- Package-wide duplicate book-ID detection across manifest entries.
- Optional manifest `book_id` consistency checks for single-book USFM files.
- Optional expected book/chapter comparison using a separate JSON inventory.
- An inventory provenance gate: comparison requires `review_status: APPROVED` and a non-empty `reference`.
- Stable issue codes alongside human-readable messages.
- A versioned audit-report JSON Schema (`1.2.0`) and expected-inventory schema (`1.0.0`).

The inventory contains book identifiers and chapter numbers only. It is not a canon assertion; its authority depends on the independently reviewed reference. No Maale inventory has been authored or populated in this increment.

### Optional inventory invocation

```sh
python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --expected-inventory EXPECTED-INVENTORY.json --output audit-report.json
```

Without `--expected-inventory`, the audit performs package-level duplicate book-ID checks but does not assert an expected book/chapter set.

### Verification status for this increment

- Source changes committed to review branch: IMPLEMENTED.
- Synthetic regression fixtures: ADDED; execution NOT RUN.
- JSON Schema documents: ADDED; schema-validator execution NOT RUN.
- GitHub Actions / CI: NOT RUN.
- Real Maale source validation: NOT RUN; source acquisition remains blocked.
- Rights gate: BLOCKED / UNRESOLVED.
- Content intake authorization: FALSE.

### Remaining A2 work

- Execute synthetic tests locally in an available authorized environment; avoid CI spending.
- Review the inventory and report schemas against the emitted report shape.
- Keep source-content intake blocked pending written rights reconciliation and separate Owner approval.
