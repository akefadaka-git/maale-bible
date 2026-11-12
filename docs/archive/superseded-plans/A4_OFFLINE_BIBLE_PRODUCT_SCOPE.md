# A4 — Advanced Offline Bible Product Scope and Architecture

**Status:** Owner-directed product scope; implementation branch established  
**Baseline:** A3 head `fcb1d04d7a227bfda320cd18a06b12fbd37e4b6d`  
**Release boundary:** Offline-capable Android application; no server-side services in this release  
**Content gate:** `content_intake_authorized=false` remains in force

## 1. Product objective

Build Maale Bible (ጌኤዦ ማፃኣፖ) as a complete, advanced, offline-first Bible reading and study workspace. The application should bring together the strongest useful capabilities commonly found across leading Bible reader, study, reference, journaling, mapping and personal productivity tools, while remaining coherent, fast, privacy-preserving and usable without an account or network connection.

This is a product capability target, not a claim that every resource is already available or licensed. Every text, translation, commentary, dictionary, atlas dataset, image, audio file, font and other third-party asset must pass its own source, licence, attribution, integrity and technical quality gates before distribution.

## 2. Release principles

1. **Offline by default:** Bible reading, local search, notes, bookmarks, highlights, reading plans, personal study records and installed reference resources work without network access.
2. **No account required:** core functionality is local-first and available without registration.
3. **Local privacy:** personal notes, diary, sermon notes, bookmarks and reading history remain on-device unless the user explicitly exports or backs them up.
4. **Modular content:** Bible editions and study resources are separately identified, versioned, attributed and permission-checked.
5. **No fabricated content:** no invented scripture, commentary, historical claims, maps or citations.
6. **Accessible and multilingual:** interface language, scripture language and selected edition are independent settings.
7. **Future-compatible, not server-dependent:** local domain models and repository boundaries should permit a later server-backed release without requiring a backend now.
8. **Preserve accepted A2/A3 evidence:** do not rewrite historical A2/A3 records or alter the approved source baseline.

## 3. Functional scope

### 3.1 Bible library and reader

- Edition library with edition metadata, language, canon, publisher, source, licence, attribution and version.
- Maale 2008 Full Bible (MDYTWF / mdyeth) as the primary target edition, subject to separate Owner content-intake authorization.
- Additional Amharic and English editions only after their exact edition/source rights are verified and approved.
- Reader navigation by testament, book, chapter and verse.
- Chapter/verse selection, previous/next chapter, reading position persistence and history.
- Adjustable font family/size, line spacing, margins, paragraph layout, verse numbers and display density.
- Day/night/sepia themes, system theme, screen-awake option and reading focus mode.
- Bookmarks, highlights, color labels, tags, collections and personal annotations.
- Parallel reading and verse comparison across installed editions; never imply translations are identical.
- Cross-reference navigation and contextual reference panels.
- Copy/share passage text only when permitted by the applicable edition licence and platform rules.
- Clear indication when a requested edition or chapter is not installed.

### 3.2 Search and concordance

- Offline full-text search across installed and selected editions.
- Exact phrase, all-words, any-word and prefix search.
- Search by book, testament, chapter range and selected edition.
- Search result snippets with edition and verse references.
- Search history, saved searches and recent queries.
- Concordance/word index generated only from authorized text and validated tokenization.
- Original-language lexical tools only when licensed, sourced and technically validated.
- Amharic and Maale-aware normalization/tokenization that preserves source text and does not silently rewrite spelling.
- Search indexing built locally or distributed only as a verified derived asset allowed by the source licence.
- Explicit empty, partial-index and unsupported-language states.

### 3.3 Bible reference centre

- Cross-reference library.
- Topical Bible index and subject collections.
- Bible dictionary and encyclopedia.
- Commentary and study notes, clearly separated from canonical scripture.
- Historical timelines and event references.
- People, tribes, nations, regions, cities and places directory.
- Biblical atlas with offline maps, place profiles, routes and geographic context.
- Scripture-to-place and place-to-scripture navigation.
- Resource provenance, author, edition, licence and citation metadata on every reference resource.
- No unsupported claims of archaeological or historical certainty; distinguish textual reference, scholarly interpretation and modern geography.

