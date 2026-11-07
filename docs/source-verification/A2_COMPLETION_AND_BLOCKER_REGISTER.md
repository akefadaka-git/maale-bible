# A2 Completion and Blocker Register

**Assessment date:** 2026-11-07  
**Phase:** A2 — source verification and readiness controls  
**Owner authorization:** REAFFIRMED — exact 2008 MDYTWF / mdyeth package acquisition and A2 inspection  
**Overall status:** CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION (2026-11-07)

## 1. Completed A2 deliverables

| Deliverable | Evidence | Status |
|---|---|---|
| Rights/edition reconciliation | Owner-supplied rights-holder response and official eBible edition record | PASS — DOCUMENTARY |
| Licence/package coverage | Package `copr.htm`, official edition record and rights-holder response identify CC BY-SA 4.0 | PASS — DOCUMENTARY |
| Owner authorization | Owner's explicit authorization for exact package and A2 operations | PASS — AUTHORIZED |
| Canon/expected inventory | Edition-specific eBible PDF directory and Owner-approved 66-book inventory | PASS — OWNER-APPROVED |
| CC BY-SA compliance controls | `docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md` | PASS — SPECIFIED |
| ZIP metadata preflight utility | `tools/archive_preflight.py`; 14/14 synthetic tests | PASS — SYNTHETIC |
| USFM structural audit tooling | PR #1 pinned corrective head; 12/12 synthetic tests | PASS — SYNTHETIC |
| Source manifest rights-evidence enforcement | `content/manifests/source-manifest.schema.json`; synthetic positive/negative checks | PASS — SYNTHETIC |
| Real source acquisition | Exact uploaded package, byte size and SHA-256 recorded | PASS — REAL SOURCE |
| Real ZIP integrity/preflight/extraction | 70 entries; CRC PASS; fail-closed metadata checks; isolated extraction | PASS — REAL SOURCE |
| Signed source-file integrity | GPG signature valid; 66/66 USFM hashes match signed list | PASS — CRYPTOGRAPHIC |
| Real USFM/book/chapter validation | 66 unique approved book IDs; 1,189 chapters; chapter sequences contiguous | PASS — REAL SOURCE |
| Real encoding/script diagnostics | UTF-8 valid; zero replacement characters; NFC and Ethiopic diagnostics recorded | PASS — MACHINE |
| Language/script specialist review | Named specialist review and signed disposition | DEFERRED — OWNER RISK-ACCEPTED |
| Immutable baseline | Metadata-only manifest, per-file hashes and independent second-pass reproducibility | PASS — OWNER ACCEPTED 2026-11-07 |

## 2. A2 gate register

| Gate | Required evidence | Status | Closure condition |
|---|---|---|---|
| A2-01 Edition identity and canon | Exact edition, official edition-specific list and approved inventory | PASS — DOCUMENTARY / OWNER-APPROVED | Retain package-to-inventory match in baseline |
| A2-02 Rights authority | Rights-holder evidence and exact edition/package licence | PASS — DOCUMENTARY | Preserve attribution and provenance caveat; original email-header/signatory authentication remains a documentary caveat |
| A2-03 Product permission | Offline storage, display, search, bookmarks, annotations and sharing | PASS — DOCUMENTARY | Audio and third-party assets remain separately gated |
| A2-04 Licence reconciliation | CC BY-SA 4.0, package applicability and compliance obligations | PASS — DOCUMENTARY | Preserve attribution, change notices, ShareAlike and no-endorsement controls |
| A2-05 Source acquisition | Exact archive, provenance and SHA-256 | PASS — REAL SOURCE | Archive SHA-256 recorded below |
| A2-06 Structural validation | ZIP, signed hashes, USFM files, book IDs, chapters and verse markers | PASS — REAL SOURCE | No outstanding machine structural blocker |
| A2-07 Character validation | Strict UTF-8, Unicode/Ethiopic diagnostics; specialist sign-off tracked separately | PASS — MACHINE; SPECIALIST DEFERRED / OWNER RISK-ACCEPTED | Specialist review remains a post-A2 risk; no specialist sign-off is claimed |
| A2-08 Immutable baseline | Manifest, per-file hashes, independent reproducibility and Owner acceptance | PASS — OWNER ACCEPTED | Baseline accepted with specialist-review exception recorded |

