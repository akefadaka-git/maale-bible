# A3 Product-Gap Reassessment — 2026-11-14

**Repository:** `akefadaka-git/maale-bible`  
**Assessment baseline:** PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**PR:** [#59 — feat(ui): consolidate Maale Bible application experience](https://github.com/akefadaka-git/maale-bible/pull/59)  
**Assessment branch:** `assessment/a3-product-gap-2026-11-14`  
**Assessment type:** Read-only product/code assessment, with this report as the only repository change on the assessment branch  
**Disposition:** A3 PRODUCT GAP IDENTIFIED — NOT PRODUCT-COMPLETE  
**Governance:** No merge, deployment, signing, release, publication, or scripture-content ingestion performed or authorized by this assessment.

## 1. Executive determination

The installed application is a verified Android foundation/UI integration slice, not the complete Maale Bible product. The gap is not explained by a failed build. The repository's A3 implementation scope explicitly limits the initial slice to an Android shell, accessible navigation, a metadata-only 66-book catalogue, and empty states for unavailable scripture. It explicitly defers production scripture ingestion and the full reader.

The A3 automated workflow passing therefore proves only the code paths and tests it exercises. It does not establish feature completeness, content availability, full localization, or production acceptance.

**Overall product completion: NOT SCORABLE from the current approved baseline.** There is no single approved, versioned, complete product requirements baseline with acceptance criteria and weighting for every requested module. Do not invent a completion percentage. The current A3 foundation and PR #59 are materially narrower than the complete product requirements now being raised.

## 2. Exact repository and PR evidence

- Inspected commit: `58eb32acc10415e3dfa6683a5342b6da824d5d27`.
- PR #59 is OPEN, DRAFT, and UNMERGED at the time of assessment.
- PR #59 changes 28 files. Its actual change inventory is predominantly the Android UI shell, language resources, study metadata/security, and tests.
- Repository default branch is `main`; PR #59 targets `feat/a3-final-hardening-2026-11-13`.
- The recursive Git tree at the inspected SHA contains metadata manifests and application Kotlin/resources, but no app scripture asset directory, USFM/USX corpus, packaged scripture database, map-data bundle, or commentary/study-guide corpus.
- Relevant source files inspected:
  - `docs/product/A3_ANDROID_IMPLEMENTATION_SCOPE.md`
  - `docs/product/A2_SCOPE.md`
  - `docs/product/A3_CATALOGUE_SEARCH_SLICE_2026-11-12.md`
  - `docs/product/A3_OFFLINE_STUDY_FOUNDATION_2026-11-12.md`
  - `content/source-register/README.md`
  - `content/manifests/candidate-book-inventory.json`
  - `app/src/main/java/org/maale/bible/data/catalog/CanonicalBookCatalog.kt`
  - `app/src/main/java/org/maale/bible/data/repository/StaticBookRepository.kt`
  - `app/src/main/java/org/maale/bible/ui/BooksScreen.kt`
  - `app/src/main/java/org/maale/bible/ui/ReaderPlaceholderScreen.kt`
  - `app/src/main/java/org/maale/bible/ui/SettingsScreen.kt`
  - `app/src/main/java/org/maale/bible/ui/StudyHubScreen.kt`
  - `app/src/main/java/org/maale/bible/ui/MaaleBibleApp.kt`
  - `app/src/main/java/org/maale/bible/ui/HomeScreen.kt`
  - `app/src/main/java/org/maale/bible/ui/SearchPlaceholderScreen.kt`
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/values-am/strings.xml`
  - `app/src/main/res/values-b+mdy/strings.xml`

## 3. Why scripture text is absent

The repository's A3 scope describes its initial implementation slice as a metadata-only 66-book catalogue, navigation, and empty states. It explicitly states that content-intake authorization is false and that raw USFM/USX, verse text, excerpts, and derived scripture datasets must not be committed to the public repository.

The code matches that limitation:

- `CanonicalBookCatalog` defines 66 ordered records with only `order`, `usfmId`, and `englishName`.
- `StaticBookRepository` returns those catalogue records only.
- `ReaderPlaceholderScreen` displays a reading coordinate, chapter/verse navigation controls, bookmarks, highlights, notes, and reading-plan metadata. It does not load or render scripture verses.
- The app source tree contains no scripture import/packaging implementation or packaged scripture database.

The 2008 Maale Full Bible metadata baseline identifies `MDYTWF / mdyeth`, 66 USFM files, 1,189 chapters, and 30,506 verse-marker lines. That is source-readiness evidence, not evidence that scripture has been imported into the app.

**Authority reconciliation required:** repository documents still say `content_intake_authorized=false`. This conflicts with a later Owner-level project authorization recorded outside this repository. The repository's authority record and the later approval must be reconciled in a controlled decision record before any production content-ingestion operation. The later approval remains subject to exact-source provenance, licence/permission, attribution, integrity, and compatibility checks. This report does not itself ingest content or alter the A2 closure.

## 4. Feature-by-feature gap register

Status definitions: **PRESENT** = functional implementation evidenced in source; **PARTIAL** = a limited foundation exists; **PLACEHOLDER** = presentation/empty-state only; **MISSING** = no implementation found in the inspected tree; **BLOCKED** = requires a separate authority, content, rights, or architecture decision; **DEVICE DEFECT REPORTED** = observed by Owner on the installed handset but root cause not yet independently reproduced.

| Product area | Current evidence at inspected SHA | Status | Required outcome / next acceptance |
|---|---|---|---|
| Android application shell and navigation | Compose/Material 3 shell with Home, Books, Search, More; Listen, Study, Settings under More | PRESENT — FOUNDATION | Verify complete device layouts, navigation, accessibility and state restoration |
| Bible content package | No scripture corpus or packaged database in app tree; no production importer | BLOCKED / MISSING IN APP | Reconcile content authority; define secure licensed ingestion, provenance, attribution, versioning, integrity and offline packaging |
| Bible reader | Reader is explicitly named `ReaderPlaceholderScreen`; coordinates and study metadata only | PLACEHOLDER | Implement verse rendering, chapter navigation from verified source boundaries, typography, reading position, offline behavior and accessibility |
| 66-book catalogue | 66 English metadata records in canonical order | PARTIAL | Retain approved order; add reviewed localized book names, testament classification and edition/language associations |
| Old Testament / New Testament grouping | Book model has no testament property or group model; screen renders a single flat list | MISSING | Add explicit approved testament grouping and test all 66 records against the accepted inventory |
| Maale book names | Canonical model only stores English names; UI renders `englishName` | MISSING | Add reviewed Maale names and specialist validation; do not transliterate or invent names |
| Amharic book names | No localized book-name field/data source in canonical catalogue | MISSING | Add a verified Amharic catalogue and localization tests |
| Interface localization | English and Amharic resource files each contain 125 string keys. Maale file has 144 keys but lacks 67 of the 125 English keys; many additional Maale keys belong to a different/older naming set | PARTIAL / DEFECT | Reconcile resource-key sets, eliminate fallback English for required Maale screens, and add automated locale completeness checks |
| Localized brand/tagline | English value is “LIVING WORD EXPERIENCE”; Amharic value is “ሕያው ቃል”; Maale value is “ናንጋ ቃኣላ”. The requested complete Amharic phrase is not present as the displayed tagline | PARTIAL / DEFECT | Implement Owner-approved complete localized brand strings and verify live language switching in header, home, app label and relevant metadata |
| Header layout | Fixed-height 64dp TopAppBar with brand and multiple actions; Owner reports clipped/hidden header on handset | DEVICE DEFECT REPORTED | Reproduce on target handset, inspect insets/action overflow/font scale, and pass narrow-screen and large-font acceptance |
| Welcome / onboarding | Home contains promotional/hero copy; no distinct first-run onboarding or personalized welcome flow found | PARTIAL / MISSING | Define first-run welcome, guest entry, language choice, privacy/content expectations and return-user behavior |
| Home / daily Bible verse | Daily Scripture card explains that a verified passage will appear after content integration; no daily verse scheduler/content selector found | PLACEHOLDER / BLOCKED | Add a source-backed daily passage selector, local date rules, offline caching and language-specific display after content intake |
| Settings | Encrypted backup/restore, accessibility information, language information and content availability sections exist; several controls are informational, and visible hard-coded English remains | PARTIAL | Implement actual preferences, localized status/errors, E.C./G.C. selector, text/display preferences and functional About/update links |
| Profile | Header dropdown shows guest/local-only state and language shortcut; no profile page or account/profile model | PLACEHOLDER | Decide guest/local profile scope first; any remote account/authentication requires a separate privacy/security/backend decision |
| Notifications | Header dropdown says there are no notifications; no notification centre, channel policy or daily reminder scheduler found | PLACEHOLDER / MISSING | Define notification centre, user consent, channels, quiet hours, schedule/time-zone rules and deep links |
| About page | No About screen or route found | MISSING | Add product/version, edition/source, licences/attribution, acknowledgements, privacy, contact and open-source notices |
| App update option | Version metadata exists in Gradle; no in-app update UI/update mechanism found | MISSING | Decide distribution channel and implement safe update discovery/hand-off; do not imply Play in-app updates for a sideloaded build |
| Biblical maps / places | Study screen renders a decorative map preview and explicitly states map data is not loaded; no verified map/place dataset found | PLACEHOLDER / RIGHTS-BLOCKED | Define map scope, source/licence, geographic accuracy, offline tiles/data, attribution and accessible text alternatives |
| Study notes, bookmarks, highlights | Local study database/repository and UI flows exist | PRESENT — FOUNDATION | Complete handset acceptance, data integrity and usability; distinguish reference coordinates from verified verse content |
| Reading plans | Plan metadata/progress foundation exists | PARTIAL | Add scripture-backed plan content, schedule semantics, E.C. date handling, reminders and completion tests |
| Commentaries | No commentary corpus, data model or screen found | MISSING / RIGHTS-BLOCKED | Define editorial sources and per-resource permissions, attribution, language, edition linking and study UX |
| Study guides | No study-guide corpus/module found | MISSING / RIGHTS-BLOCKED | Define curriculum, source/editorial governance, rights and offline distribution |
| Calendar | Study screen uses `java.time.LocalDate` and Gregorian date formatting; no Ethiopian-calendar conversion or preference selector found | PARTIAL / DEFECT | Implement Ethiopian Calendar (E.C.) as default and G.C. as an explicit user-selectable alternative; test conversion, month boundaries, leap years and date persistence |
| Quizzes | No quiz/question model, screen or assessment flow found | MISSING | Define question sources, answer validation, offline assessment and accessibility |
| Rewards / achievements | No reward, points, badge, streak or achievement system found | MISSING | Define non-manipulative reward policy, privacy-minimal progress model and anti-corruption tests before implementation |
| IROKA AI integration | No IROKA client, API contract, model gateway, AI screen or integration module found | MISSING / ARCHITECTURE DECISION | Define separate integration contract, privacy boundary, consent, offline behavior, content grounding/citations, safety and service ownership |
| Audio / listening | Listen screen exists, but playback and downloads are disabled; no audio assets are included | PLACEHOLDER / RIGHTS-BLOCKED | Separate audio source/rights verification, catalogue, playback, downloads and offline integrity acceptance |
| Search | Search works on English book names and USFM codes only; screen explicitly disclaims scripture full-text search | PARTIAL | Add localized catalogue search and full-text search only after verified scripture indexing |
| Accessibility | Some semantic labels/test tags and Material controls exist; full TalkBack and physical-device acceptance not evidenced | PARTIAL / NOT ACCEPTED | Complete TalkBack, focus order, dynamic text, contrast, touch-target and language pronunciation tests |

## 5. Confirmed localization gap

At the inspected SHA, resource-key comparison produced:

| Resource set | Keys | Missing keys relative to English |
|---|---:|---:|
| English `values` | 125 | 0 |
| Amharic `values-am` | 125 | 0 |
| Maale `values-b+mdy` | 144 | 67 |

The Maale resource file contains many extra keys that do not correspond to the current English key set, indicating that resource naming/content from an earlier UI iteration has not been reconciled with the unified UI. The 67 missing keys cause Android resource fallback to the default English strings. This is a measurable implementation defect, not merely a subjective translation concern.

In addition, several Kotlin screens still embed English text directly (including Settings, Reader, Search and study labels), bypassing Android resource localization. Resource-key parity alone will therefore not complete localization.

The required Amharic tagline supplied by the Owner is:

`መጽሐፍ ቅዱስ - ሕያው ቃል ተሞክሮ`

The exact English brand/tagline wording still needs an Owner-confirmed canonical string; do not guess it from a truncated phrase.

## 6. Scope interpretation: what A3 did and did not promise

The committed A3 Android scope's acceptance gates focus on:
1. Android project configuration and dependencies.
2. System light/dark theme.
3. Metadata-only canonical 66-book catalogue.
4. Home, Books, Search and Reader-placeholder navigation.
5. Catalogue count, uniqueness and order tests.
6. No raw scripture or database committed.
7. Build/test evidence.

The same document explicitly defers real-source ingestion, production reader content, scripture schema, full-text index, audio, sync/backend, account/authentication, analytics, release signing and deployment.

Therefore:
- The absence of production scripture, a full reader, account system, audio and cloud services is consistent with the narrow initial A3 scope.
- The Owner's current handset findings and broader product expectations reveal that the delivered A3 slice is not a complete product.
- The missing Maale resource keys, hard-coded English, English-only catalogue names, absent testament grouping and clipped header are implementation defects/gaps that require explicit work even within the broader A3 product-quality scope.
- Do not describe PR #59 or its passing CI as feature-complete or production-ready.

## 7. Authority, licensing and dependency register

1. **Scripture content:** The A2 baseline identifies the 2008 Maale Full Bible `MDYTWF / mdyeth`, CC BY-SA 4.0, and a 66-book metadata baseline. The repository still contains the older `content_intake_authorized=false` statement. Reconcile that statement against the later Owner approval before any real content ingestion. Preserve the 2008 edition identity; do not substitute the separate 2007 Maale NT.
2. **Maale language:** Specialist review of Maale language/Ethiopic script remains an accepted deferred A2 residual risk. New Maale book-name strings and UI translations require review; machine encoding checks are not linguistic sign-off.
3. **Amharic/English resources:** Each source/edition needs provenance, permission/licence, attribution and integrity checks before packaging.
4. **Maps, commentary, study guides, audio and artwork:** Separate source-specific rights and attribution evidence required. Do not assume the Bible's CC BY-SA licence covers third-party assets or derivative content.
5. **IROKA:** Requires an explicit architecture, data-sharing, safety, privacy, consent and service-availability decision. No integration should be fabricated as a local mock and represented as connected AI.
6. **Profile/accounts:** The current A3 scope excludes backend/account/authentication. Keep local guest profile distinct from any cloud account decision.
7. **Notifications and updates:** Need distribution, Android permission, scheduling, privacy and channel decisions; these are not supplied by the current shell.

## 8. Prioritized remediation programme

### P0 — Product definition and acceptance baseline
- Approve a versioned full-product requirements inventory with screen map, user journeys, offline/online boundaries and acceptance tests.
- Reconcile the scripture content-intake authority record against later Owner approval.
- Freeze language requirements: Maale-first, Amharic and English; canonical localized brand strings; E.C. default and G.C. selectable alternative.
- Record source/licence/attribution gates for each content family.

### P1 — Critical reader and catalogue
- Build a source-verified scripture ingestion and offline packaging pipeline without placing raw corpus files in the public repository.
- Add a real verse-backed reader and verified chapter/verse navigation.
- Extend the book model with testament and localized-name support; validate all 66 IDs/order.
- Add Old Testament/New Testament sections and Maale/Amharic/English catalogue presentation.
- Add real-device reader acceptance, data-integrity and offline tests.

### P2 — Localization and core UX defects
- Reconcile all Maale resource keys with English/Amharic; eliminate fallback strings on supported screens.
- Replace hard-coded UI strings with resources and test language switching on every screen.
- Correct header clipping and test edge-to-edge/insets, narrow widths, font scaling and landscape.
- Add welcome/onboarding, full profile/guest page, About and Settings preferences.
- Implement E.C.-default/G.C.-optional calendar and persistence.

### P3 — Study and discovery
- Implement daily scripture selection and reminders using verified passages.
- Implement commentary and study-guide modules after rights/source approval.
- Implement biblical place records/maps after rights and data-quality review.
- Complete reading plans with calendar-aware scheduling.
- Add quizzes and a transparent, privacy-minimal reward/achievement model.

### P4 — Platform integrations
- Define and implement IROKA integration only after its architecture, consent, privacy and safety contract is approved.
- Add notifications centre and user-configurable notification controls.
- Add update discovery/hand-off according to the chosen distribution channel.
- Implement audio playback/downloads only after separate audio rights and asset approval.

### P5 — Acceptance and release governance
- Full physical-device and TalkBack acceptance.
- Offline, performance, storage, migration, security, privacy and content-integrity testing.
- Independent review of security and content/licence controls.
- A5 signing, deployment, store submission and publication remain separate gates requiring explicit Owner approval.

## 9. A3 reassessment conclusion

| Gate | Status |
|---|---|
| Exact repository head inspected | VERIFIED — `58eb32a` |
| A3 scope reviewed | VERIFIED |
| PR #59 changed-file inventory reviewed | VERIFIED |
| App tree / scripture asset presence inspected | VERIFIED — no packaged scripture corpus/database found |
| Catalogue structure inspected | VERIFIED — 66 English-only metadata records; no testament field |
| Reader implementation inspected | VERIFIED — placeholder/coordinates, no verse renderer |
| Localization key parity | VERIFIED — 67 Maale keys missing relative to English |
| Hard-coded English in UI | VERIFIED — present in multiple screens |
| Settings/profile/notifications/maps status | VERIFIED — partial or placeholder as detailed above |
| Calendar, quiz, reward, commentary, guide and IROKA status | VERIFIED — no implementation found in inspected source tree |
| Header clipping root cause | UNRESOLVED — Owner-reported handset defect; reproduction evidence required |
| Complete product requirements baseline | MISSING |
| Content-intake authority record reconciliation | UNRESOLVED |
| Full physical-device/TalkBack acceptance | NOT RUN / NOT ACCEPTED |
| Product completeness | NOT READY |
| Merge, deployment, signing, publication | NOT AUTHORIZED |

**Decision:** Keep PR #59 OPEN and DRAFT. Do not merge it on the strength of automated CI. Treat the present app as an A3 foundation prototype. Use this reassessment to define a new, owner-approved implementation sequence and acceptance baseline. This report changes no application code, content, A2 closure, or release state.
