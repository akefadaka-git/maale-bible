# Genesis content coverage and consolidated review

**Date:** 2026-11-15  
**Repository:** `akefadaka-git/maale-bible`  
**Pull request:** [#63 — bilingual Bible book learning centre](https://github.com/akefadaka-git/maale-bible/pull/63)  
**Branch:** `integration/maale-learning-centre-2026-11-14`  
**Disposition:** **PARTIAL — content coverage expanded; A4 review is not closed.**  
**Publication state:** `draft_not_for_release`

## 1. Coverage completed in this implementation

The documentation package and Android asset contain matching English and Amharic locale objects. The Amharic work is a draft translation, not a claim of qualified language approval.

| Content component | English | Amharic draft |
|---|---:|---:|
| Book introduction | 1 | 1 |
| Chapter guides | 50 | 50 |
| Structured chapter-study prompts | 350 | 350 |
| Inductive studies | 4 | 4 |
| Quiz questions | 110 | 110 |
| Multiple-choice questions | 6 | 6 |
| True/false questions | 96 | 96 |
| Multiple-select questions | 4 | 4 |
| Short-answer questions | 4 | 4 |
| Key-verse study entries | 18 | 18 |
| Memorization-verse entries | 7 | 7 |
| Commentary units | 6 | 6 |
| Concordance entries | 14 | 14 |
| Dictionary entries | 5 | 5 |
| Timeline events | 5 | 5 |

The introductions now include authorship/composition uncertainty, literary genre, theological contribution and canonical connections. Chapter guides include literary context, related passages, discussion prompts, review summaries and structured study prompts. Inductive studies include prayer prompts. Six original commentary notes have matching Amharic draft translations.

The Android learning model and screen now read and display the additional introduction, chapter-guide, prayer and commentary fields. The locale resolver uses Amharic when available; for Maale (`mdy`) or unsupported locales it uses English only with an explicit fallback notice. No Maale translation has been fabricated.

## 2. Content-quality qualifications

- The quiz bank now contains 6 multiple-choice, 96 true/false, 4 multiple-select and 4 short-answer items in each locale. The Android renderer and scoring/session model support all four types. Completion and matching formats remain unimplemented. The 86 previously generic true/false rationales in each locale have been replaced with passage-specific draft explanations; the ten false statements have explicit corrections. These explanations still require independent review.
- Some quiz rationales are concise and repetitive. Their exact references and explanations still require source-level and editorial review.
- The Amharic material is generated draft text. A qualified reviewer candidate has been identified and an outreach email draft prepared; no independent language sign-off has been received.
- Maale learning content is not translated. A qualified Maale translator candidate has been identified and an outreach email draft prepared; agreed orthography, independent community review and signed approval remain required. English fallback is disclosed rather than represented as Maale.
- Commentary units are original draft notes, not a substitute for a licensed study Bible or a completed theological review.

## 3. Consolidated biblical review — preliminary

The broad outline is consistent with recognized Genesis introductions: Genesis 1–11 presents the primeval narratives and Genesis 12–50 follows the ancestral narratives. The finer editorial grouping into Abraham/Sarah (12–25), Isaac/Jacob/Esau (26–36) and Joseph (37–50) is a useful navigation structure, not a claim that all scholarly outlines divide the book identically.

The introduction distinguishes traditional Mosaic association from modern scholarly debate and does not state that Genesis names Moses as its author. Its themes of creation, human vocation, judgment and preservation, covenant, descendants, land and blessing are consistent with the cited secondary introductions.

Sources consulted:
- Society for Old Testament Study, [Genesis](https://www.sots.ac.uk/wiki/genesis/)
- United States Conference of Catholic Bishops, [Genesis introduction and outline](https://bible.usccb.org/bible/genesis/0)
- Cambridge University Press, [The Literature of the Pentateuch](https://www.cambridge.org/core/books/cambridge-companion-to-the-hebrew-bibleold-testament/7A41BA8CB07E73814D94E30B3437613B/listing)

**Limit:** This is a secondary-source and internal consistency review. It does not validate every passage reference against the exact authorized 2008 Maale Full Bible text.

## 4. Editorial, theological, rights and language review

| Gate | Status | Evidence / remaining work |
|---|---|---|
| English editorial review | **PENDING** | No independent English editor sign-off recorded; institutional referral requested |
| Amharic language review | **PENDING** | Full draft coverage exists; qualified reviewer sign-off absent |
| Maale language review | **MISSING** | No Maale translation or qualified sign-off recorded; candidate identified for outreach |
| Theological review | **PENDING** | Six commentary units and all study/quiz material require independent theological review |
| Exact scripture-reference validation | **PENDING** | Automated validator integrated; corrected exact Genesis source hash; final CI pass pending |
| Rights review | **PARTIAL** | Attribution and CC BY-SA 4.0 notices implemented in app and package; final asset audit remains |
| Historical/editorial claims | **PENDING** | Date, authorship and historical claims require editorial disposition |
| Publication approval | **NOT APPROVED** | Keep `draft_not_for_release`; release builds remain fail-closed |

The exact authorized source archive is downloaded in CI and its SHA-256 is checked against the Owner-accepted baseline. The extracted Genesis USFM hash was corrected to the actual 64-character SHA-256 computed from that pinned archive. The source-reference validator checks every collected learning-package reference against all 66 books. Its first exact-source run exposed the previously truncated Genesis hash; the corrected audit is now queued. No real-source SQLite scripture package has been generated or installed, and no scripture text is embedded in these learning packages.

## 5. Technical verification

Implemented:
- Documentation and Android asset locale parity for Genesis EN and AM.
- Machine-readable quiz answer parity checks and explicit Amharic explanation-locale metadata.
- Validation script at `tools/content/validate_genesis_package.py`, integrated into the A3 Android workflow.
- Android model/UI support for date/composition, genre, theological contribution, canonical connections, chapter context, cross-references, discussion prompts, review summaries, prayer prompts and locale-specific commentary.
- Unit tests for explicit English fallback behavior for Maale and unsupported locales.
- Release gate remains `draft_not_for_release`; no publication-state elevation was made.

The exact-head workflow must complete successfully before the technical gate is marked PASS. The workflow now runs resource parity, content validation, source-reference parser unit tests, downloads and verifies the pinned 2008 archive, validates all references, then runs Android unit tests, APK assembly and instrumentation.

## 6. Closure decision

**Genesis is not ready for VERIFIED-CLOSED.** Content coverage has been substantially expanded in English and Amharic draft form, but the remaining blockers are substantive:

1. Exact-source biblical reference validation against the authorized 2008 MDYTWF/MDYETH text.
2. Independent English editorial review.
3. Qualified Amharic language review.
4. Qualified Maale translation and language review.
5. Independent theological review.
6. Independent review of the expanded quiz formats, answer keys and passage-specific rationales.
7. Exact-head Android/content CI and final QA.
8. Final rights and attribution audit of the app and repository assets.

Keep PR #63 open and draft. Do not merge, deploy, publish, or change Genesis from `draft_not_for_release` until the outstanding A4 gates have evidence and explicit approval. A5 deployment remains separate.
