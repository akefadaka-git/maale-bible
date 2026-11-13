# P1 Critical Bible Experience — Execution Plan v1.0

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Owner:** Akefa Ayele Daka  
**Plan date:** 2026-11-14  
**Status:** P1 PLANNING BASELINE — IMPLEMENTATION NOT STARTED  
**Requirements authority:** `docs/product/PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md`  
**Content authority:** `docs/licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md`  
**Planning branch:** `planning/p1-critical-bible-experience-2026-11-14`  
**Parent P0 head:** `61902345e7ff2d6cd831f433bee33f5775412864`

## 1. Purpose and scope

This plan decomposes P1 into an implementation-ready sequence for the critical Bible experience:

1. source-controlled scripture ingestion and source traceability;
2. a real reader rendering exact verified scripture;
3. localized 66-book catalogue and explicit Old Testament/New Testament grouping;
4. complete Maale, Amharic and English UI localization for the P1 flows;
5. E.C.-default/G.C.-selectable calendar behavior;
6. reliable offline storage, package integrity, atomic install/update and recovery.

P1 prioritizes the first end-to-end scripture journey over secondary product features. It does not authorize work on unrelated feature families such as commentary, maps, audio, cloud accounts, IROKA integration, rewards or publication.

## 2. Governance and non-negotiable constraints

- P0 v1.0 remains frozen. Requirement changes require an Owner-approved change record and baseline revision.
- This is a planning artifact. No application source, migrations, resource files, scripture corpus, package, signing configuration or release artifact is changed by this plan.
- No merge, signing, deployment, store submission or publication is authorized.
- Keep PR #59 and its branch untouched; this planning branch is independent.
- Never commit raw USFM/USX, verse text, source archives, scripture excerpts, generated scripture databases, indexes containing verse text, or protected assets to the public repository unless the exact source terms permit that distribution and the Owner separately approves the publication operation.
- Use restricted, access-controlled source staging for source files and derived scripture payloads. Repository commits may contain parsers, schemas, metadata-only manifests, test fixtures that do not reproduce protected text, and validation reports that disclose no protected content.
- Do not silently substitute editions, translations, book names or missing passages. A missing/blocked edition must have a localized unavailable state.
- Machine validation is not Maale linguistic approval. The accepted Maale specialist-review exception remains visible until an appropriately qualified reviewer signs off.
- Preserve the exact source revision, edition identity, file hashes, parser version, transformation version and package version for every rendered passage.

## 3. Source and rights readiness

### 3.1 Maale — first implementation target

Selected source: 2008 Maale Full Bible, `MDYTWF / mdyeth`, USFM archive `mdyeth_usfm.zip`, recorded SHA-256:

`4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`

Recorded licence: CC BY-SA 4.0. The A2 metadata baseline reports 66/66 USFM files, 1,189 chapters, 30,506 verse-marker lines and strict UTF-8/Unicode checks passing. The source remains **A2 BASELINE ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION**.

Implementation gates before package distribution:
- re-acquire or materialize the exact approved source in a restricted workspace and verify the archive hash before parsing;
- verify every file against the accepted per-file hash manifest and expected 66-book inventory;
- preserve the recorded source revision and attribution;
- implement CC BY-SA 4.0 attribution, modification notices and ShareAlike obligations for any adapted/derived material as applicable;
- record the GPG signature trust limitation (cryptographically valid signature; signer-key trust undefined);
- retain the documentary caveat that original rights-holder email headers/sender/signatory authority have not been independently authenticated in the repository;
- arrange qualified Maale language review of spelling, orthography, book names, verse rendering and punctuation. Until completed, mark linguistic acceptance **DEFERRED**, not PASS.

Do not substitute the separate 2007 `MDYBSE` Maale New Testament.

### 3.2 Amharic and English

- Amharic `amh` candidate currently documented is New Testament only. It cannot satisfy the complete-Bible requirement. A complete edition and exact written rights/permission for the intended use must be verified before it is packaged as a full Bible.
- English WEB remains a candidate; exact edition/revision, completeness, public-domain status and naming/trademark conditions must be verified before package approval.
- A language may be enabled as a complete Bible edition only when its edition manifest passes completeness, rights, integrity and quality gates. UI localization support is distinct from availability of a complete scripture edition.

### 3.3 Source status state machine

Use the authority vocabulary already defined in the content-intake reconciliation. For P1 engineering, expose a source/edition readiness state that cannot be confused with user availability:

`DISCOVERED → UNDER_REVIEW → RIGHTS_RECONCILED → TECHNICALLY_VALIDATED → APPROVED_FOR_INTEGRATION → APPROVED_FOR_DISTRIBUTION`

