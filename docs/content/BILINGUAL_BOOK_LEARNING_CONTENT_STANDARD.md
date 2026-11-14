# Bilingual Bible Book Learning Content Standard

**Status:** Implementation contract / content authoring standard  
**Languages:** English (EN) and Amharic (AM)  
**Catalogue:** 66-book Protestant canon currently represented by the verified 2008 Maale Full Bible catalogue (MDYTWF / mdyeth). Other canon profiles must be separate and edition-specific.

## 1. Per-book learning package

Every one of the 66 books has a stable USFM identifier and an independent content package. Each package must provide:

1. **Book introduction (EN and AM):** concise overview; authorship and scholarly positions (with uncertainty stated); date or date range and rationale; historical and cultural setting; original audience; purpose; literary genre; structure and chapter outline; major themes; theological contribution; key people and events; key passages; and canonical connections.
2. **Chapter guides (EN and AM):** chapter summary, literary context, key observations, interpretive issues, cross-references, discussion prompts and a short review.
3. **Inductive study (EN and AM):**
   - Observation: what the passage actually says; people, actions, setting, repeated words, structure and literary form.
   - Interpretation: historical/literary context, original audience, authorial purpose, canonical context and interpretive alternatives where material.
   - Application: faithful, context-aware response, personal/community practice, reflection and prayer prompts.
4. **Quiz bank (EN and AM):** multiple choice, true/false, completion, matching, chapter comprehension and book-level assessment. Every item must have the correct answer, rationale, difficulty, learning objective and one or more verified scripture references. Distractors must be plausible but not misleading.

No book may be marked complete merely because the screen or catalogue entry exists. The JSON register records the initial state for all 66 books.

## 2. Content provenance and editorial gates

Each content asset must carry:
- stable content ID, book USFM ID, chapter/verse references where relevant, locale and version;
- author/compiler and responsible editorial reviewer;
- source title, publisher/organization, stable URL or bibliographic locator, source version/date and retrieval date;
- licence or written permission, permitted use, required attribution and share-alike/derivative obligations;
- edition compatibility and scripture-reference validation;
- language review status, reviewer role and review date;
- content integrity hash and publication state.

Required workflow: **DRAFT → SOURCE_VERIFIED → RIGHTS_VERIFIED → LANGUAGE_REVIEWED → THEOLOGICALLY/EDITORIALLY_REVIEWED → QA_VERIFIED → APPROVED_FOR_RELEASE**. Any missing gate keeps the asset unpublished. A disagreement or contested authorship/date must be described accurately, not silently converted into certainty.

## 3. Bilingual authoring

English and Amharic are first-class content locales, not machine-fallback labels. They must preserve the same factual scope, references, nuance and learning objectives. Translation must be reviewed by a competent editor in the target language. Scripture quotations must come from separately verified, licensed Bible editions and retain edition attribution. Do not copy copyrighted study Bible introductions or commentary without explicit permission.

Maale is the primary app language. Maale learning content is a later language-review stream and must not be inferred from Amharic. Owner-supplied wording is not equivalent to formal community linguistic approval.

## 4. Study and quiz quality

- Each guide is tied to a book and, where possible, a canonical passage range.
- Observation questions must not smuggle in interpretation.
- Interpretation must account for genre, context and textual uncertainty.
- Application must not detach a passage from its original context.
- Quiz answer rationales must explain why the answer is supported; scripture references must resolve against the selected verified edition.
- Do not generate questions from unavailable scripture text or unsupported facts.
- Track attempt history and scores separately from content assets; allow repeat practice without overwriting prior results.

## 5. Supporting reference modules

The learning centre is designed to connect to a Bible dictionary (terms, people and places), historical timeline, verified Bible atlas, character studies, topic studies, cross-references, memory verses, reading plans, private study journal and individual/group progress. Each reference record follows the same provenance and language-review policy. Atlas locations, boundaries and routes require independently verified geographic sources.

## 6. Release acceptance

A book's bilingual learning package is releasable only when its EN and AM introduction, chapter guides, inductive study and quiz bank have independently passed the required source, rights, language, editorial and QA gates. Partial availability must be displayed by component and locale. No app-level "complete" badge may conceal missing content.

## 7. Initial register

See [BOOK_LEARNING_CONTENT_REGISTER.json](BOOK_LEARNING_CONTENT_REGISTER.json). It lists all 66 canonical books and tracks EN/AM introduction, chapter-guide, inductive-study and quiz-bank readiness independently.