### 3.4 Personal study workspace

- Verse-linked inline notes and general notes.
- Daily reflection journal and personal diary.
- Sermon preparation notes with title, date, passage, outline, points and private tags.
- Study notebooks, topics, questions, observations and conclusions.
- Verse collections and personal reading lists.
- Link notes to multiple verses, books, topics and study sessions.
- Local attachments only where storage and privacy controls are clear.
- Search, filter, sort, edit, archive and export personal records.
- Local autosave, recovery from interrupted edits and explicit delete/restore safeguards.
- Notes remain attached to stable scripture references and edition context, not just a displayed verse string.

### 3.5 Reading plans and spiritual routines

- Built-in plans only when their content is original or rights-cleared.
- Custom plans: whole Bible, testament, book, topical and user-defined schedules.
- Daily reading checklist, progress, streaks and completion history.
- Flexible start date, rest days and catch-up support.
- Local reminders/notifications, fully optional and configurable.
- Daily verse/reflection cards only when the underlying text/resource is approved.
- Reading progress is local and does not require an account.

### 3.6 Verse cards, export and sharing

- Render selected verse(s) into image cards with typography, background, layout and attribution controls.
- Multiple templates, colors, gradients and user-selected local images only where image rights permit.
- Preserve exact scripture wording, punctuation, verse range and edition attribution.
- Export/share through Android's system share sheet.
- Export personal notes, sermon outlines, journals and reading-plan progress to user-selected formats.
- User-controlled backup/restore of local records.
- Clearly separate scripture text from user-added commentary in exports.
- No social feed or online posting service in this release; sharing is delegated to apps already installed by the user.

### 3.7 Offline maps and atlas

- Offline map package with explicit geographic coverage and version.
- Map pins for verified biblical places and routes.
- Place detail pages with source citations and related scripture references.
- Download/import and storage management for map packages, where lawful.
- No online tiles, geocoding, routing APIs or network dependency in core atlas operation.
- Map dataset source, licence, attribution and update date displayed.

### 3.8 Help, settings and accessibility

- On-device help and feature guides.
- About, edition credits, licence notices and acknowledgements.
- Interface language selection independently of Bible edition language.
- Initial interface language targets: Maale, Amharic and English, with translation completion status shown honestly.
- Font scaling, contrast, screen-reader labels, touch targets and reduced-motion support.
- Theme, reading, notification, storage, backup, privacy and search settings.
- Storage usage, installed editions/resources, index rebuild and content integrity status.
- Optional local app lock/biometric protection only after threat-model and device compatibility review.
- No analytics, advertising, telemetry or cloud tracking in this release.

## 4. Explicitly deferred to the server-backed release

- Account registration, authentication and profile service.
- Cloud synchronization and cross-device restore.
- Online groups, shared discussion forums, comments and moderation.
- News subscriptions, publisher feeds and server-delivered announcements.
- Shared reading plans and collaborative notes.
- Push notifications from a server.
- Remote content administration and content distribution service.

The offline release may define stable interfaces and local data structures for future integration. It must not ship nonfunctional server-dependent screens as if they were active features. If a future feature is shown as planned, it must be labelled as such.

## 5. Content, licensing and editorial gates

- Keep `content_intake_authorized=false` until the Owner separately approves the exact content package and intake action.
- Do not acquire, unpack, inspect, hash, ingest or commit scripture text/audio/scans or other source-content assets under this scope authorization alone.
- The accepted 2008 MDYTWF / mdyeth A2 source baseline remains the target Maale edition; do not substitute 2007 MDYBSE.
- CC BY-SA 4.0 attribution and share-alike obligations must be implemented for the Maale edition as applicable.
- Amharic/English editions, commentaries, dictionaries, maps, images, audio, fonts and derived indexes each need their own documented rights decision.
- Specialist Maale-language/Ethiopic-script review remains an accepted A2 residual risk; no linguistic sign-off is implied by this scope.
- Every resource must carry provenance and rights metadata and be excluded from release if its rights status is unresolved.

## 6. Technical architecture direction

