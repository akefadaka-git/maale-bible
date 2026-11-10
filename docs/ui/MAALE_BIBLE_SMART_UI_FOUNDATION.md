# UI-01 — Maale Bible Smart UI Design Foundation

**Status:** IMPLEMENTED — verification pending  
**Branch:** `feat/ui-01-smart-design-foundation`  
**Base:** `feat/a4-offline-study-suite`  
**Scope:** semantic color tokens, spacing/radii tokens, Material 3 light/dark color schemes, and centralized shape system.

## Design direction

Maale Bible uses a calm, premium visual language combining spiritual warmth with restrained digital futurism. The interface must prioritize scripture legibility, cultural respect, predictable navigation, and low-distraction reading over decorative effects.

### Brand palette

| Token | Hex | Intended use |
|---|---|---|
| Midnight | `#0B1020` | Primary dark canvas |
| Deep Indigo | `#171D35` | Supporting brand surface |
| Royal Gold | `#D9B76E` | Primary dark-theme accent |
| Living Cyan | `#55D6D0` | Secondary accent |
| Scripture White | `#F5F1E8` | Dark-theme foreground |
| Muted Text | `#A6ADBC` | Secondary text reference |
| Light Background | `#F7F4ED` | Light-theme canvas |

Color usage must remain semantic. Status colors are reserved for status communication, not ornament.

## Component geometry

- Spacing scale: 2, 4, 8, 12, 16, 20, 24, 32 and 40 dp.
- Compact control radius: 12 dp.
- Standard control radius: 16 dp.
- Card radius: 22 dp.
- Hero surface radius: 28 dp.
- Pill radius: 50 dp.

## Theme behavior

- Follows Android system light/dark appearance by default.
- Uses explicit Material 3 color roles for foreground/background and component states.
- Exposes a `darkTheme` parameter for deterministic previews and tests.
- Uses platform typography fallback. No font file is bundled until Maale/Ethiopic font provenance, licence and rendering quality are verified.

## Responsive and accessibility rules for subsequent UI slices

1. Phone-first layouts must support narrow Android screens and large system font settings without clipped controls.
2. Tablet layouts may use adaptive two-pane reading/study layouts; phone navigation must remain simple.
3. Touch targets should be at least 48 dp where interactive.
4. Scripture body text must remain comfortably readable and user-scalable.
5. Color must not be the only carrier of state; include labels/icons/semantics.
6. Respect system dark mode and avoid forced brightness changes.
7. Decorative glow, blur and animation must never reduce contrast, readability, battery life or motion accessibility.
8. Screen-reader labels and logical traversal order are required for interactive controls.
9. Maale and Amharic text must be validated with actual licensed text and target-device font fallback before typography is declared production-ready.

## Explicit exclusions

- No scripture text, excerpts, audio, scans, or protected assets are added.
- No new font or icon dependency is introduced.
- No navigation, reader, search, data, or persistence behavior is changed in UI-01.
- No merge, release signing, deployment or publication is included.

## Verification record

- Source inspection: theme and token files committed on the implementation branch.
- Android build, unit tests, emulator rendering, accessibility audit, and contrast measurement: **NOT RUN** in this change.
- UI-01 must not be marked VERIFIED until those checks run against the exact branch HEAD.
