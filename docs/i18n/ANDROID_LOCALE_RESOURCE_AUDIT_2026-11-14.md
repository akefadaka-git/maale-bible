# Android locale resource audit — 2026-11-14

## Scope

The audit covers the Android string resources in:

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-am/strings.xml`
- `app/src/main/res/values-b+mdy/strings.xml`
- the matching `book_learning.xml` and `book_explorer_testaments.xml` files in all three locale directories.

## Result

- Core strings: **209 keys** in EN, AM and Maale.
- Learning Centre: **45 keys** in EN, AM and Maale.
- Testament labels: **4 keys** in EN, AM and Maale.
- Format-placeholder parity: **PASS** across the three locales.
- Missing and extra resource keys: **0** for the audited files.
- CI guard: `tools/i18n/check_android_resource_parity.py`, executed by the A3 Android verification workflow.

## Translation status

- 84 core resource keys were added to the default English and Amharic files. The Amharic wording is a draft translation and still requires qualified language review.
- 65 Maale resource entries were added as explicit English fallbacks with `translatable="false"`. They are not represented as Maale translations.
- Existing Maale translations were preserved.
- The Learning Centre's Maale strings likewise remain explicit English fallback pending qualified Maale translation and community review.

## Release boundary

Resource-key parity is a technical completeness check, not translation approval. Amharic and Maale language review, accessibility review, contextual QA and Owner release approval remain separate gates.
