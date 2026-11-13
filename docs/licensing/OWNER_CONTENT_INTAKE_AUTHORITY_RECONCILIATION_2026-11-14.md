# Owner Content-Intake Authority Reconciliation — 2026-11-14

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Owner:** Akefa Ayele Daka  
**Authority date:** 2026-11-08 (Owner authorization), reconciled into canonical repository record 2026-11-14  
**Repository:** `akefadaka-git/maale-bible`  
**Related baseline:** [Product Requirements Baseline v1.0](../product/PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md)  
**Decision status:** OWNER AUTHORIZATION RECONCILED — SOURCE-SPECIFIC RIGHTS GATES REMAIN  
**Historical A2 status:** CLOSED — unchanged  
**Implementation/release status:** No production content import, public distribution, merge, signing, deployment or publication performed or authorized by this record.

## 1. Decision

The Owner's explicit authorization on 2026-11-08 accepted content intake for:

- Maale Bible content;
- Amharic (AM) Bible content;
- English (EN) Bible content; and
- other necessary Bible and study resources.

The authorization permits controlled source acquisition/intake and evaluation for the Maale Bible project, including offline database, index and derived-resource preparation **only to the extent that the exact source's licence/permission allows those operations**.

Every source remains subject to source-by-source verification of:
1. Exact identity, edition, language, version and provenance.
2. Copyright ownership, licence and/or written permission, including commercial/non-commercial and derivative-use limits.
3. Permitted acquisition, local storage, transformation, indexing, offline display, backup, export, sharing and distribution operations.
4. Required attribution, copyright notices, modification notices and ShareAlike obligations.
5. File integrity, checksums, encoding, structural correctness and compatibility with the app.
6. Linguistic/editorial quality and any specialist review required for the language or content type.

This authorization is not blanket permission to use every discovered online resource. A candidate may be acquired for rights/technical evaluation while remaining blocked from app packaging or user distribution.

## 2. Supersession of older repository wording

Older A2/A3 scope, source-register and README documents state `content_intake_authorized=false`. Those statements accurately recorded the state before the later Owner decision, but they are stale as a statement of current project authority after 2026-11-08.

**Current canonical interpretation from 2026-11-08 onward:**
- Project-level controlled content intake is authorized for the categories above, subject to source-specific gates in this decision.
- The older Boolean flag is superseded for current governance and must not be used to deny the later authorization.
- The older records are historical evidence and are not rewritten by this reconciliation. Their A2 source-readiness closure, dates, scope and accepted residual risks remain unchanged.
- Source-specific rights and release/distribution restrictions continue to control. “Content intake authorized” does not mean “all content approved for distribution.”
- Public repository storage of raw scripture, source archives, protected assets or derived scripture datasets remains prohibited unless the exact licence expressly permits it and the Owner separately approves that publication operation.
- Merge, signing, deployment, store submission and publication remain separately gated and are not authorized here.

## 3. Source-by-source authority matrix

### 3.1 Maale — 2008 Full Bible

| Field | Decision |
|---|---|
| Exact source | 2008 Maale Full Bible, `MDYTWF / mdyeth` |
| Archive | `mdyeth_usfm.zip` |
| Recorded SHA-256 | `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11` |
| Recorded licence | CC BY-SA 4.0 |
| Rights holders recorded | The Word for the World International and Bible Society of Ethiopia |
| A2 status | CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION |
| Controlled intake | AUTHORIZED by 2026-11-08 project-level decision; exact-source A2 acquisition/inspection already completed |
| App packaging/distribution | CONDITIONAL — complete CC BY-SA compliance, source integrity, attribution, modification notices and ShareAlike obligations must be implemented and verified |
| Public GitHub corpus | NOT AUTHORIZED |
| Language specialist | Maale/Ethiopic specialist review remains DEFERRED; no specialist sign-off is claimed |

**Important qualifications:** The A2 record reports 66/66 USFM hashes, 1,189 chapters, 30,506 verse-marker lines and strict UTF-8/Unicode diagnostics passing. The GPG signature is cryptographically valid but signer-key trust is UNDEFINED. Original rights-holder email headers, sender identity and signatory authority have not been independently authenticated in the repository; the official eBible edition/licence record corroborates public edition/licence information. Preserve these qualifications. The separate 2007 `MDYBSE` Maale New Testament is not the selected 2008 full Bible and must not be substituted.

### 3.2 Amharic Bible

| Field | Decision |
|---|---|
| Scope | Amharic Bible (AM) |
| Current candidate noted | eBible `amh` is New Testament only |
| Completeness | NOT COMPLETE — does not satisfy a full-Bible requirement |
| Rights | Commercial-use permission from Bible Society of Ethiopia is required for the noted candidate; verify exact edition and permission in writing |
| Controlled intake | AUTHORIZED for candidate discovery/acquisition and rights/technical evaluation |
| Production packaging/distribution | BLOCKED until a complete edition and exact rights/permission for intended use are verified |

