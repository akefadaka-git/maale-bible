# A3.1 — Maale localization audit baseline

**Baseline commit:** `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**Canonical resources:** `app/src/main/res/values/strings.xml`  
**Maale resources:** `app/src/main/res/values-b+mdy/strings.xml`

## Audit result

The canonical English resource file contains 125 string keys. The Maale resource file contains 144 string keys.

| Measure | Result |
|---|---:|
| Canonical keys | 125 |
| Exact key matches in Maale | 60 |
| Missing canonical keys | 65 |
| Maale-only keys | 84 |
| Matching-key coverage | 48.00% |

The 84 Maale-only names are not counted as translations of canonical keys. They may be legacy or feature-specific aliases. Do not delete or rename them until source-code references and resource-merger behavior have been checked.

## Missing canonical keys

The following keys need an approved Maale equivalent:

```text
nav_settings
home_tagline
browse_books
search_hint
search_support
search_try
search_boundary
settings_accessibility
settings_accessibility_help
settings_language_help
settings_backup
settings_backup_help
settings_create_backup
settings_recovery_key
settings_restore
settings_recovery_key_hint
settings_envelope_hint
settings_restore_button
settings_content
settings_content_help
reader_coordinates_notice
reader_position
reader_previous_chapter
reader_next_chapter
reader_previous_verse
reader_next_verse
reader_add_bookmark
reader_highlight
reader_note_hint
reader_update_note
reader_bookmarks
reader_annotations
reader_plans
reader_new_plan
reader_create_plan
reader_edit
reader_delete
reader_remove
reader_go_to
study_plans
study_annotations
listen_title
listen_desc
listen_track_desc
listen_library_title
listen_library_desc
listen_offline_title
listen_offline_desc
nav_more
header_notifications
header_no_notifications
header_profile
header_guest_mode
header_language
study_private_body
study_calendar_title
study_calendar_today
study_insights_title
study_metric_bookmarks
study_metric_notes
study_metric_highlights
study_metric_plans
study_places_title
study_places_body
study_places_status
```

## Acceptance requirements

1. Every canonical string key has a corresponding Maale resource entry.
2. Every format placeholder is preserved with the same index and type (for example, `%1$d`).
3. XML parses successfully; resource names are unique.
4. Maale-only resource keys are reconciled against code references; no live key is silently removed.
5. A qualified Maale reviewer checks spelling, orthography, terminology, naturalness, consistency and contextual meaning.
6. Reviewer changes are recorded with key, proposed wording, rationale, reviewer identity/qualification and approval status.
7. A separate reviewer verifies Ethiopic rendering, clipping, font fallback, TalkBack pronunciation and representative device layouts.

## Language-quality boundary

**No missing Maale wording is fabricated in this baseline.** Existing Owner-supplied Maale strings are preserved verbatim. New translations remain pending qualified review. A resource-coverage pass is not a linguistic-quality sign-off.

## Reproducible audit

From the repository root:

```bash
python tools/a3/localization_audit.py \
  --base app/src/main/res/values/strings.xml \
  --locale app/src/main/res/values-b+mdy/strings.xml
```

Add `--strict` to return a non-zero exit code when canonical keys are missing, placeholders differ, or duplicate resource names exist.


## Core UI reference reconciliation

A source-reference scan was performed against the nine core Compose UI files in the A3 baseline: app shell, Home, Books, Search, Settings, Study, Reader, Audio and shared components.

- **61 of the 65 missing canonical keys** are referenced in those core UI files and therefore represent active Maale fallback gaps in the inspected UI surface.
- Four missing canonical keys had no reference in those nine files: `home_tagline`, `browse_books`, `settings_envelope_hint` and `home_tagline` appears once in this set. Their use elsewhere in the repository has not been ruled out.
- None of the 84 Maale-only resource names was referenced by the nine inspected UI files. This is a scoped result, not proof that they are globally unused; do not remove them until a repository-wide search, including tests and generated/dynamic references, is complete.

The missing-key scan and Maale-only-key scan are resource-name based. They do not substitute for a runtime screen-by-screen fallback audit.
