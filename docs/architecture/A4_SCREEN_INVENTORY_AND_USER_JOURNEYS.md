# A4 — Screen Inventory and User Journeys

**Status:** P1 UX inventory — specification, not implementation evidence  
**Navigation target:** Kotlin + Jetpack Compose; phone-first, responsive to larger screens  
**Offline boundary:** Every core screen operates on local data; server-dependent features are excluded.

## 1. Navigation model

Primary bottom navigation:
1. **Home** — continue reading, daily plan, recent notes and quick actions.
2. **Bible** — edition library, books, reader and parallel reading.
3. **Study** — search, concordance, cross-references, topics and reference library.
4. **Library** — installed editions, dictionaries, commentaries, atlas/map packages and resource management.
5. **My Space** — notes, journal, diary, sermons, notebooks, collections and plans.

Global actions: search, current reading reference, create note, settings/help. Use a navigation drawer or secondary menu for less-frequent tools. Preserve navigation state and deep-link targets across configuration changes and process recreation.

## 2. Screen inventory

| ID | Screen / route | Primary content and actions | Key states and acceptance focus |
|---|---|---|---|
| SCR-001 | Home | Continue reading; daily reading; recent notes; bookmarks; quick actions | First-run, no edition installed, no plan, populated home; offline |
| SCR-002 | Edition Library | Installed editions, metadata, language and status | Empty, installed, blocked by rights, update available |
| SCR-003 | Edition Details | Publisher/source/licence/attribution/version/integrity | Verified, unresolved, incompatible; install unavailable until authorized |
| SCR-004 | Book Browser | Testament and canonical book list | Search/filter, canon metadata, no content claim |
| SCR-005 | Chapter Selector | Chapter grid/list and recent position | Boundary/invalid selection handling |
| SCR-006 | Bible Reader | Chapter text, verse numbers, selection and reader controls | Loading, available, missing edition/content, error; synthetic fixture in development |
| SCR-007 | Reader Settings | Typography, line spacing, margins, theme, focus, keep-awake | Persisted preferences and large text |
| SCR-008 | Verse Action Sheet | Bookmark, highlight, note, copy/share reference | Rights-aware actions; no scripture text copy when not permitted |
| SCR-009 | Bookmarks | Saved references, labels, filters and collections | Empty/populated/search |
| SCR-010 | Highlights | Highlighted references, color/category filters | Empty/populated/edit/delete |
| SCR-011 | Notes | Verse-linked and general notes | Draft/saved/recovery/search |
| SCR-012 | Parallel Reader | Two/three edition panes aligned by stable reference | Missing edition and alignment edge cases |
| SCR-013 | Search Home | Search query, mode, edition and range filters | Empty query, recent/saved searches |
| SCR-014 | Search Results | Result list, snippet, reference, selected edition | No results, partial index, unsupported language |
| SCR-015 | Concordance | Word lookup, occurrence list and counts | Resource/index unavailable; exact count validation |
| SCR-016 | Cross-References | Related passage list and source context | No references, source attribution, navigation return |
| SCR-017 | Topics | Topic catalogue, topic detail and linked passages | Empty, filtered, rights-cleared content only |
| SCR-018 | Reference Work Detail | Dictionary/encyclopedia/commentary entry | Distinguish scripture/commentary; provenance always visible |
| SCR-019 | People & Places | Searchable person/place/region directory | Verified entries only; textual vs interpretive labels |
| SCR-020 | Atlas Map | Offline map, verified pins and routes | Package missing, offline available, coverage boundary |
| SCR-021 | Place Detail | Description, coordinates/source, linked passages | Source citation and unknown-data state |
| SCR-022 | Daily Journal | Dated reflection entries | Empty, draft, saved, search |
| SCR-023 | Diary | General private entries | Empty/populated, edit/archive/delete |
| SCR-024 | Sermon Library | Sermon list, date/title/passage | Empty, search/filter |
| SCR-025 | Sermon Editor | Title, passage, outline, points, tags | Autosave/recovery and export |
| SCR-026 | Study Notebooks | Notebook list and organization | Empty/populated |
| SCR-027 | Notebook Editor | Questions, observations, conclusions, references | Multi-reference linking and persistence |
| SCR-028 | Passage Collections | Saved passages and personal lists | Empty/populated/reorder |
| SCR-029 | Reading Plan Library | Built-in approved and custom plans | Rights/provenance; empty catalogue |
| SCR-030 | Plan Detail & Schedule | Duration, daily assignments, rest/catch-up | Invalid schedule and calendar edge cases |
| SCR-031 | Plan Progress | Completion, history, streak definition | Restart persistence and date changes |
| SCR-032 | Reminder Settings | Optional local reminders | Permission denied, disabled, scheduled |
| SCR-033 | Verse Card Composer | Passage, template, typography and background | Preview, exact wording, attribution |
| SCR-034 | Export Centre | Export notes, sermons, journal, plans and cards | Destination unavailable, permission/error |
| SCR-035 | Backup & Restore | Create/inspect/restore local backup | Invalid, incompatible, interrupted and successful round-trip |
| SCR-036 | Resource Manager | Installed editions/resources, storage and integrity | Partial install rollback, remove dependencies |
| SCR-037 | App Settings | Interface language, theme, privacy, search, storage | Persistence; interface language independent |
| SCR-038 | Language Settings | Maale/Amharic/English UI and scripture selection | Translation completion shown honestly |
| SCR-039 | Accessibility Settings | Text scaling, contrast, reduced motion | System setting interaction |
| SCR-040 | Help & About | Offline help, version, credits, licences | Works without network |
| SCR-041 | Privacy & Data | Local data explanation, export/delete controls | Transparent data lifecycle |
| SCR-042 | Content Availability | Missing edition/resource and lawful next steps | No fabricated content; no false download/install claim |

