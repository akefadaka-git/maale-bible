# ZIP Archive Preflight — Synthetic Verification Evidence

Date: 2026-11-07  
Scope: Metadata-only ZIP safety preflight. No source archive or scripture content was acquired, extracted, or inspected.

## Exact source evidence

- Module Git blob: `e0498027c7384cac3a4b0096bdd4b869580ed9fa`.
- Test Git blob: `8242c29cfc95dbcfcad5864c2bb6e63917203983`.
- Execution environment: isolated workspace reconstructed from exact committed GitHub blobs.
- Result: 14/14 synthetic tests passed, exit code 0.

## Covered cases

- Path traversal and absolute paths.
- Drive-letter and backslash path forms.
- Case-fold and Unicode-normalization collisions.
- Symlink entries.
- Nested archives.
- Per-file and total uncompressed-size limits.
- Compression-ratio and entry-count limits.
- Corrupt ZIP archives.
- A valid placeholder ZIP.

## Safety properties

The utility reads ZIP central-directory metadata and hashes the archive. It does not extract archive entries or print their contents. A successful preflight is not proof of rights, edition identity, linguistic accuracy, canon completeness, or publication readiness.

## Governance status

- Real source acquisition: NOT AUTHORIZED by this utility or its test suite.
- Content intake authorization: FALSE.
- Production scripture ingestion/distribution: BLOCKED pending separate Owner authorization and rights controls.
- Independent Maale-language/Ethiopic-script specialist review: remains an explicit residual-risk item from the accepted A2 baseline.
