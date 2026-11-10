# A4 Android Private Offline Content Import — 10 November 2026

**Branch:** `feat/a4-android-offline-content-import`  
**Base:** A4 parser/private-database branch `feat/a4-maale-usfm-content-pipeline`  
**Target edition:** 2008 Maale Full Bible — MDYTWF / mdyeth

## 1. Implemented Android service boundary

Added `OfflineScriptureStore` using the existing AndroidX SQLite bundled driver. No new Gradle dependency was introduced.

The service provides:
- A typed content descriptor binding edition identity, source archive SHA-256, database SHA-256/size, schema/parser versions, book/chapter/verse/FTS counts and tokenizer qualification.
- Bounded stream import into app-private `filesDir/offline-content`.
- Streaming SHA-256 and byte-size validation before opening the staged database.
- Database metadata, schema version, 66-book, chapter, verse and FTS count checks.
- SQLite quick integrity check and FTS5 external-content integrity check during installation.
- Staging directory plus same-filesystem atomic directory rename.
- Previous-content backup and rollback if the replacement move fails; interrupted replacement recovery on next open.
- App-start integrity recheck using the private descriptor and database SHA-256.
- Read/search APIs that retain source-native `book_id`, chapter and verse reference; no unreviewed canonical verse mapping is inferred.
- FTS query length and result-limit bounds; query text is NFKC-normalized separately from stored display text.

## 2. Security and content boundaries

- The content database is never placed in APK assets, the public repository, or shared external storage by this implementation.
- The import stream is copied only into app-private staging, validated, then atomically activated.
- Invalid size, hash, metadata, schema or FTS state fails before activation; the prior installed database remains active.
- The descriptor provides corruption/integrity checking, not a digital signature or independent publisher authentication. Authenticity of future update packages still requires a trusted delivery/manifest-signing decision.
- FTS5 `unicode61` remains an engineering baseline, not a linguistically qualified Maale/Amharic tokenizer.
- Existing study-note database is untouched and remains a separate store.

## 3. Instrumentation coverage

Synthetic-only Android tests cover:
- Valid private installation and app-private reopen.
- FTS5 search and exact edition-native passage lookup.
- Invalid database digest rejection without replacing the active database.
- Source metadata mismatch rejection without replacing the active database.

The tests create their own synthetic database; no licensed scripture text or production corpus is bundled in test assets.

## 4. Not included yet

- No durable real Maale content package is retained; the prior real-source CI database was ephemeral and deleted.
- No UI import flow or content download/update UI is enabled.
- No reader/search screen integration is enabled in this slice.
- No cross-edition passage mappings, bookmarks/highlights tied to source references, or content migrations are introduced.
- No Android device/API matrix result is claimed until exact-head CI completes.

## 5. Next

Run the Android instrumentation matrix on API 26/28/30/33/35/36. After it passes, wire the installed store into the reader and search UI with explicit no-content/installed-content states, then create a separately verified private content package and exercise the end-to-end import on devices.

No merge to `main`, signing, deployment or publication is authorized by this work.
