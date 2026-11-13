# P1.1 Implementation Task Specification — Restricted Scripture Source Validation and Ingestion Foundation

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Date:** 2026-11-14  
**Status:** IMPLEMENTATION SPECIFICATION — NOT STARTED; OWNER AUTHORIZATION REQUIRED  
**Depends on:** Owner approval of `P1_0_ENGINEERING_CONTRACT_DECISIONS_2026-11-14.md`  
**Requirements baseline:** P0 v1.0, frozen at `61902345e7ff2d6cd831f433bee33f5775412864`  
**Inspected app baseline:** PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**Planning branch:** `planning/p1-critical-bible-experience-2026-11-14`

## 1. Scope and explicit exclusions

P1.1 implements the **source validation and deterministic ingestion foundation** for the first approved Maale source. It establishes safe acquisition/staging contracts, archive/file integrity validation, corpus inventory validation, parser boundary interfaces and a redacted validation report.

P1.1 does **not** implement the Android reader, UI, catalogue grouping, E.C./G.C. selector, package signing, production trust-root/key generation, package distribution, public scripture hosting or full package activation. P1.1 must not place scripture payload or source archives in the public repository.

No implementation begins until the Owner explicitly authorizes P1.1 after reviewing the five engineering contracts. This specification is not that authorization.

## 2. Source scope and hard gates

Initial target: 2008 Maale Full Bible `MDYTWF / mdyeth`, source archive `mdyeth_usfm.zip`, recorded archive SHA-256:

`4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`

Recorded licence: CC BY-SA 4.0. Source baseline recorded 66/66 USFM files, 1,189 chapters, 30,506 verse-marker lines, strict UTF-8/Unicode checks passing.

Before any processing:
- Acquire only the exact source/revision already approved in the content authority reconciliation, from an authorized channel.
- Verify archive SHA-256 against the approved manifest before extraction.
- Verify per-file SHA-256 against the approved file hash manifest after safe extraction.
- Preserve attribution and licence metadata and the original source identity.
- Preserve the unresolved trust caveat: GPG signature is cryptographically valid but signer-key trust is undefined; repository does not independently authenticate original rights-holder email identity/authority.
- Keep Maale specialist linguistic review marked DEFERRED until qualified review is completed.
- Do not substitute 2007 `MDYBSE` NT.
- Stop if exact archive/hash cannot be obtained or any rights/provenance contradiction appears. Do not fetch a convenient alternative and treat it as equivalent.

P1.1 technical validation is not a new rights approval and does not grant permission to redistribute the source archive or generated scripture payload.

## 3. Restricted source-staging requirements

### 3.1 Workspace

- Use an Owner-approved restricted workspace with access limited to authorized project maintainers/reviewers.
- The public GitHub repository contains only validator/parser source code, schemas, metadata-only manifests, synthetic fixtures and redacted reports.
- Raw source archive, extracted USFM, parsed verse text, generated SQLite payload, indexes containing verse text, golden fixtures containing protected passages and temporary intermediates remain outside public Git.
- Do not upload scripture files to public GitHub Actions artifacts, logs, caches, issue attachments, PR comments, releases or public package registries.
- Do not use unapproved third-party online converters, translation APIs, AI endpoints or cloud buckets for scripture processing.
- If CI execution is required, use synthetic fixture corpus in public CI. Actual source validation runs in the approved restricted environment; publish only redacted evidence.
- Disable shell tracing and verbose parser output for source processing. Error messages may include file ID, marker, line/byte offset and error code, never verse text.
- Restrict temporary files and crash dumps; clean staging on success/failure according to an approved retention policy. Do not delete the only approved source copy.
- Record workspace identity, operator, access boundary, acquisition timestamp, source hash and tool versions in a restricted audit log.

### 3.2 Path and archive safety

Reject:
- absolute paths, drive-letter paths, UNC paths, `..` traversal, NUL bytes and paths escaping staging;
- duplicate paths after normalization or case-folding where the target filesystem is case-insensitive;
- symbolic links, hard links, device nodes and unexpected archive entry types;
- encrypted ZIP entries unless separately approved and supported;
- excessive entry count, individual file size or total expanded size beyond configured limits;
- ZIP bombs, suspicious compression ratios, CRC failures, truncated entries and unrecognized encodings.

Extract each archive entry to a newly created staging root using normalized relative paths. Never trust the archive's declared path or size alone. Verify ZIP CRC and independently calculate SHA-256 for every extracted file.

## 4. Exact proposed file layout

All paths below are proposed implementation targets and must be reconciled against the latest repository tree before creating files. Existing paths must be updated, not duplicated.

### 4.1 Public repository — code and metadata only

