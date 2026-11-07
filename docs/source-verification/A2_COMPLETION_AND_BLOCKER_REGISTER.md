# A2 Completion and Blocker Register

**Assessment date:** 2026-11-07  
**Phase:** A2 — source verification and readiness controls  
**Owner authorization:** REAFFIRMED — exact 2008 MDYTWF / mdyeth USFM package acquisition and A2 inspection/validation operations  
**Overall status:** RIGHTS AND CANON DECISIONS RECORDED; REAL-SOURCE EXECUTION BLOCKED BY RETRIEVAL INFRASTRUCTURE

## 1. Completed A2 deliverables

| Deliverable | Evidence | Status |
|---|---|---|
| Rights/edition reconciliation | Owner-supplied rights-holder response plus official eBible edition record | PASS — DOCUMENTARY |
| Licence and package coverage | Official eBible record and supplied rights-holder response identify CC BY-SA 4.0 and USFM format | PASS — DOCUMENTARY |
| Owner authorization | Current Owner instruction reaffirms full authorization for exact package and permitted A2 inspection operations | PASS — AUTHORIZED |
| Canon and expected book inventory | Official edition-specific PDF directory lists 66 individual book PDFs; Owner decision recorded | PASS — OWNER-APPROVED |
| CC BY-SA compliance controls | `docs/licensing/LICENCE_COMPLIANCE_CONTROLS.md` | PASS — SPECIFIED |
| ZIP metadata preflight | `tools/archive_preflight.py`; 14/14 synthetic tests | PASS — SYNTHETIC |
| USFM structural audit tooling | PR #1 pinned corrective head; 12/12 synthetic tests | PASS — SYNTHETIC |
| Source manifest rights-evidence enforcement | `content/manifests/source-manifest.schema.json`; positive/negative synthetic validation | PASS — SYNTHETIC |
| Language/script review protocol | `LANGUAGE_SCRIPT_REVIEW_PROTOCOL.md` | PASS — PROCEDURE READY; reviewer execution pending |

## 2. A2 gate register

| Gate | Required evidence | Status | Closure condition |
|---|---|---|---|
| A2-01 Edition identity and canon | Exact edition, official edition-specific book list and approved inventory | PASS — DOCUMENTARY / OWNER-APPROVED | Verify actual package matches approved list |
| A2-02 Rights authority | Rights-holder evidence and exact edition/package licence | PASS — DOCUMENTARY | Preserve source evidence and attribution controls; email-header authentication caveat remains recorded |
| A2-03 Product permission | Offline storage, display, search, bookmarks, annotations and sharing | PASS — DOCUMENTARY | Audio and third-party assets remain separately gated |
| A2-04 Licence reconciliation | CC BY-SA 4.0, package applicability and compliance obligations | PASS — DOCUMENTARY | Preserve attribution, change notices, ShareAlike and no-endorsement controls |
| A2-05 Source acquisition | Exact package retrieval, archive hash and chain of custody | AUTHORIZED — EXECUTION BLOCKED | Retrieve exact archive into isolated workspace and record provenance/hash |
| A2-06 Structural validation | Real archive preflight, extraction, file inventory, USFM and chapter/verse audit | NOT RUN — RETRIEVAL BLOCKED | Package available locally; execute preflight and audit against approved 66-book inventory |
| A2-07 Character validation | Real-source Unicode/encoding diagnostics and specialist sign-off | NOT RUN — RETRIEVAL BLOCKED | Package available locally; run diagnostics and complete named specialist review |
| A2-08 Immutable baseline | Complete manifest, hashes, validation reports and Owner baseline record | BLOCKED — DEPENDENT | Complete A2-05 through A2-07, then freeze and verify baseline |

## 3. Owner authorization record

The Owner has reaffirmed in the current conversation that full authorization was already provided and instructed the project to proceed with the exact 2008 Full Bible package. This is recorded as authorization for:

- Exact package: `https://ebible.org/Scriptures/mdyeth_usfm.zip` (`MDYTWF / mdyeth`, 2008 Maale Full Bible).
- Download/acquisition into an isolated, non-public validation workspace.
- Archive byte-size and SHA-256 recording; ZIP metadata preflight.
- Safe extraction into a new isolated directory after preflight.
- Inspection of package metadata, USFM files, book IDs, markers, chapter/verse structure and encoding.
- Structural, Unicode, Ethiopic-script and character diagnostics.
- Creation of per-file hashes, source manifest, validation reports and an immutable A2 source baseline.

This authorization does not permit committing raw scripture files or verse excerpts to the public repository, publishing or distributing content, enabling production content intake, or starting A3 Android/backend implementation. Preserve the licence obligations and source custody controls.

No further Owner authorization request is required for the operations listed above.

## 4. Retrieval attempts and current technical blocker

The official eBible edition record was retrieved and confirms the 2008 Maale Full Bible identity, joint copyright attribution, CC BY-SA 4.0 terms, and a listed USFM package named `mdyeth_usfm.zip`. The edition-specific PDF directory was also retrieved and enumerates the 66 book IDs used by the approved inventory.

The exact ZIP retrieval was attempted from `https://ebible.org/Scriptures/mdyeth_usfm.zip`, but the execution environment could not reach the host:
- Direct container download: failed because DNS resolution was unavailable (`Temporary failure in name resolution`).
- Browser/web open of the binary URL: source endpoint inaccessible/cache miss.
- Browser automation retrieval: not started because the connected automation wallet returned an insufficient-balance error. No payment or top-up is requested or required from the Owner.

Therefore the archive has **not** been downloaded, hashed, unpacked or inspected. No real-source validation or immutable archive baseline is claimed.

## 5. Evidence still outstanding

- Exact archive retrieval and SHA-256.
- ZIP metadata preflight against the actual archive.
- Safe extraction and complete per-file inventory/hashes.
- Real USFM structure, book/chapter/verse validation against the approved 66-book list.
- Real-source encoding and Unicode diagnostics.
- Named Maale-language/Ethiopic-script specialist review and signed disposition.
- Independent reproducibility of real-source validation.
- Immutable baseline manifest and Owner acceptance.

## 6. Safety and governance

- `content_intake_authorized=false` remains in force for production ingestion/distribution.
- Raw scripture files and verse excerpts must remain outside the public repository.
- A1 remains **CLOSED — FINAL**.
- No CI/workflow was run.
- No PR was merged.
- No A3 implementation or deployment was performed.

## 7. Closure decision

**A2 formal closure: NOT YET ACCEPTED — REAL-SOURCE EXECUTION AND BASELINE EVIDENCE MISSING.**

Rights and canon decisions are no longer blockers. The exact source acquisition and inspection operations are Owner-authorized. The remaining blocker is technical retrieval of the authorized archive, followed by real-source validation and immutable-baseline creation. Do not represent A2 as closed until those evidence gates pass.
