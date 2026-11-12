# Source Package Readiness Checklist

**Status:** A2 SOURCE BASELINE ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION  
**Selected edition:** 2008 Maale Full Bible (MDYTWF / mdyeth)  
**Package:** `mdyeth_usfm.zip`  
**Final A2 closure:** 2026-11-07; independent technical re-verification 2026-11-12.

## 1. Pre-acquisition and authorization gates

- [x] Owner-supplied written clarification identifies the 2008 Full Bible and distinguishes the 2007 MDYBSE New Testament.
- [x] Written clarification states CC BY-SA 4.0 applies to the exact eBible USFM package and supporting files.
- [x] Owner authorization names the exact package and permitted A2 inspection operations.
- [x] Owner-approved expected 66-book inventory identified and versioned.
- [x] Exact source archive supplied by Owner and retained outside the public repository.
- [~] Original email headers and signatory authority: documentary caveat; not independently authenticated in this repository.
- [~] Maale-language/Ethiopic-script specialist review: deferred and explicitly accepted by Owner as a residual risk; no sign-off claimed.

The two residual items above are not represented as passed. The specialist-review deferral is explicitly accepted in the A2 closure record. Production intake and publication remain separately gated.

## 2. Acquisition record

| Field | Verified value |
|---|---|
| Source edition | 2008 Maale Full Bible — MDYTWF / mdyeth |
| Package filename | mdyeth_usfm.zip |
| Exact source URL | https://ebible.org/Scriptures/mdyeth_usfm.zip |
| Acquisition method | Owner-provided uploaded archive |
| Inspection date | 2026-11-07; independent recheck 2026-11-12 |
| Original byte size | 1,809,863 |
| Original archive SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| Package generation | 2026-10-30 05:20:45 UTC |
| Source revision in package notice | 2025-05-13 |
| Storage | Restricted project Library / isolated validation workspace; not in public repository |

## 3. Archive integrity and safe handling

| Check | Result |
|---|---|
| ZIP entries | 70 |
| Uncompressed payload | 7,554,174 bytes |
| ZIP CRC/integrity | PASS |
| Unsafe paths / traversal | None detected |
| Normalized duplicate paths | None detected |
| Symlinks / special entries | None detected |
| Encrypted entries | None detected |
| Nested archives | None detected |
| Extraction | PASS — isolated empty directory |
| Raw source committed to public repository | NO |

The archive was inspected without executing package members. The original archive remains outside the public repository.

## 4. Signature and file integrity

- GPG clear-signature: GOOD SIGNATURE.
- Signing key fingerprint: `54D714D76956DA6673DC1EEA5F62009D93505F26`.
- Signed checksum entries: 68; all 68 match.
- USFM files: 66; all 66 hashes match the metadata-only per-file manifest.
- GPG trust state: UNDEFINED in the isolated keyring. Do not describe signer identity as independently certified.

## 5. Structural and encoding validation

| Layer | Required evidence | Result |
|---|---|---|
| Package provenance | Exact URL, authorization, size and archive SHA-256 | PASS |
| ZIP integrity / preflight | 70 entries, CRC, safe path/type/resource checks | PASS |
| File inventory | 66 USFM files, 4 supporting files | PASS |
| Per-file integrity | 66/66 signed and manifest hashes match | PASS |
| Book inventory | 66/66 approved IDs; no missing, extra or duplicate IDs | PASS |
| Chapter structure | 1,189 chapters; contiguous from 1 per book | PASS |
| Verse-marker structure | 30,506 marker lines; 31,102 represented spans; no empty chapters | PASS |
| UTF-8 | 66/66 strict decoding | PASS |
| Unicode diagnostics | 0 replacement characters; 0 non-NFC files; 0 unexpected controls | PASS |
| Ethiopic diagnostics | 2,108,248 code points | RECORDED |
| Independent reproducibility | Archive digest, signed hashes and aggregate structure reproduced | PASS |
| Maale linguistic/script specialist review | Independent specialist disposition | DEFERRED — OWNER-ACCEPTED EXCEPTION |
| Immutable baseline | Metadata-only manifest, per-file hashes and Owner acceptance | PASS — OWNER ACCEPTED |

## 6. Final A2 decision

A2 source readiness is **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**. The Owner accepted the immutable metadata-only baseline on 2026-11-07. The exact original archive and signed file hashes were independently rechecked on 2026-11-12.

This is not a linguistic or orthographic sign-off. The deferred specialist review remains a tracked residual risk.

## 7. Binding restrictions

- `content_intake_authorized=false` remains in force.
- Do not commit the archive, raw USFM/USX, verse excerpts or derived scripture data to the public repository.
- Do not distribute production scripture content until a separate Owner intake/release authorization exists.
- Audio and third-party assets require separate rights evidence.
- No silent normalization, correction, transliteration, paraphrase or other source alteration.
