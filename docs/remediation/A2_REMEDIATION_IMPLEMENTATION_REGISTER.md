# A2 Remediation Implementation Register

**Programme:** Maale Bible Android application — post-baseline A2 remediation
**Canonical repository:** `akefadaka-git/maale-bible`
**Implementation branch:** `fix/a2-remediation-complete`
**Base:** CI baseline merge `b2ffb3b66079555ba88e7fcc30007ecc43074b7f`
**Governance:** isolated branch; no main modification, merge, signing, deployment, or publication without separate approval.

## Ordered remediation gates

| Order | Remediation | Required acceptance evidence | Status |
|---|---|---|---|
| 1 | Android CI reliability | Host contracts, APK build, regression tests and emulator instrumentation pass on APIs 26, 28, 30, 33, 35, 36 at the exact head | **PARTIAL — PR #46 provides the historical six-API baseline (run 42863806851). PR #47 exact-head run 43586458821: host contracts and APK build passed; API 26 instrumentation failed, API 28 retry remains in progress, and API 30/33/35/36 remain cancelled. A test-navigation correction is committed at the newer head; full six-API evidence for that head is still pending.** |
| 2 | Maale-first UI and language selection | Maale default; selectable Maale/Amharic/English; complete screen resource coverage; persisted preference; explicit fallback; accessibility and rendering checks | **PARTIAL — locale selector and Maale default are implemented. All 263 keys now exist in each locale resource set. Amharic has 162 existing locale values plus 101 explicit English fallbacks; Maale has 123 existing locale values plus 140 explicit English fallbacks. Fallbacks are marked translatable=false and are not translations. Exact-head persistence, rendering and accessibility evidence remains pending; no unapproved wording will be invented.** |
| 3 | Guest reading and authentication UI | Guest remains first-class; login accepts username or mobile + password; registration OTP-only; no mandatory account; real API contract integration and security tests | **PARTIAL — guest reading remains accessible. Login accepts one identifier plus password; registration collects username, mobile, password and registration OTP only. Login, OTP request and registration verification buttons are disabled because no live authentication API contract is configured. Keystore-backed session lifecycle, logout and end-to-end security tests are absent.** |
| 4 | Verified edition installation and search-to-reader | Per-edition rights gate; secure package download; archive/hash/signature/manifest validation; atomic install; rollback; installed edition selection; search result opens exact verified passage | **PARTIAL — offline store verifies package size, database SHA-256, metadata, FTS integrity and atomic replacement when given a database stream. The library UI opens external source/download URLs and does not perform in-app installation; the manifest has no trusted digital signature chain; scripture-result cards are not clickable and do not navigate to the exact verse. Book catalogue results open only the book-level reader route.** |
| 5 | Private notes and database migrations | Notes local/private by default; migration paths tested from every supported prior schema; corruption/recovery and persistence tests; no automatic sync | **PARTIAL — SQLite v4 journal CRUD is device-local/private by default. Instrumentation fixtures cover v1→v4, v2→v4 and v3→v4 preservation paths. The workflow previously targeted only P2V6 navigation tests; the current workflow configuration now runs the full connected instrumentation suite so P2V2 migration tests are included. Latest-head execution and corruption/rollback/recovery evidence remain pending.** |
| 6 | Full Android test matrix | Unit + instrumentation suites across API 26/28/30/33/35/36; font scale, locale, offline, accessibility, lifecycle and migration coverage | **PARTIAL — API 26 at earlier head 2d892911ece26cced3a591f59cd310fc97233bea ran 7 tests with 4 failures. Two 60-second timeouts originated in LazyList `performScrollToNode`; subsequent Espresso `Already interrogating!` exceptions were cascading idling failures. SDK, emulator image and KVM setup passed. The test helper now waits for the initially visible Genesis card by test tag and clicks it directly. The current workflow also runs the full instrumentation suite, including migration tests. Latest-head matrix is pending.** |

## A2-02 language policy

