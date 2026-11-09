# A4 — Product Requirements and Acceptance Matrix

**Status:** P0/P1 specification baseline — prepared, pending Owner review  
**Release:** Offline-first Android application; server-side capabilities deferred  
**Branch:** `feat/a4-offline-study-suite`  
**Content gate:** `content_intake_authorized=false`  
**Traceability rule:** Every implemented feature must map to one or more requirement IDs, acceptance tests and evidence records.

## 1. Product outcome

Deliver a complete, dependable offline Bible reading and study workspace. Core reading, search, personal study, plans, installed reference resources, export and backup must not require an account or network connection. A screen without a working data path, persistence behavior, error states and tests is not an implemented feature.

## 2. Personas and primary jobs

- **Reader:** open an installed edition, navigate reliably, adjust reading presentation and resume where they stopped.
- **Bible student:** search passages, compare installed editions, follow cross-references and consult installed reference works.
- **Teacher/preacher:** collect passages, prepare sermon outlines and export personal study material.
- **Daily reader:** follow a reading plan, record progress and use optional local reminders.
- **Personal researcher:** maintain private notes, journals, topical collections and study notebooks.
- **Offline user:** install/retain lawful resources and complete core tasks with airplane mode enabled.
- **Multilingual user:** choose interface language independently from scripture edition and preserve Maale/Amharic/English text faithfully.

## 3. Global requirements

| ID | Requirement | Acceptance criteria |
|---|---|---|
| NFR-01 | Offline core | With network disabled after installation, reader, local search, notes, bookmarks, highlights, plans and installed references remain usable; no blocking network request. |
| NFR-02 | Local-first privacy | Personal records remain local; no analytics, ads, telemetry, account or cloud sync in this release. |
| NFR-03 | Provenance | Every installed edition/resource exposes source, version, language, licence, attribution and integrity status; unresolved rights prevent installation/release. |
| NFR-04 | Stable references | Notes and annotations attach to stable edition-independent book/chapter/verse references plus edition context; edition switching does not orphan them. |
| NFR-05 | Multilingual integrity | Interface locale and scripture edition are independent; Unicode text is preserved; no silent normalization/rewrite of displayed source text. |
| NFR-06 | Resilience | Interrupted operations, process death, restart and low-storage conditions do not silently corrupt or discard user data. |
| NFR-07 | Accessibility | Scalable text, screen-reader semantics, sufficient contrast, usable touch targets and reduced-motion support are verified. |
| NFR-08 | Honest availability | Missing editions, incomplete indexes, unsupported language functions and planned server features are clearly identified; no fabricated content. |
| NFR-09 | Testability | Domain logic, persistence, import/export and UI workflows have automated tests; content-dependent tests use approved resources only. |
| NFR-10 | Compatibility | Supported Android API range and representative low/mid-range devices are declared and tested before release. |

## 4. Feature requirements and acceptance criteria

### A. Edition library and resource catalogue

| ID | Requirement | Acceptance criteria |
|---|---|---|
| LIB-01 | Edition catalogue | List installed and available-to-install edition metadata; show language, canon, publisher, version and install state. |
| LIB-02 | Rights/provenance detail | Edition detail displays source, licence, attribution, version and verification state; unresolved rights are visibly blocked. |
| LIB-03 | Edition selection | User can select an installed edition; selection persists across restart and is independent from interface language. |
| LIB-04 | Resource catalogue | Dictionaries, commentaries, maps and other resources are separately listed with provenance and compatibility metadata. |
| LIB-05 | Resource lifecycle | Installation/removal is atomic; interrupted installation rolls back; no partial resource is marked installed. |
| LIB-06 | Storage management | Show per-resource and total storage; removal warns about dependent indexes while preserving user notes. |

### B. Reader and navigation

| ID | Requirement | Acceptance criteria |
|---|---|---|
| READ-01 | Book/chapter navigation | Navigate testament → book → chapter; invalid references are rejected and boundary navigation is predictable. |
| READ-02 | Verse navigation | Select a verse/range and copy an internal stable reference; verse text is not fabricated when content is absent. |
| READ-03 | Resume/history | Reopen to the last reading position; history is locally stored, ordered and clearable. |
| READ-04 | Reading presentation | Font size/family, line spacing, margins, verse numbers and layout update immediately and persist. |
| READ-05 | Themes/focus | System/light/dark/sepia and focus mode work without changing source text. |
| READ-06 | Screen behavior | Optional keep-screen-awake works only while reading and respects device/system settings. |
| READ-07 | Missing content | Missing edition/chapter produces an explicit unavailable state, not an empty screen that appears complete. |
| READ-08 | Parallel reader | Compare two or more installed editions at the same stable reference; unavailable edition is clearly indicated. |
| READ-09 | Cross-reference navigation | Open a reference, return to prior reading position and retain edition context. |

### C. Personal annotations and collections

