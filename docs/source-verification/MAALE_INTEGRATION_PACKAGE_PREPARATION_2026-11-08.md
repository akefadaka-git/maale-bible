# Maale Source Package — Integration Preparation Record

**Record date:** 2026-11-08  
**Repository:** `akefadaka-git/maale-bible`  
**Branch:** `feat/a4-offline-study-suite`  
**Owner authorization:** `content_intake_authorized=true` (2026-11-08)  
**Package:** 2008 Maale Full Bible — MDYTWF / mdyeth  
**Preparation status:** **SOURCE IDENTITY AND RIGHTS RECONFIRMED; PRIOR PACKAGE INTEGRITY EVIDENCE REPRODUCIBLE FROM RETAINED MANIFEST; PHYSICAL RE-ACQUISITION / STAGING PENDING**

## 1. Authoritative source identity

- Title: ጌኤዦ ማፃኣፖ — Maale Bible (Ethiopic script)
- Language: Maale (mdy)
- Publisher identifier: MDYTWF
- eBible identifier: mdyeth
- Edition: 2008 Maale Full Bible
- Rights holders: The Word for the World International; The Bible Society of Ethiopia
- Official edition page: https://www.ebible.org/Scriptures/details.php?id=mdyeth
- Exact USFM package URL: https://ebible.org/Scriptures/mdyeth_usfm.zip
- Source revision shown on current official edition page: 2025-05-13
- Do not substitute MDYBSE / 2007 Maale New Testament.

The current official eBible edition page was rechecked on 2026-11-08. It identifies MDYTWF / mdyeth as the 2008 Maale Full Bible, lists the USFM package, names both rights holders, and states CC BY-SA 4.0. The page permits redistribution and reasonable adaptations subject to attribution, modification notice and ShareAlike. Images bundled with scripture documents are separately restricted and are not included in this approval.

## 2. Rights and attribution

**Text licence:** Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0), as stated on the current official edition page and corroborated by the Owner-supplied rights-holder response recorded in `RIGHTS_EDITION_RECONCILIATION_2026-11-07.md`.

Required attribution:

> Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.

Required controls:
1. Link to https://creativecommons.org/licenses/by-sa/4.0/.
2. Identify the source edition and rights holders in the in-app edition information.
3. Record all transformations (USFM parsing, formatting, normalization, indexing and database generation).
4. Indicate modifications and do not imply rights-holder endorsement.
5. Distribute adaptations of the scripture text under CC BY-SA 4.0.
6. Do not assume the text licence covers third-party images, audio, fonts, maps or other bundled assets.

**Documentary caveat:** The project has the Owner-supplied rights-holder response text, but its original email headers and signatory authority were not independently authenticated. The current official eBible page independently corroborates the edition identity and public CC BY-SA 4.0 statement. Preserve this caveat in the source register.

## 3. Previously verified archive baseline

The exact Owner-provided archive inspected on 2026-11-07 had:

| Evidence | Recorded result |
|---|---:|
| Archive filename | `mdyeth_usfm.zip` |
| Archive size | 1,809,863 bytes |
| Archive SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| ZIP entries | 70 |
| Uncompressed bytes | 7,554,174 |
| ZIP integrity / CRC | PASS |
| Unsafe paths, traversal, normalized duplicates | 0 |
| Symlinks / special entries / encrypted entries / nested archives | 0 |
| GPG clear signature | GOOD SIGNATURE |
| Signing-key fingerprint | `54D714D76956DA6673DC1EEA5F62009D93505F26` |
| Signed hash-list entries | 68 |
| USFM signed hashes matched | 66/66 |
| GPG trust in isolated keyring | UNDEFINED; do not claim key-owner trust certification |

The retained per-file hash manifest contains 66 USFM entries, with a combined file-size total of 7,511,132 bytes. The manifest file itself has SHA-256:

`d59c5c284148816b9cee23b6c1aa4d6a81dcc364e063aa9a6a361e3df330adaa`

It is retained in the user's Library as `maale-usfm-file-hashes.json`. It contains file paths, book IDs, byte lengths and hashes, not scripture text.

Previously verified content structure:
- 66/66 approved book IDs exactly once; no missing, unexpected or duplicate books.
- 1,189 chapters; all chapter sequences contiguous from 1.
- 30,506 verse-marker lines; 31,102 represented verse spans; 238 verse-range marker instances.
- No verse-order/overlap anomalies.
- 66/66 strict UTF-8; zero replacement characters, non-NFC files or unexpected controls.
- 2,108,248 Ethiopic-range code points.
- Independent second-pass CRC, hash and aggregate count reproduction passed.