- Default interface selection is Maale (`mdy`).
- Amharic (`am`) and English (`en`) are selectable.
- The selected language is stored locally in application preferences.
- Android resource locale configuration is applied within the Compose application.
- Core `strings.xml` inventory: base English 181 keys. Amharic and Maale each now contain all 181 keys. Amharic has 113 existing locale values plus 68 explicit English fallbacks; Maale has 123 existing locale values plus 58 explicit English fallbacks. The Maale resource contains the app title plus 122 Owner-supplied UI entries.
- Additional resource inventory: all three files now have Amharic and Maale locale resources. `book_explorer.xml` has 33 explicit English fallbacks in both locales; `book_explorer_testaments.xml` retains 4 Amharic values and adds 4 Maale English fallbacks; `book_learning.xml` retains 45 Amharic values and adds 45 Maale English fallbacks.
- Aggregate inventory: 263 keys exist in each locale resource set. Amharic has 162 existing locale values and 101 explicit English fallbacks; Maale has 113 existing locale values and 150 explicit English fallbacks. Fallback entries are marked `translatable=false`; they are not completed translations.
- The worksheet records 122 Owner-supplied Maale UI keys out of 262 non-app-title keys (46.6% of the complete inventory). The 67.8% figure applies only to the 180 non-app-title keys in core `strings.xml`.
- Owner-supplied Maale wording is preserved as intake; it is not represented as formal community linguistic approval. No Maale or Amharic wording was invented in this remediation.
- Source audit of Home, Books, Search, Reader, Bible Downloads, Audio, Smart Study, Book Learning, Atlas and Account found user-facing prose resource-backed. Remaining direct literals are icons, navigation glyphs, placeholders, metadata and formatted timestamps. Resource-backed does not mean translated: missing locale entries still fall back to English.
- Exact-head locale persistence, first-launch default, rendering and accessibility instrumentation is present in the test suite. The API 26 run did not reach those assertions reliably because the navigation helper blocked first; the test helper has been corrected at the latest head, but the rerun is pending.

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

The resource-key coverage gap is implemented: all 263 keys now exist in all three locale resource sets, with explicit English fallbacks for untranslated entries. This is not complete language acceptance. A2-02 remains **PARTIAL** until the 140 Maale fallback entries receive Owner-approved wording (and the 101 Amharic fallback entries receive reviewed Amharic wording where completeness is required), all three locale screens are rendered on the exact tested head, the preference survives recreation, accessibility semantics pass, and device/TalkBack evidence is recorded. The new instrumentation test is not evidence of a successful run until its exact-head Actions job passes.

## Closure rule

A remediation item is complete only after implementation, tests, exact-head CI evidence, and an explicit evidence-backed disposition. A passing APK build alone does not close product functionality. The six-item programme remains open until all six items pass. A2 remediation closure does not authorize production content ingestion, signing, deployment, store publication, or release.

 
## Exact-head API 26 instrumentation diagnosis

Run `43586458821` tested PR #47 head `2d892911ece26cced3a591f59cd310fc97233bea`. Host contracts and debug APK assembly passed. API 26 booted successfully, verified its SDK image, and passed KVM setup. Instrumentation started with seven tests; four failed. The first two navigation failures timed out inside Compose `performScrollToNode` at `P2V6NavigationSmokeTest.kt:42`, called by `revealGenesisCard`. The subsequent `IllegalStateException: Already interrogating!` errors came from Espresso idling after the timed-out scroll interaction; they are downstream synchronization fallout, not an independent authentication or app-data failure.

The test correction at head `8a3302a4fef87c40c6ca5c4572bf6aa5c19e9898` removes the scroll-to-node interaction, waits up to ten seconds for the initially visible canonical Genesis card tag, asserts visibility, and clicks the card directly. It remains **UNVERIFIED** until exact-head Android instrumentation passes.


 
## Gate-specific evidence and unresolved dependencies

### A2-03 authentication
The Account screen has an identifier/password login form and a registration form for username, mobile, password and OTP. The three submit actions are explicitly disabled. This preserves the guest-first policy and avoids fake authentication, but it does not establish a functioning authentication system. A reviewed backend contract, reachable service, OTP provider policy, secure session lifecycle and logout/security verification are still required. No credentials, OTPs or successful sessions are simulated.

