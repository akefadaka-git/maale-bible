# A3 Android Implementation Scope

**Status:** OWNER-AUTHORIZED TO PROCEED — 2026-11-07  
**Product:** Maale Bible — ጌኤዦ ማፃኣፖ  
**Source edition:** 2008 MDYTWF / mdyeth  
**Delivery model:** Offline-first Android application

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

## Implementation sequence
1. Android project, application shell, design tokens and accessible navigation.
2. Canonical 66-book catalogue and book/chapter navigation from the Owner-approved inventory.
3. Offline scripture data layer and USFM-to-domain import pipeline, only after separate content-intake approval.
4. Reader experience: typography, chapter navigation, reading position and accessibility.
5. Local full-text search, bookmarks, highlights and private notes.
6. Reading plans and progress.
7. Export/share flows preserving CC BY-SA 4.0 attribution and ShareAlike.
8. Audio only after separate audio-rights and asset approval.
9. Optional sync/account service only after separate privacy, security and backend decision.

## Hard boundaries
- Public repository must not contain raw USFM, verse text, scripture excerpts or generated scripture database.
- content_intake_authorized=false remains in force. This A3 approval authorizes app code and synthetic fixtures, not production ingestion or distribution of Maale text.
- No audio or third-party assets until separately rights-cleared.
- No backend, account system, analytics, advertising SDK or cloud sync in the initial offline foundation.
- Machine encoding checks are not linguistic validation. The accepted specialist-review exception remains tracked.
- No production release, deployment or store publication without separate Owner approval.

## Initial implementation slice
An Android app foundation, metadata-only 66-book catalogue, accessible Compose navigation, and empty states distinguishing catalogue metadata from unavailable scripture content. It must not fabricate verses or substitute another edition.

## Acceptance gates
- A3-01 Android project configuration and pinned dependencies; Gradle wrapper scripts/JAR must be present before this gate can pass.
- A3-02 Application shell supports system light/dark mode.
- A3-03 66-book catalogue matches accepted A2 inventory exactly.
- A3-04 Navigation supports Home, Books, Search and Reader placeholder.
- A3-05 Unit tests cover catalogue count, uniqueness and canonical order.
- A3-06 No raw scripture text or database committed.
- A3-07 Build/test evidence recorded before the slice is considered verified.

**Initial-slice evidence:** Kotlin/JVM domain/catalogue compilation and smoke assertions pass. The full Android build and JUnit suite have not run. Gradle wrapper properties are pinned, but wrapper scripts/JAR are missing; A3-01 and A3-02 remain unverified.

## Deferred
Real-source ingestion, production reader content, Room scripture schema, full-text index, audio, sync, backend, account/authentication, analytics, release signing and deployment remain gated.
