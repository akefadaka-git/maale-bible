# Android CI failure analysis and recovery policy

Date: 2026-11-09
Scope: Maale Bible Android instrumentation verification

## Incident examined

Workflow run: https://github.com/akefadaka-git/maale-bible/actions/runs/42613665115
Tested HEAD: `9e41b575b7a36f1ba9fd717de3b3a8205173f0a0`

Observed outcomes:
- Host contract checks passed.
- API 26, 28, 33, 35 and 36 executed the 25-test instrumentation suite and failed in the same navigation smoke test.
- API 30 lost its connected-device registration; Gradle reported `No compatible devices connected`.
- Logs across the matrix repeatedly showed transient `adb: device offline` while the emulator was starting.
- The navigation assertion failed because `onNodeWithText("Genesis")` matched two visible Compose nodes: the reader top bar title and the reader identity title. Compose's singular selector correctly rejected the ambiguous match.

## Root causes

### Deterministic test defect
The reader intentionally renders the book name twice. A generic singular text selector was not unique. This is a test selector defect, not a source UI regression. The smoke test now asserts the unique canonical identity `CANONICAL BOOK ID  •  GEN` and the reader's explicit verified-content-unavailable state.

### Emulator/ADB infrastructure instability
The runner logs show the emulator temporarily offline during startup and one API job later reported no compatible connected device. This is a runner/device lifecycle failure, distinct from an application assertion. A green result on one emulator run does not prove the emulator infrastructure will remain stable on every later run.

## Reliability changes

- Emulator readiness requires an online ADB device, `sys.boot_completed=1`, the expected API level, stopped/absent boot animation, and a responsive Android package manager.
- The runner uses a clean emulator boot without loading a stale snapshot.
- Gradle worker parallelism is bounded to two workers on the hosted runner.
- The test log is retained as a build artifact, alongside instrumentation XML/HTML reports.
- The harness allows one bounded ADB recovery and rerun only when Gradle output identifies device-discovery/connection infrastructure failure.
- Assertion, compilation, and application failures are not retried or masked. They remain hard failures.
- The workflow path filter includes `verification/android/**`, so future harness edits trigger verification.

## Why a previous pass can be followed by a failure

A CI result is evidence for one exact commit, workflow revision, runner instance, emulator image/API, and test execution. A later commit can introduce a real regression; a changed test can expose an old ambiguity; and hosted emulator/ADB services can fail intermittently even with unchanged source. Matrix members are independent jobs and may experience different runner load and boot behavior. Therefore, a historical pass is not a permanent guarantee and a transient infrastructure retry must not be confused with a test pass.

## Required interpretation of results

- **PASS** only when the exact tested HEAD has all required matrix jobs green.
- **FAIL — PRODUCT/TEST** when an assertion, compilation, or app behavior fails.
- **FAIL — INFRASTRUCTURE** when the emulator cannot become/remain available or instrumentation cannot attach.
- **RETRIED-PASS** when one documented infrastructure recovery succeeds; retain both failed attempt logs and successful attempt logs.
- Never convert a deterministic test failure into a pass by retrying it. The retry is limited to device-discovery failures, and both attempt logs are retained.
- Never mark the matrix closed while any required API job is queued, in progress, skipped without approval, or failed.

## Limit of the guarantee

No hosted emulator matrix can honestly be guaranteed to have zero transient infrastructure failures. This policy makes them diagnosable and recoverable without hiding product defects. A stable green exact-HEAD matrix, repeated clean runs, and eventual physical-device acceptance provide stronger confidence than a single green run.

## Follow-up runner incident: 2026-11-09

Workflow run: https://github.com/akefadaka-git/maale-bible/actions/runs/42676797068
Tested HEAD: `d8ea1980b7c11cc8c2504b195005912bb090ae72`

The unique Genesis selector correction and prior recovery changes are present in this HEAD. In the latest run, API 26 and API 28 passed, while API 36 failed before the tracked instrumentation script created its log. The emulator action booted the image, then its own setup command failed with an ADB broken-pipe error while changing the screen-off timeout. This is a runner/emulator-action setup failure, not a test assertion or app compilation failure.

Reliability adjustment applied after this incident:
- Pin hosted jobs to `ubuntu-24.04` instead of the moving `ubuntu-latest` image, which had resolved to Ubuntu 26.04 in the failed run.
- Limit the emulator matrix to two concurrent jobs to reduce host CPU, memory, and ADB contention across six emulator instances.
- Increase the emulator job timeout to 75 minutes so the bounded boot/readiness and instrumentation process has room to complete.

This is a risk reduction, not a guarantee that GitHub-hosted emulator infrastructure will never fail. A subsequent exact-HEAD matrix must verify the change.
