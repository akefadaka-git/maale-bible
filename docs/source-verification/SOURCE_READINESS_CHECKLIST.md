# Source Package Readiness Checklist

Status: PROCEDURE SPECIFIED; SYNTHETIC ZIP PREFLIGHT IMPLEMENTED AND TESTED — NO REAL PACKAGE ACQUIRED OR INSPECTED  
Selected edition: 2008 Maale Full Bible (MDYTWF / mdyeth)  
Candidate package: mdyeth_usfm.zip

## 1. Hard gates before acquisition

- [x] Owner-supplied written clarification identifies the 2008 Full Bible and distinguishes the 2007 MDYBSE New Testament.
- [x] Written clarification states CC BY-SA 4.0 applies to the exact eBible USFM package and supporting files.
- [ ] Original email headers, sender identity, date and attachments retained in the project evidence archive.
- [ ] Signatory authority and organizational role independently reviewed or authentication limitations formally accepted by Owner.
- [ ] Separate Owner authorization names the exact package, source URL, acquisition purpose, execution environment and permitted inspection actions.
- [ ] Approved expected book inventory and canon reference identified, versioned and approved.
- [ ] Maale-language and Ethiopic-script reviewers identified.

**Acquisition remains prohibited while any mandatory pre-acquisition checkbox is incomplete.**

## 2. Acquisition record (complete only after authorization)

| Field | Required value |
|---|---|
| Source edition | 2008 Maale Full Bible — MDYTWF / mdyeth |
| Package filename | mdyeth_usfm.zip |
| Exact source URL | Record actual URL used; do not infer from filename |
| Retrieval timestamp | UTC timestamp |
| Retrieval operator | Named operator |
| Acquisition authorization | Owner decision reference |
| Original byte size | Actual measured size |
| Original archive SHA-256 | Actual computed digest |
| Storage location | Restricted, non-public evidence storage |
| Retrieval method/tool | Exact tool and version |
| Response metadata | Content type, server metadata, HTTP status if available |
| Chain-of-custody log | Timestamped actions and custody changes |

Do not prepopulate actual hashes, timestamps, file counts or sizes from estimates.

## 3. Safe archive inspection and extraction

The A2 utility `tools/archive_preflight.py` inspects ZIP central-directory metadata and hashes the archive. It does not extract entries or read/print scripture content. It fails closed on unsafe paths, normalized path collisions, symlinks/special files, nested archives, encrypted entries and configured resource limits.

Run only after the exact archive has been separately authorized and acquired:

    python tools/archive_preflight.py SOURCE_PACKAGE.zip --output preflight-report.json

Default limits: 2,000 entries; 50 MiB per file; 250 MiB total uncompressed size; compression ratio 100:1. Any limit adjustment requires documented justification and review. The preflight report is not extraction authorization.

After a PASS, extraction still requires a separately reviewed, isolated extraction process with path containment checks, a new empty directory and no overwrite behavior. Preserve the original archive unchanged. Never extract into a repository checkout or execute package contents.

## 4. File inventory and integrity

For each extracted file record:
- Relative path in normalized POSIX form.
- Byte size.
- Detected format and declared format.
- SHA-256.
- USFM book ID, if applicable.
- Encoding and decoding result.
- Whether the file is expected, unexpected, duplicated or unsupported.

Recompute the archive hash and file hashes independently. The manifest must cover every file in the approved package scope. No missing, extra or silently ignored files.

## 5. Validation layers

| Layer | Required evidence | Status |
|---|---|---|
| Package provenance | Source URL, UTC retrieval time, authorization and archive hash | NOT RUN |
| ZIP metadata preflight | Synthetic tests for path, collision, type and resource controls | PASS — 14/14 synthetic tests |
| Real archive preflight | Report for the authorized source archive | NOT RUN / NOT AUTHORIZED |
| Safe extraction | Isolated extraction record and complete inventory | NOT RUN |
| File integrity | Complete file list, sizes and SHA-256 values | NOT RUN |
| USFM structural audit | Audit report from pinned tooling | NOT RUN on real source |
| Canon/book inventory | Compare with Owner-approved expected inventory | NOT RUN |
| Chapter/verse inventory | Compare against trusted reference where available | NOT RUN |
| Character/encoding | Unicode and Ethiopic-script diagnostics | NOT RUN |
| Maale linguistic review | Independent specialist report | NOT RUN |
| Reproducibility | Independent rerun with matching results | NOT RUN |
| Immutable baseline | Owner-approved manifest and frozen hashes | BLOCKED |

## 6. Synthetic readiness test evidence

The committed ZIP preflight utility has 14 synthetic tests covering:
- Valid placeholder ZIP with no extraction.
- Parent traversal, absolute, drive-qualified and backslash paths.
- Case-folding and Unicode-normalization path collisions.
- Symlink rejection.
- Nested archive rejection.
- Per-file, total expanded-size, compression-ratio and entry-count limits.
- Corrupt archive handling.

All fixtures contain placeholder text only. Exact source blob identities and execution results are recorded in the A2 verification report / pull request. The tests do not establish safety for a real package, and CI was not run.

The separate USFM structural suite previously passed 12/12 against the pinned PR #1 corrective head. No real Maale source was used.

## 7. Current decision

- Rights/edition reconciliation: PASS — DOCUMENTARY.
- Original email/signatory authentication: PENDING.
- Package acquisition authorization: NOT GRANTED.
- Package acquisition/inspection/hash: NOT RUN.
- ZIP preflight implementation: PASS — synthetic-only tests 14/14.
- USFM structural suite: PASS — 12/12 on pinned corrective head.
- Real-source validation: NOT AUTHORIZED.
- content_intake_authorized: FALSE.
- A2 final acceptance: PENDING.

A source package may not be acquired merely because its licence has been documented. The Owner must authorize that separate action.
