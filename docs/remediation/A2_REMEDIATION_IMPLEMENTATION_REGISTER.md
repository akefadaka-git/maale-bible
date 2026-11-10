# A2 Remediation Implementation Register

**Programme:** Maale Bible Android application — post-baseline A2 remediation
**Canonical repository:** `akefadaka-git/maale-bible`
**Implementation branch:** `fix/a2-remediation-complete`
**Base:** CI baseline merge `b2ffb3b66079555ba88e7fcc30007ecc43074b7f`
**Governance:** isolated branch; no main modification, merge, signing, deployment, or publication without separate approval.

## Ordered remediation gates

| Order | Remediation | Required acceptance evidence | Status |
|---|---|---|---|
| 1 | Android CI reliability | Host contracts, APK build, regression tests and emulator instrumentation pass on APIs 26, 28, 30, 33, 35, 36 at the exact head | **VERIFIED — PR #46 merged; exact-head run 42863806851** |
| 2 | Maale-first UI and language selection | Maale default; selectable Maale/Amharic/English; complete screen string coverage; persisted preference; locale fallback; accessibility and rendering checks | **IN PROGRESS — locale preference, selector and initial resources committed; screen-wide localization and runtime verification remain** |
| 3 | Guest reading and authentication UI | Guest remains first-class; login accepts username or mobile + password; registration OTP-only; no mandatory account; real API contract integration and security tests | **NOT STARTED — reassessment required against existing backend contracts** |
| 4 | Verified edition installation and search-to-reader | Per-edition rights gate; secure package download; archive/hash/signature/manifest validation; atomic install; rollback; installed edition selection; search result opens exact verified passage | **PARTIAL — offline store/search primitives exist; end-to-end install and reader wiring need verification** |
| 5 | Private notes and database migrations | Notes local/private by default; migration paths tested from every supported prior schema; corruption/recovery and persistence tests; no automatic sync | **PARTIAL — SQLite study database and journal operations exist; complete migration matrix/recovery verification pending** |
| 6 | Full Android test matrix | Unit + instrumentation suites across API 26/28/30/33/35/36; font scale, locale, offline, accessibility, lifecycle and migration coverage | **PARTIAL — emulator matrix is stable; expanded product-behavior coverage pending** |

## A2-02 language policy

- Default interface selection is Maale (`mdy`).
- Amharic (`am`) and English (`en`) are selectable.
- The selected language is stored locally in application preferences.
- Android resource locale configuration is applied within the Compose application.
- The Maale resource currently contains only the confirmed app title and Maale language label. Unreviewed Maale UI labels fall back to the base English resources and are explicitly disclosed in the UI.
- No Maale translation is invented or represented as linguistically approved. Full Maale string coverage requires qualified review and Owner acceptance.
- Amharic resources are provided for the language selector and navigation. All remaining screens must be migrated before A2-02 can pass.

## Implementation evidence

Initial implementation files:
- `app/src/main/java/org/maale/bible/ui/AppLanguagePreference.kt`
- `app/src/main/java/org/maale/bible/ui/MaaleBibleApp.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-am/strings.xml`
- `app/src/main/res/values-b+mdy/strings.xml`

## Closure rule

A remediation item is complete only after implementation, tests, exact-head CI evidence, and an explicit evidence-backed disposition. A passing APK build alone does not close product functionality. The six-item programme remains open until all six items pass. A2 remediation closure does not authorize production content ingestion, signing, deployment, store publication, or release.
