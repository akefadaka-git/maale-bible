# A3.2 — Maale scripture offline-import pipeline contract

**Selected source:** 2008 Maale Full Bible, `MDYTWF / mdyeth`  
**Verified archive SHA-256:** `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`  
**Observed archive size:** 1,809,863 bytes  
**Baseline:** four archived copies produced the same SHA-256  
**Content boundary:** no raw scripture, USFM, source archive or generated scripture database is added by this contract.

## 1. Current evidence and unresolved rights

Previously recorded technical intake checks:

- ZIP integrity passed; 70 entries were observed.
- 66 USFM files and 68/68 signed checksum entries were verified.
- 66 unique book IDs matched the Owner-approved 66-book inventory and catalogue order.
- 1,189 chapters and 30,506 verse-marker lines were observed.
- Strict UTF-8 decoding and NFC observation passed; no replacement characters or unexpected control/format characters were found.
- The GPG signature is cryptographically valid, but signer identity trust is **UNDEFINED**. Do not represent this as independently identity-verified.
- CC BY-SA 4.0 was reported for the selected source. Edition-specific licence and release compliance still require documentary confirmation.

The source package is technically characterized but is not yet an integrated, releasable scripture asset.

## 2. Importer contract

The importer must be deterministic, fail closed and operate on a local, immutable input archive. It must never fetch content from an unverified network location.

### Stage I0 — Intake identity

- Require an explicit input path and expected SHA-256.
- Calculate the archive SHA-256 before parsing.
- Reject any mismatch; do not silently accept a replacement archive.
- Record archive size, hash, intake timestamp, operator and source identifier in a provenance manifest.
- Keep the source archive outside the public Git repository and build artifacts unless a separately approved rights decision authorizes distribution.

### Stage I1 — Signature and provenance

- Verify the detached signature using GPG and preserve the verification output, signing-key fingerprint and keyring provenance.
- Separate cryptographic signature validity from signer identity trust.
- Keep identity trust UNRESOLVED until an authoritative publisher or rights-holder channel confirms the signing identity and edition provenance.
- Record source title, publisher/rights-holder, edition, language identifier, acquisition route, licence text/version and any written permissions.

### Stage I2 — Archive safety

- Reject absolute paths, parent-directory traversal, symlinks, duplicate archive paths and unsupported entry types.
- Enforce archive-entry count, per-entry size, total uncompressed size and compression-ratio limits.
- Reject unexpected executable or unrelated payloads.
- Verify the archive CRC for every entry before extraction or parsing.
- Parse entries in memory or a controlled temporary directory; never extract to an untrusted path.

### Stage I3 — Inventory and canonical order

- Require exactly 66 unique canonical book IDs.
- Compare IDs and order against the Owner-approved inventory, not filename guesses.
- Reject missing, duplicate, unexpected or reordered books unless an explicit versioned inventory change is approved.
- Record per-book source filename, ID, title metadata, byte length and content hash.
- Keep catalogue metadata distinct from scripture text.

### Stage I4 — Text and USFM validation

- Decode every source file as strict UTF-8; reject replacement decoding.
- Record Unicode normalization results. Do not silently rewrite source text; any normalization must be an explicit, reproducible transformation with before/after hashes.
- Validate expected USFM book IDs, chapter markers and verse markers.
- Check chapter and verse-marker totals against the verified intake baseline (1,189 chapters; 30,506 verse-marker lines) and investigate every difference.
- Detect duplicate or malformed markers, empty required content, invalid references and unsupported structural constructs.
- Preserve source line/marker provenance so each imported passage can be traced to its source file and location.

### Stage I5 — Canonical offline representation

- Convert only validated source records into the app's versioned, offline scripture representation.
- Preserve canonical book ID, chapter, verse, source edition, source file hash and importer version.
- Keep text encoding and normalization policy explicit.
- Make imports reproducible: identical input, inventory, configuration and importer version must produce byte-identical output or a documented deterministic-equivalence result.
- Generate a manifest with per-book and whole-package hashes.
- Do not bundle study notes, user data or private backup material into scripture packages.

### Stage I6 — Rights and release controls

Before any public release, the release record must establish:

- Accurate title, author/translator, publisher and source attribution as required by the actual licence.
- Exact licence name/version and a copy or authoritative URL of the applicable licence.
- Whether the app's distribution and packaging constitute adaptation, modification or collection under the licence.
- Clear marking of changes, corrections, formatting transformations and metadata additions where required.
- ShareAlike treatment of any covered adapted material, including the applicable licence for distributed adaptations.
- Rights and licence status for fonts, audio, maps, artwork and every additional asset independently.
- Required notices included in-app, in distribution metadata and with any distributed content package.
- A documented release review by a person competent to interpret the licence.

Do not assume that a technically valid source signature proves licence authority or that attribution alone satisfies ShareAlike.

## 3. Required automated tests

| Test | Expected result |
|---|---|
| Exact authorized archive hash | PASS |
| Altered archive / wrong hash | Reject before parsing |
| Invalid ZIP or CRC failure | Reject |
| Path traversal, absolute path or symlink entry | Reject |
| Duplicate ZIP path or duplicate book ID | Reject |
| Missing or unexpected book ID | Reject |
| Reordered inventory | Reject unless versioned inventory approval exists |
| Invalid UTF-8 | Reject |
| Replacement character or unexpected control | Reject / require explicit review |
| Malformed USFM marker or reference | Reject with file and line evidence |
| Chapter/verse totals differ from baseline | Fail pending reconciliation |
| Same input and importer version | Deterministic output |
| Changed source bytes | New source hash and new import identity |
| Licence evidence absent or unresolved | Block release packaging |

Tests must include positive fixtures and adversarial negative fixtures. Synthetic fixtures are permitted; real scripture text must not be copied into public tests without a separate rights basis.

## 4. Integration acceptance criteria

1. The importer is reproducible and independently testable without network access.
2. Source archive identity, provenance, signature result and signer-trust state are preserved.
3. All 66 books match the approved inventory and order.
4. Chapter and verse references are valid and traceable.
5. Offline reader navigation and full-text search use the imported canonical representation, not inferred chapter/verse coordinates.
6. Content migrations are versioned, transactional and recoverable.
7. User study data remains isolated from scripture content updates.
8. Licence notices and ShareAlike controls are implemented and verified before distribution.
9. The final package contains no unintended source files, temporary data, private keys or reviewer materials.

## 5. Current disposition

| Work item | Status |
|---|---|
| Source archive identity and checksum baseline | VERIFIED |
| ZIP / USFM / Unicode intake observations | VERIFIED |
| Signature cryptographic validity | VERIFIED |
| Signer identity trust | UNRESOLVED |
| Edition-specific licence authority | UNRESOLVED |
| Deterministic importer implementation | NOT STARTED |
| Offline scripture database / package | NOT IMPLEMENTED |
| Reader and full-text search integration | NOT IMPLEMENTED |
| LIC-01 through LIC-10 release controls | SPECIFIED; NOT IMPLEMENTED |
| Public scripture distribution | NOT AUTHORIZED BY THIS DOCUMENT |

This contract authorizes implementation planning and synthetic test development only. It does not itself authorize publishing source content, merging PRs, signing, deploying or releasing the application.
