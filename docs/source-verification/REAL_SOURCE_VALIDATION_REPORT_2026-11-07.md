# Maale Bible — A2 Real-Source Validation Report

**Assessment date:** 2026-11-07  
**Edition:** 2008 Maale Full Bible — MDYTWF / mdyeth  
**A2 disposition:** **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**  
**Report class:** Metadata, integrity, structural and encoding evidence; no scripture text

## 1. Executive result

The exact Owner-provided 2008 Maale Full Bible source archive passed archive integrity, safe metadata preflight, isolated extraction, signature verification, signed per-file hash comparison, approved book-inventory matching, chapter/verse-marker structure checks, strict UTF-8 decoding and an independent second-pass reproducibility check.

On 2026-11-07, the Owner accepted the immutable metadata-only source baseline and explicitly accepted the outstanding Maale-language/Ethiopic-script specialist review as a deferred residual risk. Accordingly, A2 is closed with that exception. **No specialist linguistic or orthographic sign-off is claimed.**

This report records evidence and disposition only. It does not authorize production content intake, publication, audio use, third-party assets, merge or deployment.

## 2. Source identity and custody

- Edition: 2008 Maale Full Bible, identifier **MDYTWF / mdyeth**.
- Archive: `mdyeth_usfm.zip`.
- Official package URL: https://ebible.org/Scriptures/mdyeth_usfm.zip
- Archive size: **1,809,863 bytes**.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Source content revision shown in package copyright notice: **2025-05-13**.
- Package generation timestamp: **2026-10-30 05:20:45 UTC**.
- Acquisition/custody: Owner-provided uploaded archive, handled in an isolated workspace.
- This is the 2008 Full Bible edition. The 2007 MDYBSE New Testament is a distinct edition and was not substituted.

## 3. Archive integrity, safety and signature

| Check | Result |
|---|---|
| ZIP central-directory entries | 70 |
| Total uncompressed payload | 7,554,174 bytes |
| ZIP CRC/integrity test | **PASS** |
| Metadata-only archive preflight | **PASS** |
| Safe extraction into a new isolated directory | **PASS** |
| Unsafe paths/traversal/normalized duplicates | None detected |
| Symlinks, special entries, encryption, nested archives | None detected |
| Configured size/ratio limit violations | None detected |
| Included GPG clear-signature | **GOOD SIGNATURE** |
| Signing-key fingerprint | `54D714D76956DA6673DC1EEA5F62009D93505F26` |
| Key ID | `5F62009D93505F26` |
| Signed hash-list entries | 68 |
| Signed USFM file hashes matching | **66/66** |

**Trust qualification:** The cryptographic signature is valid. Trust certification of the public key in the isolated keyring remains **UNDEFINED**; this report does not assert independent key-owner identity certification.

## 4. Edition inventory and document structure

| Measure | Observed result |
|---|---:|
| USFM files | 66 |
| Owner-approved book IDs present exactly once | 66/66 |
| Missing book IDs | 0 |
| Unexpected book IDs | 0 |
| Duplicate book IDs | 0 |
| Filename-to-USFM-ID mismatches | 0 |
| Chapters | 1,189 |
| Chapters without verse markers | 0 |
| Verse-marker lines | 30,506 |
| Represented verse spans, including ranges | 31,102 |
| Verse-range marker instances | 238 |
| Verse ordering/overlap anomalies | 0 |

Chapter numbering was contiguous from chapter 1 in every book. Archive member enumeration order was not treated as canonical book order; the Owner-approved inventory controls the catalogue order.

## 5. Encoding and script diagnostics

- Strict UTF-8 decoding: **66/66 files PASS**.
- Unicode replacement characters: **0**.
- Non-NFC files: **0**.
- Unexpected control characters: **0**.
- Ethiopic-range code points counted: **2,108,248**.

These are encoding and code-point diagnostics only. They do not establish linguistic accuracy, orthographic correctness, canon correctness or specialist approval.

## 6. Rights metadata and attribution

The package copyright notice identifies the Maale Full Bible, both rights holders and the **CC BY-SA 4.0** licence. Its source revision date is 2025-05-13 and is consistent with the documented rights-holder clarification and edition record.

Required attribution:

> Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.

Any distribution of adapted material remains subject to applicable CC BY-SA 4.0 attribution, modification notice and ShareAlike requirements. Audio and third-party assets require separate rights review and approval.

## 7. Independent reproducibility

A separate second-pass archive read:

- Reverified ZIP CRC integrity.
- Recomputed all 66 USFM file hashes.
- Matched all 66 hashes to the signed manifest and first-pass manifest.
- Reproduced 1,189 chapters and 30,506 verse-marker lines.
- Reproduced strict UTF-8 success, zero replacement characters and the same NFC observations.

**Second-pass structural and hash reproducibility: PASS.**

## 8. Owner decision and residual risk

On **2026-11-07**, the Owner accepted the immutable metadata-only source baseline and explicitly accepted the named Maale-language/Ethiopic-script specialist review as a deferred risk.

| Item | Final disposition |
|---|---|
| Real-source archive integrity and safe handling | **PASS** |
| Signature and signed file-hash verification | **PASS** (keyring trust separately undefined) |
| Approved 66-book inventory match | **PASS** |
| Chapter and verse-marker structural checks | **PASS** |
| Encoding diagnostics | **PASS** |
| Independent second-pass reproducibility | **PASS** |
| Owner acceptance of immutable metadata-only baseline | **ACCEPTED** |
| Maale-language/Ethiopic-script specialist review | **DEFERRED — OWNER-ACCEPTED RESIDUAL RISK** |
| A2 gate | **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION** |

No specialist review or sign-off is represented as complete. It remains a tracked post-A2 risk and must be resolved before making claims of linguistic/orthographic validation.

## 9. Continuing restrictions and authorization boundaries

- `content_intake_authorized=false` remains in force.
- The acquired archive, raw USFM and scripture text remain outside this public repository.
- Do not commit raw scripture, excerpts, generated scripture databases or derived verse datasets.
- Do not ingest or distribute production scripture content until the exact intake operation and content handling are separately authorized.
- Audio and third-party assets remain separately rights-gated.
- A3 Android implementation has separate Owner authorization and proceeds without scripture text.
- No production deployment, release or merge authorization is granted by this report.
- A2 documentation PR #2 and A3 implementation PR #3 remain separate review items; this report does not merge either PR.

## 10. Evidence limitations

This report preserves the observed aggregate results, archive digest, signature fingerprint and validation dispositions. It does not contain the source archive, scripture text, private rights-holder correspondence, or the complete per-file hash manifest. The full per-file evidence remains in the isolated validation record and is not published here.

## 11. Conclusion

The exact Owner-provided 2008 MDYTWF / mdyeth package passed the documented archive, signature, per-file hash, inventory, structural, encoding and independent reproducibility checks. The Owner accepted the immutable metadata-only baseline and explicitly risk-accepted specialist review as deferred. **A2 is CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION.**

This conclusion is limited to the A2 source-readiness baseline. It is not a linguistic sign-off, production content-intake authorization, publication approval or deployment approval.
