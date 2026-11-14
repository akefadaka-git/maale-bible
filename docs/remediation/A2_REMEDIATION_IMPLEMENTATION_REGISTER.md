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
| 2 | Maale-first UI and language selection | Maale default; selectable Maale/Amharic/English; complete screen resource coverage; persisted preference; explicit fallback; accessibility and rendering checks | **PARTIAL — locale selector and Maale default are implemented. All 263 keys now exist in each locale resource set. Amharic has 162 existing locale values plus 101 explicit English fallbacks; Maale has 113 existing locale values plus 150 explicit English fallbacks. Fallbacks are marked translatable=false and are not translations. Exact-head persistence, rendering and accessibility evidence remains pending; no unapproved wording will be invented.** |
| 3 | Guest reading and authentication UI | Guest remains first-class; login accepts username or mobile + password; registration OTP-only; no mandatory account; real API contract integration and security tests | **PARTIAL — guest reading remains accessible; login/OTP registration UI is present but disabled because no live API endpoint is configured; server-side authentication must not be simulated** |
| 4 | Verified edition installation and search-to-reader | Per-edition rights gate; secure package download; archive/hash/signature/manifest validation; atomic install; rollback; installed edition selection; search result opens exact verified passage | **PARTIAL — offline store/search primitives exist; end-to-end install and reader wiring need verification** |
| 5 | Private notes and database migrations | Notes local/private by default; migration paths tested from every supported prior schema; corruption/recovery and persistence tests; no automatic sync | **PARTIAL — SQLite v4 private journal CRUD and v1/v3 migration coverage already exist; v2-to-v4 preservation test added; full migration/recovery matrix remains to verify** |
| 6 | Full Android test matrix | Unit + instrumentation suites across API 26/28/30/33/35/36; font scale, locale, offline, accessibility, lifecycle and migration coverage | **PARTIAL — emulator matrix is stable; expanded product-behavior coverage pending** |

## A2-02 language policy

- Default interface selection is Maale (`mdy`).
- Amharic (`am`) and English (`en`) are selectable.
- The selected language is stored locally in application preferences.
- Android resource locale configuration is applied within the Compose application.
- Core `strings.xml` inventory: base English 181 keys. Amharic and Maale each now contain all 181 keys. Amharic has 113 existing locale values plus 68 explicit English fallbacks; Maale has 113 existing locale values plus 68 explicit English fallbacks. The Maale resource contains the app title plus 112 Owner-supplied UI entries.
- Additional resource inventory: all three files now have Amharic and Maale locale resources. `book_explorer.xml` has 33 explicit English fallbacks in both locales; `book_explorer_testaments.xml` retains 4 Amharic values and adds 4 Maale English fallbacks; `book_learning.xml` retains 45 Amharic values and adds 45 Maale English fallbacks.
- Aggregate inventory: 263 keys exist in each locale resource set. Amharic has 162 existing locale values and 101 explicit English fallbacks; Maale has 113 existing locale values and 150 explicit English fallbacks. Fallback entries are marked `translatable=false`; they are not completed translations.
- The worksheet records 112 Owner-supplied Maale UI keys out of 262 non-app-title keys (42.7% of the complete inventory). The 62.2% figure applies only to the 180 non-app-title keys in core `strings.xml`.
- Owner-supplied Maale wording is preserved as intake; it is not represented as formal community linguistic approval. No Maale or Amharic wording was invented in this remediation.
- Source audit of Home, Books, Search, Reader, Bible Downloads, Audio, Smart Study, Book Learning, Atlas and Account found user-facing prose resource-backed. Remaining direct literals are icons, navigation glyphs, placeholders, metadata and formatted timestamps. Resource-backed does not mean translated: missing locale entries still fall back to English.
- Exact-head locale persistence, first-launch default, rendering and accessibility instrumentation is being added; emulator and independent TalkBack/device evidence remain pending.

## Latest implementation evidence

- Account entry is reachable from the global app bar. Guest reading remains available.
- Login UI accepts username-or-mobile plus password; registration UI collects username/mobile/password and registration OTP only. Submit actions remain disabled until a real backend API base URL and enabled authentication routes exist.
- A2-05 v2-to-v4 migration coverage added in `P2V2LocalStudyPersistenceTest.kt`.
- Home, Books, Search, Reader, Bible Downloads, Audio, Smart Study, Book Learning, Atlas and Account use resource-backed visible prose. The source audit found no hard-coded user-facing sentences in these screen composables; icon glyphs, symbols, date formatting and dynamic metadata remain literal by design.
- Full inventory reconciliation is recorded above and in `docs/localization/MAALE_LANGUAGE_KEY_WORKSHEET.md`. The earlier 104-key/9-pending and 113-key-only counts are superseded.
- Canonical book names remain in the approved English catalogue because no reviewed Maale/Amharic book-name catalogue has been established.
- Existing instrumentation covers Amharic/English switching and selected navigation. New exact-head coverage now resets activity state between tests and exercises Maale first-launch default, all three language controls, immediate rendering, persistence across Activity recreation and accessibility click semantics.

## Implementation evidence

Initial implementation files:
- `app/src/main/java/org/maale/bible/ui/AppLanguagePreference.kt`
- `app/src/main/java/org/maale/bible/ui/MaaleBibleApp.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-am/strings.xml`
- `app/src/main/res/values-b+mdy/strings.xml`

## A2-02 exact-head acceptance boundary

The resource-key coverage gap is implemented: all 263 keys now exist in all three locale resource sets, with explicit English fallbacks for untranslated entries. This is not complete language acceptance. A2-02 remains **PARTIAL** until the 150 Maale fallback entries receive Owner-approved wording (and the 101 Amharic fallback entries receive reviewed Amharic wording where completeness is required), all three locale screens are rendered on the exact tested head, the preference survives recreation, accessibility semantics pass, and device/TalkBack evidence is recorded. The new instrumentation test is not evidence of a successful run until its exact-head Actions job passes.

## Closure rule

A remediation item is complete only after implementation, tests, exact-head CI evidence, and an explicit evidence-backed disposition. A passing APK build alone does not close product functionality. The six-item programme remains open until all six items pass. A2 remediation closure does not authorize production content ingestion, signing, deployment, store publication, or release.
