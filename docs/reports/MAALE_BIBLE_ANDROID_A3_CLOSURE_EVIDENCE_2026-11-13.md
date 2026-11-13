# Maale Bible Android — A3 Closure Evidence Report

**Report date:** 13 November 2026  
**Repository:** `akefadaka-git/maale-bible`  
**Pull request:** [#59](https://github.com/akefadaka-git/maale-bible/pull/59)  
**PR head assessed:** `58eb32acc10415e3dfa6683a5342b6da824d5d27`  
**Documentation branch:** `docs/a3-closure-evidence-2026-11-13`

## 1. Overall disposition

**A3: PARTIAL — NOT VERIFIED-CLOSED**

The Android foundation, core navigation, encrypted study storage, portable backup implementation, and automated emulator verification have substantial evidence. Localization, production scripture integration, complete licence controls, independent security review, and physical-device/accessibility acceptance remain incomplete.

**Production readiness: NOT READY.**

## 2. Android implementation and exact-head verification

### Settings Activity Result registry correction

- `MainActivity` now explicitly supplies the Activity Result registry owner to the Compose tree.
- Correction commit: `33c89a45bfb004c0a731c29bbb9ecc57a13c6eb9`.
- The correction is included in the assessed PR head.

### Exact-head workflow result

[Android verification — run #43468081954](https://github.com/akefadaka-git/maale-bible/actions/runs/43468081954)

- Exact SHA: `58eb32acc10415e3dfa6683a5342b6da824d5d27`
- Latest run attempt: 2
- Workflow conclusion: **SUCCESS**
- Unit tests: **17 passed, 0 failed, 0 skipped**
- Instrumentation tests: **25 passed, 0 failures, 0 errors, 0 skipped**
- Instrumentation result: `OK (25 tests)`
- Test exit code: `0`
- Debug APK assembly: PASS
- Settings control test `settingsExposeAccessibilityAndPortableBackupControls`: PASS
- Reader-to-Study bookmark flow: PASS
- Emulator restart, encrypted migration, backup/restore, recovery, tamper and rollback coverage: PASS within the 25-test suite.

The first instrumentation attempt failed before app tests because the Android Emulator SDK archive setup failed and ADB refused the connection. The same workflow job was retried; attempt 2 completed successfully on the unchanged exact SHA. The infrastructure failure is retained as run history, not treated as an application test failure.

### Artifact evidence

[Successful retry artifact #14059319376](https://github.com/akefadaka-git/maale-bible/actions/runs/43468081954/artifacts/14059319376)

- Artifact SHA-256: `1f1149e977807f3848adf74bb32560da92322d51dcd8cac397b02502af3eea8b`
- APK SHA-256: `fe944e317b57baa8f54c0b1d96e7b8a8cc200c98142c3479b57d260d3c35150d`
- APK size: 22,440,386 bytes.
- Artifact includes JUnit XML, instrumentation textproto, instrumentation test log, per-test logcat, unit-test reports and debug APK.

The JUnit XML reports 25 tests, zero failures, zero errors and zero skipped. The unit-test aggregate reports 17 tests, zero failures and zero skipped.

### R6 persisted-bookmark evidence

The R6 per-test logcat reports:

```text
R6_DIAGNOSTIC: stage=reader; persistedCount=1; bookIds=[GEN]; savedLanguage=en; activityLocales=en-US; expectedResourceText=Bookmarks (1)
```

The Reader-to-Study test completed successfully. The Settings per-test logcat contains no app fatal exception or missing ActivityResult registry failure. Emulator/system component warnings were present but did not fail the tests.

**Automated exact-head Android verification: VERIFIED PASS for this SHA.** This does not substitute for physical-device or TalkBack acceptance.

## 3. Localization

### Amharic

- 125/125 default resource keys are present.
- Sixteen Arabic-script values previously found in the Amharic resources were replaced.
- Latest script scan: zero Arabic-script entries.
- Placeholder consistency: PASS.
- Resource correction commit: `477ef2e536c480b3f36bbdac008b746ea0e26124`.

**Assessment:** Resource-key coverage complete; independent Amharic linguistic review NOT RUN.

### Maale

- 60 of 125 default resource keys have corresponding Maale translations.
- 65 keys still fall back to English.
- Approximate resource coverage: 48%.
- Bookmark-count placeholder corrected and checked.
- No placeholder mismatches in the checked resource sets.
- Catalogue book names remain English; Maale book-name orthography and translation quality require specialist review.

**Assessment:** PARTIAL. Full UI translation and signed Maale language QA remain outstanding.

## 4. Scripture source verification and content boundary

Selected source: **2008 Maale Full Bible — MDYTWF / mdyeth**.

Four archived copies were compared and produced the same SHA-256:

`4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`

Archive size: 1,809,863 bytes.

| Technical check | Result |
|---|---|
| ZIP integrity | PASS |
| Archive entries | 70 |
| USFM files | 66 |
| Signed checksum entries | 68/68 PASS |
| Unique book IDs | 66 |
| Chapters | 1,189 |
| Verse-marker lines | 30,506 |
| Strict UTF-8 decoding | PASS |
| NFC normalization observation | PASS |
| Replacement characters | 0 |
| Unexpected control/format characters | 0 |
| Duplicate book IDs | 0 |
| Structural anomalies detected | 0 |

The package GPG signature is cryptographically valid. The signing key's identity trust remains **UNDEFINED**; signature validity alone does not establish independently verified signer identity.

The source's 66 book IDs match the Owner-approved 66-book inventory and application catalogue in canonical order.

### Content boundary

- No raw USFM, scripture excerpts, source ZIP, or generated scripture database was found in the public repository tree at the assessed revision.
- Public repository content includes source metadata and hash manifests.
- The authorized archive remains in the private working/library area.
- The app still uses a reader placeholder and metadata catalogue; production scripture content and offline full-text search are not integrated.

The later Owner content-intake authorization permits source-by-source intake subject to provenance, licence/permission, attribution, integrity and compatibility verification. It does not authorize unrestricted redistribution, release or publication.

## 5. Security and backup/restore

Implemented controls include:

- SQLCipher-encrypted study database.
- Device-bound database key wrapping through Android Keystore and AES-GCM.
- Portable backup using Argon2id key derivation and AES-GCM authenticated encryption.
- Bounded backup-envelope input.
- Recovery on an empty destination database.
- Refusal to overwrite an existing destination database.
- `android:allowBackup="false"`.

Automated test coverage includes encryption/decryption, tamper rejection, wrong recovery-key rejection, plaintext database migration, database restart/reopen, portable recovery without the originating Keystore, failed recovery cleanup, preservation of existing destination data, migration fault injection and restore rollback.

The exact-head emulator run passed these automated tests. The Settings test verifies that accessibility and portable backup controls are exposed. Full UI document-picker export/import acceptance on a physical device remains outstanding.

**Independent security review: MISSING.** Internal source inspection and automated tests are not a substitute for an independent assessment.

## 6. External and device acceptance

| Requirement | Status |
|---|---|
| Independent Android security review | MISSING |
| Maale language specialist review | NOT RUN |
| Amharic linguistic review | NOT RUN |
| Ethiopic-script specialist sign-off | NOT RUN |
| Physical Android device acceptance | NOT RUN |
| TalkBack acceptance | NOT RUN |
| Font-scaling and accessibility acceptance | NOT RUN |
| Full UI document-picker export/import acceptance | NOT RUN |
| LIC-01 through LIC-10 release controls | SPECIFIED; NOT IMPLEMENTED |

## 7. Required remaining closure work

1. Complete all remaining Maale UI translations and obtain signed language/orthography QA.
2. Integrate the authorized Maale source through a reproducible, licence-compliant offline import pipeline.
3. Implement and test all scripture attribution, modification, ShareAlike, asset-rights and release controls (LIC-01 through LIC-10).
4. Obtain an independent Android security review with documented scope, findings, severity, remediation and retest evidence.
5. Complete physical-device, TalkBack, font-scaling and accessibility acceptance.
6. Complete physical-device document-picker backup/export/import and recovery acceptance.
7. Produce the final A3 closure record only after every required gate has passing evidence.

## 8. Governance disposition

- A1: CLOSED.
- A2: CLOSED, with its documented specialist-review exception retained.
- A3: **PARTIAL — NOT VERIFIED-CLOSED**.
- A4 automated exact-head CI: **VERIFIED PASS** for `58eb32acc10415e3dfa6683a5342b6da824d5d27`; physical-device and accessibility acceptance remain open.
- A5: NOT STARTED; separate Owner approval required.

PR #59 remains open, draft and unmerged. Its head at the time of this report is unchanged. `main` was not modified. No signing, deployment, release or publication was performed.

**Final decision: A3 closure is NOT APPROVED.**
