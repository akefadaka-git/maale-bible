# P1.1 Exact-Head Closure Evidence — 2026-11-14

**Branch:** `implementation/p1-1-source-ingestion-2026-11-14`  
**Implementation code head reviewed:** `201fb61456f9ca29c5734675a91177a009a62ee1`  
**Implementation status:** PARTIAL — metadata and synthetic-only validator foundation implemented  
**Closure status:** NOT CLOSED  
**Merge / release / signing / deployment:** NOT AUTHORIZED

## Frozen metadata-only inputs

- Selected source: MDYTWF / mdyeth, 2008 Maale Full Bible.
- Archive SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`.
- Per-file metadata hash register: `content/manifests/maale-usfm-file-hashes.json`.
- Frozen expected inventory: 66 unique USFM book IDs, drawn from the existing hash register. The validator compares the ID set; it does not infer display order from ZIP order.
- Historical A2 counts: 1,189 chapters; 30,506 verse-marker lines.
- New P1 manifest, policy, source schema and report schema contain metadata only. Distribution is explicitly NOT APPROVED.
- Rights state: A2 baseline accepted with exception; original signer identity/authority caveat remains; Maale specialist linguistic review DEFERRED.

## Implemented

- Metadata-only source manifest: `content/manifests/maale-p1-source-manifest.v1.json`.
- Frozen validation policy: `content/manifests/p1-source-validation-policy.v1.json`.
- Source-manifest and redacted-report JSON schemas.
- SHA-256 verifier with known-vector and mismatch tests.
- ZIP safety extraction: archive hash pre-check, normalized-path and traversal rejection, duplicate-path detection, symlink/encryption rejection, entry/expanded-size and compression-ratio limits, bounded streaming and staging cleanup on failure.
- 66-book USFM inventory validator with strict UTF-8 decoding, NFC, replacement-character and control-character checks.
- Redacted report writer that accepts only stable error codes and sets `scriptureTextIncluded=false`.
- Restricted-only marker inventory generator: verifies each expected file's byte size and SHA-256, rejects duplicate IDs, counts marker tokens, and writes only marker counts and per-book metadata.
- Maven/JUnit test suite and synthetic-only GitHub Actions workflow.

## Verification actually performed

| Check | Result | Scope / limitation |
|---|---|---|
| Source manifest assembled from existing metadata hash register | PASS | 66 unique IDs; no scripture text copied |
| Marker-inventory generator algorithm | PASS | Local Python synthetic fixture: 66 files; marker counts correct; output contained no fixture text. This is not the Maale corpus run |
| Core Java compilation | PASS | Local `javac` compilation of SHA-256, Unicode, inventory and redacted-report classes on Java 21; not the full Maven build |
| Redacted report serialization | PASS | Local Java execution; parsed output as JSON and confirmed text-free report flag |
| Full Maven/JUnit suite | NOT RUN | Maven is unavailable in the local execution environment |
| GitHub Actions exact-head status | UNKNOWN / NOT RETURNED | GitHub combined status query returned no status checks for the implementation head; no successful workflow run is claimed |
| Commons Compress ZIP validator compilation/runtime | NOT RUN | Requires Maven dependency resolution; no local Maven executable |
| JSON Schema validation | NOT RUN | Schema files committed; formal schema test remains required |
| Actual 2008 Maale archive extraction and per-file verification | NOT RUN | Exact archive not present in an accessible Owner-approved restricted workspace |
| Corpus-derived USFM marker inventory | BLOCKED | No corpus-derived marker counts generated; marker profile remains explicitly pending |
| Restricted redaction scan | NOT RUN | Synthetic output only; restricted corpus evidence is absent |

## Required evidence to close P1.1

1. Run the complete Maven/JUnit suite on the exact implementation branch head and retain the successful run URL and SHA.
2. In the Owner-approved restricted workspace, verify the archive SHA before extraction, then verify all 66 file hashes, inventory, UTF-8/NFC and control-character checks.
3. Run `tools/scripture-ingest/scripts/generate_marker_inventory.py` against that exact verified corpus. Store its output only in restricted evidence storage unless a separate disclosure review approves a metadata-only public profile.
4. Produce a redacted validation report and scan it for scripture text, source excerpts, protected payload, and user data.
5. Preserve the rights/provenance exception and DEFERRED specialist-review state. A technical PASS is not distribution approval.

**Disposition:** PARTIAL. The metadata-only contract and synthetic implementation are committed, but P1.1 is not VERIFIED-CLOSED. P1.2 parser implementation, package signing, package activation, merge and distribution remain outside this closure.
