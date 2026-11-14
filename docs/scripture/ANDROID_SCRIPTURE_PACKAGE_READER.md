# Android Maale scripture package reader

## Current package contract

The Android reader consumes the separate SQLite candidate emitted by the controlled scripture importer:

- Package format: `MAALE-SCRIPTURE-SQLITE-1`
- Edition: `MDYTWF-2008-MAALE-FULL`
- Source identifier: `mdyeth`
- Language: `mdy`
- Source archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`
- Source archive size: `1,809,863` bytes
- Licence: CC BY-SA 4.0
- Expected inventory: 66 books, 1,189 chapters and 30,506 verse-marker records.

The importer schema and source contract are maintained in PR #61. The source-intake validation foundation is maintained separately in PR #62.

## Android installation behavior

1. The user selects a separately generated SQLite package through Android's document picker.
2. The app copies it to app-private storage with a 64 MiB size limit.
3. The app checks the SQLite header, application ID, schema version, integrity, foreign-key consistency, exact table inventory, edition metadata, source archive hash, licence and attribution.
4. The app checks the complete canonical 66-book order and the expected chapter/verse-marker totals.
5. The validated database is installed atomically and marked read-only.
6. The reader opens it read-only and queries one book/chapter at a time.
7. The package is never merged into the encrypted personal-study database.

## Release boundary

The current importer output explicitly declares:

- `signer_trust = UNDEFINED_IN_ISOLATED_KEYRING`
- `release_eligible = false`
- `content_is_immutable = true`

Accordingly, import and reading are enabled only in debuggable Android builds. The UI identifies the package as an unsigned internal-review candidate and displays the source attribution and licence. The package is not embedded in this public repository, and no scripture text or generated SQLite database is committed here.

This Android-side validation checks the generated database contract. It does not replace the upstream source-archive signature validation, independent source review, signing-key trust establishment, theological/editorial review or Owner release approval. Release support remains disabled until those gates are separately completed.
