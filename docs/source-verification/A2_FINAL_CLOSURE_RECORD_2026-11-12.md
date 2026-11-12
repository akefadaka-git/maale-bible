# Maale Bible — A2 Final Source-Readiness Closure Record

**Decision date:** 2026-11-12  
**Gate:** A2 — source and rights verification  
**Repository baseline assessed:** `main` at `330a8eaa646a4002b8ccab8b08ad71a6d2e7cc54`  
**Disposition:** **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**  
**Authority:** Owner acceptance recorded 2026-11-07; independent technical re-verification recorded 2026-11-12.

## 1. Closure decision

A2 source-readiness acceptance is confirmed. The exact Owner-provided 2008 Maale Full Bible package was rechecked against its recorded archive digest, signed checksum list, and Owner-approved 66-book inventory. The immutable metadata-only baseline was accepted by the Owner on 2026-11-07. This closure retains the explicit residual risk that Maale-language/Ethiopic-script specialist review is deferred; no linguistic or orthographic specialist sign-off is claimed.

This decision closes **A2 source readiness only**. It does not authorize production content intake, public scripture distribution, audio or third-party assets, Android implementation, deployment, signing, publication, or merge.

## 2. Exact source identity

| Field | Verified value |
|---|---|
| Edition | 2008 Maale Full Bible |
| Publisher identifier | `MDYTWF` |
| eBible identifier | `mdyeth` |
| Language / script | Maale (`mdy`) / Ethiopic |
| Archive | `mdyeth_usfm.zip` |
| Source URL | `https://ebible.org/Scriptures/mdyeth_usfm.zip` |
| Archive size | 1,809,863 bytes |
| Archive SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| Package source revision | 2025-05-13 |
| Package generated | 2026-10-30 05:20:45 UTC |
| Copyright holders stated in package | The Word for the World International; The Bible Society of Ethiopia |
| Licence stated in package | CC BY-SA 4.0 |

The separate 2007 `MDYBSE` Maale New Testament is not the selected source and is not interchangeable with `MDYTWF / mdyeth`.

## 3. Independent technical re-verification — 2026-11-12

The original archive was retrieved from the Owner's project Library and checked in an isolated working directory. The public repository contains only metadata and hashes; no scripture text or archive was added.

| Check | Result |
|---|---|
| Archive byte size and SHA-256 | PASS — exact match |
| ZIP directory entries / CRC | PASS — 70 entries; CRC test clean |
| Package contents | 66 USFM files + 4 supporting files |
| GPG clear-signature | PASS — GOOD SIGNATURE |
| Signing key fingerprint | `54D714D76956DA6673DC1EEA5F62009D93505F26` |
| Signed checksum list | PASS — 68 entries; 68/68 match |
| USFM file hashes against stored per-file manifest | PASS — 66/66 |
| Owner-approved book inventory | PASS — 66/66 unique IDs; no missing or unexpected IDs |
| Chapter inventory | PASS — 1,189 chapters; contiguous from 1 per book |
| Verse-marker lines | 30,506 |
| Represented verse spans including ranges | 31,102 |
| Chapters without verse markers | 0 |
| Strict UTF-8 | PASS — 66/66 |
| Unicode replacement characters | 0 |
| Non-NFC files | 0 |
| Unexpected control characters | 0 |
| Ethiopic-range code points | 2,108,248 |

**Cryptographic trust qualification:** GPG verifies the signature and signed content, but the isolated keyring trust state is **UNDEFINED**. The key's identity/trust certification is not independently asserted. A valid signature is not the same as independently certified signer identity.

The archive member order is not canonical. Application order is governed by the Owner-approved inventory, not ZIP entry order.

## 4. A2 gate register

| Gate | Acceptance criterion | Result |
|---|---|---|
| A2-01 | Exact edition identity and Owner-approved 66-book inventory | **PASS** |
| A2-02 | Rights authority and exact package licence | **PASS — DOCUMENTARY**, with email/signatory authentication caveat |
| A2-03 | Product permission for offline storage, display, search, annotations and sharing | **PASS — DOCUMENTARY**, subject to licence conditions |
| A2-04 | CC BY-SA 4.0 obligations identified and compliance controls specified | **PASS — SPECIFIED**; application/release implementation remains a later release gate |
| A2-05 | Exact package acquired under Owner authorization; provenance and archive digest recorded | **PASS — REAL SOURCE** |
| A2-06 | Archive, signature, file hashes, inventory and USFM structure validated | **PASS — REAL SOURCE** |
| A2-07 | UTF-8, Unicode and Ethiopic diagnostics | **PASS — MACHINE**; specialist review deferred and Owner-accepted |
| A2-08 | Metadata-only immutable baseline, per-file hashes, independent reproducibility and Owner acceptance | **PASS — OWNER ACCEPTED 2026-11-07; REPRODUCED 2026-11-12** |

## 5. Residual risks and accepted exceptions

1. **Maale-language/Ethiopic-script specialist review — DEFERRED.** The Owner accepted this as a residual risk on 2026-11-07. It remains unperformed. Do not describe the translation, orthography, or language quality as specialist-validated.
2. **Email/signatory authentication — DOCUMENTARY CAVEAT.** The project records the written response supplied by the Owner and corroborating public edition/licence records. Original email headers, delivery metadata, sender identity and signatory authority have not been independently authenticated in this repository.
3. **GPG key trust — UNDEFINED.** The cryptographic signature is valid; the public key's trust certification is not independently established.
4. **Application licence implementation — NOT COMPLETE.** Attribution display, licence-link behavior, modification notices, ShareAlike handling, export/share notices, asset rights and release blocking remain product/release acceptance controls under `LICENCE_COMPLIANCE_CONTROLS.md`; their implementation is not claimed by A2 closure.

These limitations do not erase the Owner's recorded A2 baseline acceptance. They remain visible, bounded risks and later release constraints.

## 6. Binding restrictions

- `content_intake_authorized=false` remains binding.
- Do not commit raw USFM/USX, scripture excerpts, audio, scans, or derived scripture datasets to this public repository.
- Keep the original archive and extracted scripture files in restricted, non-public evidence storage.
- Audio, images, fonts, logos, maps, recordings and third-party metadata require separate rights evidence.
- Do not silently normalize, correct, transliterate, paraphrase or alter source scripture.
- A2 closure does not grant merge, signing, deployment, publication or release approval.

## 7. Reproducible metadata artifacts

- `content/manifests/maale-source-baseline-manifest.json` — metadata-only immutable baseline.
- `content/manifests/maale-usfm-file-hashes.json` — 66 per-file hashes and sizes; no scripture text.
- `content/manifests/candidate-book-inventory.json` — Owner-approved 66-book expected inventory.
- `docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md` — original validation report.
- `docs/source-verification/A2_COMPLETION_AND_BLOCKER_REGISTER.md` — gate and residual-risk register.

## 8. A2 exit statement

**A2 source-readiness is CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION.** All machine-verifiable source gates have recorded passing evidence and were independently rechecked against the original archive on 2026-11-12. The specialist review remains deferred, not passed. Production content intake remains false. No subsequent phase is entered by this closure record.
