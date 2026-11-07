# A2 Scope and Acceptance

Status: CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION (2026-11-07)  
Owner approval: A2 source-verification implementation and exact 2008 Full Bible acquisition/inspection authorization reaffirmed 2026-11-07  
Repository baseline at start: 8b17b5830a19b63814c025db63b75e4b76855a74

## Objective

Establish an auditable, rights-compliant and immutable source baseline for the 2008 Maale Full Bible before production content intake or Android implementation.

## In scope

- Edition and rights reconciliation for MDYTWF / mdyeth.
- CC BY-SA 4.0 package compliance.
- Exact `mdyeth_usfm.zip` acquisition into an isolated validation workspace.
- Archive hashing, ZIP metadata preflight, safe extraction and chain of custody.
- Per-file hashes and source manifest.
- USFM book/chapter/verse structural validation against the Owner-approved 66-book inventory.
- Unicode, Ethiopic-script and encoding diagnostics.
- Named language/script specialist review and independent reproducibility.
- Immutable A2 source baseline and evidence-backed closure.

## Owner acceptance and risk disposition

On 2026-11-07 the Owner stated: “Accept all and approved to proceed.” This accepts the real-source candidate immutable baseline and authorizes progression to A3. The named Maale-language/Ethiopic-script specialist review is explicitly deferred and accepted as a residual risk; it is not marked passed and no specialist sign-off is claimed. A2-07 machine diagnostics passed. Independent second-pass verification reproduced archive CRC, all 66 USFM hashes, 1,189 chapters and 30,506 verse-marker lines.

A2 closure does not by itself authorize raw scripture publication or distribution. `content_intake_authorized=false` remains in force until a separate content-ingestion decision is recorded. Audio and third-party assets remain separately gated.

## Explicitly out of scope

- Committing raw scripture files, verse excerpts, or derived scripture datasets to the public repository.
- Production ingestion/distribution, Android application, Gradle/Kotlin/Compose, backend, database, cloud sync, or deployment.
- Audio and third-party images/assets; these remain separately rights-gated.

## A2 gates

| Gate | Required evidence | Current status |
|---|---|---|
| A2-01 Edition identity and canon | Exact edition and approved expected book inventory | PASS — official edition-specific list and Owner decision |
| A2-02 Rights authority | Rights-holder evidence and package licence | PASS — documentary |
| A2-03 Product permission | Offline storage, display, search, annotations, sharing | PASS — documentary |
| A2-04 Licence reconciliation | CC BY-SA 4.0 obligations | PASS — documentary |
| A2-05 Source acquisition | Exact archive, provenance, size and SHA-256 | AUTHORIZED — retrieval attempted, technical blocker |
| A2-06 Structural validation | Real package inventory and USFM audit | NOT RUN — archive unavailable |
| A2-07 Character validation | Real-source Unicode/encoding checks and specialist review | NOT RUN — archive unavailable |
| A2-08 Immutable baseline | Complete manifest, hashes, validation and Owner acceptance | BLOCKED — dependent on A2-05 through A2-07 |

## Acceptance rule

A2 closes when machine-verifiable gates have passing real-source evidence, the immutable baseline is independently reproducible, and the Owner accepts the baseline and any explicitly identified residual risks. The Owner has accepted the specialist-review gap as a deferred risk on 2026-11-07. Synthetic tests do not substitute for real-source validation.

## Current evidence

See [A2 Completion and Blocker Register](../source-verification/A2_COMPLETION_AND_BLOCKER_REGISTER.md), [Approved 66-book Inventory](../source-verification/CANDIDATE_CANON_INVENTORY.md), and [Language/Script Review Protocol](../source-verification/LANGUAGE_SCRIPT_REVIEW_PROTOCOL.md).

A1 remains CLOSED — FINAL. A2 is CLOSED — OWNER-ACCEPTED WITH EXPLICIT EXCEPTION. A3 is authorized to proceed under the separately recorded A3 scope and implementation gates.
