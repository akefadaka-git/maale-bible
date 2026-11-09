# Maale Bible UI-04 — Smart Search

## Objective
Replace the search placeholder with a responsive, accessible search experience that is honest about the current content baseline.

## Implemented
- Search field with clear action and live local filtering.
- Functional lookup against the existing 66-entry metadata-only canonical book catalogue.
- Matches book English names and canonical USFM IDs, case-insensitively.
- Result cards open the existing reader route using the canonical USFM ID.
- Empty-query capability panel distinguishes available book lookup from future scripture search.
- No-match state provides examples without fabricating results.
- Width-constrained responsive layout for larger screens; full-width layout on phones.
- Search field has a descriptive accessibility semantic label.

## Explicitly not implemented
- Full-text scripture search, keyword/phrase matching against verses, language selection, filters over scripture content, result snippets, relevance ranking, and offline scripture index.
- These require an authorized, provenance- and licence-verified corpus plus a validated indexing pipeline. The UI must not imply that such data exists.

## Scope and safety
- No scripture text, sample verses, new dependencies, permissions, persistence, or network service added.
- Existing canonical catalogue and reader navigation are reused.
- Existing unrelated work is untouched.
- This is a draft implementation slice; no merge, signing, deployment, or publication is authorized.

## Verification
- Source inspection and GitHub diff review are required.
- Android build, unit/instrumented tests, emulator/device matrix, and TalkBack audit remain NOT RUN unless exact evidence is recorded separately.
