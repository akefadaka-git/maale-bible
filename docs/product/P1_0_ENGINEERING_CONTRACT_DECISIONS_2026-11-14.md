# P1.0 Engineering Contract Decisions — Scripture, Identity, Parsing, Trust and Calendar

**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Date:** 2026-11-14  
**Status:** OWNER-DIRECTED CONTRACT DECISIONS RECORDED — P1.1 IMPLEMENTATION AUTHORIZATION REMAINS SEPARATE  
**Authority:** P0 Product Requirements Baseline v1.0 and Owner Content Intake Authority Reconciliation  
**Branch:** `planning/p1-critical-bible-experience-2026-11-14`

This document resolves the five P1.0 architecture questions into concrete engineering contracts. These are proposed decisions for Owner approval; writing them does not authorize application implementation, source acquisition, key generation, signing, merge, deployment or publication.

## 1. Scripture storage boundary

### 1.1 Two physically and logically separate stores

**Scripture Content Store (SCS)**

- Holds immutable, edition-scoped, verified scripture package versions and their read-only indexes.
- Does not contain user notes, bookmarks, highlights, profile data, reading history or recovery secrets.
- Does not reuse the current SQLCipher personal-study database or its backup format.
- Content is installed from a package only after signature (when signed), manifest, payload digest, schema and source/rights checks succeed.
- Reader access is read-only. No app screen or user operation edits canonical verse text.
- A package version is immutable after activation. An update creates a new version directory/database and atomically switches the active edition-version pointer.
- Keep the last-known-good package until the replacement has passed activation and post-activation smoke checks.

**Personal Study Store (PSS)**

- Retain the existing SQLCipher-encrypted study database and its recovery/backup boundary.
- Holds user-owned notes, bookmarks, highlights, reading positions, reading-plan progress and preferences as defined by the existing schema.
- References scripture by stable edition-aware passage identity, never by scripture database row ID.
- Scripture package updates must not rewrite or replace user-owned records.
- Personal backups do not silently include scripture packages. Package restoration/reacquisition is a separate, rights-checked process.

### 1.2 Storage layout and lifecycle contract

Proposed app-private layout (exact Android storage API/location to be selected during implementation; no external/public shared-storage path):

```
files/scripture/
  packages/<edition-id>/<package-version>/
    manifest.json
    payload.sqlite
    attribution/
    integrity.json
  active/<edition-id>.json
  staging/<install-id>/
  trust/
    pinned-root-metadata.json
    accepted-key-state.json
`

- Package directories are write-once after verification; use atomic filesystem operations where supported.
- Never expose raw scripture payload through exported content providers, backup intents, debug endpoints or public CI artifacts.
- Database schema must use immutable edition and package identifiers and indexes scoped by edition.
- Treat package files as untrusted until every validation step completes.
- Disk-full, process death, interrupted copy, invalid signature, checksum mismatch, unsupported schema or failed migration leaves the active pointer unchanged.
- If encryption at rest is considered, separately assess source licence, Android Keystore availability, backup semantics and key-loss recovery. Encryption is not a substitute for signature or integrity verification. Do not promise that local files are unreadable on a rooted/compromised device.

### 1.3 Required interfaces

- `ScripturePackageInstaller.install(source, policy): InstallResult`
- `ScripturePackageVerifier.verify(package, trustState): VerificationResult`
- `ScripturePackageRegistry.activeEdition(editionId): ActivePackage?`
- `ScriptureRepository.getBookList(editionId, locale): List<BookMetadata>`
- `ScriptureRepository.getChapter(PassageChapterRef): ChapterContent`
- `ScriptureRepository.getPassage(PassageRef): PassageContent`
- `StudyRepository` continues to own user records and accepts `PassageRef` values only.

No SCS API may expose mutable verse updates. A package replacement is a version activation, not an in-place content edit.

## 2. Edition-aware passage identity and legacy migration

### 2.1 Canonical identifiers

