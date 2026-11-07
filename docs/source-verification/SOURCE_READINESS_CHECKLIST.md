# Source Package Readiness Checklist

Status: PROCEDURE SPECIFIED — NO PACKAGE ACQUIRED OR INSPECTED  
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

After acquisition is separately authorized, perform archive metadata inspection in an isolated environment. Do not execute files from the package.

Reject or quarantine archives containing:
- Absolute, drive-qualified, UNC, or parent-traversal paths.
- Symlinks, hard links, device nodes or unsupported file types.
- Duplicate paths after normalization, case folding or Unicode normalization.
- Nested archives not explicitly included in the approved package scope.
- Excessive entry count, per-file size, total expanded size or compression ratio.
- Executable files or unexpected formats outside the declared source package scope.
- Encrypted entries that prevent complete inspection, unless explicitly authorized.

Extraction must occur into a new empty directory with path containment checks and no overwrite behavior. Preserve the original archive unchanged. Record tool versions, rejected entries and complete output. Never extract into a repository checkout.

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
| Archive safety | No traversal, links, collisions, bombs or unexpected files | NOT RUN |
| File integrity | Complete file list, sizes and SHA-256 values | NOT RUN |
| USFM structural audit | Audit report from pinned tooling | NOT RUN |
| Canon/book inventory | Compare with Owner-approved expected inventory | NOT RUN |
| Chapter/verse inventory | Compare against trusted reference where available | NOT RUN |
| Character/encoding | Unicode and Ethiopic-script diagnostics | NOT RUN |
| Maale linguistic review | Independent specialist report | NOT RUN |
| Reproducibility | Independent rerun with matching results | NOT RUN |
| Immutable baseline | Owner-approved manifest and frozen hashes | BLOCKED |

## 6. Synthetic readiness tests before any real package

Before any authorized source package is processed, test the intake and audit pipeline using synthetic archives only. Include:
- Valid minimal ZIP with placeholder files.
- Path traversal and absolute-path entries.
- Duplicate normalized paths and case collisions.
- Symlink and unsupported file-type entries.
- Excessive file count/size/compression-ratio rejection.
- Hash mismatch and missing/extra file detection.
- Invalid UTF-8 and replacement-character detection.
- Duplicate book IDs and invalid chapter/verse ordering.
- Rights status not approved and missing Owner approval reference.
- Approved manifest missing licence evidence or Owner approval reference.

Synthetic tests must contain no Maale scripture excerpts or real source content. Record exact source SHA, command, complete output and exit code. Do not trigger CI unless separately authorized.

## 7. Current decision

- Rights/edition reconciliation: PASS — DOCUMENTARY.
- Original email/signatory authentication: PENDING.
- Package acquisition authorization: NOT GRANTED.
- Package acquisition/inspection/hash: NOT RUN.
- Synthetic USFM audit suite on pinned PR #1 corrective head: PASS, 12/12, exit 0 (historical evidence; exact SHA is recorded in PR #1).
- Archive-safety test suite: NOT IMPLEMENTED / NOT RUN.
- Real-source validation: NOT AUTHORIZED.
- content_intake_authorized: FALSE.
- A2 final acceptance: PENDING.

A source package may not be acquired merely because its licence has been documented. The Owner must authorize that separate action.
