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
| 2 | Maale-first UI and language selection | Maale default; selectable Maale/Amharic/English; complete screen string coverage; persisted preference; locale fallback; accessibility and rendering checks | **IN PROGRESS — persisted locale selector and Maale default are present; English and Amharic resource inventories are now aligned with all 104 Maale resource keys. Nine English/Amharic-only keys are explicitly recorded as pending Maale wording and fall back to English. Search, Reader, Library, Listen and Study composables still contain hard-coded UI text; they are not yet wired to localized resources. Exact-head runtime, rendering and accessibility verification remain.** |
| 3 | Guest reading and authentication UI | Guest remains first-class; login accepts username or mobile + password; registration OTP-only; no mandatory account; real API contract integration and security tests | **PARTIAL — guest reading remains accessible; login/OTP registration UI is present but disabled because no live API endpoint is configured; server-side authentication must not be simulated** |
| 4 | Verified edition installation and search-to-reader | Per-edition rights gate; secure package download; archive/hash/signature/manifest validation; atomic install; rollback; installed edition selection; search result opens exact verified passage | **PARTIAL — offline store/search primitives exist; end-to-end install and reader wiring need verification** |
| 5 | Private notes and database migrations | Notes local/private by default; migration paths tested from every supported prior schema; corruption/recovery and persistence tests; no automatic sync | **PARTIAL — SQLite v4 private journal CRUD and v1/v3 migration coverage already exist; v2-to-v4 preservation test added; full migration/recovery matrix remains to verify** |
| 6 | Full Android test matrix | Unit + instrumentation suites across API 26/28/30/33/35/36; font scale, locale, offline, accessibility, lifecycle and migration coverage | **PARTIAL — emulator matrix is stable; expanded product-behavior coverage pending** |

## A2-02 language policy

- Default interface selection is Maale (`mdy`).
- Amharic (`am`) and English (`en`) are selectable.
- The selected language is stored locally in application preferences.
- Android resource locale configuration is applied within the Compose application.
- The Maale (`values-b+mdy`) resource contains 104 keys, including the 103 Owner-supplied Maale labels and the app title. The nine additional English/Amharic UI keys remain absent from Maale resources and therefore use Android's base-English fallback.
- English (`values`) and Amharic (`values-am`) resource inventories have been reconciled to the same 113-key set; every Maale resource key exists in both reference locales.
- The worksheet now records 112 UI keys (excluding the app title): 103 supplied Maale labels and 9 pending Maale labels.
- Owner-supplied Maale wording is preserved as intake; it is not represented as formal community linguistic approval. No Maale wording was invented.
- Resource availability is not screen localization: Search, Reader, Bible Library, Listen and Study composables still contain hard-coded visible text and require resource wiring.

## Latest implementation evidence

- Account entry is reachable from the global app bar. Guest reading remains available.
- Login UI accepts username-or-mobile plus password; registration UI collects username/mobile/password and registration OTP only. Submit actions remain disabled until a real backend API base URL and enabled authentication routes exist.
- A2-05 v2-to-v4 migration coverage added in `P2V2LocalStudyPersistenceTest.kt`.
- Home and Books screens now use Android string resources; Amharic resources are provided. Canonical book names remain in the approved English catalogue because no reviewed Maale/Amharic book-name catalogue has been established.
- Locale resource inventory reconciliation: English 113 keys; Amharic 113 keys; Maale 104 keys; zero duplicate keys; every Maale key has English and Amharic counterparts. Nine base/Amharic-only keys intentionally fall back to English in Maale.
- Screen source audit: Home, Books and Account use resource-backed strings; Search, Reader, Bible Downloads, Audio and Smart Study screens still contain hard-coded visible text. This is a source audit, not a runtime rendering pass.

## Implementation evidence

Initial implementation files:
- `app/src/main/java/org/maale/bible/ui/AppLanguagePreference.kt`
- `app/src/main/java/org/maale/bible/ui/MaaleBibleApp.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-am/strings.xml`
- `app/src/main/res/values-b+mdy/strings.xml`

## Closure rule

A remediation item is complete only after implementation, tests, exact-head CI evidence, and an explicit evidence-backed disposition. A passing APK build alone does not close product functionality. The six-item programme remains open until all six items pass. A2 remediation closure does not authorize production content ingestion, signing, deployment, store publication, or release.
