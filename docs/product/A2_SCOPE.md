# A2 Scope and Acceptance

Status: IN PROGRESS — IMPLEMENTATION WORK PACKAGE COMPLETE; FORMAL ACCEPTANCE BLOCKED  
Owner approval: Option 1 — A2 documentation and source-verification implementation  
Repository baseline at start: 8b17b5830a19b63814c025db63b75e4b76855a74

## Objective

Establish an auditable, rights-gated process for identifying, obtaining, validating, and freezing an authoritative Maale Bible source before any scripture content is admitted to this repository or any Android implementation begins.

## In scope

- Edition and publisher identity research and source register.
- Written rights and permission evidence tracking.
- USFM package intake requirements and local structural audit tooling.
- ZIP metadata preflight and safe package-handling requirements.
- Manifest schema, SHA-256 inventory, and reproducible validation reports.
- Synthetic test fixtures that contain no actual scripture text.
- Decision records and evidence-based A2 acceptance.
- Licence compliance controls and source-package safety/provenance readiness.
- Provisional canon inventory and language/script specialist review protocol.

## Explicitly out of scope

- Actual Bible, scripture, translation, audio, or sample-verse content.
- Android application, Gradle project, Kotlin, Compose, or mobile UI code.
- Backend, database, account, cloud sync, or deployment implementation.
- Republishing, modifying, converting, or distributing any candidate source before source acquisition and intake are separately authorized.
- Treating a public download, metadata listing, or third-party licence label alone as sufficient permission.

## A2 gates

| Gate | Required evidence | Current status |
|---|---|---|
| A2-01 Edition identity and canon | Exact edition, primary contents evidence, approved canon and inventory | PARTIAL — 66-book candidate inventory prepared; primary-edition verification, specialist review and Owner approval pending |
| A2-02 Rights authority | Rights-holder identity and authority to license the exact edition | PARTIAL — written response supplied; original email/header and signatory authentication review pending |
| A2-03 Product permission | Written permission covering app distribution, offline storage, display, search, sharing and adaptation subject to licence | PASS — DOCUMENTARY; audio and third-party assets excluded and separately gated |
| A2-04 Licence reconciliation | Reconciled publisher terms, repository/source terms and any reserved/library records | PASS — DOCUMENTARY; compliance controls specified |
| A2-05 Source acquisition | Owner-authorized source package, provenance, acquisition date and source URL | BLOCKED — separate Owner authorization not granted |
| A2-06 Structural validation | Complete real-source book/chapter/verse inventory validated against an approved reference | NOT RUN — no authorized source package or approved inventory |
| A2-07 Character validation | Real-source Unicode/script review and Maale-language specialist acceptance | NOT RUN — no authorized source package or named specialists |
| A2-08 Immutable baseline | Approved manifest, actual hashes, reviewer sign-off and frozen source revision | BLOCKED — dependent on A2-01 and A2-05 through A2-07 |

## Acceptance rule

A2 is not formally accepted while any required evidence is MISSING, UNRESOLVED, NOT RUN, or BLOCKED. Documentary rights reconciliation does not authorize source acquisition. Automated structural checks do not establish translation accuracy, completeness, canon, legal permission, or linguistic correctness. All such matters require independent documentary or specialist evidence.

The 66-book candidate inventory is explicitly provisional and must not be used as the expected source inventory until primary-edition evidence, specialist review, and Owner approval are recorded.

## Repository content rule

Only project documentation, schemas, validation tools, synthetic tests, and non-scriptural metadata may be committed during A2. Do not commit USFM, USX, PDF, DOCX, audio, verse excerpts, screenshots containing scripture, or derived scripture datasets until the Owner explicitly approves a verified rights package and content baseline.

## Change control

Changes to the target edition, canon, licence, rights interpretation, source package, validation acceptance criteria, or content admission policy require Owner review. Record decisions in docs/decisions/. A1 remains CLOSED — FINAL and is not reopened for routine A2 execution.

## Current completion record

See [A2 Completion and Blocker Register](../source-verification/A2_COMPLETION_AND_BLOCKER_REGISTER.md), [Provisional Canon Inventory](../source-verification/CANDIDATE_CANON_INVENTORY.md), and [Language/Script Review Protocol](../source-verification/LANGUAGE_SCRIPT_REVIEW_PROTOCOL.md). The implementation work package is complete for the currently authorized scope; formal A2 acceptance remains blocked by the gates above.
