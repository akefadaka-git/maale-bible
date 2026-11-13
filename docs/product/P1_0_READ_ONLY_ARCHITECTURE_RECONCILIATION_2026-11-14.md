# P1.0 Read-Only Architecture Reconciliation — Critical Bible Experience

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Assessment date:** 2026-11-14  
**Status:** READ-ONLY ASSESSMENT — IMPLEMENTATION NOT AUTHORIZED BY THIS DOCUMENT  
**Requirements baseline:** P0 v1.0, branch `requirements/p0-product-baseline-2026-11-14`, head `61902345e7ff2d6cd831f433bee33f5775412864`  
**P1 plan:** `docs/product/P1_CRITICAL_BIBLE_EXPERIENCE_EXECUTION_PLAN_v1.0_2026-11-14.md`  
**Inspected application source:** PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**PR #59:** OPEN / DRAFT / UNMERGED; base `feat/a3-final-hardening-2026-11-13`; head `integration/unified-ui-a3-2026-11-13`  
**Repository default branch:** `main`  
**Assessment branch:** `planning/p1-critical-bible-experience-2026-11-14`

## 1. Executive finding

The inspected application source is still a **metadata catalogue + study-data foundation**, not a functioning scripture reader. The canonical 66-book inventory exists, but it is English-only, lacks testament metadata and is rendered as one flat list. The reader screen persists reading coordinates and personal study records but does not load or render scripture. No scripture ingestion/parser/package/persistence implementation was found in the inspected source tree.

The current locale resources also do not satisfy P0's no-hidden-English-fallback rule: English and Amharic each contain 125 matching keys, while the Maale resource has a different set of 144 keys and is missing 63 keys present in the default resource. The app contains additional hard-coded English UI strings. This is a concrete source-level explanation for the English text still visible in the installed app.

The P1 plan is directionally implementable, but the first engineering work must establish the scripture content contracts and persistence boundaries before reader UI is wired to data. No application code was changed during this assessment.

## 2. Exact inspection scope and evidence

Repository tree was inspected recursively at PR #59 head `58eb32acc10415e3dfa6683a5342b6da824d5d27` (100 tracked files returned). PR metadata was independently fetched and confirms OPEN/DRAFT, not merged.

Key source files and inspected blob SHAs:

| Path | Blob SHA | Finding |
|---|---|---|
| `app/src/main/java/org/maale/bible/data/catalog/CanonicalBookCatalog.kt` | `647d81e40e2769a6e0e63f1176f33af8124f121d` | 66 canonical IDs/order; only English display names; metadata-only |
| `app/src/main/java/org/maale/bible/domain/model/Book.kt` | `163e4f1e30ba9b86e7ac951897ded6ab0c8f53a9` | Model has only order, USFM ID and English name; no testament or localized names |
| `app/src/main/java/org/maale/bible/data/repository/StaticBookRepository.kt` | `7334464de09231a078c07dbc30befe45fbc8b5ed` | Static in-memory catalogue repository |
| `app/src/main/java/org/maale/bible/ui/BooksScreen.kt` | `3c5a10a79fcbd89e38d50dcedb0b5e470281d143` | Flat list; displays English names; no OT/NT sections |
| `app/src/main/java/org/maale/bible/ui/ReaderPlaceholderScreen.kt` | `5beb403a3775dd381b27d1506440dcea2cd6e6c8` | Coordinates, bookmarks, annotations and plans; no scripture repository or verse text rendering |
| `app/src/main/java/org/maale/bible/domain/repository/BookRepository.kt` | `c125bd14174a8706e9571a806ea1ab3f47f631ae` | Catalogue-only repository contract |
| `app/src/main/java/org/maale/bible/data/repository/StudyDatabase.kt` | `3fe016e945de2f3571e6f93ae61cfc7187c29370` | SQLCipher-encrypted user study metadata; source comment explicitly says no scripture content is stored |
| `app/src/main/java/org/maale/bible/domain/model/ReadingPlanSchedule.kt` | `07e552dffcbbc1537cbab1985d9d0cc6d929fd5a` | Uses Gregorian `LocalDate` / epoch-day arithmetic |
| `app/src/main/java/org/maale/bible/ui/AppLanguagePreference.kt` | `a98a5c1a1b3e26aa99fe1f0c2a6d4e92585bf9ae` | UI locale preference supports mdy/am/en and defaults to Maale |
| `app/src/main/java/org/maale/bible/ui/SettingsScreen.kt` | `3fb766c5ff6c1ab5b48fde3650c335f78415607c` | Backup/restore is implemented; Settings labels/status include hard-coded English; no calendar selector visible in inspected screen |
| `app/src/main/res/values/strings.xml` | `19c9d793a84a0c1a2a7bc0694e8fa934d503bf54` | 125 default locale keys |
| `app/src/main/res/values-am/strings.xml` | `6391452ffc2a24043d5d888b31ccb69ecd7f0065` | 125 keys; same key set as default |
| `app/src/main/res/values-b+mdy/strings.xml` | `e8f07a839ecc7e13ef3ac83ee4d3e792c5872017` | 144 keys; key set differs from default; 63 default keys missing |
| `app/build.gradle.kts` | `54c7545eb8cf5d1a44b880705c885382fb5d74ed` | Compose, SQLCipher and SQLite dependencies; no scripture parser/database/package dependency evident |