## 3. Navigation and interaction principles

- The reader must be reachable in no more than three deliberate navigation steps from Home when an edition is installed.
- Search is globally reachable and retains query/filter state when opening a result and returning.
- A selected verse exposes contextual actions without obscuring the passage or trapping accessibility focus.
- Every detail screen exposes a predictable back path to its originating list/context.
- Reader, notes and plans retain state across rotation, process death and app restart.
- Empty states explain what is missing and why; do not show dead-end buttons.
- Resource installation UI is hidden/disabled while content intake remains unauthorized; metadata can be shown without implying source content is present.
- Server-only capabilities are not placed in active navigation for this release.

## 4. End-to-end journeys

### J-01 First launch and choose an edition
Home → Edition Library → Edition Details → choose installed edition → Book Browser → Chapter Selector → Reader. If no content is installed, show the content-availability explanation and metadata-only catalogue; never fabricate verses.

### J-02 Read and resume
Home → Continue Reading → Reader → adjust typography/theme → navigate chapter → leave app → relaunch → Home/Reader resumes exact reference and preferences.

### J-03 Annotate and organize
Reader → select verse → Verse Action Sheet → bookmark/highlight/add note → assign tag/collection → My Space → Notes/Bookmarks → reopen exact reference.

### J-04 Search and cross-reference
Study → Search Home → enter query and filters → Search Results → open passage → Cross-References → open related passage → back to preserved results.

### J-05 Compare editions
Bible → Parallel Reader → select installed editions → align stable reference → navigate → remove one edition from comparison without losing primary reading context.

### J-06 Prepare sermon
My Space → Sermon Library → Sermon Editor → add title/passage/outline/points → link notes → save → Export Centre.

### J-07 Journal and notebook
My Space → Daily Journal or Study Notebooks → create entry → attach references/tags → save → search/filter → export selected records.

### J-08 Reading plan
Home or My Space → Reading Plan Library → Plan Detail → start/customize → daily assignment → mark complete → configure optional reminder → Plan Progress.

### J-09 Offline atlas
Library → Atlas → Atlas Map → search verified place → Place Detail → related passage → Reader; repeat with airplane mode enabled.

### J-10 Verse card
Reader → select passage → Verse Card Composer → choose template → verify exact verse range and attribution → preview → system share sheet.

### J-11 Backup/restore
Settings → Backup & Restore → create backup → inspect metadata → restore to clean test profile → compare counts/references → report mismatch without destructive overwrite.

### J-12 Language independence
Settings → Language Settings → change UI locale → return to reader → choose another installed scripture edition → verify UI locale and annotations remain stable.

## 5. Responsive and accessibility behavior

- Phone: bottom navigation, single-pane reader and modal/bottom-sheet contextual actions.
- Tablet/large screen: optional navigation rail, two-pane library/detail and parallel reading; do not require a tablet to access any capability.
- Screen reader: meaningful labels for book/chapter/verse, selected state, result count, progress and action controls.
- Dynamic text: avoid clipped controls and fixed-height text containers; test long Maale/Amharic strings.
- Touch: controls have adequate target size and spacing.
- RTL is not assumed; preserve correct script direction per language metadata and test shaping/line breaking.
- Contrast and reduced-motion preferences apply consistently.

## 6. Implementation notes

- Route names and screen IDs are stable identifiers for issue/test traceability, not a commitment to separate Android activities.
- Compose components may be reused across screens; screens must still have distinct state, semantics and tests.
- P2 will finalize route graph, design tokens, state ownership and screen-to-domain contract.
- P0/P1 documentation does not assert any of these screens are implemented.
