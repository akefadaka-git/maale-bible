# A4.1 — Canonical Identity Reconciliation

**Assessment date:** 2026-11-09  
**Source:** 2008 Maale Full Bible — MDYTWF / mdyeth  
**Canonical registry:** `maale-bible:canon:66-v1`  
**Disposition:** **TECHNICAL SOURCE-TO-CANON RECONCILIATION — PASS; VERIFIED-CLOSED WITH EXPLICIT LINGUISTIC REVIEW EXCEPTION**

## 1. Rights and authorization disposition

The Owner supplied written rights-holder confirmation attributed to Dr. Tesfaye Yohannes, East African representative of The Word for the World International. It identifies the 2008 MDYTWF / mdyeth Maale Full Bible, joint copyright holders The Word for the World International and the Bible Society of Ethiopia, and CC BY-SA 4.0 coverage of the eBible USFM package and supporting files. It expressly covers offline storage, display, search, bookmarks, annotations and user sharing, subject to attribution, licence notice, indication of changes and ShareAlike for adaptations.

**Rights gate: PASS — DOCUMENTARY, OWNER-AUTHORIZED.** No further permission request or separate DBL agreement is required for this edition/package and the stated app uses. Application attribution and licence/ShareAlike controls remain implementation requirements. The separate 2007 MDYBSE New Testament is not the selected source.

## 2. Immutable source identity

| Field | Verified value |
|---|---|
| Edition | 2008 Maale Full Bible |
| Publisher identifier | MDYTWF |
| eBible identifier | mdyeth |
| Source package | `mdyeth_usfm.zip` |
| Package size | 1,809,863 bytes |
| Package SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| Source content revision | 2025-05-13 |
| Licence | CC BY-SA 4.0 |

The prior isolated A2 source audit records ZIP integrity and safe extraction PASS, a good GPG signature, all 66 signed USFM hashes matching, and an independent second-pass reproduction. GPG keyring trust remains undefined; the signature result is not represented as independent identity certification.

## 3. Book identity and ordering reconciliation

The source per-file hash manifest was joined to both the Owner-approved A2 expected inventory and the frozen canon registry by exact USFM book ID.

| Check | Result |
|---|---:|
| Source USFM file records | 66 |
| Unique source book IDs | 66 |
| Frozen registry books | 66 |
| Approved inventory books | 66 |
| Missing source IDs | 0 |
| Unexpected source IDs | 0 |
| Duplicate source IDs | 0 |
| Filename-to-USFM-ID mismatches | 0 |
| Ordered source-manifest / registry parity | 66/66 |

**Finding:** Exact set equality and canonical-order equality PASS. The 66 source file records are now represented in registry order in `content/manifests/maale-2008-source-manifest.json`.

Important: ZIP member enumeration and filename prefixes are not treated as canonical order. The source manifest explicitly joins each source ID to the frozen registry. This avoids mistaking archive packaging order for Bible book order.

## 4. Chapter and verse structural reconciliation

The previously executed isolated source audit recorded:

| Structural check | Observed result |
|---|---:|
| Total chapters | 1,189 |
| Chapter sequences | Contiguous from 1 in every book |
| Chapters without verse markers | 0 |
| USFM verse-marker lines | 30,506 |
| Represented verse spans, including ranges | 31,102 |
| Verse-range marker instances | 238 |
| Verse ordering / overlap anomalies | 0 |

The canonical identity contract uses:

`maale-bible:canon:66-v1/<BOOK>/<CHAPTER>/<VERSE>[/<SEGMENT>]`

Source-native book/chapter/verse references are retained as edition references. The source structural audit supports positive, ordered source coordinates and the canonical book identity join. It does **not** prove that all other editions share Maale versification, nor does it create 31,102 approved cross-edition mapping records. Verse ranges and segmentation must remain explicit in the importer; no nearest-verse or ordinal fallback is permitted.

## 5. Encoding and integrity diagnostics

- Strict UTF-8: 66/66 files.
- Unicode replacement characters: 0.
- Non-NFC files: 0.
- Unexpected control characters: 0.
- Per-file SHA-256 entries: 66/66 matched the signed hash list in the isolated A2 audit.
- Independent second-pass archive/hash/structural reproduction: PASS.

## 6. Automated verification added

- `content/manifests/maale-2008-source-manifest.json` — metadata-only source manifest, per-file hashes and registry-ordered book records; no scripture text.
- `verification/canon/maale_source_canon_reconcile.py` — deterministic verifier for exact source/registry/inventory set equality, canonical ordering, hash metadata shape, chapter/verse aggregate diagnostics and encoding results.
- CI integration runs this verifier alongside canonical registry and P2 host checks.

## 7. Explicit exclusions and residual risks

- Maale linguistic, translation-quality, book-name orthography and Ethiopic-script specialist review remain **DEFERRED**, as previously Owner-accepted. No specialist sign-off is claimed.
- The source archive's chapter/verse aggregates are source structural evidence, not a per-book canonical versification table.
- Cross-edition mappings (EXACT/SPLIT/MERGED/PARTIAL/UNMAPPED) still require edition-specific provenance and review.
- Raw scripture text and generated scripture databases are not included in this metadata-only reconciliation commit.
- This A4.1 closure does not authorize merge, release signing, publication or deployment.

## 8. Final disposition

**A4.1 TECHNICAL CANONICAL IDENTITY RECONCILIATION: VERIFIED-CLOSED WITH EXPLICIT LINGUISTIC REVIEW EXCEPTION.**

The selected 2008 Maale source has 66/66 exact book IDs, no missing/unexpected/duplicate IDs, and an exact ordered match after joining by USFM ID to the frozen 66-book registry. The source audit's chapter/verse structural diagnostics pass. Canon v1 remains frozen; any future canon membership change requires a new version.
