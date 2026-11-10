# A4 Offline Search and Multi-Edition Source Catalogue — 10 November 2026

**Branch:** `feat/a4-search-multi-edition-catalogue`  
**Base:** Android private content import branch `feat/a4-android-offline-content-import`  
**Target:** Maale Bible Android app

## 1. Offline Search integration

- Search now opens the installed private scripture database on an IO dispatcher.
- Full-text search uses the existing FTS5 index and is debounced by 300 ms.
- Search results show source-native book ID, chapter, verse reference and display text.
- Book-name / USFM-code catalogue lookup remains available.
- Search result list is one scroll container; content-unavailable, verification-failure and no-result states are explicit.
- Search can switch among independently installed edition databases.
- No account or network is required for search after an edition is installed.

## 2. Multiple edition storage

The private content store now isolates each edition under its own app-private directory, keyed by SHA-256 of the edition ID. It supports listing, opening and installing multiple editions without overwriting another translation. The install/rollback and integrity validation contract remains in force.

Synthetic Android instrumentation now covers two edition databases installed side-by-side and queried independently.

## 3. Edition catalogue and rights decisions

The catalogue groups editions by language and retains each edition's own source, license, attribution, completeness and readiness.

### Eligible source records

- Maale Full Bible (2008 MDYTWF / mdyeth) — CC BY-SA 4.0.
- King James (Authorized) Version — public domain outside the UK; UK print/import restrictions are recorded.
- American Standard Version (1901) — public domain.
- World English Bible Classic — public domain text; trademark distinction retained.
- World English Bible Updated — public domain.
- American Standard Version Byzantine Text — public domain.
- Geneva Bible 1599 — public domain; historical spelling.
- Berean Standard Bible — public-domain dedication effective 30 April 2023.
- Open English Bible — CC0; current 2025.6 release is partial (New Testament, Psalms and selected Old Testament books).
- Free Bible Version — CC BY-SA 4.0; attribution and ShareAlike obligations apply.
- JPS TaNaKH 1917 — public domain, but Old Testament / Jewish Scriptures only.

### Amharic records

- Open Word Bible Amharic is CC0, but its own download page states that AI-assisted sentences remain drafts until a named reviewer approves them. It is therefore marked **EDITORIAL REVIEW REQUIRED**, not installable as an approved Bible translation.
- The Revised Amharic Bible (2003) New Testament is copyrighted by United Bible Societies. The source requires the full copyright statement for non-commercial use and written permission for commercial use. It is **not** treated as an open/free license and is marked **RIGHTS REVIEW REQUIRED**.
- No verified, mature, unrestricted Amharic Bible translation is currently marked ready for in-app installation by this catalogue. This is a source-verification finding, not a claim that no such translation exists anywhere.

## 4. Download boundary

The Bibles screen opens the official source page or a verified source-package URL where one is available. It does **not yet** provide end-to-end in-app installation of those downloaded source archives. The current private installer accepts a validated SQLite package, not arbitrary ZIP/USFM archives.

Next implementation work must:
1. Add a source-specific USFM/USX ingestion adapter and edition-aware parser contracts.
2. Preserve exact license text, copyright notices, source file hashes, version/revision and book coverage.
3. Build a SQLite/FTS package per edition and run source-specific structural and text-integrity checks.
4. Deliver a verified package to Android through an explicit package manifest with a trusted signature or equivalent authenticity mechanism.
5. Add resumable download, checksum verification, storage checks, install/rollback and edition management UI.
6. Qualify tokenizer/search behavior separately for English and Amharic; no linguistic quality is implied by FTS5 compatibility.

No unrestricted bulk import is assumed from Open.Bible or eBible.org catalogs. Every edition must be checked against its individual license; CC BY, CC BY-SA, CC BY-ND, CC BY-NC and public-domain entries do not have interchangeable conditions.

## 5. Verification scope

- Search UI and multi-edition store code are implemented on this branch.
- Catalog unit tests assert that Amharic draft/permission-limited editions are not marked open-ready.
- Android instrumentation tests include multiple edition isolation.
- Exact-head Android CI result is pending.

No scripture payload is committed. No merge to `main`, signing, deployment or publication is authorized.