### A2-04 edition installation and passage navigation
`OfflineScriptureStore` validates the database size, SHA-256 digest, required metadata and FTS/database integrity, and stages replacement atomically with backup restoration on failure. These are integrity controls, not publisher authenticity: the manifest is unsigned and no pinned signing key or trusted source-manifest chain is implemented. `BibleDownloadsScreen` opens external source/download URLs; it does not download, verify and install packages in-app. In `SearchPlaceholderScreen`, book catalogue cards navigate at book level, but scripture passage result cards are display-only and have no click callback to the exact canonical passage. These are direct blockers to A2-04 acceptance.

### A2-05 migration coverage
The v4 database open path explicitly migrates schema 1→2→3→4, 2→3→4 and 3→4; the instrumentation fixtures exercise v1→v4, v2→v4 and v3→v4 preservation. Journal CRUD tests cover create/update/delete without a passage reference. These tests have not yet been evidenced on the latest full Android matrix. Corrupt-database recovery and interrupted migration/rollback behavior still require explicit tests.



## Owner-authorized execution update — 2026-11-22

**Current execution authority:** controlled A2 remediation on the existing `fix/a2-remediation-complete` working line, with A3 specification preparation only. A2 remains PARTIAL/OPEN. A3 implementation has not been started under this execution. Preserve `main`; no merge, signing, deployment, publication or paid-provider activation.

### Controlled integration and exact commits

- Existing A2 working branch: `fix/a2-remediation-complete`.
- PR: #47, remains open/draft; base `feat/a4-search-multi-edition-catalogue` at `b2ffb3b66079555ba88e7fcc30007ecc43074b7f`.
- Navigation-test correction commit: `255a83d0b2fbd7cfb4103b408dea68c22631eac5`.
- A3 preparation-only specification commit: `65de8b789d519ba053a2a4b2ef7740775b487c0b`.
- The A3 preparation commit changes documentation only; it does not alter Android source or workflow files.
- No changes were made to `main`.

### A2-01 / A2-06 latest navigation-test correction

The exact-head API 30 job in run `44269861070` failed three navigation tests. The detailed failure trace showed that `P2V6NavigationSmokeTest.revealGenesisCard()` timed out while waiting for the book-card content description `Open Genesis`, despite the card exposing a stable test tag `book_card_GEN`. The test was changed to wait for and click that stable tag instead of matching the content description. This is a test-selector correction, not proof that the app navigation itself now passes.