- Retain Kotlin, Jetpack Compose, Material 3 and the existing Android foundation.
- Introduce feature-oriented modules/packages with clear domain/data/presentation boundaries.
- Local persistence: Room/SQLite with schema migrations and repository interfaces.
- Scripture model: stable book/chapter/verse references, edition identifiers and source provenance.
- Search: local full-text index with language-specific tokenization and rebuildable index data.
- Personal data: normalized local entities for notes, journals, sermons, bookmarks, highlights, collections and plans.
- Resource registry: metadata and install state for editions, maps, dictionaries, commentaries and other datasets.
- Import pipeline: manifest validation, licence/attribution checks, integrity verification, atomic installation and rollback.
- Export pipeline: deterministic rendering, correct verse attribution, safe file handling and Android share integration.
- Offline guarantee: network access must not be required for core use; test with network disabled.
- Backup/restore: explicit user-controlled export, encryption assessment, versioning and recovery tests.
- Future server adapters must be optional and must not leak personal data without explicit user consent.

## 7. Non-functional requirements

- Reliable operation on low- and mid-range Android devices.
- Fast cold start, smooth chapter navigation and bounded memory use.
- Search responsiveness targets to be measured on a documented reference device and corpus.
- Robust behavior under low storage, process death, interrupted import/export and device restart.
- Database migrations tested from every supported prior schema.
- Accessibility testing and Maale/Amharic/English text rendering validation.
- Security review for local data, exported files, import packages and backup handling.
- No secrets or private user data committed to the public repository.

## 8. Delivery sequence

| Work package | Scope | Exit evidence |
|---|---|---|
| A4.1 | Requirements, edition/resource registry, architecture and UX specification | Reviewed specification and traceability matrix |
| A4.2 | Local database, migrations, repositories and offline state | Migration tests and persistence tests |
| A4.3 | Reader, navigation, edition selection and personal annotations | UI and domain tests; content-free fixtures |
| A4.4 | Search, concordance and cross-reference engine | Index correctness, language and performance tests |
| A4.5 | Study workspace, journals, sermon notes and reading plans | CRUD, recovery, export and reminder tests |
| A4.6 | Atlas, maps and reference-resource framework | Rights-cleared dataset checks and offline map tests |
| A4.7 | Verse-card rendering, export, sharing, help and settings | Pixel/content correctness and export tests |
| A4.8 | Integration, accessibility, security, offline and device QA | Complete acceptance report and reproducible APK |
| A5 | Release signing and deployment preparation | Separate Owner approval and release gates |
| A6 | Publication and rollout | Separate Owner approval |

Work packages are sequenced by dependency; each must be implemented and verified before being marked complete. No A4 completion claim is made by this scope document.

## 9. Acceptance criteria

The release is not considered full-featured or complete merely because screens exist. Each included capability must be functional, tested and usable offline. Acceptance requires:

- Core Bible reader works with an authorized, installed edition and airplane mode enabled.
- Local search returns correct references from the installed corpus.
- Notes, highlights, bookmarks, journals, sermons and reading plans persist across restart.
- Edition switching and parallel comparison preserve reading position and stable verse references.
- Atlas and reference resources work offline when installed.
- Verse-card exports preserve exact text and required attribution.
- Backup/restore round-trip preserves user records and handles incompatible versions safely.
- Help, settings and accessibility functions are usable.
- No unlicensed third-party content or unauthorized scripture data is bundled.
- No backend, login, analytics, ads or cloud sync is required for core functionality.
- CI runs unit tests, instrumentation/UI tests where supported, static checks and debug APK assembly against an exact pinned source SHA.
- Release readiness remains separate from A4 feature completion.

## 10. Scope decision and unresolved dependencies

This document records the Owner's direction to make this release a comprehensive offline Bible and study application, with server-side capabilities deferred to a later release. It does not authorize scripture/content intake, third-party asset acquisition, release signing, merge, deployment or publication.

The following dependencies must be resolved as work progresses: authorized scripture package intake; rights-cleared Amharic and English editions; rights-cleared commentaries/dictionaries/atlas datasets; Maale language/script specialist review; reference-device performance baselines; and final Owner acceptance of the A4 architecture and detailed UX.

