# A4 Maale USFM Parser and Private Offline Database Pipeline — 10 November 2026

**Implementation branch:** `feat/a4-maale-usfm-content-pipeline`  
**Pull request:** [#42](https://github.com/akefadaka-git/maale-bible/pull/42)  
**Tested code SHA:** `14a1977c3ca167665d9e6d70a1459c3008762755`  
**Source edition:** 2008 Maale Full Bible — MDYTWF / mdyeth

## 1. Implemented

- Added `tools/usfm_content_pipeline.py`, a standard-library-only, fail-closed source-to-private-SQLite pipeline.
- Added parser tests with synthetic fixtures only.
- Added GitHub Actions workflow `.github/workflows/content-pipeline-verification.yml`.
- The workflow downloads the exact public source package to ephemeral runner storage, verifies its pinned archive SHA-256, checks safe ZIP metadata and CRC, verifies all 66 member hashes/sizes, and builds the private database twice.
- The workflow emits aggregate counts and digests only. It uploads no archive, database, extracted scripture, or full-text index; its cleanup removes the temporary source and generated databases.

## 2. Parser behavior and limitations

The first parser pass:
- Validates one expected USFM book ID per source file.
- Requires contiguous chapter numbering and strictly increasing, non-overlapping source-native verse references; keeps verse ranges as source-native labels and does not split them into guessed verse identities.
- Removes footnote, cross-reference and figure blocks from the initial plain-text reader/search extraction.
- Excludes section headings and other non-verse metadata from verse text.
- Retains paragraph/poetry continuation lines and strips inline formatting marker syntax while preserving enclosed words.
- Fails closed on unbalanced excluded blocks and unsupported markers.
- Keeps display text separate from NFKC/case-folded search text.
- Retains edition, book, chapter, verse label, source file, source-file hash and parser version on every record.

This is a conservative text/index extraction pass, **not** a complete USFM renderer. It does not yet preserve all rich USFM character styling, footnote display, cross-reference display, figure presentation or specialist editorial decisions. Original USFM source bytes remain the immutable reference.

## 3. Private database and FTS5

The generated private SQLite database contains:
- Source/edition/parser/schema/tokenizer provenance metadata.
- The 66-book frozen registry order.
- 30,506 edition-native verse-marker records.
- A separate FTS5 index over search text, linked to source records.

The selected FTS5 tokenizer is `unicode61 remove_diacritics 0`, recorded as an **engineering baseline only**. It is not yet linguistically qualified for Maale or Amharic. Source display text is never overwritten by search normalization.

## 4. Exact-head verification

Workflow: [Content pipeline verification #42757379089](https://github.com/akefadaka-git/maale-bible/actions/runs/42757379089)

- Tested SHA: `14a1977c3ca167665d9e6d70a1459c3008762755`.
- Synthetic parser/archive/FTS suite: **7/7 PASS**.
- Public content-boundary scan: **PASS**.
- Real source private build: **PASS**.
- Exact archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Books: **66**.
- Chapters: **1,189**.
- Verse records: **30,506**.
- Private database size: **22,134,784 bytes**.
- Private database SHA-256: `ccf284a586e12c4bf11e8ada54614fdf40ee1dd93a99466cffb266c744541b22`.
- Two clean builds on the same runner/runtime produced identical database digests: **PASS**.
- Scripture text emitted to workflow logs: **NO**.
- Generated archive/database artifacts uploaded or retained by the workflow: **NO** (ephemeral runner cleanup).

The database digest is a build-provenance identifier for this exact workflow runtime, parser version, schema and source. A build on a different SQLite runtime may produce a different binary digest; rebuild reproducibility is currently asserted within the same pinned runner job, not across all platforms.

## 5. Remaining gates

| Gate | Status |
|---|---|
| Exact source archive and per-file integrity | PASS |
| Parser synthetic contract tests | PASS — 7/7 |
| Actual 66-book parse and count reconciliation | PASS |
| Private SQLite/FTS5 database generation | PASS |
| Same-runtime deterministic rebuild | PASS |
| Full USFM renderer and rich-format preservation | NOT IMPLEMENTED |
| Maale and Amharic linguistic tokenizer qualification | NOT RUN |
| FTS5 Android runtime/API compatibility matrix | NOT RUN |
| Durable private content package retention/import | NOT IMPLEMENTED |
| Android atomic install, rollback and offline reader/search integration | NEXT |
| Linguistic/orthographic specialist acceptance | NOT RUN |

**Disposition:** A4 parser and private database build foundation **PASS WITH LIMITATIONS**. The private build output was intentionally ephemeral and must be regenerated or securely staged for Android integration. No scripture payload is committed to the public repository.

No merge to `main`, signing, deployment or publication is authorized by this record.