```
content/
  source-register/
    README.md                         # existing: update only if needed
  manifests/
    maale-source-baseline-manifest.json  # existing: verify schema; no payload
    maale-usfm-file-hashes.json          # existing: verify hashes
    p1-source-validation-policy.v1.json  # new: limits, required inventory, encoding policy
    p1-usfm-marker-profile.v1.json       # new: corpus-derived allowlist and semantics
  schemas/
    scripture-source-manifest.schema.json # new
    scripture-validation-report.schema.json # new, redacted output
tools/
  scripture-ingest/
    README.md                           # new: restricted-run procedure
    settings.gradle.kts                 # new if standalone Kotlin/JVM module chosen
    build.gradle.kts                    # new if standalone Kotlin/JVM module chosen
    src/main/kotlin/org/maale/bible/ingest/
      SourceManifest.kt                 # new
      ArchiveSafetyValidator.kt         # new
      Sha256Verifier.kt                 # new
      UsfmInventoryValidator.kt         # new
      UnicodeValidator.kt               # new
      ValidationPipeline.kt             # new
      ValidationReport.kt               # new
      RestrictedWorkspace.kt            # new
    src/test/kotlin/org/maale/bible/ingest/
      ArchiveSafetyValidatorTest.kt
      Sha256VerifierTest.kt
      UsfmInventoryValidatorTest.kt
      UnicodeValidatorTest.kt
      ValidationPipelineTest.kt
      RedactedReportTest.kt
docs/
  product/
    P1_1_SOURCE_INGESTION_VALIDATION_RUNBOOK.md # new
```

Prefer a standalone Kotlin/JVM tool with no Android runtime dependency so source validation is deterministic and can run in a restricted workstation. Do not add parser libraries until license, maintenance, supported USFM profile and deterministic behavior have been reviewed. If a maintained parser is selected, pin the exact version and add adapter tests.

### 4.2 Runtime Android files — not part of P1.1 implementation

P1.1 may define interfaces but must not wire the Android application or modify existing database migrations. Later work-package candidates:

```
app/src/main/java/org/maale/bible/domain/scripture/
  EditionId.kt
  BookId.kt
  PassageRef.kt
  ChapterRef.kt
  ScriptureRepository.kt
app/src/main/java/org/maale/bible/data/scripture/
  ScripturePackageManifest.kt
  ScripturePackageVerifier.kt
  ScripturePackageInstaller.kt
  ScripturePackageRegistry.kt
  ScriptureContentDatabase.kt
```

These paths are architectural candidates only, not P1.1 authorized file changes.

## 5. Interfaces and data contracts

### 5.1 Input manifest

Metadata-only `ScriptureSourceManifest` fields:

- `manifestVersion`
- `sourceId`, `editionId`, `languageTag`
- `sourceName`, `publisher`, `rightsHolder`
- `sourceRevision`, `sourceArchiveName`, `expectedArchiveSha256`
- `expectedFiles[]` with relative path, expected size (if authoritative) and SHA-256
- `expectedBookIds[]` in canonical order
- `expectedChapterCountsByBook`
- `expectedLicenceId`, `attributionRecordId`
- `sourceProfileId`, `usfmVersionProfile`
- `reviewState`, `linguisticReviewState`

The public manifest must not include protected verse text or private source correspondence. Any confidential provenance records remain in restricted evidence storage.

### 5.2 Validator interfaces

- `ArchiveSafetyValidator.validate(archive, limits): ArchiveValidationResult`
- `Sha256Verifier.verify(file, expectedDigest): DigestVerificationResult`
- `UsfmInventoryValidator.validate(stagedRoot, sourceManifest): InventoryValidationResult`
- `UnicodeValidator.validate(file, encodingPolicy): UnicodeValidationResult`
- `ValidationPipeline.run(source, manifest, policy): SourceValidationReport`
- `ValidationReportWriter.writeRedacted(report, destination): RedactedReport`

Every result includes status, stable error codes and evidence metadata. It must not include raw verse text.

### 5.3 Result states

Top-level result:
- `PASS_TECHNICAL_VALIDATION`
- `FAIL_ARCHIVE_INTEGRITY`
- `FAIL_FILE_INTEGRITY`
- `FAIL_INVENTORY`
- `FAIL_ENCODING`
- `FAIL_USFM_STRUCTURE`
- `BLOCKED_RIGHTS_REVIEW`
- `BLOCKED_SOURCE_PROVENANCE`
- `BLOCKED_LINGUISTIC_REVIEW`
- `NOT_RUN`

A technical PASS does not transition the source to distribution approval. The report must keep separate fields for technical, rights, provenance and linguistic states.

## 6. P1.1 task breakdown and acceptance

