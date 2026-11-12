# Maale Bible — Implementation and Deployment Timeline

**Planning baseline:** 2026-11-07  
**Repository:** `akefadaka-git/maale-bible`  
**Implementation branch:** `feat/a3-android-offline-foundation`  
**Current tracked PR:** [#3 — A3 Android foundation](https://github.com/akefadaka-git/maale-bible/pull/3)  
**Schedule status:** PROVISIONAL — estimates, not committed delivery dates.

## Executive target

- **A3 Android foundation verification:** 2026-11-13
- **Feature-complete candidate:** 2026-12-18
- **Release candidate and deployment preparation:** 2026-12-25
- **Provisional production deployment target:** 2027-01-08

The production target is conditional on successful Android build/test verification, Owner authorization for scripture content intake, content/rights checks, release acceptance and explicit Owner approval to publish. If any gate is not satisfied, deployment moves; the date must not override the gates.

## Milestone schedule

| Phase | Planned window | Deliverables | Exit evidence / gate |
|---|---|---|---|
| A3 — Foundation verification | Nov 9–13, 2026 | Resolve Android Gradle Plugin/dependency availability; execute Gradle configuration, JUnit and debug APK build; record toolchain and APK SHA-256 | Actual Gradle resolution, tests and APK build pass on exact reviewed SHA; evidence committed |
| A4.1 — Content architecture | Nov 16–20 | Metadata/content domain model, local database design, import pipeline contract, synthetic fixtures and migration tests | Schema and importer tests pass using synthetic data only; no real scripture content imported |
| A4.2 — Reader experience | Nov 23–27 | Reader layout, book/chapter navigation, typography, reading position, accessibility and explicit unavailable-content states | UI/unit tests and Android build pass; no fabricated or substituted scripture |
| A4.3 — Personal reading tools | Nov 30–Dec 4 | Offline search implementation, bookmarks, highlights and private notes | Local persistence/search tests pass; offline behavior verified |
| A4.4 — Plans and sharing | Dec 7–11 | Reading plans, progress tracking, export/share with required CC BY-SA attribution and ShareAlike handling | Functional tests and rights/attribution review pass |
| A4.5 — Quality and hardening | Dec 14–18 | Regression, accessibility, offline, data integrity, performance, privacy and security checks; defect closure | Release-blocking defects closed; full verification report and known-risk register |
| A5 — Release candidate | Dec 21–25 | Versioning, app icon/metadata, signing configuration, privacy/store disclosures, release notes and reproducible release artifact | Owner review of release candidate; signing and publication remain unauthorized until explicitly approved |
| Deployment window | Dec 28, 2026–Jan 8, 2027 | Approved production/store submission and staged rollout | Separate explicit Owner deployment approval; store/production acceptance evidence and rollback plan |

## Critical dependencies and non-negotiable gates

1. **Build environment:** A3 cannot close as verified until a real Gradle invocation resolves the declared Android Gradle Plugin and dependencies, the JUnit suite runs, and an Android debug APK is assembled. Static inspection and Kotlin/JVM smoke checks do not substitute for these gates.
2. **Scripture content:** `content_intake_authorized=false` remains in force. The schedule does not authorize importing, transforming, storing, committing or distributing the Maale scripture text. A4.1–A4.4 can progress with schemas, metadata and synthetic fixtures, but a complete offline Bible and meaningful reader/search acceptance require separate Owner content-intake authorization and the approved 2008 MDYTWF / mdyeth source.
3. **Edition integrity:** Do not substitute the 2007 Maale NT or another edition for the 2008 full Bible.
4. **Rights:** Preserve CC BY-SA 4.0 attribution, modification notices and ShareAlike obligations. Audio and third-party assets require separate rights/asset approval.
5. **Language review:** Maale-language/Ethiopic-script specialist review remains deferred as an accepted residual risk. No linguistic sign-off may be claimed.
6. **Release governance:** No merge, production deployment, store submission, release publication or content intake is authorized by this schedule. Each remains subject to its applicable Owner approval.
7. **CI:** No GitHub Actions workflow is dispatched by this plan. A local or otherwise authorized real build environment is required for A3 evidence.

## Immediate execution order

1. Recheck the exact A3 PR head and repository state before changing files.
2. Obtain actual Gradle 9.6.0 distribution and resolve AGP 9.5.0 plus declared dependencies; do not change version pins without evidence.
3. Run `./gradlew --version`, `./gradlew test` and `./gradlew assembleDebug` on a compatible network-enabled Android environment (JDK 17 and Android SDK API 37/build tools).
4. If dependency resolution or compatibility fails, record the exact failure and correct only the demonstrated incompatibility on the feature branch.
5. Record exact source SHA, JDK/Gradle/SDK versions, resolved plugin/dependency versions, test results, APK path/size/SHA-256 and any blockers in the repository.
6. Only after A3 verification, advance to A4.1 using synthetic fixtures until separate scripture content-intake authorization is granted.

## Schedule control and reporting

- This document is a planning baseline, not a delivery guarantee or authorization to bypass approval gates.
- Reforecast the remaining dates when A3 build evidence is available and whenever a critical dependency changes.
- Report each milestone as **PASS**, **VERIFIED-CLOSED**, **PARTIAL**, **MISSING**, **UNRESOLVED**, **UNKNOWN** or **NOT RUN**, with exact commit SHA and evidence.
- Preserve unrelated changes and existing implementation. Keep PR #3 draft/unmerged until the applicable review and approval gates are satisfied.
