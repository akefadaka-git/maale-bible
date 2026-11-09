# UI-02 — Smart Home

**Status:** IMPLEMENTED — verification pending  
**Branch:** `feat/ui-02-smart-home`  
**Parent foundation:** `feat/ui-01-smart-design-foundation`  
**Scope:** responsive Compose home dashboard using the approved Maale Bible UI-01 theme and tokens.

## Delivered UI

- Maale script identity header and English product lockup.
- Living Word hero panel with restrained orbital geometry and a real navigation action to the existing canonical book catalogue.
- Daily Scripture card with an explicit not-yet-loaded state. It does not fabricate a verse or reference.
- Reading journey card with a real action to browse the book catalogue; no false reading percentage or progress is shown.
- Catalogue and study-space discovery cards.
- Phone-first single-column layout and wider two-column discovery layout at 600 dp.
- Scrollable content and a bounded 760 dp content width for larger screens.
- All colors use Material 3 semantic roles or approved UI-01 brand tokens.

## Source and governance constraints

- No scripture passage, verse reference, audio, image, font, or other protected content was added.
- The daily scripture card clearly states that verified source text has not yet been integrated.
- No new dependency, permission, network access, persistence, or data behavior was introduced.
- The existing canonical book catalogue is accessed only through the pre-existing callback.
- Existing navigation, reader, search, and offline behavior are unchanged.
- No merge, signing, deployment, or publication is included.

## Responsive and accessibility considerations

- Layout adapts at a 600 dp width threshold.
- Main content scrolls vertically and is capped at 760 dp on wide screens.
- Interactive controls are Material 3 buttons with platform touch-target behavior.
- Text styles are semantic Material 3 typography roles and inherit system font scaling.
- Decorative orbital rings are non-interactive and do not carry information.
- Further TalkBack traversal, contrast, font-scale, narrow-device and tablet verification remains required.

## Verification

- Source changes committed to the dedicated UI-02 branch.
- Build, unit tests, instrumented tests, emulator screenshots, TalkBack audit, and visual regression: **NOT RUN**.
- Do not mark UI-02 VERIFIED until the exact PR head is built and exercised on representative devices.
