# Source Verification Procedure

## 1. Authorized source and edition

Target: 2008 Maale Full Bible, MDYTWF / mdyeth, language mdy, Ethiopic script. Exact USFM package: `https://ebible.org/Scriptures/mdyeth_usfm.zip`.

The Owner has reaffirmed full authorization for acquisition of this exact archive into an isolated, non-public workspace, hashing, ZIP metadata preflight, safe extraction, inspection of USFM structure/encoding, structural and Unicode validation, specialist review, and creation of the immutable A2 baseline. Do not ask again for authorization for these listed A2 operations.

This does not authorize committing raw scripture content to the public repository, production content intake/distribution, audio or third-party assets, or A3 implementation.

## 2. Rights and edition evidence

The Owner supplied a written rights-holder response stating that MDYTWF / mdyeth is the 2008 Full Bible jointly held by The Word for the World International and the Bible Society of Ethiopia, and that the exact eBible USFM package is covered by CC BY-SA 4.0.

The official eBible edition record independently lists:
- 2008 Maale Full Bible, MDYTWF / mdyeth.
- Copyright attribution to both rights holders.
- CC BY-SA 4.0 licence terms.
- The USFM format and `mdyeth_usfm.zip` package.

The distinct 2007 MDYBSE Maale New Testament remains a separate work and its catalogue status must not be applied to MDYTWF.

## 3. Approved canon and expected inventory

The official edition-specific PDF directory at `https://ebible.org/pdf/mdyeth/` enumerates one book-level PDF for each of 66 USFM book identifiers, in order from GEN through REV, with no additional deuterocanonical book entries. The Owner-approved A2 expected book inventory is recorded in [CANDIDATE_CANON_INVENTORY.md](CANDIDATE_CANON_INVENTORY.md) and its JSON companion.

Validate the actual USFM archive against this inventory. Any mismatch is a validation failure requiring investigation; do not silently modify the approved inventory. Maale book-name orthography remains subject to language specialist review.

## 4. Acquisition and chain of custody

Download only the exact package URL above into an isolated workspace outside the repository checkout. Record:
- Exact source URL and UTC retrieval timestamp.
- Original filename, content type and byte size.
- SHA-256 of the untouched archive before processing.
- Tool/runtime versions and exact commands.
- Storage location and access controls.
- Every extracted path, size, format and SHA-256.
- Custody events and validation report identifiers.

Preserve the original archive unchanged. Never commit the archive or extracted scripture files to the public repository.

## 5. ZIP preflight and safe extraction

Run:

    python tools/archive_preflight.py mdyeth_usfm.zip --output preflight-report.json

The tool reads ZIP central-directory metadata and hashes the archive; it does not extract files or emit member contents. It fails closed on traversal, absolute/drive-qualified/backslash paths, duplicate normalized paths, symlinks/special files, encryption, nested archives, resource limits and corrupt archives.

Only after PASS, extract into a new empty isolated directory with path-containment checks and no overwrite. Never execute archive members or extract into a repository checkout.

## 6. Structural and character validation

1. Inventory every package member and hash it.
2. Validate each USFM book ID against the approved 66-book inventory; fail on missing, extra, duplicate or out-of-order books.
3. Parse USFM markers and validate chapter/verse structure, duplicate markers and numeric continuity where supported by trusted references.
4. Record strict decoding, declared/detected encoding, replacement characters, control/format/private-use/unassigned characters, Unicode normalization observations and Ethiopic code-point diagnostics.
5. Never normalize original source bytes in place.
6. Complete Maale orthography and Ethiopic-script review with named specialists.
7. Independently reproduce the technical diagnostics.
8. Build and freeze the immutable source manifest only after all findings are resolved or explicitly dispositioned.

Logs and public repository records must avoid verse excerpts and scripture text; use issue codes, counts, paths and code-point identifiers.

## 7. Current retrieval result

The official edition record and 66-book PDF index were retrieved. The exact USFM ZIP endpoint was then attempted, but the execution environment could not resolve/reach the eBible host. Browser automation was unavailable because its connected wallet reported an insufficient balance; no payment is requested from the Owner. Therefore no archive bytes, SHA-256, extraction inventory or real-source validation result are claimed. Resume by retrieving the same exact URL; do not substitute MDYBSE or a different edition/package.

## 8. Synthetic evidence and phase status

- ZIP metadata preflight suite: 14/14 synthetic tests PASS.
- USFM audit suite: 12/12 synthetic tests PASS.
- Candidate inventory JSON: 66 unique IDs, contiguous order 1–66.
- Real archive preflight, extraction and audit: NOT RUN — technical retrieval blocker.
- Real-source character validation and specialist review: NOT RUN.
- Immutable baseline: NOT CREATED.
- CI/workflow: NOT RUN.

See [A2 Completion and Blocker Register](A2_COMPLETION_AND_BLOCKER_REGISTER.md). A2 remains open until real-source validation and immutable baseline evidence pass. A1 remains CLOSED — FINAL.