| ID | Requirement | Acceptance criteria |
|---|---|---|
| ANNO-01 | Bookmarks | Add/remove bookmarks from reader; bookmark retains stable reference and optional label. |
| ANNO-02 | Highlights | Apply/remove highlight with selectable color/category; survives restart and edition switch. |
| ANNO-03 | Verse notes | Create/edit/delete note linked to one or more stable references; autosave/recovery behavior is tested. |
| ANNO-04 | Tags/collections | Organize bookmarks, notes and passages into user-defined collections and tags. |
| ANNO-05 | Annotation search | Search/filter personal notes and collections locally without searching uninstalled scripture. |
| ANNO-06 | Safe deletion | Destructive actions are confirmed; recoverable archive/trash behavior is defined and tested where implemented. |

### D. Offline search and study references

| ID | Requirement | Acceptance criteria |
|---|---|---|
| SEARCH-01 | Full-text search | Search installed, selected edition corpus offline and return exact stable references. |
| SEARCH-02 | Query modes | Exact phrase, all terms, any term and prefix modes return deterministic results. |
| SEARCH-03 | Filters | Filter by edition, testament, book and chapter range; filters combine predictably. |
| SEARCH-04 | Results | Show edition, book/chapter/verse and context snippet; selecting result opens exact location. |
| SEARCH-05 | Search history | Recent and saved searches persist locally and can be cleared. |
| SEARCH-06 | Language handling | Maale/Amharic tokenization and normalization are separately tested; original displayed text remains unchanged. |
| SEARCH-07 | Index lifecycle | Index is versioned, rebuildable and bound to exact edition/resource version; partial index state is disclosed. |
| REF-01 | Concordance | Word lookup returns validated occurrences and counts from an authorized indexed corpus. |
| REF-02 | Cross-reference library | Related references have source/provenance metadata and navigate to exact passages. |
| REF-03 | Topical index | Browse/search licensed or original topics; each topic-to-verse relationship is traceable to its source. |
| REF-04 | Dictionaries/encyclopedias | Display entries from installed rights-cleared resources with author/source/licence attribution. |
| REF-05 | Commentaries | Commentary is visually and semantically distinct from scripture; source and author are always visible. |
| REF-06 | People/places/timelines | Entries distinguish textual data from interpretation and cite their resource provenance. |
| REF-07 | Unsupported resource state | Missing/unlicensed resource is not represented as installed or populated with invented entries. |

### E. Personal study workspace

| ID | Requirement | Acceptance criteria |
|---|---|---|
| STUDY-01 | Daily reflection journal | Create dated private entries; edit, search and export locally. |
| STUDY-02 | Diary | Create general private diary entries with timestamps, tags and local search. |
| STUDY-03 | Sermon notes | Capture title, date, primary passage, outline, points and private tags; persist across restart. |
| STUDY-04 | Study notebooks | Create notebooks containing notes, questions, observations, conclusions and linked references. |
| STUDY-05 | Passage collections | Save and organize passages with custom titles and notes. |
| STUDY-06 | Multi-reference links | A note may link to multiple passages/topics without duplicating note content. |
| STUDY-07 | Editing recovery | Interrupted edit/process death does not silently lose the last saved version; recovery state is clear. |
| STUDY-08 | Local search/export | Search personal study content and export selected records without network access. |

### F. Reading plans and reminders

| ID | Requirement | Acceptance criteria |
|---|---|---|
| PLAN-01 | Plan catalogue | Show original or rights-cleared plans with duration, scope and provenance. |
| PLAN-02 | Custom plans | Create custom schedule by passages/books/topics and dates; invalid schedules are rejected. |
| PLAN-03 | Daily checklist | Mark scheduled readings complete/incomplete and persist progress. |
| PLAN-04 | Rest/catch-up | Configure rest days and reschedule missed readings without losing history. |
| PLAN-05 | Progress | Show completed/remaining readings, percentage and streak definition transparently. |
| PLAN-06 | Local reminders | Optional reminders use Android local scheduling; user can disable and edit them. |
| PLAN-07 | Calendar behavior | Dates/time zones and device clock changes do not duplicate or silently erase completion records. |

### G. Atlas and maps

| ID | Requirement | Acceptance criteria |
|---|---|---|
| MAP-01 | Offline map package | Map coverage/version/licence/attribution are shown; map opens with network disabled. |
| MAP-02 | Place directory | Search/browse verified place records and their source metadata. |
| MAP-03 | Map pins/routes | Display only verified coordinates and documented route geometry; unknown geography is not guessed. |
| MAP-04 | Scripture links | Place detail links to associated passages with source provenance. |
| MAP-05 | Storage/update | Import/remove map packages atomically and show storage footprint/version. |
| MAP-06 | No online dependency | No network tiles, geocoding or routing required for installed map operation. |

### H. Verse cards, export, backup and sharing

| ID | Requirement | Acceptance criteria |
|---|---|---|
| EXPORT-01 | Verse-card composer | Select passage, template, typography and background; output preserves exact text and verse range. |
| EXPORT-02 | Attribution | Required edition attribution appears in exported card/document according to licence and chosen layout. |
| EXPORT-03 | Share sheet | Share uses Android system chooser; app does not upload or publish independently. |
| EXPORT-04 | Personal document export | Export selected notes, sermons, journals and plans; scripture and user commentary remain distinguishable. |
| EXPORT-05 | Backup | User explicitly initiates local backup; content, version and integrity metadata are included. |
| EXPORT-06 | Restore | Restore validates version/integrity, previews consequences and does not overwrite current records silently. |
| EXPORT-07 | Failure recovery | Interrupted export/restore leaves source data intact and reports actionable failure. |

