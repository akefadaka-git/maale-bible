# Real Maale USFM Source Revalidation — 10 November 2026

**Project:** Maale Bible (ጌኤዦ ማፃኣፖ)  
**Repository:** `akefadaka-git/maale-bible`  
**Target edition:** 2008 Maale Full Bible — MDYTWF / mdyeth  
**Language/script:** Maale (mdy), Ethiopic  
**Source package:** `mdyeth_usfm.zip`  
**Disposition:** **REAL SOURCE INTEGRITY PASS — SIGNER TRUST CAVEAT RETAINED**

## 1. Authority and distribution boundary

The source is covered by the Owner's content-intake authorization and recorded CC BY-SA 4.0 evidence. The named rights holders are The Word for the World International and the Bible Society of Ethiopia. Required attribution and ShareAlike treatment remain mandatory.

The exact source archive and extracted scripture files remain in private staging. No archive, USFM source, verse text, generated scripture database or full-text index is committed to this public repository by this change.

This revalidation does not authorize public redistribution by itself, establish linguistic quality, or authenticate the legal identity of the signing key owner.

## 2. Exact archive revalidation

| Check | Result |
|---|---|
| Filename | `mdyeth_usfm.zip` |
| Archive bytes | 1,809,863 |
| SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| Expected baseline SHA-256 | Exact match |
| ZIP entries | 70 |
| Uncompressed bytes | 7,554,174 |
| ZIP CRC test | PASS |
| Expected USFM files | 66 |
| USFM file hashes and byte lengths | 66/66 PASS |
| Missing / unexpected / duplicate USFM members | 0 / 0 / 0 |
| Unsafe paths, traversal, duplicate normalized paths | 0 |
| Encrypted, symlink or special-file entries | 0 |
| Nested archive members | 0 |

The four non-USFM members are package support material (copyright notice, public key, signature and font stylesheet). They were not imported as scripture text.

## 3. Canonical inventory and structural diagnostics

The 66 unique source USFM identifiers match the frozen project-owned 66-book registry. ZIP member order and source filename prefixes are not treated as canonical order; importer ordering must be derived from `content/canon/66-v1/registry.json`.

| Diagnostic | Result |
|---|---:|
| Unique book IDs | 66 |
| Chapters | 1,189 |
| Chapter sequences contiguous from 1 | PASS |
| Verse-marker lines | 30,506 |
| Represented verse spans, including ranges | 31,102 |
| Verse-range marker instances | 238 |
| Verse order / overlap / descending-range anomalies | 0 |
| Strict UTF-8 decode | 66/66 PASS |
| Unicode replacement characters | 0 |
| Non-NFC files | 0 |
| Unexpected control characters | 0 |
| Ethiopic-range code points | 2,108,248 |

These are structural and encoding diagnostics only. They do not establish Maale translation accuracy, orthography, semantic correctness, or specialist acceptance.

## 4. Package signature

The included clear-signature was verified cryptographically with GnuPG against the included public key.

- Signature result: **GOOD SIGNATURE / VALIDSIG**.
- Fingerprint: `54D714D76956DA6673DC1EEA5F62009D93505F26`.
- Signature timestamp: 30 October 2026.
- GnuPG trust: **UNDEFINED**.

A cryptographically valid signature proves that the signed package hash list matches the included signing key. It does not independently establish that the key belongs to the rights holder or that the key identity is trusted. This caveat remains explicit.

## 5. Chain-of-custody and processing boundary

The exact archive was materialized from the Owner's existing Library file into an isolated private working area. Archive and per-file hashes were computed from the bytes; ZIP CRC and metadata safety checks were run before USFM inspection. Only the 66 expected USFM members were extracted into a new private staging directory for diagnostics.

No scripture excerpts or verse text are included in this report or public CI output. Private staging is not a release artifact and is not a public repository payload.

## 6. Gate disposition

| Gate | Status |
|---|---|
| Owner source authorization | PASS |
| Edition identity and stated CC BY-SA 4.0 metadata | RECONFIRMED |
| Exact archive SHA-256 | PASS |
| ZIP CRC and safe-member preflight | PASS |
| Per-file SHA-256 / byte-size reconciliation | 66/66 PASS |
| Frozen 66-book identifier reconciliation | PASS |
| Structural and Unicode diagnostics | PASS |
| Package signature cryptographic validity | PASS |
| Signing-key owner trust | UNRESOLVED / UNDEFINED |
| Maale linguistic/orthographic specialist acceptance | NOT RUN |
| Marker-aware verse parser qualification | NEXT |
| Private deterministic SQLite content database | NOT RUN |
| FTS5 tokenizer/language qualification | NOT RUN |
| Android content-resource integration | NOT RUN |

**Source revalidation result:** PASS WITH SIGNER-TRUST AND LINGUISTIC-REVIEW CAVEATS.

## 7. Next implementation sequence

1. Implement a marker-aware USFM parser using synthetic fixtures; exclude footnotes, cross-references and figure metadata from verse search text while preserving immutable source bytes and source-native verse labels.
2. Parse the private source and reconcile per-book, chapter, verse-marker and verse-range counts against this report without printing verse text.
3. Build a deterministic, versioned SQLite content database and separate FTS5 index in private staging. Keep source-native edition references; do not infer cross-edition verse equivalence.
4. Rebuild the database twice and compare output digests; verify source-to-record provenance and index counts.
5. Qualify FTS5 runtime compatibility on the Android API matrix. Treat `unicode61` as an engineering baseline, not a linguistically qualified Maale or Amharic tokenizer.
6. Integrate only through the versioned Android offline-content contract after parser/database verification.

**No merge to `main`, signing, deployment or publication is authorized by this record.**
