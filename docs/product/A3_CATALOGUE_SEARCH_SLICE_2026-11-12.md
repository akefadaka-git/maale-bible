# A3 Android — Metadata Catalogue Search Slice

**Date:** 2026-11-12  
**Base:** `main` at `38d792ec1647ed52fbafdfce9f5dd294b4e47107`  
**Branch:** `feat/a3-catalog-search-2026-11-12`  
**Status:** IMPLEMENTED — exact-head Android CI pending

## Delivered

- Added deterministic, case-insensitive catalogue search by English book name or USFM book code.
- Preserved the Owner-approved 66-book canonical order.
- Replaced the Search placeholder with an offline metadata search screen.
- Connected search results to the existing reader placeholder.
- Added unit tests for blank queries, case folding, trimmed input, partial names, canonical ordering and no-result behavior.

## Data and rights boundary

This is catalogue metadata search only. It does not search, display, embed, transform or generate scripture text. The screen explicitly distinguishes catalogue search from full-text scripture search. No USFM, verse text, generated scripture database, audio or third-party assets were added.

`content_intake_authorized=false` remains in force. The A2 baseline and closure record are not modified.

## Verification

The repository's A3 Android foundation workflow is configured to run unit tests and assemble a debug APK for pull requests touching app code. Exact-head CI evidence will be recorded after the workflow completes. Until then, unit tests and APK assembly are **NOT RUN / NOT VERIFIED** for this slice.

## Governance

This branch is based on the A2-closed main commit. The change is submitted as a draft PR for review and exact-head verification. No merge, signing, release, publication or deployment is authorized by this change.