| Task | Deliverable | Required tests / evidence | Exit |
|---|---|---|---|
| P1.1-01 | Source manifest schema and validation policy | JSON schema tests; required identity/licence/hash fields; invalid/missing field tests | PASS |
| P1.1-02 | Restricted workspace runner and redaction rules | Workspace boundary tests; logs/artifacts scan; retention behavior | PASS |
| P1.1-03 | Safe archive validator | traversal, absolute path, duplicate path, symlink, zip bomb, CRC, truncation and size-limit tests | PASS |
| P1.1-04 | Archive/file SHA-256 verifier | known digest vectors; altered archive/file; empty file; mismatch | PASS |
| P1.1-05 | USFM file inventory validator | 66 expected IDs/files, missing/extra/duplicate files, duplicate IDs, deterministic ordering | PASS |
| P1.1-06 | Encoding/Unicode validator | strict UTF-8, malformed sequences, BOM policy, control chars, combining marks and Ethiopic text | PASS |
| P1.1-07 | Corpus-derived marker inventory | exact marker-frequency inventory generated in restricted workspace; compare with frozen marker profile | PASS |
| P1.1-08 | Redacted validation report | schema validation; no verse text, no user data, stable digest references and provenance metadata | PASS |
| P1.1-09 | Reproducibility and rerun evidence | same input/tool versions yield same report content apart from explicitly excluded run timestamps | PASS |
| P1.1-10 | P1.1 closure report | exact source hash, per-file results, tool versions, workspace evidence, failures/exceptions and exact commit SHA | VERIFIED |

### Mandatory negative tests

- Wrong archive hash must stop before extraction.
- Any per-file digest mismatch must fail the entire edition.
- Unsafe archive path/type must fail before writing outside staging.
- Missing, duplicate or unexpected book file must fail inventory validation.
- Invalid UTF-8 must fail; never replace invalid bytes with U+FFFD.
- Unexpected USFM marker must fail marker-profile validation.
- Any attempt to emit verse text in a report/log must fail redaction tests.
- Rights/provenance state not approved must block any distribution-ready status even when all technical tests pass.

## 7. Source-derived USFM marker inventory

Before parser implementation, the restricted tool must inventory markers actually used by the exact approved corpus. The inventory output includes marker name, occurrence count, file count, context category and parser support decision; it must not include marker-adjacent scripture text.

Every encountered marker receives one of:
- `SUPPORTED_PRESERVE`
- `SUPPORTED_RENDER`
- `SUPPORTED_STRUCTURED_NONRENDERED`
- `UNSUPPORTED_BLOCKING`

No marker may be silently ignored. The marker profile is frozen only after source inventory and contract review. P1.1 validates and reports marker inventory; full parser semantics are a subsequent P1.2 task.

## 8. Test architecture and execution environments

- Public CI runs synthetic fixtures only; tests cover deterministic validation behavior without licensed source text.
- Restricted source run processes the exact Maale archive and produces a redacted report. Evidence is reviewed by the Owner or delegated authorized reviewer.
- Unit tests must not embed real verse text. Synthetic USFM fixtures use invented, clearly non-scriptural text.
- Property/fuzz tests cover archive paths, marker tokenization boundaries, malformed encodings and oversized inputs.
- Pin JDK, Kotlin, Gradle wrapper and dependency versions; record toolchain versions in the report.
- No network access is required for the validation run after authorized source acquisition; dependency resolution occurs before entering restricted/offline processing.
- Do not depend on GitHub Actions paid minutes or external paid services.

## 9. Definition of done

P1.1 is done only when:
1. All mandatory tests pass on the exact implementation head.
2. The approved archive and all expected files match recorded SHA-256 values in the restricted workspace.
3. The expected book/file inventory is complete and ordered deterministically.
4. Encoding and Unicode validation passes without lossy repair.
5. Every source marker is inventoried and assigned an explicit support decision.
6. Validation reports/logs contain no scripture text.
7. The source rights/provenance/linguistic limitations remain accurately represented.
8. Public repository diff contains no raw archive, USFM, scripture text, generated content database or text-bearing index.
9. Closure report records exact source archive digest, tool versions, test results, exact commit SHA and any exceptions.

P1.1 technical completion does not mean the Maale edition is approved for distribution, does not complete P1, and does not authorize merge, signing, deployment or publication.

## 10. Immediate execution sequence after Owner authorization

1. Reconcile these proposed paths against the current branch tree and existing manifests; reuse existing metadata instead of creating duplicates.
2. Freeze source-manifest and validation-report schemas.
3. Implement safe archive handling and hash validation first.
4. Implement inventory and strict Unicode checks.
5. Run public synthetic tests.
6. Execute the actual source validation only in the approved restricted workspace.
7. Produce a redacted report and P1.1 closure evidence.
8. Stop for Owner review before proceeding to parser implementation (P1.2).
