# Maale scripture importer — private candidate package

The build-time importer creates a separate SQLite scripture package. It does not write to the encrypted personal-study database and does not bundle source USFM in the Android APK.

## Source pin
- Edition: 2008 Maale Full Bible (MDYTWF / mdyeth)
- Archive size: 1,809,863 bytes
- Archive SHA-256: 4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11
- Inventory: 66 books in canonical Owner-approved order
- Reported GPG signature: cryptographically good; signer trust remains undefined.

## Run
Keep the source ZIP and A2 evidence manifest in a private, access-controlled workspace. Do not commit either file.

    python tools/scripture/scripture_importer.py --source /secure/intake/mdyeth_usfm.zip --manifest /secure/intake/maale-source-evidence-manifest.json --output /secure/build/maale-mdytwf-2008.sqlite

The importer verifies exact archive size and SHA-256, manifest edition/licence/signature fields, ZIP safety and CRC, all 66 per-file hashes, book identity/order, strict UTF-8/NFC, chapter continuity and supported USFM structure. Unknown markers fail closed.

The generated database has edition-aware passage identity (edition_id, book_id, chapter_number, verse_label). Verse labels remain strings to preserve suffixes and ranges. Source path/hash/line are retained. Headings are stored separately.

## Initial marker profile
The parser recognizes explicit structural, paragraph, heading, chapter, verse, inline-character, alternate-label, word-attribute and note marker sets in scripture_importer.py. Footnotes and cross-references are excluded from displayed verse text. Inline character styles are removed while visible text is retained. Unknown markers or malformed references stop the import. This is a bounded first profile, not a claim of complete USFM 3.x support. Extend only from observed source markers, with a fixture and rendering decision for each addition.

## Package/data separation
- Scripture package: immutable, edition-scoped content in a separate SQLite file.
- Personal study database: existing encrypted SQLCipher database; bookmarks, notes, highlights and reading positions remain separate.
- Passage links include edition identity. Existing study references need a compatibility mapping before runtime integration.
- Candidate database is read-only at the filesystem level and explicitly not release-eligible.

## Release boundary
Package metadata records signer trust as UNDEFINED_IN_ISOLATED_KEYRING and release_eligible=false. This importer does not establish signer identity trust, independent linguistic correctness or final licence compliance. Do not ship until those gates are separately satisfied.
