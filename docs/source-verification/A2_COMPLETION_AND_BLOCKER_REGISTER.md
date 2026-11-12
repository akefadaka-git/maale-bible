# A2 Completion and Blocker Register

**Assessment date:** 2026-11-12  
**Phase:** A2 — source verification and readiness controls  
**Overall status:** **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**  
**Owner acceptance:** 2026-11-07  
**Independent source re-verification:** 2026-11-12.

## 1. Completed A2 deliverables

| Deliverable | Evidence | Status |
|---|---|---|
| Rights/edition reconciliation | Owner-supplied rights-holder response; official eBible edition record | PASS — DOCUMENTARY |
| Licence/package coverage | Package `copr.htm`, edition record and supplied response identify CC BY-SA 4.0 | PASS — DOCUMENTARY |
| Product permission | Offline storage, display, search, annotations and sharing described in supplied response | PASS — DOCUMENTARY |
| Owner authorization | Exact package and A2 inspection operations authorized | PASS — AUTHORIZED |
| Canon/expected inventory | Edition-specific reference and Owner-approved 66-book inventory | PASS — OWNER-APPROVED |
| CC BY-SA controls | LIC-01 through LIC-10 specified | PASS — SPECIFIED; application implementation is a later release gate |
| ZIP preflight tooling | `tools/archive_preflight.py`; 14 synthetic tests | PASS — SYNTHETIC |
| USFM audit tooling | `tools/usfm_audit.py`; manifest/inventory fail-closed controls | PASS — SYNTHETIC |
| A2 synthetic CI | 27/27 tests passed on run [#43199212983](https://github.com/akefadaka-git/maale-bible/actions/runs/43199212983), tested SHA `b54146c62b9a6e1fc9a530eab9eaed16535800e5` | PASS — CI |
| Enhanced A2 baseline regression CI | 33/33 tests passed on PR #52 run [#43226548676](https://github.com/akefadaka-git/maale-bible/actions/runs/43226548676), tested SHA `ac857628aea97515f09243a887b2b217c3ea014c` | PASS — EXACT HEAD |
| Real source acquisition | Owner-provided exact `mdyeth_usfm.zip`; 1,809,863 bytes; SHA-256 recorded | PASS — REAL SOURCE |
| ZIP integrity and safe handling | 70 entries; CRC clean; no unsafe paths, duplicate normalized paths, special entries, encryption or nested archives | PASS — REAL SOURCE |
| Digital signature | GPG GOOD SIGNATURE; 68/68 signed checksums match | PASS — CRYPTOGRAPHIC; key trust UNDEFINED |
| Per-file integrity | 66/66 USFM SHA-256 values match signed list and metadata manifest | PASS — REAL SOURCE |
| Book/chapter structure | 66 unique approved IDs; 1,189 chapters; contiguous chapter sequences | PASS — REAL SOURCE |
| Verse-marker structure | 30,506 marker lines; 31,102 represented spans; no empty chapters | PASS — REAL SOURCE |
| Encoding/script diagnostics | UTF-8 66/66; zero replacement chars, non-NFC files or unexpected controls | PASS — MACHINE |
| Independent reproducibility | Archive digest, signed checksums, file hashes and aggregate diagnostics reproduced 2026-11-12 | PASS |
| Maale language/script specialist review | Named specialist report and sign-off | DEFERRED — OWNER-ACCEPTED RESIDUAL RISK |
| Immutable baseline | Metadata-only manifest and 66-file hash register; Owner accepted 2026-11-07 | PASS — OWNER ACCEPTED |

## 2. Exact source identity

- Edition: 2008 Maale Full Bible — `MDYTWF / mdyeth`.
- Archive: `mdyeth_usfm.zip`.
- Source URL: https://ebible.org/Scriptures/mdyeth_usfm.zip.
- Archive bytes: **1,809,863**.
- Archive SHA-256: **`4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`**.
- Package source revision: **2025-05-13**.
- Package generated: **2026-10-30 05:20:45 UTC**.
- ZIP entries: **70**; USFM files: **66**; supporting files: **4**.
- GPG signing-key fingerprint: `54D714D76956DA6673DC1EEA5F62009D93505F26`.
- GPG trust status: **UNDEFINED**; do not overstate signer identity.

## 3. Structural and encoding results

| Measure | Verified result |
|---|---:|
| Approved book IDs | 66/66 |
| Missing / unexpected / duplicate IDs | 0 / 0 / 0 |
| Chapters | 1,189 |
| Chapters without verse markers | 0 |
| Verse-marker lines | 30,506 |
| Represented verse spans including ranges | 31,102 |
| Verse-range marker instances | 238 |
| UTF-8 valid | 66/66 |
| Unicode replacement characters | 0 |
| Non-NFC files | 0 |
| Unexpected control characters | 0 |
| Ethiopic-range code points | 2,108,248 |

The archive order is not canonical; the Owner-approved inventory controls application ordering.

## 4. Rights and evidence qualifications

The supplied rights-holder response states that the 2008 Full Bible is jointly held by The Word for the World International and the Bible Society of Ethiopia, licensed under CC BY-SA 4.0, and covers the exact eBible USFM package and product uses documented in the rights record. The separate 2007 MDYBSE New Testament is not the selected work.

Original email headers, sender identity, delivery metadata and signatory authority have not been independently authenticated in this repository. The official edition/licence record corroborates public edition and licence details. This remains a documentary caveat. The cryptographic GPG signature is valid, but the key trust state is UNDEFINED.

## 5. A2 gate register

| Gate | Status |
|---|---|
| A2-01 Edition identity and canon | PASS |
| A2-02 Rights authority | PASS — DOCUMENTARY |
| A2-03 Product permission | PASS — DOCUMENTARY |
| A2-04 Licence reconciliation | PASS — SPECIFIED |
| A2-05 Source acquisition | PASS — REAL SOURCE |
| A2-06 Structural validation | PASS — REAL SOURCE |
| A2-07 Character validation | PASS — MACHINE; SPECIALIST DEFERRED / OWNER RISK-ACCEPTED |
| A2-08 Immutable baseline | PASS — OWNER ACCEPTED 2026-11-07; REPRODUCED 2026-11-12 |

## 6. Binding restrictions and later release gates

- `content_intake_authorized=false` remains in force.
- Raw scripture, source archive, excerpts and derived scripture data remain prohibited in the public repository.
- Audio and third-party assets remain separately rights-gated.
- LIC-01 through LIC-10 application/release controls are specified but not represented as implemented.
- Specialist review remains deferred and is not represented as passed.
- No production content intake, signing, deployment, release or publication is authorized by this A2 closure.

## 7. Closure decision

**A2 is CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION.** The Owner accepted the immutable metadata-only source baseline on 2026-11-07. The original source archive, signature, signed checksums, 66 per-file hashes, expected inventory and aggregate structure were independently reproduced on 2026-11-12.

See [A2 Final Closure Record](A2_FINAL_CLOSURE_RECORD_2026-11-12.md). A1 remains CLOSED — FINAL. This record does not authorize progression into another phase; subsequent work remains separately governed.
