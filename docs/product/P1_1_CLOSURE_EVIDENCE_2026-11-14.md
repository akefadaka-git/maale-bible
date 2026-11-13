# P1.1 Exact-Head Closure Evidence — 2026-11-14

**Branch:** `implementation/p1-1-source-ingestion-2026-11-14`  
**Implementation status:** PARTIAL — public synthetic implementation prepared; restricted corpus evidence pending  
**Closure status:** NOT CLOSED  
**Merge / release / signing / deployment:** NOT AUTHORIZED

## Frozen metadata-only inputs

- Selected source: MDYTWF / mdyeth, 2008 Maale Full Bible.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Metadata file-hash manifest: `content/manifests/maale-usfm-file-hashes.json`.
- Expected inventory: 66 unique USFM book IDs; 1,189 chapters; 30,506 verse-marker lines (historical A2 evidence).
- Policy and P1 manifest are metadata-only; distribution remains explicitly NOT APPROVED.
- Rights state: A2 baseline accepted with exception; provenance caveat remains; Maale specialist linguistic review DEFERRED.

## Implemented in this branch

- Metadata-only source manifest and JSON Schema.
- Frozen source validation policy and redacted report schema.
- SHA-256 verifier.
- Safe ZIP extraction with archive hash pre-check, normalized path checks, traversal/symlink/encryption/size/ratio/duplicate rejection, and bounded streaming.
- 66-book inventory validator with strict UTF-8/NFC/control-character checks.
- Redacted report writer that accepts only stable error codes and records scriptureTextIncluded=false.
- Restricted-only marker inventory generator; it verifies every file hash before counting marker tokens and never emits verse text.
- Maven/JUnit synthetic test suite and synthetic-only GitHub Actions workflow.

## Evidence status

| Requirement | Status | Evidence |
|---|---|---|
| Metadata manifest frozen | PASS — metadata-only file committed; exact-head verification pending | `content/manifests/maale-p1-source-manifest.v1.json` |
| SHA-256 verifier | IMPLEMENTED; test execution pending | `Sha256Verifier.java`, synthetic digest vectors |
| Safe archive extraction | IMPLEMENTED; negative coverage partial; test execution pending | `ArchiveSafetyValidator.java` |
| 66-book inventory | IMPLEMENTED against the frozen 66-ID list; actual corpus rerun pending | `UsfmInventoryValidator.java` |
| Strict Unicode | IMPLEMENTED; synthetic test execution pending | `UnicodeValidator.java` |
| Corpus-derived marker inventory | BLOCKED — exact archive not present in accessible restricted workspace | restricted-only generator is committed; no fabricated inventory |
| Synthetic tests | NOT RUN in this report | workflow must complete on exact head |
| Report redaction | IMPLEMENTED; test execution pending | `RedactedReport.java`, `RedactedReportTest.java` |
| Restricted source validation | NOT RUN | restricted workspace access/evidence unavailable |
| Rights / provenance / specialist review | EXCEPTION / CAVEAT / DEFERRED | unchanged A2 status |

## Exact-head gate

The closure record must be amended only after:
1. CI synthetic tests pass on the exact branch head SHA.
2. Restricted operator records archive digest, file-hash verification, 66-book inventory, strict Unicode results and corpus-derived marker counts in restricted evidence.
3. Public report is scanned and proven free of scripture text, raw excerpts and protected payload.
4. Rights/provenance and specialist-review states remain accurately disclosed.

**Current disposition:** P1.1 implementation is PARTIAL. Do not mark VERIFIED-CLOSED; do not start P1.2 parser implementation or activate/distribute packages under this evidence alone.