- `EditionId`: stable namespaced identifier for a specific translation/edition, not just language (e.g. a source-specific edition key, not `mdy`).
- `BookId`: stable USFM book identifier (e.g. `GEN`), independent of localized display name and database row.
- `ChapterNumber`: positive canonical chapter integer.
- `VerseLocator`: canonical source verse token preserving suffix/range identity, including forms such as `1`, `1a`, `1b`, and bridged markers such as `3-4`.
- `PassageRef`: `editionId + bookId + chapterNumber + verseLocator`.
- `ChapterRef`: `editionId + bookId + chapterNumber`.

A range selection may be represented as an ordered list of PassageRef values or a validated `PassageRangeRef` containing start/end PassageRef within the same edition/book/chapter. Do not flatten USFM verse bridges into invented verse text or assume every numeric verse is a standalone source record.

Canonical serialized form is versioned and opaque to UI code, for example:

```text
passage:v1:<edition-id>:<USFM-book-id>:<chapter>:<verse-locator>
```

Use a structured database representation rather than parsing ad-hoc strings as the source of truth. Escape/validate identifiers before serialization. Never use display names, localized strings, auto-increment row IDs or content hashes as passage identity.

### 2.2 User-owned references

- Bookmarks, highlights and notes store `PassageRef` plus optional end reference and selected-text offsets only where offsets are tied to a content digest and safely invalidated on edition change.
- Reading position stores edition ID, book ID, chapter and optional verse locator.
- Reading plans store their canonical plan schedule separately; a daily assignment's passage reference includes edition identity.
- User may change the selected Bible edition without silently rewriting references. If the new edition lacks an equivalent passage, keep the original edition reference and show a localized unavailable-in-selected-edition state.
- Cross-edition mapping is an explicit, versioned mapping table (if ever introduced); matching book/chapter/verse coordinates alone is not proof of semantic equivalence.

### 2.3 Migration of existing records

Current study references are legacy edition-less coordinates. Migration is additive and lossless:

1. Add nullable `edition_id` and a versioned passage-reference representation; preserve all existing columns and primary keys.
2. Determine a single default legacy edition only from a separately approved, installed and rights-eligible edition policy. Do not assume a default package exists simply because Maale is the first target.
3. If a verified default edition is available and the legacy coordinates resolve unambiguously, backfill the edition identity and preserve the original values for audit/rollback.
4. If no default edition is available, or a verse coordinate is ambiguous (bridge/suffix/missing), retain the legacy coordinate as `UNRESOLVED_LEGACY_REFERENCE`; never discard or guess.
5. Migration must be transactional, idempotent, restart-safe and reversible before schema finalization.
6. Verify counts and stable IDs before/after; prove notes, bookmarks, highlights, reading positions and plan progress remain intact.
7. The UI must still expose unresolved legacy records and offer a safe manual remapping flow later; P1 must not silently drop them.

Acceptance invariant: number of user-owned records and their stable IDs before migration equals after migration; each reference is either deterministically mapped or explicitly unresolved.

## 3. USFM parser contract

### 3.1 Parser principles

- Parse USFM structurally using a tokenizer/state machine or a maintained parser with pinned version and audited behavior. Do not parse scripture using regex-only marker stripping.
- Preserve scripture text exactly as decoded from verified source bytes. Do not paraphrase, autocorrect spelling, transliterate, silently normalize Unicode or rewrite punctuation.
- Keep raw source bytes/file hash and source line/offset coordinates in restricted build provenance; runtime packages may carry source locator metadata without carrying unnecessary raw source.
- Treat markers as syntax. Unknown markers, malformed nesting, duplicate book IDs, invalid chapter/verse order, malformed ranges, unexpected encoding and unsupported constructs fail the edition build closed.
- No best-effort dropping of content. No log output containing verse text.
- Pin parser implementation/version, USFM specification profile, schema version and normalization rules in package manifest.

### 3.2 P1 marker profile

The implementation must first inventory every marker actually present in the approved 2008 Maale USFM corpus and freeze a corpus-derived marker allowlist. The following is the initial expected profile, not permission to silently ignore any unlisted source marker.