Do not represent an NT-only candidate as a complete Amharic Bible. Any separate Old Testament source must be independently verified and edition compatibility assessed before combining.

### 3.3 English Bible

| Field | Decision |
|---|---|
| Scope | Complete English Bible |
| Initial candidate | World English Bible (WEB) |
| Alternative candidate | Open English Bible (OEB), recorded as CC0 but incomplete |
| Completeness | WEB candidate requires exact-version verification; OEB is not a complete substitute in the current record |
| Rights | Verify exact text/version, public-domain status and any naming/trademark conditions; record source revision and attribution requirements |
| Controlled intake | AUTHORIZED for candidate discovery/acquisition and rights/technical evaluation |
| Production packaging/distribution | CONDITIONAL on exact edition verification, completeness, source integrity and naming/attribution compliance |

### 3.4 Other Bible and study resources

Potential categories include cross-references, concordances, dictionaries, atlases, commentaries, study guides, maps/places, devotional material, quizzes and audio. The Owner authorization covers necessary resources in principle, but **each individual work/source requires its own record and rights decision**.

No blanket authorization is created for:
- audio recordings or music;
- maps, geographic tiles or third-party metadata;
- artwork, fonts, photographs, logos or illustrations;
- copyrighted commentaries, dictionaries, guides or devotional material;
- user-generated or third-party AI content.

For each such source, record provenance, rights, permission, licence scope, offline/download/derivative rights, attribution, integrity, quality and distribution restrictions before integration.

## 4. Permitted workflow and prohibited transitions

| Operation | Authority |
|---|---|
| Discover candidate sources | AUTHORIZED |
| Acquire candidate source for controlled evaluation | AUTHORIZED, subject to lawful access and source terms |
| Store source in restricted non-public validation workspace | AUTHORIZED, subject to source terms and access controls |
| Hash, inspect metadata, validate structure/encoding/integrity | AUTHORIZED |
| Prepare derived indexes/offline database | AUTHORIZED ONLY where source-specific rights permit; keep restricted until distribution approval |
| Add source to application package | CONDITIONAL on complete source-specific rights, quality, attribution and compatibility gates |
| Commit raw scripture/protected assets to public GitHub | NOT AUTHORIZED by this decision |
| Share/export user-facing content | CONDITIONAL on exact source terms and required attribution/ShareAlike compliance |
| Merge PRs | NOT AUTHORIZED |
| Sign/release/deploy/store submission/publication | NOT AUTHORIZED |

## 5. Required source manifest

Every candidate approved for application integration must have a machine-readable and human-readable record containing at least:
- stable source ID;
- title, language, edition, publisher/rights holder, author/editor where relevant;
- canonical source URL or acquisition channel and acquisition date;
- exact version/revision/date and package hash;
- licence identifier and copy/evidence of terms;
- written permission evidence and scope, where required;
- permitted and prohibited operations;
- attribution text and placement;
- derivative/modification/ShareAlike obligations;
- completeness/canon inventory where relevant;
- integrity, encoding, structural and quality report;
- specialist review status and known limitations;
- package/version mapping and update/withdrawal procedure;
- approval status: `LEAD`, `UNDER_REVIEW`, `RIGHTS_BLOCKED`, `RIGHTS_RECONCILED`, `APPROVED_FOR_INTEGRATION`, `APPROVED_FOR_DISTRIBUTION`.

No source may skip statuses based on a general project-level authorization.

## 6. Relationship to closed A2 and A3

- A2 remains **CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**.
- The 2008 Maale source baseline and immutable metadata/hash manifests remain unchanged.
- This decision reconciles the later content-intake authority; it does not reopen A2 or claim the deferred specialist review passed.
- The initial A3 scope's metadata-only restriction is superseded as a current project-level content-authority statement only to the extent of this later Owner decision. Its implementation evidence remains valid for the narrow foundation slice.
- Production scripture integration still requires a controlled source-specific implementation plan and acceptance tests under the Product Requirements Baseline.
- The current application contains no packaged production scripture corpus/database at the inspected PR #59 head.

## 7. Governance decision

**Decision:** OWNER AUTHORIZATION RECONCILED. The 2026-11-08 Owner authorization is the current project-level authority for controlled content intake across Maale, Amharic, English and necessary Bible/study resources, with the conditions and restrictions in this record.

**Current source status:**
- Maale 2008 MDYTWF: A2 source baseline accepted; controlled intake authorized; distribution implementation and specialist review remain gated.
- Amharic: candidate evaluation authorized; complete edition and rights unresolved.
- English: candidate evaluation authorized; exact edition/completeness/naming conditions must be verified.
- Other study/media resources: discovery and evaluation authorized; each source individually gated.

**Not authorized:** blanket content distribution, public corpus publication, merge, signing, deployment, release or store publication.

**Historical evidence rule:** Do not edit or delete closed A2 records to conceal the former authority state. Link this later decision as the superseding current authority.
