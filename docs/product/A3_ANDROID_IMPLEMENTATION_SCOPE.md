# A3 Android Implementation Scope

**Status:** OWNER-AUTHORIZED TO PROCEED — 2026-11-07  
**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Source edition:** 2008 MDYTWF / mdyeth  
**Delivery model:** Offline-first Android application

## Current authority addendum — 2026-11-14

This document records the narrower A3 foundation scope approved on 2026-11-07. Its original `content_intake_authorized=false` statement reflected the authority state at that time and is superseded for current project-level content-intake authority by the Owner's 2026-11-08 authorization, reconciled in [Owner Content-Intake Authority Reconciliation](../licensing/OWNER_CONTENT_INTAKE_AUTHORITY_RECONCILIATION_2026-11-14.md). Controlled intake/evaluation is authorized for Maale, Amharic, English and necessary Bible/study resources, subject to exact-source rights, attribution, integrity, compatibility and quality gates. This does not authorize blanket packaging/distribution, public corpus commits, merge, signing, deployment or publication. Historical A3 acceptance criteria and implementation evidence below remain a record of the initial foundation slice; the complete current product scope is the [Product Requirements Baseline v1.0](PRODUCT_REQUIREMENTS_BASELINE_v1.0_2026-11-14.md).

## Objective
Build the Android application foundation and incrementally deliver a usable offline-first Bible reading experience. The named Maale-language/Ethiopic-script specialist review is explicitly deferred as an accepted residual risk.

## Approved technical baseline
- Kotlin and Android Gradle Plugin.
- Jetpack Compose + Material 3.
- Clean Architecture boundaries: presentation, domain and data.
- Repository interfaces isolate UI/domain from persistence and source-format details.
- Local-first storage; Room/SQLite is intended for a later persistent-data slice.
- Local search must work without network connectivity.
- Android minimum API 26; compile SDK 37; target SDK 36; JVM 17.
- Gradle Wrapper 9.6.0 is committed, with POSIX and Windows launchers and the wrapper JAR.

## Implementation sequence
1. Android project, application shell, design tokens and accessible navigation.
2. Canonical 66-book catalogue and book/chapter navigation from the Owner-approved inventory.
3. Offline scripture data layer and USFM-to-domain import pipeline, under the later Owner content-intake authority and only after source-specific rights, quality and integrity gates pass.
4. Reader experience: typography, chapter navigation, reading position and accessibility.
5. Local full-text search, bookmarks, highlights and private notes.
6. Reading plans and progress.
7. Export/share flows preserving CC BY-SA 4.0 attribution and ShareAlike.
8. Audio only after separate audio-rights and asset approval.
9. Optional sync/account service only after separate privacy, security and backend decision.

## Hard boundaries
- Public repository must not contain raw USFM, verse text, scripture excerpts or generated scripture database.
- The original `content_intake_authorized=false` was the status when this scope was approved on 2026-11-07; it is superseded for current controlled intake by the 2026-11-08 Owner authorization, as reconciled on 2026-11-14. Production packaging/distribution remains source-rights-gated.
- No audio or third-party assets until separately rights-cleared.
- No backend, account system, analytics, advertising SDK or cloud sync in the initial offline foundation.
- Machine encoding checks are not linguistic validation. The accepted specialist-review exception remains tracked.
- No production release, deployment or store publication without separate Owner approval.

## Initial implementation slice
An Android app foundation, metadata-only 66-book catalogue, accessible Compose navigation, and empty states distinguishing catalogue metadata from unavailable scripture content. It must not fabricate verses or substitute another edition.

## Acceptance gates
- A3-01 Android project configuration and pinned dependencies; wrapper scripts/JAR must be present.
- A3-02 Application shell supports system light/dark mode.
- A3-03 66-book catalogue matches accepted A2 inventory exactly.
- A3-04 Navigation supports Home, Books, Search and Reader placeholder.
- A3-05 Unit tests cover catalogue count, uniqueness and canonical order.
- A3-06 No raw scripture text or database committed.
- A3-07 Build/test evidence recorded before the slice is considered verified.

## Current evidence and limitations
- Wrapper JAR, POSIX launcher, Windows launcher and Gradle 9.6.0 distribution pin are present on the A3 branch: **VERIFIED (repository inspection)**.
- Material 3 system light/dark theme selection is present in source: **VERIFIED (source inspection)**; Android runtime rendering is **NOT RUN**.
- The A3-01 configuration is committed, but plugin/dependency resolution and full Android project configuration are **NOT VERIFIED** because no Gradle build has run.
- The JUnit suite and full Android Gradle build remain **NOT RUN**.
- The earlier statement that wrapper scripts/JAR were missing is obsolete and is corrected by this revision.

## Deferred
Real-source ingestion, production reader content, Room scripture schema, full-text index, audio, sync, backend, account/authentication, analytics, release signing and deployment remain gated.
