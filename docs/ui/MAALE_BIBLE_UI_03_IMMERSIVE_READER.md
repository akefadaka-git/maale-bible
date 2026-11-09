# UI-03 — Immersive Bible Reader

**Status:** IMPLEMENTED — verification pending  
**Branch:** `feat/ui-03-immersive-reader`  
**Parent:** `feat/ui-02-smart-home`  
**Scope:** replace the plain reader placeholder with a responsive, accessible reader shell that preserves canonical book identity and does not fabricate scripture content.

## Delivered

- Reader top bar with return navigation and current canonical book title.
- Book identity panel showing the catalogue's existing English name and USFM identifier.
- Reading typography size controls with bounded 16–32 sp state.
- Focus mode that hides nonessential reader chrome and can be exited.
- Distinct scripture-unavailable state with explicit source-integrity messaging.
- Chapter navigation unavailable state until verified book/chapter structure is integrated.
- Scrollable, centered content constrained to 760 dp for wider displays.
- Material 3 semantic colors and existing design-system shapes.

## Content integrity and source constraints

The existing Owner-approved metadata-only 66-book catalogue is the sole source for book names and USFM identifiers. This change does not add or reconstruct any Bible text, verse references, chapter counts, audio, scans, font assets, or external content. Chapter and verse navigation remain visibly unavailable until the verified and licensed corpus is integrated and parsed.

## Functional boundaries

- Back navigation returns to the existing book catalogue.
- Text-size controls adjust the reader's displayed type scale within 16–32 sp.
- Focus mode can be entered and exited.
- Bookmarking, highlighting, chapter selection, verse selection, and persistence are not represented as working features in this slice because no verified passage data or reader persistence contract is wired here.

## Verification

- Source changes committed to dedicated UI-03 branch.
- Android Gradle build, unit/instrumented tests, emulator screenshots, font-scale/device matrix, TalkBack audit, and visual regression: **NOT RUN**.
- UI-03 is not VERIFIED until the exact PR head passes the repository's supported build and test workflows.
- No merge, signing, deployment, or publication is included.