Blocking states include `RIGHTS_BLOCKED`, `INCOMPLETE`, `INTEGRITY_FAILED`, `QUALITY_REVIEW_REQUIRED` and `WITHDRAWN`. No transition may be inferred from project-level intake authorization alone.

## 4. Target architecture and data contracts

### 4.1 Ingestion pipeline

Implement a deterministic, offline-capable pipeline with separable stages:

1. **Acquire:** source archive enters restricted staging; record acquisition date/channel and source identity.
2. **Verify archive:** SHA-256, archive entry safety, ZIP CRC, path traversal rejection, duplicate path rejection, expected file inventory.
3. **Verify source files:** per-file hashes, encoding, Unicode validity, expected USFM IDs, no missing/extra/duplicate books.
4. **Parse:** versioned USFM parser; parse structure and markers rather than stripping markup with regular expressions.
5. **Normalize structurally only:** preserve source text exactly; retain verse bridges/ranges, paragraph/poetry markers, headings, footnotes and cross-reference markers as structured nodes. No spelling correction, paraphrase, transliteration or silent Unicode normalization of scripture text.
6. **Validate:** book/chapter/verse counts, duplicate or missing references, malformed markers, ranges, empty content, unexpected controls, source-to-output reconciliation.
7. **Build package:** deterministic manifest plus database/content payload, attribution/licence notices, schema version, parser/build version, edition identity, content hash and package signature/checksum metadata.
8. **Stage and verify:** write to a temporary versioned location; reopen and validate the produced package before activation.
9. **Activate atomically:** transaction/atomic pointer switch only after all validation passes; preserve last-known-good package for rollback.
10. **Audit:** emit redacted machine-readable validation evidence without copying protected scripture text into public logs.

Pipeline requirements:
- repeatable builds from the same inputs and pinned tool versions must produce identical logical content and deterministic package metadata;
- fail closed on unknown USFM constructs until explicitly supported and tested;
- preserve source coordinates and map every output verse/node to source file and source reference;
- maintain parser compatibility tests against representative structures (poetry, paragraph breaks, headings, verse bridges, footnotes, cross-references, chapter boundaries and Ethiopic Unicode);
- separate immutable source data from user annotations and app-owned metadata.

### 4.2 Canonical domain model

Minimum logical entities:

- `BibleEdition`: stable edition ID, language, title, publisher/rights holder, source ID/revision, licence, attribution, completeness status, package version, schema version and integrity digest.
- `BibleBook`: stable USFM/book ID, canonical order, testament, localized display names by locale, chapter count and edition availability.
- `BibleChapter`: edition ID, book ID and chapter number.
- `BibleVerse`: edition ID, book ID, chapter, verse/range identity, exact source text and source locator.
- `BibleNode`: ordered structural node for paragraphs, poetry, headings, footnotes, cross-references and other supported USFM constructs.
- `ContentPackageManifest`: package ID/version, edition ID, source digest, file inventory, schema/parser versions, attribution/licence references, build metadata and payload digest.
- `ReadingPosition`: edition ID, book/chapter/verse anchor and last-read timestamp.
- User-owned notes/bookmarks/highlights must reference stable edition and passage identities, not row IDs that can change during package replacement.

Use stable canonical identifiers and explicit edition scoping. Never join passages from different editions solely because book/chapter/verse coordinates match.

### 4.3 Catalogue and testament grouping

- Use the Owner-approved canonical 66-book order as the authoritative order; never derive order from ZIP entry order or alphabetical sorting.
- Each catalogue record carries a stable USFM ID, canonical order, testament enum, chapter count and per-locale reviewed display name.
- Render two explicit groups: Old Testament and New Testament. Section labels, accessibility semantics and book counts are localized.
- Catalogue UI language follows the selected interface locale. The displayed scripture edition remains independently selected/identified.
- Do not invent Maale or Amharic book names. Missing reviewed names are tracked as localization defects and block that locale's catalogue acceptance.
- Validate 66 unique books, correct canonical sequence, correct testament membership and exact per-edition chapter counts.

## 5. Real reader experience

P1 reader must render installed, verified content—not sample text, hard-coded examples or placeholders.

Required behavior:
- open a book and chapter from the localized catalogue;
- render exact text for the selected edition, with stable book/chapter/verse coordinates;
- support previous/next chapter and direct book/chapter navigation;
- display headings, paragraph and poetry layout according to parsed structure;
- support Ethiopic glyphs, font scaling, line spacing, light/dark theme and large-text layout without clipping;
- preserve reading position across process death and restart;
- expose accessible reading order, meaningful TalkBack labels/actions and selectable controls;
- show explicit localized loading, no-content, edition-not-installed, integrity-failure and recoverable-error states;
- never silently replace an unavailable passage or edition;
- maintain source/edition provenance in the reader's edition information surface;
- remain fully usable offline after successful package installation.

