# P0 Closure Report — Product Requirements and Content Authority

**Project:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Owner:** Akefa Ayele Daka  
**Date:** 2026-11-14  
**Repository:** `akefadaka-git/maale-bible`  
**Requirements branch:** `requirements/p0-product-baseline-2026-11-14`  
**Branch head:** `e788f7b7762053ec867404546b5afc81574c02ed`  
**Assessment baseline:** PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**Overall P0:** **VERIFIED-CLOSED — OWNER-APPROVED REQUIREMENTS BASELINE FROZEN**  
**P1 implementation authority:** NOT GRANTED BY THIS CLOSURE  
**Merge/deployment/publication:** NOT AUTHORIZED

## 1. Closure decision

P0 is complete. The Owner's 2026-11-14 instruction explicitly directed establishment and approval of the complete product requirements baseline, reconciliation of content-intake authority, and freezing prioritized acceptance criteria before implementation. The baseline and authority record have been committed on a dedicated documentation/requirements branch and fetched back from GitHub for verification.

The canonical requirements baseline is v1.0 and is frozen as of 2026-11-14. The current project-level content-intake authority is reconciled in a separate decision record. Historical A1/A2 evidence remains unchanged. No application source, scripture payload or protected content was changed or added.

## 2. P0 acceptance register

| ID | Acceptance criterion | Evidence | Status |
|---|---|---|---|
| P0-01 | One canonical versioned product requirements baseline covers all Owner-raised modules and non-functional requirements | `docs/product/PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md` (v1.0, 315 lines) | VERIFIED |
| P0-02 | E.C.-default/G.C.-selectable calendar rule is explicit and testable | CAL-01 through CAL-08; P1-07 | PASS |
| P0-03 | Later Owner content-intake authorization is reconciled without rewriting closed A2 history | `docs/licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md`; README, source register and A3 scope current-authority notes | VERIFIED |
| P0-04 | Source authority matrix records allowed intake, source rights conditions, distribution limits and unresolved evidence | Authority matrix for Maale 2008 MDYTWF, Amharic, English, other study/media sources | VERIFIED |
| P0-05 | Prioritized acceptance criteria are frozen with stable requirement IDs and pass/fail evidence | P0, P1, P2, P3, P4 and P5 criteria in baseline v1.0 | VERIFIED |
| P0-06 | P0 documents are committed on a dedicated requirements branch; application source and PR #59 remain untouched | Branch `requirements/p0-product-baseline-2026-11-14`; compare shows 8 documentation files changed, no `app/` changes | VERIFIED |
| P0-07 | No implementation begins before separate next-stage authorization | No application code, scripture payload, build, merge, deployment or publication performed under P0 | PASS |

## 3. Canonical records

1. [Product Requirements Baseline v1.0](PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md) — complete functional and non-functional scope, language, E.C./G.C. calendar, module requirements and prioritized acceptance criteria.
2. [Owner Content-Intake Authority Reconciliation](../licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md) — records the 2026-11-08 Owner authorization and source-specific rights conditions.
3. [A3 Product-Gap Reassessment](A3_PRODUCT_GAP_REASSESSMENT_2026-11-14.md) — evidence-backed status of the existing implementation at PR #59 exact head.
4. [Source Register](../../content/source-register/README.md) — current source status and A2 evidence.
5. [Repository README](../../README.md) — current governance entry points.

## 4. Authority reconciliation result

The Owner's 2026-11-08 authorization is the current project-level authority for controlled intake/evaluation of Maale, Amharic, English and necessary Bible/study resources, subject to exact-source provenance, rights/licence/permission, attribution, integrity, compatibility and quality checks.

The old `content_intake_authorized=false` wording is superseded as a statement of current project authority. Historical A2 documents are not rewritten. This does not authorize blanket packaging/distribution, public-repository corpus storage, merge, signing, deployment or publication.

- Maale 2008 `MDYTWF / mdyeth`: A2 source baseline accepted; controlled intake authorized; CC BY-SA implementation and distribution compliance remain required; specialist review remains deferred.
- Amharic: `amh` candidate is NT-only; a complete Amharic Bible and exact rights/permission remain unresolved.
- English: WEB is an initial candidate requiring exact-version, completeness and naming/attribution checks; OEB is recorded as incomplete.
- Other Bible/study/media resources: source-by-source approval only.

## 5. Frozen product decisions

- Maale-first, offline-first Android Bible and study product.
- Supported initial UI languages: Maale, Amharic and English, with no hidden English fallback in a claimed-complete supported flow.
- Exact Amharic brand text: **መጽሐፍ ቅዱስ - ሕያው ቃል ተሞክሮ**.
- English tagline wording remains pending final Owner copy confirmation; do not invent/freeze the full phrase.
- 66-book catalogue with localized book names and separate Old Testament/New Testament sections.
- Scripture must be edition-exact and never fabricated or silently substituted.
- E.C. default; G.C. explicitly selectable and persistent.
- Full scope includes reader, catalogue, search, onboarding, profile/guest, settings, About, update option, notifications, daily verses, personal study, reading plans, commentaries, study guides, maps, quizzes, rewards, audio and optional IROKA AI integration.
- IROKA, cloud accounts, backend and external AI data processing require separate architecture/privacy/consent approvals.
- Every third-party content or asset source is rights-gated individually.

## 6. Branch and repository evidence

- Requirements branch: `requirements/p0-product-baseline-2026-11-14`.
- Final branch head: `e788f7b7762053ec867404546b5afc81574c02ed`.
- Compared with assessment baseline `58eb32acc10415e3dfa6683a5342b6da824d5d27`: 9 commits ahead, 0 behind.
- Changed paths: 8 documentation files (2 new canonical reports, 6 documentation/index updates).
- Application code changed: NO.
- Raw scripture/USFM/USX/database payload added: NO.
- PR #59 remains OPEN and DRAFT, head unchanged at `58eb32acc10415e3dfa6683a5342b6da824d5d27`.
- No PR was created for the requirements branch; no merge was attempted.

## 7. Remaining decisions and next-stage gates

The following items are explicitly unresolved and must not be guessed:
- Final exact English tagline wording.
- Complete Amharic Bible edition and rights/permission.
- Final selected English edition/version after completeness and naming/attribution review.
- Source-specific commentary, study-guide, map, audio and other asset selections/rights.
- IROKA service/API, data-flow, privacy, consent and safety architecture.
- Any cloud account/backend or synchronization scope.
- Maale-language/Ethiopic-script specialist review (deferred residual risk; not passed).

**Next stage:** P1 planning and implementation sequencing may be proposed against the frozen acceptance register. P1 implementation is not authorized by this closure; it requires separate explicit Owner instruction. No merge, deployment, signing or publication is authorized.