## 3. Real source identity and evidence

- Exact package: `mdyeth_usfm.zip` — `MDYTWF / mdyeth`, 2008 Maale Full Bible.
- Source URL: `https://ebible.org/Scriptures/mdyeth_usfm.zip`.
- Archive size: **1,809,863 bytes**, matching the official eBible directory listing.
- Archive SHA-256: **`4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`**.
- Package source revision date in `copr.htm`: **2025-05-13**.
- Package generated: **2026-10-30 05:20:45 UTC**.
- ZIP entries: 70; USFM files: 66; uncompressed payload: 7,554,174 bytes.
- ZIP CRC/integrity: PASS; metadata preflight: PASS; safe extraction: PASS into a new isolated directory.
- No unsafe paths, traversal, normalized duplicate names, symlinks/special entries, encrypted entries, nested archives or configured size/ratio limit violations.

### Digital signature

- Included GPG clear-signature: **GOOD SIGNATURE**.
- Signing key fingerprint: `54D714D76956DA6673DC1EEA5F62009D93505F26`.
- Key ID: `93505F26`, matching the key ID stated by eBible's official signature-verification instructions.
- Signed hash list: 68 entries; all **66/66 USFM file hashes match**.
- GPG trust status: **UNDEFINED** in the isolated keyring. Cryptographic validity is confirmed; independent trust certification of the public key is not asserted.

### Real-source structure and encoding

- Approved book IDs present exactly once: **66/66**; missing, unexpected and duplicate IDs: **0**.
- Filename-to-USFM-ID mismatches: **0**.
- Chapters: **1,189**; chapter numbering contiguous from 1 in every book.
- Chapters without verse markers: **0**.
- Verse marker lines: **30,506**; represented verse spans including range markers: **31,102**.
- Verse ordering/overlap anomalies: **0**.
- UTF-8 valid: **66/66**; Unicode replacement characters: **0**; non-NFC files: **0**; unexpected control characters: **0**.
- Ethiopic-range code points: **2,108,248**.
- ZIP entry order is not canonical; it is non-semantic. The Owner-approved inventory is the ordering authority for application display and ingestion.

## 4. Rights and attribution

The package's `copr.htm` identifies the Maale Full Bible, both rights holders and CC BY-SA 4.0. This is consistent with the documented rights-holder response and official eBible edition record.

Required attribution:

> Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.

## 5. Candidate baseline artifacts

Metadata-only candidate evidence artifacts were generated outside the public repository:
- `maale-source-evidence-manifest.json` — archive identity, provenance, signature, structural diagnostics and per-file hashes.
- `maale-usfm-file-hashes.json` — per-USFM-file hashes only.
- `maale-a2-real-source-validation-report.md` — human-readable validation report.

A separate second-pass ZIP read verified CRC integrity and re-computed all 66 USFM hashes; all matched the first-pass manifest and signed hash list. It reproduced 1,189 chapters and 30,506 verse-marker lines. The Owner accepted the metadata-only immutable baseline on 2026-11-07. The named specialist review remains deferred as an explicitly accepted risk; no specialist sign-off is claimed. The metadata-only artifacts contain no scripture text.

## 6. Restrictions and governance

- `content_intake_authorized=false` remains in force for production ingestion/distribution.
- Raw scripture files and verse excerpts must not be committed to the public repository.
- Audio and third-party assets remain separately rights-gated.
- A1 remains **CLOSED — FINAL**.
- No CI/workflow was run.
- No PR was merged.
- A3 implementation is authorized and tracked separately in PR #3; no production release or deployment is authorized.

## 7. Closure decision

**A2 formal closure: CLOSED — OWNER-ACCEPTED WITH EXPLICIT EXCEPTION (2026-11-07).**

The Owner's instruction “Accept all and approved to proceed” accepts the candidate immutable source baseline and explicitly accepts the outstanding named Maale-language/Ethiopic-script specialist review as a deferred risk. All machine-verifiable A2 gates and independent reproducibility passed. No specialist review or sign-off is represented as completed. The specialist review remains a tracked post-A2 risk and may be completed later without reopening A1. Production content intake remains separately controlled and is not authorized by this closure record.