P1 does not require commentary, maps, audio, cloud sync or AI-generated explanation in the reader.

## 6. Localization completion

P1 localization scope is every flow introduced or touched by the critical Bible experience:

- onboarding and language selection;
- Home/continue reading and daily verse states needed for this slice;
- Books catalogue, Old Testament/New Testament headings, book counts and empty states;
- reader navigation, controls, errors, edition metadata and accessibility descriptions;
- Settings language and calendar selectors;
- offline package/download/update/verification states;
- About/attribution surface required to display source credits and licence notices.

Locales: Maale (`mdy`), Amharic (`am`) and English (`en`). Use reviewed string resources; remove hard-coded user-visible strings in affected flows. CI must compare required resource keys across locales and detect missing translations. No hidden English fallback for a supported locale.

Binding copy:
- Amharic tagline: **መጽሐፍ ቅዱስ - ሕያው ቃል ተሞክሮ**
- Complete English tagline: **OWNER CONFIRMATION REQUIRED**; do not complete or freeze the phrase by inference.
- Maale language review: qualified reviewer required; machine encoding tests do not satisfy it.

QA includes Ethiopic glyph coverage, combining marks, normalization diagnostics, font fallback, line wrapping, small/large displays, TalkBack labels, screen-reader navigation and mixed-locale/edition combinations.

## 7. E.C.-default / G.C.-selectable calendar

Implement a single calendar-preference abstraction shared by all P1 date surfaces:

- initial default is Ethiopian Calendar (E.C.);
- user can explicitly select Gregorian Calendar (G.C.) in Settings;
- preference persists across restart and is independent of interface language;
- changing calendar changes display and date interpretation at the UI boundary, not canonical stored instants or scripture passage identity;
- all date formatting routes through the same tested calendar service;
- E.C. conversion handles 13th month Pagume, leap-year behavior, month/year boundaries and local time zone;
- G.C. conversion handles leap years and month boundaries;
- tests cover dates on both sides of each year boundary, Pagume 5/6 behavior, leap years, midnight/local-zone boundaries, round trips and stable event identity;
- if P1 only has limited date surfaces, architecture must still make the shared preference reusable by later Home, daily verse, reading plan, history, reminder and quiz work.

Do not store a formatted E.C. or G.C. date string as the canonical timestamp.

## 8. Offline persistence, package integrity and recovery

- Core reader and catalogue queries must not require network access after package activation.
- Store content in versioned, edition-scoped local persistence with explicit schema migrations.
- Verify package and payload digests before activation and on defined integrity checkpoints.
- Install/update must be staged and atomic. A failed download, parse, migration, disk-full event, process death or integrity check must preserve the last-known-good package.
- Prevent mixed-version state: the active manifest, content rows and indexes must refer to one package version.
- Keep content package updates separate from app binary updates.
- Do not overwrite user annotations during content updates; preserve stable reference mappings and report any unmappable annotation.
- Define uninstall/reinstall and backup boundaries clearly; scripture package restoration must still pass integrity and rights checks.
- Never log scripture text, user notes or sensitive local data in CI or crash diagnostics.
- Add tests for corruption, truncation, checksum mismatch, interrupted install, concurrent reader access during update, migration rollback, insufficient storage and recovery to last-known-good state.

## 9. Work breakdown and dependencies

| Order | Work package | Depends on | Exit evidence |
|---|---|---|---|
| P1.0 | Architecture and source-contract freeze | This plan; exact repo/source inspection | Owner-approved technical contracts and implementation task breakdown |
| P1.1 | Restricted source staging + ingestion validator | P1.0; Maale source hash manifests | Reproducible validation report; no public scripture payload |
| P1.2 | USFM parser + normalized domain model | P1.1 | Parser tests and source-to-output reconciliation |
| P1.3 | Edition package builder + integrity manifest | P1.2; rights/attribution decisions | Deterministic package and verification tests |
| P1.4 | Local persistence and atomic activation | P1.3 | Crash/fault-injection, rollback and migration tests |
| P1.5 | Canonical localized catalogue | P1.2; reviewed book-name inventory | 66-book/testament/order/localization tests |
| P1.6 | Real offline reader | P1.4 + P1.5 | Exact-source passage rendering and navigation tests |
| P1.7 | P1-flow localization completion | P1.5 + P1.6 UI scope | Locale parity, linguistic review and UI QA evidence |
| P1.8 | E.C./G.C. calendar service and selector | P1.0; P1 UI surfaces | Conversion, persistence and boundary tests |
| P1.9 | Integrated acceptance and hardening | P1.1–P1.8 | Exact-head CI, offline/device/accessibility and integrity evidence |

