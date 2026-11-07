# Maale Bible — A3 Android Foundation Progress Report

**Date:** 2026-11-07  
**Branch:** `feat/a3-android-offline-foundation`  
**Current reviewed branch head before this report revision:** `db9d5196790f52c931d5e98cdc3a7ff1ac3811d2`  
**A3 implementation/reconciliation commit:** `d55e966aed98b18d65c9c5e2265c61be6973c787`  
**A2 base commit incorporated:** `b012a4c0e47ca8d22f4b72b2e08b3d58585a3071`  
**PR:** [#3 — A3 Android foundation](https://github.com/akefadaka-git/maale-bible/pull/3)  
**Disposition:** A3 foundation source is committed; Android build and JUnit gates remain unverified; PR remains draft/unmerged.

## 1. Changes and commit evidence

### Gradle Wrapper foundation
The branch contains:
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew` (POSIX executable mode 100755)
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`, pinning `https://services.gradle.org/distributions/gradle-9.6.0-bin.zip`.

Wrapper source was obtained from the Gradle `v9.6.0` release tag. Repository inspection confirms all wrapper files and the distribution pin are present. The earlier scope-document statement that scripts/JAR were missing was stale; it has been corrected in `docs/product/A3_ANDROID_IMPLEMENTATION_SCOPE.md`.

### Android foundation retained
- Android Gradle Plugin pin: 9.5.0; Kotlin and Compose compiler plugin pins: 2.4.10.
- Compose BOM 2026.09.00; Activity Compose 1.14.0; Lifecycle 2.12.0.
- compile SDK 37; target SDK 36; minimum SDK 26; JVM 17.
- Kotlin Book domain model and repository interface.
- Metadata-only canonical 66-book catalogue for the 2008 MDYTWF / mdyeth edition.
- Compose shell with Home, Books, Search and Reader placeholder screens.
- Material 3 light/dark theme follows the system theme.
- Catalogue JUnit tests cover count, uniqueness, canonical order and lookup.

No scripture text, USFM source, scripture excerpts or generated scripture database were added.

## 2. A2 base reconciliation

The A3 feature branch had been five commits behind the A2 documentation branch. A two-parent merge commit incorporated the current A2 base while preserving the A3 implementation history and files.

Merge commit: `d55e966aed98b18d65c9c5e2265c61be6973c787`  
Tree: `59cf39e42704d856af065907ddcb9560563d8e25`  
Parents:
1. A3 feature head: `2dd6ddbd57649dbb34788450b15aabf70e0cc9c2`
2. A2 base head: `b012a4c0e47ca8d22f4b72b2e08b3d58585a3071`

The merge retained the A2 README updates, A2 scope status, A2 completion register and the complete real-source validation report. The merge-base comparison previously confirmed the A3 branch was 22 commits ahead and 0 behind the A2 base.

## 3. Verification results

| Gate | Result | Evidence / limitation |
|---|---|---|
| Wrapper JAR, POSIX/Windows launchers and 9.6.0 pin | VERIFIED | All four repository paths inspected on the feature branch |
| Material 3 system light/dark theme selection | VERIFIED (source) | `isSystemInDarkTheme()` selects light/dark Material 3 schemes; runtime not exercised |
| 66-book inventory and unique IDs | PASS (prior execution) | Exact inventory comparison against Owner-approved list |
| Kotlin/JVM domain/catalogue compile | PASS (prior execution) | Previously executed with kotlinc |
| Catalogue smoke assertions | PASS (prior execution) | 66 entries, canonical order, GEN/REV/unknown lookup |
| JUnit test suite | NOT RUN | No successful Gradle test execution |
| Full Android Gradle build | BLOCKED / NOT RUN | No Gradle distribution cache, Android SDK or outbound DNS/network in the available execution container |
| GitHub Actions | NOT RUN | No workflow dispatched |
| Commit status checks | NONE REPORTED (last checked) | No status entries were reported for the reviewed SHA at the last status check |

### Build environment evidence
The isolated execution container previously reported:
- Java: OpenJDK 21.0.12.1.
- Kotlin compiler: kotlinc-jvm 1.9.0.
- No Android SDK found in the checked standard locations.
- No cached Gradle wrapper distribution.
- Outbound DNS/network resolution unavailable; GitHub clone attempt failed with `Could not resolve host: github.com`.

Consequently, the Gradle distribution could not be downloaded, dependencies could not be resolved and no APK build could be run. The wrapper's presence and pin are verified, but Gradle execution, plugin/dependency resolution and Android project configuration remain unverified. This is not a build pass.

### Dependency-version compatibility review
The checked-in root build pins Android Gradle Plugin (AGP) 9.5.0 and the wrapper pins Gradle 9.6.0. Android's AGP 9.5.0 release-notes page lists Gradle 9.6.0 as its minimum/default Gradle version and API 37 as its maximum supported API level: https://developer.android.com/build/releases/agp-9-5-0-release-notes

However, the Android Gradle Plugin API reference last updated 2026-10-26 lists 9.4.1 as the current release and 9.5.0-rc02 as a preview: https://developer.android.com/reference/tools/gradle-api

These official pages are not fully aligned about the release channel. Therefore the AGP 9.5.0 coordinate and all Android plugin/dependency artifacts still require actual repository resolution. **Do not treat the declared AGP version as verified stable or the build as reproducible until a real Gradle configuration/build resolves it.** No version was changed without resolution evidence.

## 4. PR and release status

- PR #3 remains OPEN and DRAFT.
- PR #3 remains UNMERGED.
- The PR description records the A2 reconciliation and verification limitations.
- No CI/workflow was dispatched.
- No merge into `main`, production deployment, release signing or publication was performed.

## 5. Rights and content boundaries

- `content_intake_authorized=false` remains in force.
- Source archive and all scripture text remain outside the public repository.
- Specialist Maale-language/Ethiopic-script review remains deferred as an Owner-accepted residual risk; no specialist sign-off is claimed.
- Audio and third-party assets, backend, accounts, analytics, sync, release signing and production deployment remain separately gated.

## 6. Required next verification

On a network-enabled Android build environment with JDK 17 and Android SDK platform/build-tools for API 37:
1. `./gradlew --version`
2. `./gradlew test`
3. `./gradlew assembleDebug`

Record exact Gradle/JDK/SDK versions, plugin/dependency resolution, test counts and APK output digest. Do not dispatch GitHub Actions without separate authorization.


## 7. Environment recheck after timeline recording

**Recheck date:** 2026-11-07  
**Reviewed feature-branch SHA before this report update:** `8a6e515f005b978cedd109f4d14762f961aadffa`

A fresh execution-container check was performed after the implementation timeline was committed:
- Java runtime: OpenJDK 21.0.12.1.
- System Gradle: **NOT AVAILABLE** (`gradle: command not found`).
- Android SDK: **NOT FOUND** in the configured environment variables or checked standard locations.
- Gradle distribution endpoint DNS: **UNRESOLVED** (`curl: (6) Could not resolve host: services.gradle.org`).

Therefore the wrapper cannot download Gradle 9.6.0 in this environment, and Android dependency resolution, JUnit execution and APK assembly remain **BLOCKED / NOT RUN**. No GitHub Actions workflow was dispatched. No dependency pins were changed without artifact-resolution evidence.

**Disposition:** A3 remains **PARTIAL**. The next technical gate is a real Gradle/Android build in a network-enabled compatible environment; this execution attempt does not satisfy that gate.


## 8. Dependency compatibility review and targeted correction

**Review date:** 2026-11-07  
**Source commit before this report update:** `d1561d6572b2828275c87af279c565efe8c77b7b`

A fresh review of the exact Gradle files and current official release documentation found two AndroidX version declarations that were not valid stable-release pins:

- `androidx.activity:activity-compose:1.14.0` — official Activity release notes list 1.13.0 as stable and 1.14.0-alpha03 as the alpha track. The dependency was corrected to stable **1.13.0**.
- `androidx.lifecycle:lifecycle-runtime-ktx:2.12.0` — official Lifecycle release notes list 2.11.0 as stable and 2.12.0-alpha05 as the alpha track. The app source does not use this artifact directly, so the unused explicit dependency was removed rather than introducing an unnecessary alpha or additional library.

The other declared baseline versions have matching official release documentation:
- AGP 9.5.0 is documented as the current stable example/release; it requires Gradle 9.6.0, JDK 17 and supports API 37.
- Gradle 9.6.0 is an official stable Gradle release.
- Kotlin Gradle plugin 2.4.10 is published on the Gradle Plugin Portal.
- Compose BOM 2026.09.00 is documented by Android Developers.

References:
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/build/releases/agp-9-5-0-release-notes
- https://gradle.org/releases/
- https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android/2.4.10
- https://developer.android.com/develop/ui/compose/bom
- https://developer.android.com/jetpack/androidx/releases/activity
- https://developer.android.com/jetpack/androidx/releases/lifecycle

**Changed file:** `app/build.gradle.kts` only.  
**Dependency correction commit:** `d1561d6572b2828275c87af279c565efe8c77b7b`.

This is a documentation-backed compatibility correction, not a successful dependency resolution. No Gradle invocation could be run in the current execution environment. Artifact resolution, JUnit, Android build and APK generation remain **NOT RUN / BLOCKED** pending a real Android build host.
