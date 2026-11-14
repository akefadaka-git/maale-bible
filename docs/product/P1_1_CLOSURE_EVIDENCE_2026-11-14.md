# P1.1 Exact-Head Closure Evidence — 2026-11-14

**Branch:** `implementation/p1-1-source-ingestion-2026-11-14`  
**Exact code/test head:** `d4f166e1fb7131826751138be7d0f6f16bb3f750`  
**Implementation status:** PARTIAL — exact-head CI and restricted corpus evidence pass; one integration verification remains  
**Closure status:** NOT CLOSED  
**Merge / release / signing / deployment / publication:** NOT AUTHORIZED

## Frozen metadata-only inputs

- Selected source: MDYTWF / mdyeth, 2008 Maale Full Bible.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Per-file metadata hash register: `content/manifests/maale-usfm-file-hashes.json`.
- Frozen inventory: 66 unique expected USFM book IDs.
- Rights state: A2 baseline accepted with exception; cryptographic signer trust remains undefined; Maale specialist linguistic review remains DEFERRED.
- Distribution state remains NOT APPROVED. Restricted validation does not grant publication or distribution rights.

## Implemented

- Metadata-only source manifest, frozen validation policy, source-manifest and redacted-report schemas.
- SHA-256 verifier and safe ZIP extraction implementation with traversal, normalized duplicate, symlink/special entry, encryption, entry count/size, expanded size, compression ratio, bounded streaming, CRC and cleanup checks.
- 66-book USFM inventory validator with strict UTF-8, NFC, replacement-character and control-character checks.
- Redacted report writer and restricted-only marker inventory generator.
- Maven/JUnit synthetic test suite and GitHub Actions regression test for the Python marker generator.

## Exact-head CI evidence

| Check | Result | Evidence |
|---|---|---|
| P1.1 Python marker-generator regression | PASS | Run [#43550622738](https://github.com/akefadaka-git/maale-bible/actions/runs/43550622738), head `d4f166e1fb7131826751138be7d0f6f16bb3f750`; 1 test passed |
| Maven/JUnit synthetic suite | PASS | Same run; 15 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS` |
| Earlier exact PR-head Maven run | PASS | [#43550202451](https://github.com/akefadaka-git/maale-bible/actions/runs/43550202451), 15 tests passed |
| A2 source-readiness evidence | PASS | [#43550202454](https://github.com/akefadaka-git/maale-bible/actions/runs/43550202454) |

The restricted corpus was never sent to GitHub Actions. CI used synthetic fixtures only.

## Restricted corpus evidence

The original Owner-provided `mdyeth_usfm.zip` was recovered from the Library and materialized into the isolated restricted workspace. It was not downloaded from a substitute source.

| Check | Result | Evidence |
|---|---|---|
| Archive identity | PASS | 1,809,863 bytes; SHA-256 exactly matches the frozen manifest |
| ZIP structure and safe extraction | PASS | 70 entries; 7,554,174 expanded bytes; no traversal, absolute paths, normalized duplicates, symlinks/special entries, encryption, or configured limit violations; CRC-checked extraction completed |
| Archive signature | PASS WITH TRUST CAVEAT | Good signature; fingerprint `54D714D76956DA6673DC1EEA5F62009D93505F26`; GPG trust remains UNDEFINED |
| Signed per-file hashes | PASS | 66/66 USFM files match the archive's signed hash list; no mismatches |
| Book inventory | PASS | 66/66 expected unique book IDs; no missing or unexpected IDs |
| Structural counts | PASS | 1,189 chapters; 30,506 verse-marker lines |
| Unicode | PASS | 66/66 strict UTF-8; 0 non-NFC files; 0 replacement characters; 0 unexpected controls |
| Corpus marker inventory | PASS — RESTRICTED ONLY | Generated with the corrected marker generator; 66 books, 37 marker kinds and 77,099 marker occurrences; output retained only in restricted storage |
| Redaction scan | PASS | Redacted JSON report and marker inventory parse; metadata allowlist validated; no verse text, source excerpts or scripture payload found; `scriptureTextIncluded=false` |

The marker inventory and restricted validation report are intentionally not committed to the public repository or uploaded as public CI artifacts.

## Remaining integration verification

- **NOT RUN:** Execute the production Java `ArchiveSafetyValidator` / `ValidationPipeline` against this exact real archive inside the restricted workspace with the actual Commons Compress dependency. Maven and the dependency are unavailable in the restricted local runtime. The Java implementation passes the synthetic exact-head Maven suite, while the real archive was safely extracted and verified through the isolated independent restricted-workspace procedure above.
- **NOT RUN:** Formal JSON Schema engine validation. The committed schemas were parsed as JSON.
- **NOT RUN:** Maale specialist linguistic review. This remains an explicit A2 exception and must not be inferred from technical checks.

## Disposition

**P1.1 remains PARTIAL and NOT VERIFIED-CLOSED.** Exact-head CI and the restricted corpus checks pass, including real marker inventory generation and redaction verification. Closure is held only on the production Java pipeline's real-archive integration run; no substitute implementation result is represented as that Java run.

P1.2 parser implementation, merge, package signing, activation, release, deployment and distribution remain outside this gate and require separate authorization.
