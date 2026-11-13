# Maale Bible — Product Requirements Baseline v1.0 (P0)

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Owner:** Akefa Ayele Daka  
**Baseline date:** 2026-11-14  
**Status:** OWNER-APPROVED REQUIREMENTS BASELINE — P0 FROZEN  
**Repository:** `akefadaka-git/maale-bible`  
**Baseline source:** PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27`; A3 Product-Gap Reassessment commit `a3be545b6ff0bc53fff11a0778aa8b4cbccabe34`  
**Requirements branch:** `requirements/p0-product-baseline-2026-11-14`  
**Implementation status:** NOT STARTED UNDER THIS BASELINE  
**Release authority:** NOT GRANTED

## 1. Owner decision and baseline purpose

By the Owner's explicit P0 instruction on 2026-11-14, this document establishes and freezes the complete product requirements baseline for the Maale Bible Android product, reconciles the later content-intake authority with older repository statements, and records prioritized acceptance criteria before implementation.

This baseline is the product-level requirements authority for future A3 implementation planning. The current content-intake authority is separately recorded in [Owner Content-Intake Authority Reconciliation](../licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md). It supersedes the narrower initial A3 foundation-slice scope as the definition of the intended product. It does not erase the historical purpose or evidence of A1/A2 closure, nor does it turn existing placeholder screens into accepted product features.

**Freeze rule:** requirements may be changed only through an explicit Owner-approved change record that identifies the requirement IDs, reason, impact, affected acceptance tests and revised baseline version. Implementation convenience, an existing placeholder, a passing CI run, or a third-party source does not silently change this baseline.

## 2. Product vision and principles

Deliver a reliable, accessible, language-first, offline-first Android Bible and study application centered on the verified Maale Bible, with Amharic and English editions/interface support and an extensible framework for other properly authorized Bible and study resources.

Binding principles:

1. Maale identity and language are first-class, not a translated English catalogue.
2. Scripture must be exact to its selected edition and source. Never fabricate, paraphrase, silently normalize, transliterate, or substitute verses.
3. Offline reading and core personal study must work without network connectivity after the authorized content package is installed.
4. E.C. is the default calendar. G.C. is an explicit, selectable alternative.
5. Every Bible/study/asset source is individually provenance-, rights-, attribution-, integrity- and quality-checked.
6. Protect user notes, bookmarks, reading history, profile data and recovery material.
7. Distinguish local guest functionality from any future online identity/account service.
8. IROKA must be a clearly disclosed, permission-based integration; AI-generated output must never be presented as canonical scripture.
9. All supported languages must be complete for the shipped UI flows; fallback to English is not acceptable as a hidden substitute for Maale or Amharic.
10. No feature is complete based solely on a placeholder, static mock, screenshot, compile success or emulator-only test.

## 3. Users and supported language modes

### 3.1 Language modes

- **Maale (mdy):** primary language and identity; support the verified Maale 2008 Full Bible edition when source-specific content checks are complete.
- **Amharic (am):** supported UI and Bible edition when a complete, rights-cleared edition is verified.
- **English (en):** supported UI and Bible edition when the selected edition is verified.
- Additional languages may be added only through source/translation review and explicit Owner-approved scope.

### 3.2 Localization requirements

- **LOC-01:** Provide an in-app language selector for Maale, Amharic and English.
- **LOC-02:** Language selection applies consistently to navigation, headings, buttons, error/empty states, dialogs, settings, onboarding, notifications, catalogue names, book metadata and app branding.
- **LOC-03:** Language selection persists across process death, restart and device configuration changes.
- **LOC-04:** Do not silently fall back to English for a supported screen. If a translation is unavailable, identify the missing translation during QA; do not ship the affected flow as complete.
- **LOC-05:** Remove user-visible hard-coded strings from Kotlin/Compose source and use reviewed localized resources.
- **LOC-06:** The Amharic home brand/tagline must use the Owner-specified exact text: **“መጽሐፍ ቅዱስ - ሕያው ቃል ተሞክሮ”**.
- **LOC-07:** English brand presentation must clearly render the English equivalent beginning “Bible — Living Word …”; the final complete English wording must be confirmed by Owner before visual copy is frozen.
- **LOC-08:** Maale branding and all Maale book names must be reviewed by a qualified Maale-language reviewer. Machine encoding tests are not linguistic approval.
- **LOC-09:** Test Ethiopic glyph coverage, line wrapping, font fallback, mixed-direction text where applicable, accessibility labels and large text.
- **LOC-10:** CI must compare resource keys across all supported locales and fail on missing required strings; also scan for unintended user-visible hard-coded strings.

## 4. Calendar and time requirements

- **CAL-01 — DEFAULT:** Ethiopian Calendar (E.C.) is the app-wide default for date display and date-based user experiences.
- **CAL-02 — ALTERNATIVE:** Gregorian Calendar (G.C.) is an explicit user-selectable alternative in Settings.
- **CAL-03:** Calendar preference persists across restart and applies consistently to Home, daily verse, reading plans, progress/streaks, study history, reminders, notifications, quizzes and any date-labelled activity.
- **CAL-04:** Changing calendar changes presentation, not the underlying instant/event or stored canonical timestamp.
- **CAL-05:** Date conversion must correctly handle E.C. month boundaries, Pagume, leap-year rules, year transitions, local time zone and G.C. leap years.
- **CAL-06:** Date arithmetic and reminders must be deterministic and tested around midnight, daylight/time-zone changes and calendar-year boundaries.
- **CAL-07:** The user must be able to identify the active calendar and change it without losing study data.
- **CAL-08:** Calendar choice is separate from language choice; changing language must not silently change calendar preference.

## 5. Scripture catalogue and Bible reader

- **BIB-01:** Maintain the Owner-approved canonical 66-book order for the selected 2008 Maale Full Bible edition.
- **BIB-02:** Catalogue records include stable book ID/USFM ID, testament grouping, canonical order, reviewed Maale name, reviewed Amharic name, English name and edition/language availability.
- **BIB-03:** Display separate Old Testament and New Testament sections, with accessible section headings and book counts.
- **BIB-04:** Book names display in the currently selected UI language when a reviewed translation exists; never display English-only names as the intended Maale catalogue.
- **BIB-05:** Reader opens a selected book/chapter and displays the exact verse text from the selected, verified edition.
- **BIB-06:** Reader supports chapter/verse navigation, previous/next chapter, reading position, clear book/chapter/verse coordinates and stable deep links.
- **BIB-07:** Reader supports readable typography, font scaling, line spacing, theme/dark mode, selection and accessible reading order.
- **BIB-08:** Reader handles long chapters, verse ranges, poetry, headings, footnotes/cross-references where licensed, and Ethiopic text without clipping or data loss.
- **BIB-09:** If a content edition is not installed or unavailable, show an explicit localized state; never fabricate sample scripture or silently substitute another edition.
- **BIB-10:** Each displayed passage is traceable to edition, language, source revision and content-package version.
- **BIB-11:** Offline reading works after package installation, without a network connection.
- **BIB-12:** Content package installation/update is atomic and integrity-checked; interrupted installation must not leave a mixed or corrupted edition.
- **BIB-13:** Search includes localized book-name/code lookup; full-text scripture search is available only for installed, verified content and preserves edition/language boundaries.
- **BIB-14:** Search results deep-link to exact book/chapter/verse and remain correct offline.
- **BIB-15:** No raw source text or generated scripture database is committed to the public GitHub repository unless the exact licence expressly permits that distribution and the Owner separately approves it.

## 6. Home, welcome and daily scripture

- **HOME-01:** Provide a first-run welcome/onboarding experience introducing the app, language selection, offline content model, privacy and guest mode.
- **HOME-02:** Provide a localized home screen with clear entry points to Read, Books, Search and Study.
- **HOME-03:** Display the app's approved localized title/tagline without clipping or inconsistent variants.
- **HOME-04:** Provide a daily Bible verse/passage selected from a verified installed edition, with edition and reference displayed.
- **HOME-05:** Daily verse selection is deterministic for a user-local calendar day and consistent with the selected E.C./G.C. presentation preference.
- **HOME-06:** Daily verse works offline from a locally available verified schedule/cache; network failure must not break Home.
- **HOME-07:** If no authorized scripture package is available, show a localized unavailable-content state, not a fabricated daily verse.
- **HOME-08:** Support a welcome-back state and a clear continue-reading action when reading history exists.

## 7. Personal study and learning

### 7.1 Personal study foundation

- **STU-01:** Users can create, view, edit and delete private notes attached to a scripture reference when the corresponding edition is installed.
- **STU-02:** Users can bookmark and highlight passages; records retain edition and reference identity.
- **STU-03:** User study data persists locally, survives restart and is included in encrypted backup/restore according to the backup contract.
- **STU-04:** Users can create and track reading plans, daily progress and completion history.
- **STU-05:** Reading plans support E.C. default scheduling and G.C. alternative display without shifting stored events.
- **STU-06:** Export/share preserves source attribution, edition identifiers and applicable CC BY-SA notices; sharing is disabled where source terms do not permit it.

### 7.2 Commentaries and study guides

- **STU-10:** Provide a commentary library linked to book/chapter/verse references.
- **STU-11:** Each commentary displays author/editor, title, edition/version, language, source, licence/permission, attribution and publication metadata.
- **STU-12:** Provide study guides organized by topic/book/lesson, with progress and reference links.
- **STU-13:** Commentary and guide content can be searched and used offline when installed.
- **STU-14:** No commentary or guide is bundled until its source and derivative/distribution rights are verified and recorded.
- **STU-15:** Distinguish canonical Bible text, editorial commentary and user notes visually and semantically.

### 7.3 Quizzes and rewards

- **QUIZ-01:** Provide quizzes linked to verified scripture and approved study-guide content.
- **QUIZ-02:** Each question records its source/reference, language, answer key, explanation and version.
- **QUIZ-03:** Support multiple-choice and other question types only where answer validation and accessibility can be tested.
- **QUIZ-04:** Provide feedback and review of incorrect answers without altering scripture text.
- **REW-01:** Provide transparent learning achievements/rewards tied to user-chosen study activity (for example, completed lessons or reading plans).
- **REW-02:** Reward rules are documented, predictable and locally testable; users can inspect progress and reset optional progress where appropriate.
- **REW-03:** No gambling, paid random rewards, manipulative streak pressure, advertising-based rewards or unnecessary personal-data collection.
- **REW-04:** Rewards must not imply spiritual status, doctrinal authority or comparative ranking of users.

## 8. Maps and places

- **MAP-01:** Provide a biblical places/maps experience with searchable place names and linked Bible references.
- **MAP-02:** Each place record identifies source, geographic confidence/precision, editorial notes, attribution and licence.
- **MAP-03:** Map data and tiles must be licensed for intended offline storage and display; map provider restrictions must be honored.
- **MAP-04:** Provide a text/list alternative to map visualization and accessible labels.
- **MAP-05:** Support offline access to installed map/place data where rights permit.
- **MAP-06:** If map data is not installed, show a truthful localized empty state; no decorative map may be represented as real geographic data.

## 9. Profile, privacy and account boundary

- **PRO-01:** Provide a dedicated profile page, distinct from the header dropdown.
- **PRO-02:** The initial product supports a local guest profile with language/calendar preferences and local study identity.
- **PRO-03:** Profile shows locally stored reading/study progress and user preferences with clear privacy information.
- **PRO-04:** Cloud accounts, registration, OTP, remote synchronization and social login are outside this baseline's initial offline scope and require a separate Owner-approved backend/privacy/security architecture decision.
- **PRO-05:** No account or network dependency may block local reading, notes, bookmarks or installed content.
- **PRO-06:** User data export, deletion/reset and backup recovery behavior must be clear and tested.

## 10. Notifications and reminders

- **NOT-01:** Provide a dedicated notification centre/page with read/unread state and notification history.
- **NOT-02:** Support opt-in daily verse and reading-plan reminders; permission must be requested contextually and users can disable each category.
- **NOT-03:** Settings expose schedule/time, quiet hours and reminder category controls.
- **NOT-04:** Reminder date/time presentation follows E.C. default or G.C. alternative while actual scheduling uses correct local time semantics.
- **NOT-05:** Notification deep links open the exact verse/plan when content is available; otherwise show a safe localized fallback.
- **NOT-06:** No notification is sent without applicable OS permission and user opt-in; no sensitive scripture/study content is exposed on lock screen by default without user choice.

## 11. Settings, About and updates

- **SET-01:** Settings is a functional page, not a placeholder; it exposes language, calendar, reading/display, accessibility, notifications, privacy, content management and backup controls.
- **SET-02:** Settings changes persist and are reflected immediately in affected screens.
- **SET-03:** Provide a dedicated About page with app identity, version/build, edition/source information, licences/attributions, acknowledgements, privacy policy and support/contact information.
- **SET-04:** Provide an update option that checks or hands off to the actual distribution channel; clearly report current version and available update state.
- **SET-05:** Update behavior must match distribution method (for example, managed store versus sideload); never promise an in-app update where the channel does not support it.
- **SET-06:** Content edition updates and application updates are distinct operations with separate version, integrity and rollback behavior.
- **SET-07:** Backup/restore remains encrypted, file-based and user-controlled; recovery keys remain transient and are never persisted in saveable UI state or logs.
- **SET-08:** Backup file I/O runs off the main thread; invalid, cancelled or interrupted restore must preserve existing user data.

## 12. Audio and listening

- **AUD-01:** Provide a listening library with book/chapter/track selection and clear edition/language metadata.
- **AUD-02:** Playback controls include play/pause, seek, progress and navigation appropriate to the licensed recording.
- **AUD-03:** Offline download is available only for recordings with verified offline-distribution rights.
- **AUD-04:** Audio sources, performers, recording rights, licence, attribution and territorial/term restrictions are individually recorded.
- **AUD-05:** Audio must remain separate from scripture text edition selection; do not assume scripture licence grants audio rights.
- **AUD-06:** If audio is not available, show a localized disabled/unavailable state rather than a non-functional playback control.

## 13. IROKA AI integration

- **AI-01:** IROKA is an optional, explicitly identified AI study assistant, not the source of canonical Bible text.
- **AI-02:** Before integration, approve a separate technical and governance contract defining API/service ownership, authentication, data flow, retention, model/provider, cost controls, offline behavior, availability and incident handling.
- **AI-03:** Obtain clear user consent before sending prompts, notes, profile data or reading history to an external service.
- **AI-04:** Do not send private notes or profile data by default; provide data-minimizing controls and a way to clear conversation history.
- **AI-05:** AI answers about scripture must cite the selected edition and passage where evidence is available, distinguish quotation from interpretation, and disclose uncertainty.
- **AI-06:** AI-generated content must never overwrite, modify or masquerade as canonical scripture, approved commentary or Owner-approved study material.
- **AI-07:** Provide a localized unavailable/offline state and preserve normal Bible reading when IROKA is unavailable.
- **AI-08:** Integration is not considered delivered until end-to-end security, privacy, grounding, safety and failure-mode tests pass.

## 14. Navigation, layout and accessibility

- **UX-01:** Provide consistent navigation to Home, Books, Search, Read, Study, Listen, Maps, Profile, Notifications, Settings and About; surface placement may be adapted responsively but no required page may be hidden behind an inert placeholder.
- **UX-02:** Header content must not be clipped, obscured by system insets, or inaccessible at supported screen widths and font scales.
- **UX-03:** Test compact Android phones, large font/display sizes, landscape and expanded layouts.
- **UX-04:** Provide TalkBack semantics, logical focus order, meaningful labels, keyboard/switch access where supported, adequate touch targets and contrast.
- **UX-05:** Provide user-visible loading, empty, error, offline, permission-denied and success states for every network/file/content flow.
- **UX-06:** Support system light/dark theme and verify contrast in both.
- **UX-07:** All user-facing flows are localized in each shipped language, including accessibility labels and notifications.
- **UX-08:** No placeholder, disabled control or decorative mock may be presented as a completed feature.

## 15. Technical, security and quality requirements

- **NFR-01:** Preserve the approved Android/Kotlin/Gradle/Jetpack Compose/Material 3 technical baseline unless a separately reviewed compatibility decision changes it.
- **NFR-02:** Maintain clear presentation/domain/data boundaries and repository interfaces for content and persistence.
- **NFR-03:** Offline-first architecture: installed scripture, catalogue, personal study and installed study resources work without network.
- **NFR-04:** Network failure, missing content, interrupted downloads and storage exhaustion fail safely and do not corrupt existing data.
- **NFR-05:** Encrypt sensitive local user data and backup artifacts; protect keys and secrets; do not log recovery keys, passwords, OTPs or private notes.
- **NFR-06:** Validate content package hashes, manifests, schema/version compatibility and atomic installation/rollback.
- **NFR-07:** Database migrations are non-destructive, tested with migration fixtures, fault injection and restore rollback.
- **NFR-08:** Provide automated unit, integration, instrumentation, localization, content-integrity and UI accessibility tests.
- **NFR-09:** Every release candidate is verified on the exact source SHA; post-verification code changes invalidate prior exact-head evidence.
- **NFR-10:** Report known limitations and unsupported Android/device capabilities honestly.
- **NFR-11:** No analytics, advertising SDK, cloud sync or account backend is introduced without separate Owner approval.
- **NFR-12:** Maintain a source/edition manifest and user-visible attribution/licence information for each content package.

## 16. Prioritized implementation and acceptance gates

Priority definitions:
- **P0 — Baseline and authority:** requirements, source authority, acceptance definitions; no product implementation.
- **P1 — Critical usable Bible:** content pipeline, reader, catalogue, localization, calendar, layout and offline integrity.
- **P2 — Core app experience:** onboarding, settings, profile/guest, About, daily verse, personal study, reading plans and notifications.
- **P3 — Study enrichment:** commentaries, guides, maps, quizzes, rewards and audio, each source/rights gated.
- **P4 — Optional integration:** IROKA AI and any future online service, with separate architecture/privacy approval.
- **P5 — Product acceptance and release:** complete device/accessibility/security/content acceptance; A5 remains separately gated.

### P0 — baseline acceptance (this stage)

- **P0-01:** One canonical versioned product requirements baseline covers every Owner-raised product module and non-functional requirements.
- **P0-02:** E.C.-default/G.C.-selectable requirement is explicit, consistent and testable.
- **P0-03:** Later Owner content-intake authorization is reconciled with stale `content_intake_authorized=false` repository text without rewriting closed A2 historical evidence.
- **P0-04:** Source-by-source authority matrix identifies permitted intake operations, rights conditions, distribution restrictions and unresolved evidence.
- **P0-05:** Prioritized acceptance criteria are frozen with requirement IDs and objective pass/fail evidence.
- **P0-06:** P0 documents are committed on a dedicated requirements branch; application source and PR #59 remain untouched.
- **P0-07:** No implementation begins under this baseline until the Owner's separate P0 closure/next-stage authorization.

### P1 — critical Bible experience acceptance

- **P1-01:** Source package acquisition is reproducible; provenance, exact version, rights, attribution, checksums and integrity are recorded.
- **P1-02:** 66/66 canonical books appear in approved order, with correct testament classification and reviewed names in every shipped language.
- **P1-03:** Installed reader displays correct source verses for a representative test matrix spanning all books, chapter boundaries, verse ranges, poetry and special markers; full-source structural reconciliation passes.
- **P1-04:** No missing/duplicated/reordered/modified verses relative to the verified source; edition/language identity is visible and testable.
- **P1-05:** Reader and catalogue operate offline after package installation; airplane-mode acceptance passes.
- **P1-06:** Maale, Amharic and English locale completeness checks pass; no missing keys or unintended hard-coded UI strings.
- **P1-07:** E.C. is selected on clean install; G.C. can be selected; preference persists; conversions pass defined boundary tests.
- **P1-08:** Header and primary flows pass physical-device layout, font scaling, rotation and insets tests.
- **P1-09:** Full-text search returns correct edition-scoped references and opens exact passage offline.
- **P1-10:** Content installation/update is atomic, integrity checked, recoverable and never substitutes another edition.

### P2 — core app experience acceptance

- **P2-01:** First-run welcome, language choice and guest-mode onboarding complete without network.
- **P2-02:** Dedicated profile/guest page exposes preferences and local progress without implying cloud account.
- **P2-03:** Functional Settings page controls language, calendar, accessibility, reading and notifications; changes persist.
- **P2-04:** About page displays correct app/build/edition/licence/attribution/privacy details.
- **P2-05:** Daily verse is verified, date-deterministic, offline-capable and localized; unavailable content is never fabricated.
- **P2-06:** Notes, bookmarks, highlights and reading plans pass persistence, backup/restore and reference-integrity tests.
- **P2-07:** Notification centre and opt-in reminders pass permission, schedule, quiet-hours, calendar and deep-link tests.
- **P2-08:** Update option correctly reflects actual distribution channel and separates app update from content update.

### P3 — study enrichment acceptance

- **P3-01:** Every commentary and study guide has verified provenance, licence, attribution, version and reference integrity before packaging.
- **P3-02:** Commentary/guide content is distinguishable from canonical scripture and searchable offline where installed.
- **P3-03:** Maps use verified/licensed geographic data, include attribution and accessible text alternative, and work offline where supported.
- **P3-04:** Quizzes cite verified source material, localize questions/feedback, validate answers and preserve progress offline.
- **P3-05:** Rewards are transparent, non-monetary/non-random, privacy-minimal and do not imply spiritual rank.
- **P3-06:** Audio playback/download works only for individually rights-cleared recordings; offline integrity and attribution pass.

### P4 — IROKA acceptance

- **P4-01:** Separate Owner-approved integration architecture and data-processing/privacy contract exists.
- **P4-02:** User consent is explicit and granular; private notes/profile/history are not transmitted by default.
- **P4-03:** AI answers distinguish canonical text, quoted sources, interpretation and generated suggestions; references are verifiable.
- **P4-04:** IROKA outage/offline behavior does not impair reading/study; no AI response is represented as scripture.
- **P4-05:** Security, privacy, safety, retention and end-to-end integration tests pass.

### P5 — product acceptance / release readiness

- **P5-01:** All P1–P4 applicable acceptance criteria are PASS or explicitly Owner-waived with a recorded risk; no silent omissions.
- **P5-02:** Physical-device tests pass on supported handset profiles.
- **P5-03:** TalkBack and accessibility acceptance passes in Maale, Amharic and English.
- **P5-04:** Security, privacy, backup/restore, migration, offline, performance and data-integrity review passes.
- **P5-05:** Independent security review and content/licence review are completed where required.
- **P5-06:** Release candidate evidence identifies exact SHA, artifact hashes, tests, known issues and rollback plan.
- **P5-07:** A5 signing, deployment, store submission and publication require separate explicit Owner authorization and are not authorized by this baseline.

## 17. Current baseline-to-implementation traceability

The current PR #59 / A3 foundation is not accepted as satisfying this complete baseline. The A3 gap reassessment (2026-11-14) remains the evidence-backed snapshot of current implementation status. Existing functionality must be re-tested against the criteria above; source presence alone does not imply acceptance.

Initial disposition:
- **Present foundation, revalidation required:** Android shell/navigation, local study metadata, encrypted backup/restore foundation, Material 3, theme, catalogue metadata, search by English name/USFM ID.
- **Partial/defective:** localized UI, book catalogue, settings, reading plans, accessibility, header layout, daily verse card.
- **Missing/placeholder:** production scripture reader/content, testament grouping, localized book names, maps, profile page, About, update option, notification centre, full daily verse feature, commentary library, study guides, IROKA integration, E.C./G.C. selector, quizzes, rewards, full audio.
- **Rights/architecture gated:** scripture editions, commentaries, study guides, maps, audio, third-party assets and IROKA data/service integration.

## 18. Explicit exclusions and separate approvals

This baseline does **not** authorize:
- Merge of PR #59 or any other PR.
- Deployment, signing, store submission, release or publication.
- Public-repository storage of raw scripture or protected assets.
- Automatic approval of all candidate Bible translations, commentaries, maps, audio or study resources.
- Backend accounts, cloud sync, analytics, advertising SDKs or social login.
- IROKA data transmission or external AI provider use before its separate integration approval.
- Any alteration of the closed A1/A2 historical evidence or silent removal of accepted residual risks.

## 19. Baseline closure record

**P0 disposition:** OWNER-APPROVED / FROZEN by explicit Owner instruction dated 2026-11-14.  
**Canonical baseline version:** 1.0.  
**Requirements freeze:** 2026-11-14.  
**Next stage:** P1 planning may be prepared only after the Owner authorizes transition from P0. No implementation is authorized by this baseline alone.  
**Release readiness:** NOT READY.  
**PR #59:** Keep OPEN and DRAFT; no merge.