Supporting source inventory files include `content/manifests/candidate-book-inventory.json`, `content/manifests/maale-source-baseline-manifest.json`, `content/manifests/maale-usfm-file-hashes.json` and schema/report definitions. These are metadata/manifests, not a production scripture ingestion pipeline or scripture corpus.

## 3. Requirement-to-source reconciliation

| P1 area | Current state | Status | Required work |
|---|---|---|---|
| Exact-source ingestion | Metadata/hash manifests exist; no source staging runner, archive validator, USFM parser or normalized output builder found | MISSING | Build restricted, deterministic ingestion pipeline; keep source/payload outside public Git |
| Maale source readiness | A2 accepted 2008 MDYTWF source baseline with specialist-review exception | PARTIAL | Re-verify exact archive/file hashes at intake; retain rights caveats; obtain specialist review before linguistic acceptance |
| Amharic/English editions | Candidate records only; Amharic candidate NT-only and English candidate verification pending | BLOCKED / PARTIAL | No full-edition availability claim until completeness and rights gates pass |
| Scripture domain model | No edition/chapter/verse/USFM-node/package model found | MISSING | Define edition-scoped immutable scripture model and provenance contract |
| Local scripture persistence | StudyDatabase stores user study metadata only | MISSING | Separate scripture content store/package lifecycle from encrypted personal study store |
| Package integrity | No scripture package manifest/install/atomic activation implementation found | MISSING | Versioned manifest, digest verification, staging, atomic activation, rollback and fault injection |
| Canonical catalogue | 66 IDs and canonical order present | PASS (narrow) | Extend metadata with testament and localized reviewed names |
| Testament grouping | No testament field; BooksScreen renders one flat list | MISSING | Explicit OT/NT grouping with localized headings/counts |
| Localized catalogue | English names only in Book model/catalogue | MISSING | Locale-keyed book-name inventory with reviewer evidence |
| Real reader | Placeholder coordinates and study controls; no scripture text provider | MISSING | Reader backed by verified installed edition, structural nodes and exact verse text |
| UI localization | EN/AM keys match; Maale has 144 keys but misses 63 default keys; hard-coded English remains | PARTIAL / FAIL | Resource-key parity, hard-coded-string audit, reviewed translations and runtime locale tests |
| Calendar preference | No E.C./G.C. preference/service found; LocalDate scheduling is G.C.-based | MISSING | Shared calendar abstraction; E.C. default; G.C. selectable; persisted independent of language |
| Offline reading | Study data is local/encrypted; scripture package absent | MISSING | Offline scripture package and airplane-mode acceptance |
| Accessibility | Some Compose semantics and UI tests exist; reader's real content semantics not present | PARTIAL | Real reader TalkBack order, controls, font scaling and Ethiopic rendering tests |

## 4. Localization defect analysis

A resource-key comparison at the exact inspected PR head found:

- Default `values/strings.xml`: **125** keys.
- Amharic `values-am/strings.xml`: **125** keys; no missing or extra keys relative to default.
- Maale `values-b+mdy/strings.xml`: **144** keys; **63 default keys missing** and 82 Maale-specific extra keys (including alternate/newer naming conventions).

Android resource resolution will use the default resource for missing Maale keys. Because the default file contains English UI strings, missing Maale translations can appear as English. In addition, the inspected `BooksScreen.kt`, `ReaderPlaceholderScreen.kt`, `SettingsScreen.kt` and `MaaleBibleApp.kt` contain direct English literals or English concatenation patterns. A key-count-only check is insufficient: the P1 localization gate must compare key sets, audit user-visible literals, and review translation meaning and runtime rendering.

The Amharic key parity result does not establish linguistic quality or full flow correctness. Maale's extra keys also do not prove the app uses those strings; several appear to belong to alternate resource naming conventions. Map every used `R.string` reference to a reviewed translation before claiming completion.

## 5. Architecture decisions required before implementation

### A. Separate scripture data from personal study data

Keep the current SQLCipher study store and its backup/recovery behavior isolated. Create a separate edition-scoped scripture repository/store with immutable content package versions. Do not mix raw scripture rows with notes/bookmarks or include scripture payload automatically in the existing personal-study backup.

### B. Edition-aware references

Current `ReadingPosition`, bookmark and annotation references are based on book/chapter/verse without an edition identity. P1 must define migration behavior before multiple editions are installed. The durable passage key must include edition identity plus canonical book/chapter/verse coordinates; legacy study records require a deterministic default-edition migration or an explicit unresolved mapping state.

