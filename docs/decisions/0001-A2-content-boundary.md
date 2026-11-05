# Decision 0001 — A2 Content Boundary

Date: 2026-11-05  
Status: APPROVED FOR A2 EXECUTION  
Authority: Owner approval of Option 1 in the project conversation

## Decision

Proceed with A2 documentation and source-verification implementation only.

## Authorized

- Product and source-verification documentation.
- Candidate source metadata register with unverified status clearly stated.
- Rights and licensing evidence checklist.
- Source manifest schema and SHA-256 inventory conventions.
- Local USFM structural audit tooling.
- Synthetic-only parser tests and developer guidance.

## Prohibited until separate approval

- Committing scripture text, USFM/USX packages, excerpts, audio, scans, or derived scripture content.
- Android/Kotlin/Compose application implementation.
- Backend, database, cloud sync, or deployment implementation.
- Treating a public source listing or automated audit as publishing permission.

## Control

The audit utility must never authorize content intake or publication. Rights evidence and structural checks are separate gates. A2 acceptance requires independent rights reconciliation, linguistic/script review, a trusted expected inventory, reproducible evidence, and explicit Owner sign-off.
