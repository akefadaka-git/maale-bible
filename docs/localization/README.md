# Maale translation review workflow

The CSV worklist contains the 65 canonical Android string keys that do not yet have an exact-key Maale resource at the A3 baseline.

## Reviewer instructions

1. Work from the English source column and review the UI context in the app.
2. Enter proposed Maale wording only when the translator is confident in the intended meaning.
3. Preserve format placeholders exactly, including index and type (for example, `%1$d`).
4. Record rationale for terminology choices, especially Bible, study, accessibility, security, backup and Android navigation terms.
5. Record reviewer name and relevant Maale language qualification.
6. Set review status to `REVIEWED`, `NEEDS_DISCUSSION` or `UNRESOLVED`; do not mark a translation approved merely because it has been proposed.
7. Owner approval is a separate decision. Only after linguistic review and Owner approval should wording be transferred into `values-b+mdy/strings.xml`.
8. Keep a traceable record of changes and re-run the localization audit after each approved batch.

## Non-negotiable quality boundary

Do not fill gaps with machine-generated or guessed Maale text and label it reviewed. The current baseline is 60/125 exact-key coverage (48.00%). Translation completion and linguistic QA are separate acceptance gates.