These figures are historical evidence for the exact archive with the recorded digest. They are not a claim that a newly downloaded archive has been rehashed in this preparation session.

## 4. Current-session re-confirmation and limitation

On 2026-11-08 the official HTML edition page was successfully re-read. The linked ZIP endpoint was exposed, but the web retrieval service rejected the binary ZIP content type, and the runtime download attempt did not produce a local archive. The original uploaded ZIP is not present among the currently accessible conversation or Library files; only the A2 reports and hash manifests are retained.

Therefore:
- Current official edition identity and displayed licence: **RECONFIRMED**.
- Prior exact archive integrity evidence: **RETAINED / HISTORICALLY VERIFIED**.
- New archive download and digest comparison in this session: **NOT RUN — BINARY NOT AVAILABLE**.
- Safe extraction and fresh per-file rehash in this session: **NOT RUN**.
- Content database / FTS index generation: **NOT RUN**.
- Android integration and runtime validation: **NOT RUN**.

Do not mark the physical source package as freshly staged or integrated until the exact archive is reacquired into isolated, non-public storage and its SHA-256 matches the baseline above.

## 5. Integration package contract

**Approved input:** exact `mdyeth_usfm.zip` package matching archive SHA-256 `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.

**Staging location:** private, access-controlled content workspace outside the public repository. Never commit raw USFM, extracted scripture, verse excerpts, generated scripture databases or derived verse datasets to this public repository unless a separately documented distribution decision and licence review explicitly permit it.

**Required pipeline:**
1. Acquire the exact official USFM ZIP to a new isolated workspace.
2. Verify byte size and archive SHA-256 against the baseline; stop on mismatch.
3. Verify ZIP CRC and run safe archive preflight before extraction.
4. Verify the included clear-signature; record cryptographic validity separately from key trust.
5. Extract only into a new empty directory with traversal, symlink, duplicate-path, nested-archive, encryption and decompression-bomb controls.
6. Compare all 66 USFM file hashes and byte lengths against the retained per-file manifest.
7. Re-run book inventory, chapter/verse structure, UTF-8, NFC and control-character checks.
8. Preserve original USFM source bytes. Do not rewrite or normalize scripture source text in place.
9. Generate a versioned, deterministic import artifact and database/index from a separately versioned transformation pipeline.
10. Record source digest, per-file hashes, parser version, schema version, transformation parameters and output digests in provenance metadata.
11. Verify attribution/licence presentation in the app and confirm CC BY-SA treatment of adapted scripture text.
12. Run Android offline reader, search, passage mapping and data-integrity tests against the staged content.
13. Keep Maale linguistic/orthographic specialist review explicitly marked DEFERRED until qualified review occurs.

## 6. Integration readiness decision

| Gate | Disposition |
|---|---|
| Owner content-intake authorization | PASS — GENERAL AUTHORIZATION |
| Edition identity | PASS — RECONFIRMED |
| Current official text licence | PASS — CC BY-SA 4.0 DISPLAYED |
| Documentary rights-holder response | PASS — DOCUMENTARY; AUTHENTICATION CAVEAT |
| Prior archive CRC / signature / 66-file hash checks | PASS — PRIOR EXACT ARCHIVE |
| Current archive reacquisition and SHA match | **BLOCKED — PACKAGE BYTES UNAVAILABLE IN THIS SESSION** |
| Fresh extraction and file-level revalidation | NOT RUN |
| Linguistic/orthographic specialist review | DEFERRED |
| Content transformation / offline DB generation | NOT RUN |
| App integration and runtime verification | NOT RUN |

**Disposition:** The Maale source is approved to proceed through the controlled intake pipeline under the Owner's 2026-11-08 authorization. The exact package specification, rights metadata, attribution, prior validation evidence and integration contract are prepared. The physical source package is **not yet staged** because the binary archive could not be reacquired in this session. Do not represent it as integrated or release-ready.

## 7. Related evidence

- A2 validation report: `docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md`
- Rights reconciliation: `docs/source-verification/RIGHTS_EDITION_RECONCILIATION_2026-11-07.md`
- Owner authorization: `docs/governance/OWNER_CONTENT_INTAKE_AUTHORIZATION_2026-11-08.md`
- Multilingual discovery: `docs/source-verification/MULTILINGUAL_SOURCE_DISCOVERY_2026-11-08.md`
- Current implementation plan: `docs/project/CURRENT_IMPLEMENTATION_PLAN_2026-11-08.md`

No merge, signing, deployment or publication is authorized by this preparation record.