### I. Settings, help, privacy and accessibility

| ID | Requirement | Acceptance criteria |
|---|---|---|
| SET-01 | Interface language | Maale/Amharic/English options reflect actual translation completion; incomplete translations are not misrepresented. |
| SET-02 | Scripture language | Edition language can be changed independently of interface language. |
| SET-03 | Preferences | Theme, reading, search, notifications and storage settings persist locally. |
| SET-04 | Privacy controls | Explain local data location, export/delete behavior and absence of cloud sync/analytics/ads. |
| SET-05 | Help/about | On-device guides, version, credits, licences and acknowledgements are available offline. |
| A11Y-01 | Screen reader | Core navigation, reader, search and forms expose meaningful labels and reading order. |
| A11Y-02 | Text scaling | Layout remains usable at supported large font/display scales. |
| A11Y-03 | Contrast/touch | Text contrast and touch targets meet the adopted accessibility standard; failures are recorded. |
| A11Y-04 | Motion | Reduced-motion preference is respected; animation is nonessential. |

### J. Explicitly deferred capabilities

| ID | Capability | Acceptance boundary |
|---|---|---|
| DEFER-01 | Account/authentication/profile | Not implemented or presented as operational in this release. |
| DEFER-02 | Cloud sync/cross-device restore | Local export/restore only; no background synchronization. |
| DEFER-03 | Online groups/forums/comments/moderation | No network community screens or service. |
| DEFER-04 | News subscriptions/remote announcements | No server feed or remote content delivery. |
| DEFER-05 | Collaborative plans/notes | Plans and notes are private local records. |
| DEFER-06 | Server push/remote administration | Local reminders only; no server control plane. |

## 5. User journeys and end-to-end acceptance

1. **Open and read offline:** launch → choose installed edition → choose book/chapter → read → change typography → leave app → reopen → same position/settings restored.
2. **Annotate a passage:** select verse → bookmark/highlight → add note → close/restart → annotation visible at same stable reference.
3. **Search offline:** enter phrase → apply edition/book filter → open result → return to results → saved/recent query remains available.
4. **Parallel comparison:** select two installed editions → align at stable reference → scroll/navigate → switch primary edition without losing reference context.
5. **Prepare sermon:** create sermon → attach passage(s) → add outline/points → save → search → export to local destination.
6. **Follow reading plan:** select/create plan → mark daily reading → configure rest day/reminder → restart → progress and schedule retained.
7. **Study a place:** open offline atlas → search verified place → view map/profile/source → follow linked passage without network.
8. **Create/share verse card:** select authorized passage → compose card → verify exact wording/attribution → share via system chooser.
9. **Backup and restore:** create backup → validate metadata → restore into clean test profile → compare record counts and references.
10. **Language independence:** change interface language while keeping scripture edition; switch edition while keeping interface locale and stable notes.

## 6. Content and rights dependencies

- The 2008 Maale Full Bible MDYTWF / mdyeth remains the intended primary edition. No substitution with 2007 MDYBSE.
- Content intake remains separately unauthorized. Do not download, unpack, inspect, hash, ingest, transform, store or commit scripture/source assets.
- Amharic and English editions require exact-edition/source rights decisions.
- Commentaries, dictionaries, concordances, atlas/maps, images, audio, fonts and derived indexes require individual provenance/licence/attribution/integrity review.
- Until explicit content-intake authorization, use metadata-only catalogues and synthetic fixtures only.
- Specialist Maale language/script review remains a deferred accepted residual risk; no linguistic sign-off is claimed.

## 7. Prioritization and release gates

- **P0/P1 required:** requirements, traceability, screen inventory, journeys, risk/dependency register and issue breakdown.
- **P2 required before schema implementation:** architecture decisions, entity relationships, migration plan, import/export threat model and test contracts.
- **P3–P7:** implementation in dependency order; each requirement must be linked to tests.
- **P8:** integrated offline, accessibility, security, privacy, performance, backup/restore and device verification.
- **P9/P10:** separate release candidate, signing and deployment approval gates.

## 8. Definition of done

A requirement is done only when: UI and domain behavior exist; persistence and failure states are implemented; offline behavior is demonstrated; automated tests pass; accessibility/localization implications are checked; content rights are cleared where relevant; exact source SHA and evidence are recorded; and no deferred server capability is represented as working.

## 9. Open decisions for P2 (not silently assumed)

- Minimum supported Android API and formal device matrix.
- Whether local database encryption/app lock is required after threat-model review.
- Backup format, encryption and cross-version restore policy.
- Supported export formats and file-provider strategy.
- Search engine/library selection and measurable performance corpus.
- Offline map format and geographic coverage.
- Final interface-language coverage and translation review ownership.
- Exact content packages and licences for each non-Maale edition/reference resource.
