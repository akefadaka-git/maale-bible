# Maale Bible Android — A3 Closure Evidence Report

**Assessment date:** 14 November 2026  
**Repository:** `akefadaka-git/maale-bible`  
**PR:** [#59](https://github.com/akefadaka-git/maale-bible/pull/59)  
**Verified PR head:** `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**Documentation branch:** `docs/a3-closure-evidence-2026-11-14`

## 1. Executive disposition

**A3: PARTIAL — NOT CLOSED.** The exact-head Android CI workflow now passes, including 17 unit tests and 25 emulator instrumentation tests. This resolves the previously blocked CI verification of the Settings Activity Result registry correction. It does not satisfy the remaining product, language, rights, independent-review, and physical-device gates.

**Production readiness: NOT READY.**  
**A5 merge, signing, deployment, release, and publication: NOT AUTHORIZED / NOT STARTED.**

This report records evidence and remaining closure conditions; it is not an approval to merge or release.

## 2. Exact-head Android verification

### Workflow evidence

- Workflow run: [#43468081954](https://github.com/akefadaka-git/maale-bible/actions/runs/43468081954)
- Run attempt: 2
- Exact tested SHA: `58eb32acc10415e3dfa6683a5342b6da824d5d27`
- Conclusion: **SUCCESS**
- Job: `android-foundation` — **SUCCESS**
- Unit tests: **17 passed, 0 failures, 0 skipped (100%)**
- Emulator instrumentation: **25 passed, 0 failures, 0 skipped (100%)**
- Debug APK assembly: **PASS**
- Instrumentation command: `:app:connectedDebugAndroidTest`
- Instrumentation duration: 1m 34.25s
- Gradle instrumentation result: `OK (25 tests)`
- Test-result exit code: `0`

Artifact: [#14059319376 — Android debug APK and test reports](https://github.com/akefadaka-git/maale-bible/actions/runs/43468081954/artifacts/14059319376)

- Artifact size: 18,640,814 bytes
- SHA-256: `1f1149e977807f3848adf74bb32560da92322d51dcd8cac397b02502af3eea8b`
- Local downloaded archive SHA-256 independently recomputed: same value.
- Archive contains the debug APK, Gradle unit-test HTML report, instrumentation HTML report, JUnit XML, instrumentation text log, test-result protobuf/textproto, and per-test logcat captures.

The previous attempt failed before completing instrumentation because the Android Emulator SDK archive/ADB setup failed. The successful second attempt completed emulator startup, executed all 25 tests, and uploaded the artifact. The earlier infrastructure failure is not counted as an application test failure.

### JUnit and test-result evidence

JUnit XML:
`outputs/androidTest-results/connected/debug/TEST-emulator-5554 - 15-_app-.xml`

- `tests=25`
- `failures=0`
- `errors=0`
- `skipped=0`

Gradle reports independently show:

- Unit report: 17 tests, 0 failures, 0 skipped, 100% successful.
- Instrumentation report: 25 tests, 0 failures, 0 skipped, 100% successful.

### Settings Activity Result registry correction

The Settings test `settingsExposeAccessibilityAndPortableBackupControls` completed and passed on the exact tested head. Its logcat ends with the test runner reporting it finished; no Activity Result registry exception or Maale Bible fatal exception was observed in that test capture.

Important limitation: the automated test verifies Settings accessibility and that backup/restore controls are exposed. It does **not** constitute complete document-picker export/import acceptance, nor does it prove all picker lifecycle/cancellation flows.

### R6 bookmark persistence

The passing test `bookReaderBookmarkAndStudyHubFlowIsConnected` produced this diagnostic:

```
R6_DIAGNOSTIC: stage=reader; persistedCount=1; bookIds=[GEN];
savedLanguage=en; activityLocales=en-US; expectedResourceText=Bookmarks (1)
```

This confirms one persisted Genesis bookmark at the independent database query point. The same test passed the Reader-to-Study flow and Study assertions. Restart/reopen, backup/restore, encryption, migration, rollback, fault-injection, and recovery-key-isolation instrumentation tests also passed in this run.

The logcat contains Android emulator and Google system-service warnings, but no Maale Bible fatal exception or SQLite/SQLCipher failure associated with these passing tests.

## 3. Localization and language QA

### Amharic

- Default resource keys checked: 125.
- Amharic resource keys checked: 125.
- Arabic-script values previously present in the Amharic resource file were replaced.
- Placeholder consistency check: PASS.
- Automated language selection/activity recreation test: PASS.

**Remaining:** qualified Amharic linguistic review, terminology consistency review, visual review at supported font scales, and full-screen QA.

### Maale

- Default resource keys checked: 125.
- Maale resource keys checked: 60.
- Default keys without a Maale translation: 65.
- Approximate UI string coverage: 48%.
- Bookmark-count placeholder corrected and placeholder consistency checked.
- Automated test confirms Maale is the default app language and language selection persists across Activity recreation.

**PARTIAL:** the remaining 65 strings still fall back to English. Canonical Maale book names, UI terminology, spelling/orthography, and scripture display require a qualified Maale language reviewer. The passing locale-persistence test is not linguistic QA.

## 4. Authorized scripture package and content boundary

Authorized candidate source: **2008 Maale Full Bible — MDYTWF / mdyeth**.

Previously recorded source checks:

- Four archived copies compared; identical SHA-256:
  `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`
- Archive size: 1,809,863 bytes.
- ZIP entries: 70.
- USFM files: 66.
- Signed checksum entries: 68/68 verified.
- Unique book IDs: 66.
- Chapters: 1,189.
- Verse-marker lines: 30,506.
- Strict UTF-8 decoding: PASS.
- NFC normalization observation: PASS.
- Replacement characters: 0.
- Unexpected control/format characters: 0.
- Duplicate book IDs: 0.
- Structural anomalies detected: 0.
- The source's GPG signature is cryptographically valid; the signing key's identity trust is **UNDEFINED**. Cryptographic validity is not independent identity certification.
- The 66 book IDs match the recorded 66-book inventory and application catalogue order.

The public repository content boundary check found no raw USFM files, source ZIP, scripture excerpts, or generated scripture database in the inspected repository tree. Source metadata and hash manifests are present.

**Implementation gap:** the app still has a reader placeholder/metadata catalogue; the full authorized scripture text, offline reader integration, and offline full-text search are not yet complete.

The source's stated CC BY-SA 4.0 terms require a release-specific compliance review. Source permission for one edition does not automatically authorize other editions, translations, maps, fonts, illustrations, audio, or third-party study resources.

## 5. Security and backup/restore

Implemented controls and prior code review evidence include:

- SQLCipher-encrypted study database.
- Device-bound database key wrapping using Android Keystore and AES-GCM.
- Portable backup envelope using Argon2id key derivation and AES-GCM authenticated encryption.
- Bounded backup-envelope input.
- Recovery on an empty destination database.
- Refusal to overwrite an existing destination database.
- `android:allowBackup="false"`.

The exact-head instrumentation run passed tests covering:

- Encrypted backup round trip and plaintext non-disclosure checks.
- Tamper rejection.
- Database close/reopen persistence.
- Backup/restore round trip and invalid-payload rejection.
- Database migration and content equivalence.
- Fault-injected migration rollback and recovery.
- Legacy plaintext-to-encrypted migration.
- Portable recovery without the originating Keystore.
- Wrong recovery-key rejection.
- Existing destination database protection.

**Remaining:** full UI-level document-picker export/import acceptance, cancellation and Activity recreation tests, recovery-key handling/usability review, independent security assessment, and remediation/retest of any independent findings.

**Independent security review: MISSING.** Internal code inspection and CI are not substitutes for a reviewer independent of implementation.

## 6. Physical-device and accessibility acceptance

| Acceptance area | Status |
|---|---|
| Exact-head unit tests | PASS — 17/17 |
| Exact-head emulator instrumentation | PASS — 25/25 |
| Settings screen and backup/restore controls visible | PASS — emulator test |
| Maale default locale and locale persistence | PASS — automated test |
| Physical Android device installation and acceptance | NOT RUN |
| Real-device document-picker export/import | NOT RUN |
| Backup cancellation and Activity recreation on device | NOT RUN |
| TalkBack end-to-end acceptance | NOT RUN |
| Font scaling / display-size acceptance on device | NOT RUN |
| Amharic linguistic sign-off | NOT RUN |
| Maale linguistic sign-off | NOT RUN |

## 7. Independent reviewer requests

Two separate independent reviews are required. Neither has been commissioned or completed by this report.

### Language review

SIL Ethiopia publicly describes Bible translation advisory support and independent consultant checking. The outreach draft is addressed to its published information/partnership contact. The request is to confirm whether an appropriately qualified Maale language consultant can independently review the Maale UI strings, book names, orthography, and representative scripture rendering, or refer the project to a suitable reviewer.

Official contact: [SIL Ethiopia contact page](https://ethiopia.sil.org/contact-us/) — `info_ethiopia@sil.org`.

### Android security review

The outreach draft is addressed to OWASP Foundation support with a request to route it to OWASP Addis Ababa chapter leadership or a qualified independent mobile application security reviewer. The requested assessment scope includes Android application security, local encrypted storage, Android Keystore key handling, portable backup/recovery, document-provider flows, data leakage, and lifecycle/error handling. Review should use OWASP MASVS/MASTG as a baseline and deliver a written findings report with severity, evidence, remediation recommendations, and retest status.

- [OWASP Addis Ababa chapter](https://owasp.org/chapters/addis-ababa)
- [OWASP Foundation contact](https://owasp.org/contact) — `support@owasp.org`
- [OWASP MASVS / MASTG](https://mas.owasp.org/)

These addresses are routing contacts, not confirmation that a reviewer has accepted the work.

## 8. A3 closure checklist

1. **Settings Activity Result registry:** implementation correction present; exact-head automated Settings test PASS. Full document-picker acceptance remains open.
2. **Amharic localization:** resource coverage PASS; specialist QA outstanding.
3. **Maale localization:** PARTIAL; 65/125 default strings remain untranslated; specialist QA outstanding.
4. **Authorized scripture package:** archive/hash/structure checks recorded; source identity trust and final rights sign-off outstanding.
5. **Scripture integration:** NOT COMPLETE; full reader content and offline search remain outstanding.
6. **Security and backup/restore:** automated exact-head coverage PASS; full UI/device acceptance and independent security review outstanding.
7. **Independent security review:** MISSING.
8. **Physical device acceptance:** NOT RUN.
9. **TalkBack/accessibility acceptance:** NOT RUN.
10. **A3 closure review:** BLOCKED pending the above gates and evidence.

## 9. Stage and governance status

- A1 scope and acceptance baseline: **CLOSED**.
- A2 source reconciliation: **CLOSED**, retaining its documented specialist-review exception.
- A3 implementation: **PARTIAL — NOT CLOSED**.
- A4 exact-head Android CI verification: **PASS for SHA `58eb32acc10415e3dfa6683a5342b6da824d5d27`**. This is not a substitute for device acceptance.
- A5 merge/deployment/signing/release/publication: **NOT STARTED; separate explicit Owner approval required**.

PR #59 remains open, draft, and unmerged at the verified head when this report was prepared. The report is stored on a separate documentation branch so PR #59's implementation head and CI evidence are not changed by this documentation commit. The default branch `main` is untouched.

## 10. Final decision

**Do not close A3.** Exact-head automated Android verification now passes, including the Settings screen test and R6 bookmark persistence. The project remains not production-ready until the remaining language, scripture-content, rights, independent-security, physical-device, and accessibility gates have evidence-backed closure.

No merge, signing, deployment, release, or publication is authorized by this report.
