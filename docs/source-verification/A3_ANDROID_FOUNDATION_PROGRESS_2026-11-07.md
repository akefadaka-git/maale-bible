# Maale Bible — A3 Android Foundation Progress Report

**Original report date:** 2026-11-07
**Latest verification update:** 2026-11-08  
**Branch:** `feat/a3-android-offline-foundation`  
**Current reviewed branch head before this report revision:** `db9d5196790f52c931d5e98cdc3a7ff1ac3811d2`  
**A3 implementation/reconciliation commit:** `d55e966aed98b18d65c9c5e2265c61be6973c787`  
**A2 base commit incorporated:** `b012a4c0e47ca8d22f4b72b2e08b3d58585a3071`  
**PR:** [#3 — A3 Android foundation](https://github.com/akefadaka-git/maale-bible/pull/3)  
**Latest disposition (2026-11-08):** A3 foundation is implemented but remains PARTIAL. Two remote GitHub Actions builds were executed against source SHA `ef1bf242d7f240222b38624f0cd0088eacae6bbc`; both failed during Kotlin Android plugin application because AGP 9's new DSL is enabled while the legacy `org.jetbrains.kotlin.android` plugin is applied. JUnit and APK assembly did not run. PR remains draft/unmerged.

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

The initial documentation comparison was inconclusive; the subsequent current-official-documentation review in **Section 8** resolves the AGP/Gradle compatibility question: AGP 9.5.0 is the documented stable release and requires Gradle 9.6.0, JDK 17 and supports API 37. Section 8 also records the Activity/Lifecycle AndroidX pin corrections. Actual Gradle plugin/dependency resolution remains **NOT RUN**, so source documentation does not constitute a successful build or artifact-resolution result.

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


## 9. Remote GitHub Actions verification — 2026-11-08 update

**Verification date:** 2026-11-08 (workflow runs executed 2026-11-07 UTC)  
**Exact application source SHA tested:** `ef1bf242d7f240222b38624f0cd0088eacae6bbc`  
**Feature branch at time of test:** `feat/a3-android-offline-foundation`

### Run evidence

Two final runs were executed by the repository's one-off, SHA-pinned workflow:

| Run | Workflow commit | Result | Job |
|---|---|---|---|
| [#5](https://github.com/akefadaka-git/maale-bible/actions/runs/42389109110) | `848f93407d3c8a32c508f8a4df7d344a942c7e4d` | FAILURE | `128452722436` |
| [#6](https://github.com/akefadaka-git/maale-bible/actions/runs/42389128766) | `a3c454e4dff1c387b187ca9ebcb6a20cac97d093` | FAILURE | `128453043581` |

Both jobs successfully:
- checked out the exact approved application source SHA;
- asserted the immutable SHA;
- installed JDK 17;
- configured Android SDK tooling;
- installed Android platform 36 and build-tools 36.0.0;
- verified the Gradle wrapper and Gradle 9.6.0 distribution pin.

Both failed at the `Run unit tests and assemble debug APK` step before JUnit execution or APK assembly. The artifact-upload step was skipped. The artifact API returned no artifacts for either run.

### Confirmed failure cause

The Gradle logs for both runs identify the same configuration error at `app/build.gradle.kts` line 1:

```
Failed to apply plugin 'org.jetbrains.kotlin.android'.
The 'org.jetbrains.kotlin.android' plugin is not compatible with
AGP's 9.0 new DSL (android.newDsl=true is enabled by default).
```

Current project configuration at the tested SHA:
- AGP: 9.5.0
- Kotlin Android plugin: 2.4.10
- Compose compiler plugin: 2.4.10
- `gradle.properties`: `android.builtInKotlin=false`
- `gradle.properties`: no `android.newDsl=false` override
- App module applies `org.jetbrains.kotlin.android`.

This is a real AGP/Kotlin plugin configuration incompatibility. It is not an SDK installation, checkout, wrapper, or source-SHA mismatch. The earlier API 37 to API 36 correction allowed the workflow to proceed to Gradle configuration, exposing this next blocker.

### Recommended remediation and verification

The Kotlin Gradle Plugin compatibility table currently lists Kotlin 2.4.0–2.4.10 as fully supported through AGP 9.1.0, not AGP 9.5.0. The current Android documentation's AGP 9.5 example and Kotlin's narrower KGP compatibility matrix therefore need to be reconciled before pinning a new version combination.

A low-impact temporary compatibility path identified by the build diagnostic is to set `android.newDsl=false` while retaining the existing Kotlin Android plugin; the longer-term AGP 9 path is migration to AGP built-in Kotlin, removing the legacy Kotlin Android plugin and validating Compose compiler configuration. The project should not claim build readiness until one chosen configuration passes the full Gradle/JUnit/APK gates.

References:
- Kotlin Gradle configuration and compatibility matrix: https://kotlinlang.org/docs/gradle-configure-project.html
- Android AGP 9 migration/release notes: https://developer.android.com/build/releases/agp-9-0-0-release-notes
- Kotlin/JetBrains AGP 9 migration guidance: https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/

### Updated gate status

| Gate | Result |
|---|---|
| Exact source checkout and SHA assertion | PASS |
| JDK 17 setup | PASS |
| Android SDK platform/build-tools 36 setup | PASS |
| Gradle wrapper verification | PASS |
| Gradle project configuration | FAIL — AGP/Kotlin Android plugin DSL incompatibility |
| JUnit unit tests | NOT RUN |
| `assembleDebug` / APK generation | NOT RUN |
| APK artifact upload / digest | NOT AVAILABLE |
| PR #3 | OPEN, DRAFT, UNMERGED; mergeable reported true at latest check |
| Deployment / release | NOT AUTHORIZED; not performed |

**A3 disposition remains PARTIAL / BLOCKED at the Android build gate.** No claim of a successful Android build, passing JUnit suite, or generated APK is made. No scripture content was imported, and `content_intake_authorized=false` remains unchanged.
