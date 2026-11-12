# Source Verification Procedure

## 1. Selected source and A2 disposition

Target: 2008 Maale Full Bible, `MDYTWF / mdyeth`, language `mdy`, Ethiopic script. Exact USFM package: `https://ebible.org/Scriptures/mdyeth_usfm.zip`.

**A2 source-readiness status:** CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION. Owner acceptance was recorded on 2026-11-07; the original archive was independently rechecked on 2026-11-12.

The Owner authorized acquisition of this exact archive into an isolated, non-public workspace, hashing, ZIP metadata preflight, safe extraction, inspection of USFM structure/encoding, structural and Unicode validation, specialist review, and creation of the immutable A2 baseline. No further authorization request is required for those listed A2 operations.

This does not authorize committing raw scripture content to the public repository, production content intake/distribution, audio or third-party assets, or production release.

## 2. Edition and rights evidence

The Owner supplied a written rights-holder response stating that MDYTWF / mdyeth is the 2008 Full Bible jointly held by The Word for the World International and the Bible Society of Ethiopia, and that the exact eBible USFM package is covered by CC BY-SA 4.0. The response also distinguishes the separate 2007 MDYBSE Maale New Testament.

The official eBible edition record corroborates the 2008 Full Bible, identifiers, rights holders, public licence and package. Original email headers and signatory authority have not been independently authenticated in this repository; this remains a documentary caveat.

## 3. Approved inventory and source baseline

The Owner-approved 66-book expected inventory is recorded in [CANDIDATE_CANON_INVENTORY.md](CANDIDATE_CANON_INVENTORY.md) and `content/manifests/candidate-book-inventory.json`.

The accepted metadata-only baseline is:
- `content/manifests/maale-source-baseline-manifest.json`
- `content/manifests/maale-usfm-file-hashes.json`

The baseline records the exact archive digest, signed checksum evidence, per-file hashes and structural/encoding metadata. It contains no scripture text. ZIP member order is non-semantic; application display order follows the Owner-approved inventory.

## 4. Acquisition and chain of custody

- Exact archive: `mdyeth_usfm.zip`.
- Owner-provided upload; acquired/inspected 2026-11-07.
- Archive size: 1,809,863 bytes.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Source content revision: 2025-05-13.
- Package generation: 2026-10-30 05:20:45 UTC.
- Preserve the original archive unchanged in restricted, non-public storage.
- Never commit the archive or extracted scripture files to the public repository.

## 5. ZIP preflight, signature and integrity

The archive contains 70 entries: 66 USFM files and four supporting files. ZIP CRC/integrity passed. The package's clear-signed checksum list has 68 entries; GPG reports GOOD SIGNATURE. All 68 signed checksums match, including 66/66 USFM files.

Signing-key fingerprint: `54D714D76956DA6673DC1EEA5F62009D93505F26`. GPG trust is UNDEFINED in the isolated keyring; cryptographic validity is confirmed, signer identity certification is not.

## 6. Structural and character validation

- 66/66 approved book IDs; missing, extra and duplicate IDs: zero.
- 1,189 chapters, contiguous from chapter 1 in every book.
- 30,506 verse-marker lines; 31,102 represented verse spans including ranges.
- Zero chapters without verse markers.
- Strict UTF-8 valid in 66/66 files.
- Zero replacement characters, non-NFC files and unexpected control characters.
- Ethiopic-range code points: 2,108,248.

The independent recheck on 2026-11-12 reproduced the archive SHA-256, ZIP CRC, signed checksums, per-file hashes, inventory and aggregate structure. These diagnostics do not establish translation accuracy, Maale orthography, linguistic quality or theological correctness.

## 7. Specialist review and residual risks

The named Maale-language/Ethiopic-script specialist review remains **DEFERRED — OWNER-ACCEPTED RESIDUAL RISK**. No specialist review or sign-off is claimed. The Owner accepted this exception on 2026-11-07.

The original rights-holder email headers and signatory authority remain a documentary caveat. The GPG signing-key trust state remains UNDEFINED. Product/release implementation of attribution, modification notices, ShareAlike treatment, export notices and separate asset clearance remains governed by `docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md`.

## 8. Binding restrictions

- `content_intake_authorized=false` remains in force.
- No raw scripture, source archive, USFM/USX, excerpts or derived scripture datasets in the public repository.
- No production ingestion or distribution without separate Owner authorization.
- Audio and third-party assets remain separately rights-gated.
- No unreviewed source transformations.
- A2 closure does not authorize signing, release, deployment or publication.

## 9. Final closure record

See [A2 Final Source-Readiness Closure Record](A2_FINAL_CLOSURE_RECORD_2026-11-12.md) and [A2 Completion and Blocker Register](A2_COMPLETION_AND_BLOCKER_REGISTER.md).