**Book/chapter/verse and identification**
- `\id`, `\ide`, `\h`, `\toc1`, `\toc2`, `\toc3`, `\cl`, `\cp`, `\c`, `\v`.

**Paragraph and poetry structure**
- `\p`, `\m`, `\po`, `\pr`, `\cls`, `\pmo`, `\pm`, `\pmc`, `\pmr`, `\pi`, `\pi1`, `\pi2`, `\pi3`, `\mi`, `\nb`, `\b`, `\q`, `\q1`, `\q2`, `\q3`, `\q4`, `\qr`, `\qc`, `\qa`, `\qm`, `\qm1`, `\qm2`, `\qm3`, `\li`, `\li1`, `\li2`, `\li3`, `\li4`, `\lim`, `\lim1`, `\lim2`.

**Titles, section headings and descriptive text**
- `\mt`, `\mt1`, `\mt2`, `\mt3`, `\mte`, `\ms`, `\ms1`, `\ms2`, `\ms3`, `\mr`, `\s`, `\s1`, `\s2`, `\s3`, `\s4`, `\sr`, `\r`, `\d`, `\sp`, `\qa`, `\qc`.

**Inline character styles**
- `\add`, `\bd`, `\bdit`, `\em`, `\it`, `\k`, `\lit`, `\nd`, `\no`, `\ord`, `\pn`, `\qt`, `\sc`, `\sig`, `\sls`, `\sup`, `\tl`, `\w`, `\wg`, `\wh`, `\xt` only in contexts explicitly supported by the parser profile.

**Footnotes and cross references**
- `\f`, `\fe`, `\fr`, `\ft`, `\fk`, `\fq`, `\fqa`, `\fl`, `\fw`, `\fp`, `\fv`, `\x`, `\xo`, `\xop`, `\xt`, `\xq`, `\xot`, `\xnt`.

**Milestones and other**
- `\zaln-s`, `\zaln-e`, `\k-s`, `\k-e`, `\qt-s`, `\qt-e`, `\ts`, `\ts-s`, `\ts-e`, `\pb`, `\periph`, `\rem`, `\restore`, `\sts`, `\st`, `\esb`, `\esbe`, `\b` only where present and semantics are explicitly implemented.

The list is intentionally conservative and must be reconciled against the actual corpus before coding. Marker variants and USFM3 milestone syntax require dedicated parser tests. If a listed marker is absent from the source, it may remain supported by parser tests but must not be claimed as exercised by source coverage.

### 3.3 Parser output model

Emit an ordered typed document tree, at minimum:

- `BookDocument(bookId, metadata, chapters)`
- `ChapterNode(chapterNumber, orderedNodes)`
- `VerseNode(verseLocator, orderedInlineNodes, sourceLocator)`
- `ParagraphNode(style, children)`
- `PoetryLineNode(level, style, children)`
- `HeadingNode(level, style, children)`
- `InlineTextNode(exactText)`
- `CharacterStyleNode(style, children)`
- `FootnoteNode(caller, orderedContent)`
- `CrossReferenceNode(caller, targetText)`
- `MilestoneNode(kind, attributes, boundary)`
- `SourceLocator(fileId, sourceLine, byteStart, byteEnd)` in build-time provenance.

Retain verse ranges and suffixes as source locators. Keep footnotes/cross-references as separate structured content, not concatenated into canonical verse text. Unknown marker nodes are fatal by default. If later product decisions intentionally suppress a known marker from the reader, its source content must still be preserved in the package model and its omission must be explicit and tested.

### 3.4 Parser failure contract

Fatal errors include unknown marker, unsupported milestone, malformed marker nesting, unclosed inline style, invalid UTF-8, duplicate book/chapter/verse identity, impossible sequence, malformed verse bridge, orphan text outside a valid structural context, source file mismatch and content-loss during serialization.

Build output is rejected as a whole edition; partial editions are not activated unless an explicit future partial-edition product policy is separately approved. Diagnostics contain file ID, marker, line/offset and error code, never scripture text.

## 4. Package integrity, signing and offline trust

### 4.1 Distinguish integrity from authenticity

