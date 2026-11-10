#!/bin/sh
# Invoked as one command by reactivecircus/android-emulator-runner.
# Keep this logic in a file: the action splits its script input by newline.
set -eu

expected_api="${ANDROID_EXPECTED_API:?ANDROID_EXPECTED_API must be set}"
log_prefix="app/build/ci-emulator-api-${expected_api}"
attempt=1
log_file="${log_prefix}-attempt-${attempt}.log"
mkdir -p "$(dirname "${log_file}")"

diagnostics() {
  echo "::group::Android emulator diagnostics (API ${expected_api})"
  adb devices -l 2>&1 || true
  for prop in sys.boot_completed init.svc.bootanim ro.build.version.sdk; do
    echo "--- ${prop} ---"
    timeout 10s adb shell getprop "${prop}" 2>&1 || true
  done
  echo "--- emulator processes ---"
  pgrep -af 'emulator|qemu' 2>&1 || true
  echo "--- recent logcat ---"
  timeout 20s adb logcat -b all -d -t 1500 2>&1 || true
  echo "::endgroup::"
}

wait_for_ready() {
  deadline=$(( $(date +%s) + 240 ))
  while [ "$(date +%s)" -lt "${deadline}" ]; do
    state="$(timeout 8s adb get-state 2>/dev/null || true)"
    if [ "${state}" = "device" ]; then
      boot_completed="$(timeout 8s adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
      boot_animation="$(timeout 8s adb shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r' || true)"
      actual_api="$(timeout 8s adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || true)"
      if [ "${boot_completed}" = "1" ] && [ "${actual_api}" = "${expected_api}" ]; then
        if [ -z "${boot_animation}" ] || [ "${boot_animation}" = "stopped" ]; then
          if timeout 15s adb shell pm path android >/dev/null 2>&1; then
            echo "Emulator ready: API ${expected_api}; boot_completed=1; package manager responsive."
            return 0
          fi
        fi
      fi
    fi
    sleep 5
  done
  echo "::error::Emulator did not reach a stable boot-ready state for API ${expected_api}."
  diagnostics
  return 1
}

recover_adb() {
  echo "::warning::Restarting ADB and waiting for a clean emulator state (attempt ${attempt})."
  adb kill-server 2>&1 || true
  sleep 2
  adb start-server 2>&1 || true
  timeout 45s adb wait-for-device || true
  wait_for_ready
}

echo "Waiting for emulator API ${expected_api}."
timeout 45s adb wait-for-device || true
if ! wait_for_ready; then
  recover_adb
fi

run_instrumentation() {
  if ./gradlew --no-daemon --max-workers=2 --console=plain --stacktrace :app:connectedDebugAndroidTest >"${log_file}" 2>&1; then
    cat "${log_file}"
    return 0
  else
    result=$?
    cat "${log_file}"
    return "${result}"
  fi
}

is_retryable_infrastructure_failure() {
  grep -Eiq 'No compatible devices connected|No connected devices|Failed to install.*device|Failed to install split APK|Failed to commit install session|Broken pipe|API level=1|Unknown API Level|ShellCommandUnresponsiveException|Unable to connect to adb|Could not load test results' "${log_file}" ||
    { grep -Eiq 'device offline|device unauthorized|Lost network stack|FATAL EXCEPTION IN SYSTEM PROCESS|DeadSystemException' "${log_file}" &&
      ! grep -Eq 'Starting [0-9]+ tests on emulator' "${log_file}"; }
}

if run_instrumentation; then
  echo "Instrumentation tests passed on API ${expected_api}."
  exit 0
fi
test_rc=$?

# Permit at most two bounded retries, and only for signatures showing that
# Android/ADB failed before a trustworthy instrumentation result was produced.
# Assertion failures and ordinary test failures are never retried or hidden.
while [ "${attempt}" -lt 3 ] && is_retryable_infrastructure_failure; do
  attempt=$((attempt + 1))
  log_file="${log_prefix}-attempt-${attempt}.log"
  echo "::warning::Detected an emulator/ADB infrastructure failure on API ${expected_api}; bounded recovery ${attempt}/2."
  recover_adb
  if run_instrumentation; then
    echo "Instrumentation tests passed after infrastructure recovery on API ${expected_api}."
    exit 0
  fi
  test_rc=$?
done

echo "::error::Instrumentation tests failed on API ${expected_api}; no further retries will hide this result."
diagnostics
exit "${test_rc}"
