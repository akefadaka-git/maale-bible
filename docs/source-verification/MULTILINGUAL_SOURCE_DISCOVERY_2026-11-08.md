# Multilingual Bible and Study Resource Discovery — 2026-11-08

**Status:** INITIAL DISCOVERY — NOT A SOURCE APPROVAL OR INTAKE COMPLETION  
**Owner authorization:** See `docs/governance/OWNER_CONTENT_INTAKE_AUTHORIZATION_2026-11-08.md`  
**Scope:** Maale (mdy), Amharic (amh), English (eng), and supporting offline study resources.

## Executive result

The Owner has authorized multilingual content-intake work. Initial public-source discovery identified candidate packages, but the findings show that each edition has materially different rights, completeness and quality conditions. No source in this report is automatically approved for integration merely because it is publicly downloadable.

## Candidate register

| Language | Candidate | Completeness | Publicly stated rights | Current disposition |
|---|---|---|---|---|
| Maale | 2008 MDYTWF / mdyeth Full Bible | Full Bible, 66-book inventory in existing A2 evidence | Owner-supplied rights-holder clarification says CC BY-SA 4.0; older public eBible catalogue page displays CC BY-ND 4.0 | RIGHTS_REVIEW — reconcile conflicting licence evidence before transformation/distribution |
| Amharic | eBible AMH / amh | New Testament only | Copyright © 1962, 2003 United Bible Societies; non-commercial use requires full copyright statement; commercial use requires written permission from Bible Society of Ethiopia | RIGHTS_REVIEW — incomplete canon; use requires confirmation of app distribution model and permission scope |
| English | World English Bible (WEB / engwebp) | Full 66-book Protestant canon | Public domain; “World English Bible” is a trademark; changed text must not retain that name | CANDIDATE — verify exact package/version, book inventory and trademark notice |
| English | Open English Bible (OEB) | Current release 2025.6 includes NT, Psalms and some OT books | CC0 | CANDIDATE — incomplete; not a substitute for a complete English Bible |

## Evidence and source URLs

### Maale
- Existing Owner-accepted technical A2 report: `docs/source-verification/REAL_SOURCE_VALIDATION_REPORT_2026-11-07.md`.
- Rights-holder clarification recorded in `docs/source-verification/RIGHTS_EDITION_RECONCILIATION_2026-11-07.md`.
- Older public catalogue page: https://divinerevelations.info/documents/bible/All_HTML/Maale_Bible_Ethiopic_script/copyright.htm
- That public page describes the 2008 Maale Full Bible and displays CC BY-ND 4.0. This conflicts with the Owner-supplied rights-holder clarification recorded by the project as CC BY-SA 4.0. Obtain current direct written clarification from the rights holders or a definitive current package licence before applying modifications, generating adapted text, or distributing derivatives. Existing A2 hashes/structure evidence remains valid only for the exact archive already inspected.

### Amharic
- Official eBible edition record: https://ebible.org/bible/details.php?id=amh
- Copyright/permission page: https://ebible.org/amh/copyright.htm
- The listed resource is the New Testament, copyright © 1962, 2003 United Bible Societies, with permission from the Bible Society of Ethiopia. The page states that non-commercial use must include the full copyright statement and commercial use requires written permission from the Bible Society of Ethiopia.
- This is not a full Bible. Do not label it as a complete Amharic Bible. Determine whether the intended app distribution qualifies as non-commercial before intake; obtain written permission if commercial or uncertain.

### English
- Official WEB edition record: https://www.ebible.org/find/details.php?id=engwebp
- WEB copyright page: https://ebible.org/engwebp/copyright.htm
- WEB is described as public domain. The name “World English Bible” is a trademark; the source asks that modified text not retain that name.
- Open English Bible: https://openenglishbible.org/
- OEB current release is described as CC0, but the currently available release is partial (NT, Psalms and some OT books). Record completeness per release.

## Intake and validation plan

1. Obtain/retain authoritative licence and publisher evidence for each exact edition and package.
2. Select editions with explicit permission for offline redistribution and required transformations; record commercial-use conditions.
3. Acquire only exact approved package URLs into isolated storage outside the public repository.
4. Record timestamp, size, content type, archive SHA-256, source version, package metadata and custody.
5. Run archive safety preflight before extraction; reject traversal, symlinks, duplicate normalized paths, nested archives, encryption and resource-limit violations.
6. Extract into a fresh isolated directory; hash every file; validate UTF-8, Unicode, book inventory, chapters, verse markers, versification and duplicates.
7. Obtain qualified language review for Maale and Amharic; no linguistic correctness claim from encoding checks alone.
8. Build canonical passage mappings and offline indexes only after text integrity and rights conditions are resolved.
9. Preserve edition-specific attribution, licence, modification history and source hashes in app metadata and exported content.
10. Keep source assets outside the public repository unless the exact licence permits public redistribution and that distribution decision is documented.

## Current blockers and decisions

- **Maale licence conflict:** RIGHTS_REVIEW. Do not assume CC BY-SA permission for modifications until the CC BY-ND discrepancy is resolved.
- **Amharic completeness:** current candidate is NT-only; discover a full Bible edition and obtain its rights evidence. The eBible NT's commercial-use condition must be resolved for the app's distribution model.
- **English:** WEB is the strongest initial complete open candidate; confirm exact package and trademark conditions. OEB is a supplementary partial translation, not a full edition.
- **Other resources:** discover only from identifiable publishers/providers; evaluate each dataset independently. Do not bundle proprietary commentaries, maps, audio, fonts or images without explicit rights.
- **Linguistic review:** encoding and structure checks do not replace Maale/Amharic specialist review.

## Source-status vocabulary

- DISCOVERED — candidate identified.
- RIGHTS_REVIEW — rights, completeness or use conditions unresolved.
- RIGHTS_BLOCKED — permission unavailable or incompatible.
- APPROVED_FOR_INTAKE — exact source package and intended use pass source-specific gates.
- INTAKED — package safely acquired and hashed.
- VERIFIED — structural, integrity, language and licence checks completed to stated scope.
- INTEGRATED — approved resource installed in the app and end-to-end tested.
- REJECTED — candidate not selected.

**Report conclusion:** Owner authorization is active. The WEB is a promising complete English candidate; the current Amharic eBible candidate is only a New Testament and has use restrictions; the Maale public catalogue contains a licence notice conflict that must be reconciled. Continue source discovery and rights verification before content integration.