- SHA-256 digests detect payload changes relative to a manifest; they do not prove who produced the manifest.
- A digital signature authenticates a manifest only when verified against a trusted public key.
- The manifest signature must cover canonicalized manifest bytes and the payload digest(s), edition identity, package version, schema version, source revision, licence/attribution references and required policy fields.
- Use a standard, reviewed signature implementation; proposed algorithm: Ed25519. Do not invent cryptographic primitives or implement Ed25519 manually.
- Signature verification, trust-state processing and payload hashing must be separate checks with explicit results.

### 4.2 Key hierarchy and custody

- Offline Owner-controlled root key: kept offline, never embedded in the APK, repository, CI secrets or end-user package. Root key signs trust metadata and authorizes release-signing public keys.
- Package release-signing key: dedicated Ed25519 private key, generated and held in an Owner-approved secure signing environment; not on developer workstations or ordinary CI runners. It signs package manifests only.
- Runtime app contains a pinned root public key (or a versioned root trust set). Public keys are not secrets.
- Debug/test keys are separate and categorically rejected by production trust policy.
- Key generation, custody, backup, dual-control policy, compromise response and audit ownership must be documented before any production key is generated.
- No production private key is to be created as part of P1.1 source ingestion work.

### 4.3 Rotation and revocation

- Root-signed trust metadata contains metadata sequence number, validity interval, key IDs, public keys, permitted usages, activation/retirement times and revocation status/reason.
- Routine signing-key rotation: root signs metadata authorizing the new public key; app accepts both old and new only during the explicitly defined overlap; new packages use the new key after activation; old key is retired according to policy.
- Compromise: root-signed revocation metadata marks key compromised and establishes a minimum acceptable package sequence/version or package issuance time. Reject packages signed by revoked keys, including packages that were previously downloaded but not yet activated.
- Root rotation requires an update signed by the currently trusted root and, where possible, a cross-signature by the new root; define a recovery path for root compromise that does not silently trust arbitrary network metadata.
- Metadata sequence numbers prevent rollback to older trust state. Store the highest accepted sequence in app-private durable state; reject lower sequence metadata.
- Key IDs are fingerprints derived from public-key bytes, not arbitrary labels alone.

### 4.4 Offline verification and its limits

- A device with no network can verify a package against its pinned root and most recently accepted trust metadata, check revocation known at that point, verify manifest signature, payload hashes, schema compatibility and package policy, then activate.
- Offline verification cannot know about a revocation published after the device last received trust metadata. The UI and audit state must expose trust metadata freshness and last refresh time; do not claim globally current revocation status while offline.
- For first install, ship a pinned root public key in the app and deliver signed trust metadata with the package/app update or trusted import. Never trust an unsigned key bundled beside the package.
- Package expiry policy must not make already-installed scripture unusable merely because the device is offline; distinguish package content validity from signing-key acceptance for new activation.
- Reject rollback to an older package version/sequence unless a separately authorized recovery operation is cryptographically and audibly recorded.
- Test corrupted manifest, modified payload, wrong key, revoked key, stale metadata, metadata rollback, package rollback, unknown signer and offline first install.

### 4.5 Package manifest minimum

`packageId, editionId, packageVersion, packageSequence, schemaVersion, parserId, parserVersion, sourceId, sourceRevision, sourceArchiveSha256, sourceFileInventoryDigest, payloadFileInventory, payloadSha256, manifestFormatVersion, createdAt, licenceId, attributionRecordId, signatureAlgorithm, signingKeyId`.

Signature envelope is detached or embedded in a clearly specified field; canonical serialization (UTF-8 canonical JSON or deterministic CBOR) must be frozen and covered by test vectors. The signature must not be calculated over a representation that can be reserialized differently.

## 5. Calendar identity and conversion semantics

### 5.1 Two kinds of time data

**Instant / timestamp:** an actual moment (e.g. createdAt, modifiedAt, reminder trigger). Store as UTC epoch milliseconds or seconds with an explicit precision contract. Render in the device/user-selected time zone. Calendar preference affects display only.

