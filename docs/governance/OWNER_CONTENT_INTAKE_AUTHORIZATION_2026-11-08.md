# Owner Content-Intake Authorization — 2026-11-08

**Project:** Maale Bible (ጌኤዦ ማፃኣፖ)  
**Repository:** `akefadaka-git/maale-bible`  
**Decision authority:** Owner  
**Decision:** CONTENT INTAKE AUTHORIZED — SOURCE-BY-SOURCE RIGHTS GATES REMAIN MANDATORY

## 1. Owner authorization

On 2026-11-08, the Owner explicitly authorized content intake for the Maale Bible project, stating:

> “Yes, not only maale, but am, en and other necessary sources”

This supersedes the prior blanket `content_intake_authorized=false` restriction. The project may now acquire, inspect, validate, transform and integrate source packages for the offline application within the boundaries below.

## 2. Authorized content scope

The authorization covers:
- Maale Bible editions, beginning with the previously verified 2008 MDYTWF / mdyeth Full Bible.
- Amharic (am) Bible editions.
- English (en) Bible editions.
- Other necessary Bible and study resources, including concordances, dictionaries, encyclopedias, commentaries, topical references, cross-reference datasets, biblical atlases/maps, and optional audio or visual resources where useful to the approved product scope.
- Required metadata, provenance records, source manifests, offline databases, indexes and derived datasets, provided the source licence permits the specific processing and distribution.

## 3. Mandatory per-source acceptance gates

This is authorization to conduct intake work; it is not a blanket licence to every resource. Before a source is integrated or distributed, record:
1. Exact work, edition, language, publisher/rights holder and version/revision.
2. Authoritative source URL and acquisition timestamp.
3. Applicable licence or written permission, including whether it covers package files, adaptations, derived indexes, offline redistribution and commercial/non-commercial use.
4. Required attribution, modification notices, ShareAlike or other obligations.
5. Archive and per-file integrity hashes, safe extraction and malware/archive safety checks.
6. Text structure, encoding, canon/versification and quality checks.
7. Language-specialist review where linguistic correctness is material.
8. A source-specific status: DISCOVERED, RIGHTS_REVIEW, RIGHTS_BLOCKED, APPROVED_FOR_INTAKE, INTAKEN, VERIFIED, or REJECTED.

Do not scrape, redistribute, bundle or adapt material merely because it is publicly accessible. Do not infer that one edition's licence applies to another edition or to audio, maps, fonts, images, logos or commentary.

## 4. Initial source priorities

1. **Maale:** 2008 MDYTWF / mdyeth Full Bible. Existing A2 evidence remains valid for the previously inspected package. Reconcile its archive identity and integrity evidence before any new processing.
2. **Amharic:** identify authoritative complete Bible editions and obtain edition-specific licence/permission evidence.
3. **English:** identify a small set of reputable, useful translations with explicit offline redistribution/adaptation permissions. Prefer open licences and avoid assuming that popular copyrighted translations may be bundled.
4. **Study resources:** prioritize open/licensed cross-references, dictionaries, concordance and atlas datasets; verify each resource independently.

## 5. Repository and release controls

- Do not commit raw scripture text, source archives, audio, scans or restricted reference assets to this public repository unless the exact licence expressly permits public redistribution and the repository's distribution model is approved.
- Prefer private build-time content packages or a separately controlled content repository for licensed assets where public source publication is not permitted.
- Keep source manifests, licence notices, transformation recipes, non-sensitive hashes and verification reports in the public repository where safe.
- Implement required in-app attribution, licence display, modification notices and ShareAlike boundaries before distribution.
- User approval here does not authorize merge, signing, deployment, store publication or public distribution of a release. Those remain separate stage-gated decisions.

## 6. Current authorization flag

`content_intake_authorized=true`

The flag means the Owner has authorized source intake work within this policy. Individual resources can still be RIGHTS_BLOCKED or NOT APPROVED_FOR_INTAKE until their own evidence passes. No source may bypass the per-source gates.

## 7. Governance

This record supersedes earlier statements that content intake was categorically unauthorized. It does not rewrite historical A2 reports; those reports remain accurate for the time and scope in which they were produced. Where an older document states that a separate authorization is still required, this record supplies the general Owner authorization, while source-specific rights and quality checks remain mandatory.
