# A2 Scope and Acceptance

Status: IN PROGRESS  
Owner approval: Option 1 — A2 documentation and source-verification implementation  
Repository baseline at start: 8b17b5830a19b63814c025db63b75e4b76855a74

## Objective

Establish an auditable, rights-gated process for identifying, obtaining, validating, and freezing an authoritative Maale Bible source before any scripture content is admitted to this repository or any Android implementation begins.

## In scope

- Edition and publisher identity research and source register.
- Written rights and permission evidence tracking.
- USFM package intake requirements and local structural audit tooling.
- Manifest schema, SHA-256 inventory, and reproducible validation reports.
- Synthetic test fixtures that contain no actual scripture text.
- Decision records and evidence-based A2 acceptance.

## Explicitly out of scope

- Actual Bible, scripture, translation, audio, or sample-verse content.
- Android application, Gradle project, Kotlin, Compose, or mobile UI code.
- Backend, database, account, cloud sync, or deployment implementation.
- Republishing, modifying, converting, or distributing any candidate source before rights clearance.
- Assuming that a public download, metadata listing, or third-party license label is sufficient permission for this product.

## A2 gates

| Gate | Required evidence | Current status |
|---|---|---|
| A2-01 Edition identity | Written confirmation of exact edition, canon, revision, language/script, publisher and contributors | MISSING |
| A2-02 Rights authority | Rights-holder identity and authority to license the exact edition | MISSING |
| A2-03 Product permission | Written permission covering app distribution, offline storage, display, search, quotation, modification and audio if applicable | MISSING |
| A2-04 License reconciliation | Reconciled publisher terms, repository/source terms and any reserved/library records | MISSING |
| A2-05 Source acquisition | Authorized source package, provenance, acquisition date and source URL | BLOCKED |
| A2-06 Structural validation | Complete book/chapter/verse inventory validated against an approved reference | NOT RUN |
| A2-07 Character validation | Unicode/script review and Maale-language specialist acceptance | NOT RUN |
| A2-08 Immutable baseline | Approved manifest, hashes, reviewer sign-off and frozen source revision | BLOCKED |

## Acceptance rule

A2 is not accepted while any rights gate is MISSING, UNRESOLVED, or BLOCKED. Automated structural checks do not establish translation accuracy, completeness, canon, legal permission, or linguistic correctness. All such matters require independent documentary or specialist evidence.

## Repository content rule

Only project documentation, schemas, validation tools, synthetic tests, and non-scriptural metadata may be committed during A2. Do not commit USFM, USX, PDF, DOCX, audio, verse excerpts, screenshots containing scripture, or derived scripture datasets until the Owner explicitly approves a verified rights package.

## Change control

Changes to the target edition, canon, license, rights interpretation, source package, validation acceptance criteria, or content admission policy require Owner review. Record decisions in docs/decisions/.