### C. Catalogue metadata

Retain existing 66-book inventory and order. Add testament classification and locale-keyed names from a controlled reviewable metadata file. Do not hard-code Maale/Amharic book names based on unreviewed guesses. Keep the selected scripture edition independent from UI language.

### D. Calendar model

Keep stored timestamps/instants and epoch-day event identity canonical and locale/time-zone aware. Introduce a calendar preference service with E.C. as initial default and G.C. selectable. Do not reinterpret the existing Gregorian epoch-day values as Ethiopian dates. Define whether reading-plan start dates remain canonical Gregorian epoch days with E.C. presentation or use a separately defined civil-date identity; this is an architecture decision to settle in P1.0 before calendar implementation.

### E. Ingestion and rights enforcement

Build the parser and validator in a restricted workspace or controlled CI environment. Public CI logs and artifacts must not contain scripture text. Package output must carry source identity, source digest, parser/schema version, licence and attribution notices, and integrity metadata. The public repository may carry parser code, schemas, non-content metadata and synthetic fixtures only.

## 6. Recommended implementation sequence

1. **P1.0a — freeze contracts:** decide scripture store boundary, edition-aware passage identity, source manifest schema, parser output model, package manifest, package trust model and calendar canonical-date semantics.
2. **P1.1 — source validator:** exact archive/file hash checks, safe archive handling, USFM inventory/encoding validation and restricted staging.
3. **P1.2 — parser/domain:** versioned parser and structured USFM representation; golden tests for all constructs present in the approved source.
4. **P1.3 — package builder:** deterministic package, manifest, attribution, payload digest and offline verification.
5. **P1.4 — scripture store:** atomic activation, rollback, migration and concurrent-reader behavior.
6. **P1.5 — catalogue:** testament and locale metadata; OT/NT grouped UI.
7. **P1.6 — reader:** exact content rendering and navigation against the installed package.
8. **P1.7 — localization:** remove hard-coded literals in touched flows; resource parity and qualified linguistic review.
9. **P1.8 — calendar:** shared E.C./G.C. service and persisted selector.
10. **P1.9 — integration acceptance:** exact-head CI, airplane mode, device typography/TalkBack, corruption recovery and source attribution.

Do not start P1.6 reader UI with fake scripture content. It may be developed against repository interfaces and localized unavailable states until a verified package exists.

## 7. P1.0 decision/blocker register

| ID | Decision/blocker | Required resolution |
|---|---|---|
| P1-ARCH-01 | Scripture DB/package separate from SQLCipher study DB | Freeze interface, storage location, encryption/integrity and lifecycle boundary |
| P1-ARCH-02 | Edition identity absent from current personal study references | Specify additive migration and default edition mapping; preserve notes/bookmarks |
| P1-ARCH-03 | USFM structural representation | Approve supported marker inventory and unknown-marker fail-closed behavior |
| P1-ARCH-04 | Package authenticity/trust | Specify key custody, signature verification, rotation/revocation and offline trust-root update |
| P1-ARCH-05 | Calendar canonical identity | Decide stored date/instant semantics before E.C. implementation |
| P1-LOC-01 | Maale resource-key mismatch and hard-coded English | Map all runtime strings to reviewed locale resources; preserve Owner-provided Maale wording |
| P1-LOC-02 | Complete English tagline | Owner confirmation required |
| P1-SRC-01 | Maale specialist review deferred | Arrange qualified reviewer; do not claim linguistic acceptance |
| P1-SRC-02 | Amharic full Bible and rights | Resolve complete source and written permission |
| P1-SRC-03 | English edition exact revision/completeness | Verify exact WEB release and conditions |

## 8. Read-only conclusion

- **P1.0 source inspection:** VERIFIED for the exact inspected PR #59 head and paths listed above.
- **P1 critical scripture ingestion:** MISSING.
- **Real scripture reader:** MISSING.
- **Localized 66-book catalogue/testament grouping:** PARTIAL.
- **Maale localization completeness:** FAIL at resource-key parity and hard-coded-string inspection.
- **Amharic localization resource-key parity:** PASS (125/125 keys); linguistic/runtime acceptance NOT RUN.
- **E.C./G.C. implementation:** MISSING.
- **Offline scripture package/integrity:** MISSING.
- **User study-data encryption/backup foundation:** existing implementation present; not equivalent to scripture storage or package integrity.
- **Application code changes in this assessment:** NONE.
- **PR #59:** unchanged, OPEN/DRAFT/UNMERGED.
- **Implementation authorization:** NOT GRANTED by this assessment.

## 9. Immediate next action

Owner review of the P1.0 architecture contracts in section 5 and blocker register in section 7. After those contracts are accepted, prepare a P1.1 implementation task specification with exact files, interfaces, tests and restricted-workspace requirements. No application implementation begins until separately authorized.
