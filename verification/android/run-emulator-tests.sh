#!/bin/sh
# Invoked as one command by reactivecircus/android-emulator-runner.
# Keep this logic in a file: the action splits its script input by newline.
set -eu

expected_api="${ANDROID_EXPECTED_API:?ANDROID_EXPECTED_API must be set}"

diagnostics() {
  echo "::group::Android emulator diagnostics (API ${expected_api})"
  adb devices -l 2>&1 || true
  for prop in sys.boot_completed init.svc.bootanim ro.build.version.sdk ro.build.version.release; do
    echo "--- ${prop} ---"
    timeout 10s adb shell getprop "${prop}" 2>&1 || true
  done
  echo "--- emulator processes ---"
  pgrep -af 'emulator|qemu' 2>&1 || true
  echo "--- recent logcat ---"
  timeout 20s adb logcat -b all -d -t 1500 2>&1 || true
  echo "::endgroup::"
}

ready=0
attempt=1
while [ "${attempt}" -le 3 ]; do
  echo "Emulator readiness attempt ${attempt}/3 (API ${expected_api})"
  timeout 30s adb wait-for-device || true
  deadline=$(( $(date +%s) + 180 ))
  while [ "$(date +%s)" -lt "${deadline}" ]; do
    state="$(timeout 5s adb get-state 2>/dev/null || true)"
    if [ "${state}" = "device" ]; then
      boot_completed="$(timeout 8s adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
      boot_animation="$(timeout 8s adb shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r' || true)"
      actual_api="$(timeout 8s adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || true)"
      if [ "${boot_completed}" = "1" ] && [ "${actual_api}" = "${expected_api}" ]; then
        if [ -z "${boot_animation}" ] || [ "${boot_animation}" = "stopped" ]; then
          ready=1
          break
        fi
      fi
    fi
    sleep 5
  done
  if [ "${ready}" -eq 1 ]; then
    break
  fi
  echo "::warning::Emulator readiness attempt ${attempt}/3 failed."
  diagnostics
  if [ "${attempt}" -lt 3 ]; then
    adb reconnect offline 2>&1 || true
    sleep 5
  fi
  attempt=$((attempt + 1))
done

if [ "${ready}" -ne 1 ]; then
  echo "::error::Emulator failed boot-readiness checks after 3 bounded attempts."
  diagnostics
  exit 1
fi

echo "Emulator ready: API ${expected_api}; boot_completed=1."
adb shell getprop ro.build.version.sdk
adb shell pm path android >/dev/null
if ./gradlew --no-daemon :app:connectedDebugAndroidTest; then
  echo "Instrumentation tests passed on API ${expected_api}."
else
  test_rc=$?
  echo "::error::Instrumentation tests failed on API ${expected_api}; collecting diagnostics."
  diagnostics
  exit "${test_rc}"
fi