- Changed file: `app/src/androidTest/java/org/maale/bible/P2V6NavigationSmokeTest.kt`.
- New exact code commit: `255a83d0b2fbd7cfb4103b408dea68c22631eac5`.
- Workflow run: [44996476615](https://github.com/akefadaka-git/maale-bible/actions/runs/44996476615).
- Host contracts: PASS.
- Debug APK build, checksum and artifact upload: PASS.
- API 30 instrumentation job: in progress at the time this addendum was written; no pass/fail disposition is claimed.
- Full six-API matrix: NOT VERIFIED by this run.

### Localization inventory reconciliation

The current branch resource files were enumerated directly. Across `strings.xml`, `book_explorer.xml`, `book_explorer_testaments.xml` and `book_learning.xml`, each locale has 263 unique keys:

| Locale | Translatable values | Explicit English fallbacks | Total |
|---|---:|---:|---:|
| English base | 263 | 0 | 263 |
| Amharic | 162 | 101 | 263 |
| Maale | 123 | 140 | 263 |

The older aggregate statement of 113 Maale values / 150 fallbacks elsewhere in this register is superseded by this direct current-branch enumeration. The Maale total includes the app title plus 122 Owner-supplied UI entries among the 262 non-title keys. Fallback strings marked `translatable=false` are not translations and are not counted as linguistically complete. Exact-head rendering, persistence, accessibility and independent language review remain acceptance requirements.

### A3 specification preparation

Added `docs/product/A3_PREPARATION_ONLY_SPEC_2026-11-22.md` at commit `65de8b789d519ba053a2a4b2ef7740775b487c0b`. It defines A3 workstreams, entry conditions, acceptance deliverables and explicit non-authorizations. It is a documentation artifact only. It does not close A2 or start A3 implementation.

### Current disposition

A2-01 remains PARTIAL pending the API 30 result and six-API matrix evidence. A2-02 through A2-05 remain PARTIAL. A2-06 remains UNRESOLVED until the full matrix is executed and failures are dispositioned. No A2 closure or A3 implementation start is claimed.


### Navigation-test follow-up — 2026-11-22

The first selector correction at `255a83d0b2fbd7cfb4103b408dea68c22631eac5` did not resolve the navigation tests. The resulting exact-head PR run `44996476615` failed API 30: 36 instrumentation tests ran, 3 failed, and the failures still timed out in `revealGenesisCard()` while looking for `book_card_GEN`. The exact-head push run `44996471891` also recorded the same three navigation timeouts on API 26. QEMU network-pipe warnings were present, but the explicit test result identifies a Compose UI/test-selector failure; this is not classified as infrastructure-only.

A second correction was committed:

- Commit: `c92848a2b92b189ba94c3c792c7e950cea07a3a6`.
- Changed file: `app/src/androidTest/java/org/maale/bible/P2V6NavigationSmokeTest.kt`.
- The helper no longer edits the search field. It resets the Old Testament/all-category filters, then calls `performScrollTo()` on the canonical `book_card_GEN` node before asserting and clicking it.
- New PR run: [44996894032](https://github.com/akefadaka-git/maale-bible/actions/runs/44996894032), pending at the time of this update.
- New push run: [44996889626](https://github.com/akefadaka-git/maale-bible/actions/runs/44996889626), pending at the time of this update.

The second correction is not accepted until those new exact-head runs finish and the affected tests pass. Full six-API acceptance remains open.


### Navigation semantics follow-up — 2026-11-22

The second test-only correction at `c92848a2b92b189ba94c3c792c7e950cea07a3a6` changed the helper to scroll directly to `book_card_GEN`, but that exact commit has not yet completed CI. The previous run logs established that the tag was not discoverable in the merged semantics tree. Inspection found the tag modifier placed before the card's `semantics(mergeDescendants = true)` modifier.

A further UI semantics correction was committed:

- Commit: `6b9f890a1cb1618827f5fbc6057c0d543dd9a97e`.
- File: `app/src/main/java/org/maale/bible/ui/BooksScreen.kt`.
- Change: move `testTag("book_card_" + book.usfmId)` after the merged semantics modifier so the tag is attached to the merged book-card node.
- Current controlled branch head at this update: `6b9f890a1cb1618827f5fbc6057c0d543dd9a97e`.
- Current-head push workflow: [44997070823](https://github.com/akefadaka-git/maale-bible/actions/runs/44997070823), pending when inspected.
- Current-head PR workflow: not yet returned at the time of this update.
- Earlier runs for `c92848a2b92b189ba94c3c792c7e950cea07a3a6`: PR run `44996894032` and push run `44996889626`, both pending/queued when inspected.

No pass is claimed for either correction until the current-head emulator tests complete. The six-API matrix remains UNRESOLVED.


### Earlier six-API push run disposition — 2026-11-22

Run `44996471891` is the push-triggered matrix for code commit `255a83d0b2fbd7cfb4103b408dea68c22631eac5` (before the second test-helper and UI-semantics corrections). Subsequent job inspection showed:

- API 26: FAIL — 36 instrumentation tests, 3 failed.
- API 28: FAIL — 36 instrumentation tests, 3 failed.
- API 30: in progress at last inspection.
- API 33, 35 and 36: queued at last inspection.
- Build installable debug APK: PASS.
- Host contracts: PASS.

API 26 and API 28 logs both identify `ComposeTimeoutException` in `P2V6NavigationSmokeTest.revealGenesisCard()`. The same three navigation tests fail. Emulator QEMU warnings are present, but the test assertions themselves fail to locate the Genesis card; therefore these are application/UI-test failures, not infrastructure-only failures.

The newer current-head push run `44997070823` for commit `6b9f890a1cb1618827f5fbc6057c0d543dd9a97e` was pending and had not started jobs at last inspection. It is the run that must validate the updated semantics modifier order. Earlier PR/push runs for the intermediate commits were cancelled by GitHub workflow concurrency after subsequent commits; no manual cancellation or rerun was issued.