**Civil date / date-only value:** a calendar day with no time or time zone (e.g. reading-plan day, liturgical/date label). Store as an integer ISO-8601 proleptic Gregorian epoch-day, equivalent to `LocalDate.toEpochDay()`, with an explicit `CivilDate` semantic type. This is a stable, time-zone-free day identity; it is not an instant.

Never persist a formatted E.C. or G.C. date string as canonical identity. Never convert an instant to a date without an explicit time zone.

### 5.2 Calendar preference

- `CalendarSystem.ETHIOPIAN` is the first-run default.
- `CalendarSystem.GREGORIAN` is selectable in Settings.
- Preference is persisted separately from interface locale and Bible edition.
- Preference changes presentation and calendar input/output conversion only; it does not mutate stored epoch-day values, UTC instants, reading positions, package IDs or event identity.
- All P1 date formatting/parsing routes through one `CalendarDateService`; screens must not implement ad-hoc conversion.
- Existing `ReadingPlanSchedule` epoch-day fields retain their current Gregorian epoch-day identity and are rendered through the new preference-aware service. Migration must not reinterpret stored values as E.C. dates.
- Date-only user input in E.C. is converted to the corresponding ISO epoch-day at the boundary, then stored canonically. Date-only user input in G.C. maps directly to the same canonical representation.
- Store the selected time zone separately only where an instant or local-day boundary requires it; a date-only value itself has no zone.

### 5.3 Conversion contract

`CalendarDateService` exposes:
- `toDisplayDate(civilDate, calendarSystem, locale): LocalizedCalendarDate`
- `fromDisplayDate(year, month, day, calendarSystem): CivilDateResult`
- `today(clock, zoneId, calendarSystem): LocalizedCalendarDate`
- `formatInstant(instant, zoneId, calendarSystem, locale): String`
- `startOfCivilDay(civilDate, zoneId): Instant` only for workflows that explicitly require a day interval.

Ethiopian conversion must implement 13 months, Pagume 5/6 day behavior and leap-year rules. Gregorian conversion uses ISO proleptic Gregorian rules. Conversion library/algorithm must be pinned, reviewed and tested against independent published vectors before use. Do not infer the Ethiopian year from Gregorian year with a fixed offset.

### 5.4 Calendar acceptance tests

- Round-trip E.C. → CivilDate → E.C. for every valid date in a broad multi-century test range.
- Round-trip G.C. → CivilDate → G.C. including Gregorian leap days and century rules.
- E.C. New Year boundary and G.C. year boundary.
- Pagume 5 and Pagume 6 for Ethiopian leap/non-leap years.
- Invalid date rejection (e.g. Pagume 6 in non-leap year).
- Month/day boundaries, localized month names/digits, locale independence and preference persistence.
- Instant formatting around UTC midnight, time-zone offsets and daylight-saving transitions where applicable.
- Same CivilDate identity under E.C. and G.C. display preferences.
- Existing reading-plan epoch-day fixtures unchanged before/after migration.

## 6. Contract freeze and change control

The following decisions are the proposed P1.0 architecture baseline:

1. Scripture package store is separate and immutable; personal study remains in existing SQLCipher store.
2. All durable passage references are edition-aware; legacy references migrate additively and may remain explicitly unresolved.
3. USFM ingestion is structural, deterministic and fail-closed; source-derived marker inventory is frozen before parser implementation.
4. Package authenticity uses Ed25519 signatures rooted in an offline Owner-controlled trust root; SHA-256 provides payload integrity; rotation/revocation metadata is signed and anti-rollback protected.
5. Civil dates are stored as ISO Gregorian epoch-day; instants remain UTC; E.C. is default display/input calendar and G.C. is selectable; calendar preference never changes canonical identity.

**Decision record:** These five architecture decisions are recorded in response to the Owner's explicit direction to resolve the contracts before preparing P1.1. They are the governing P1.0 technical baseline for task specification. This instruction does not authorize production key generation, actual source processing, implementation, merge, signing, deployment or publication. Any material change requires a documented Owner-approved change.