Parallel work is permitted only after interface contracts are frozen. Do not start reader UI against invented sample data; use repository interfaces and explicit unavailable states until verified packages are available.

## 10. Acceptance matrix

| ID | Acceptance criterion | Required evidence |
|---|---|---|
| P1-SRC-01 | Exact source archive and per-file hashes verified before parsing | SHA-256 report, inventory and restricted staging record |
| P1-SRC-02 | No raw scripture corpus or derived scripture payload in public Git | Repository diff and automated secret/content policy scan |
| P1-SRC-03 | Parser preserves exact scripture text and supported USFM structure | Golden structural tests and source/output reconciliation |
| P1-SRC-04 | Unknown/malformed markers fail closed | Negative parser tests |
| P1-SRC-05 | Every passage traces to exact edition/source revision | Provenance query and manifest test |
| P1-PKG-01 | Package is deterministic, versioned and integrity checked | Reproducible build and digest verification |
| P1-PKG-02 | Install/update is atomic and rollback safe | Fault injection, interruption and recovery tests |
| P1-CAT-01 | Exactly 66 unique books in approved canonical order | Catalogue invariant tests |
| P1-CAT-02 | OT/NT membership and grouping correct | Testament inventory tests and UI assertions |
| P1-CAT-03 | Maale/Amharic/English names are reviewed and locale-selected | Name inventory, review evidence and locale tests |
| P1-RDR-01 | Reader displays exact installed scripture for selected edition | Passage fixtures derived from restricted test corpus; provenance checks |
| P1-RDR-02 | Navigation and reading position persist correctly | Unit/instrumentation/lifecycle tests |
| P1-RDR-03 | Reader works offline after install | Airplane-mode device/emulator acceptance |
| P1-RDR-04 | No placeholder, fabricated text or silent edition fallback | UI/state tests and manual review |
| P1-LOC-01 | Every P1 flow has complete mdy/am/en resources | Resource parity CI and string audit |
| P1-LOC-02 | Ethiopic rendering and accessibility pass | Device screenshots, TalkBack and linguistic QA |
| P1-CAL-01 | E.C. is initial default; G.C. selectable and persistent | Preference and UI tests |
| P1-CAL-02 | Conversion and boundary behavior correct | Deterministic date test vectors and property/round-trip tests |
| P1-OFF-01 | Reader/catalogue operate without network | Network-disabled acceptance |
| P1-OFF-02 | Corruption/update failure preserves last-known-good content | Fault-injection and recovery report |
| P1-SEC-01 | Scripture and user data are not leaked to logs | Log inspection and automated tests |
| P1-SEC-02 | Rights/attribution and source limitations are visible and enforced | Source manifest review and attribution UI acceptance |

## 11. P1 exit criteria

P1 is complete only when all mandatory acceptance IDs above are PASS or have a specifically Owner-accepted exception; the Maale specialist review and any distribution-specific rights gate must remain explicit and cannot be silently waived by engineering. The integrated build must be verified at an exact commit SHA. Physical-device offline reading, Ethiopic rendering and TalkBack acceptance are required; emulator-only success is insufficient.

P1 completion does not itself authorize merge, signing, deployment, release or publication. Those remain separate Owner-controlled stages.

## 12. Open decisions and blockers

1. **English tagline:** Owner to confirm the complete English wording before visual copy freeze.
2. **Maale specialist review:** qualified reviewer and review evidence remain to be arranged.
3. **Amharic full-Bible edition:** current documented candidate is NT-only; full edition and rights remain unresolved.
4. **English edition:** exact WEB revision/completeness and naming/attribution conditions require source verification.
5. **Package signing/trust model:** technical key custody, rotation, revocation and offline verification contract must be specified before production package distribution; do not reuse debug/test keys.
6. **Storage/backup boundary:** define whether scripture packages are independently reacquired or included in user backup; user notes and annotations must remain separately protected.
7. **Source corpus workspace:** identify and document the restricted non-public staging location and retention/access policy before source processing.

## 13. Immediate next step

Perform P1.0: inspect the current application source, Gradle/dependency baseline, existing catalogue, resource files, persistence layer, source manifests and PR #59 exact head. Produce a read-only architecture reconciliation mapping each frozen P1 requirement to existing files, missing components, migration risks and implementation tasks. No application code changes until that reconciliation and the implementation task sequence are reviewed and explicitly authorized by the Owner.
