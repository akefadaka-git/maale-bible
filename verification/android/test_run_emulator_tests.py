#!/usr/bin/env python3
"""Regression tests for emulator CI retry and exit-status handling."""
from __future__ import annotations

import os
import subprocess
import tempfile
from pathlib import Path

SCRIPT = Path(__file__).with_name("run-emulator-tests.sh").resolve()

ADB_STUB = r"""#!/bin/sh
if [ "$1" = "-s" ]; then shift 2; fi
case "$1" in
  wait-for-device|kill-server|start-server|devices) exit 0 ;;
  get-state) echo device; exit 0 ;;
  shell)
    shift
    case "$1" in
      getprop)
        case "$2" in
          sys.boot_completed) echo 1 ;;
          init.svc.bootanim) echo stopped ;;
          ro.build.version.sdk) echo 35 ;;
          *) echo "" ;;
        esac
        exit 0
        ;;
      pm) echo "package:/system/framework/framework-res.apk"; exit 0 ;;
      input|settings) exit 0 ;;
    esac
    ;;
  logcat) exit 0 ;;
esac
exit 0
"""

GRADLEW_STUB = r"""#!/bin/sh
count=0
[ ! -f "$TEST_GRADLE_COUNT" ] || count=$(cat "$TEST_GRADLE_COUNT")
count=$((count + 1))
printf '%s' "$count" > "$TEST_GRADLE_COUNT"
case "$TEST_SCENARIO:$count" in
  assertion:*)
    printf '%s\n' 'Starting 3 tests on emulator' 'FAILED' 'Broken pipe'
    exit 23
    ;;
  recover:1|exhaust:*)
    echo 'Failed to install APK on device'
    exit 17
    ;;
  recover:*)
    printf '%s\n' 'Starting 2 tests on emulator' 'BUILD SUCCESSFUL'
    exit 0
    ;;
esac
exit 99
"""

def run_case(scenario: str, expected_rc: int, expected_attempts: int) -> None:
    with tempfile.TemporaryDirectory(prefix="maale-emulator-ci-") as temp:
        root = Path(temp)
        fake_bin = root / "bin"
        fake_bin.mkdir()
        adb = fake_bin / "adb"
        adb.write_text(ADB_STUB, encoding="utf-8")
        adb.chmod(0o755)

        gradlew = root / "gradlew"
        gradlew.write_text(GRADLEW_STUB, encoding="utf-8")
        gradlew.chmod(0o755)

        count_file = root / "gradle-count"
        env = os.environ.copy()
        env.update({
            "ANDROID_EXPECTED_API": "35",
            "PATH": f"{fake_bin}:{env.get('PATH', '/usr/bin:/bin')}",
            "TEST_SCENARIO": scenario,
            "TEST_GRADLE_COUNT": str(count_file),
        })
        result = subprocess.run(
            ["sh", str(SCRIPT)],
            cwd=root,
            env=env,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            check=False,
            timeout=90,
        )
        count = int(count_file.read_text(encoding="utf-8")) if count_file.exists() else 0
        if result.returncode != expected_rc or count != expected_attempts:
            raise AssertionError(
                f"{scenario}: rc={result.returncode} (expected {expected_rc}), "
                f"attempts={count} (expected {expected_attempts})\n{result.stdout}"
            )
        for attempt in range(1, expected_attempts + 1):
            log = root / f"app/build/ci-emulator-api-35-attempt-{attempt}.log"
            if not log.is_file():
                raise AssertionError(f"{scenario}: missing attempt log {log}")

def main() -> None:
    # Incidental ADB text after test execution must not trigger a retry.
    run_case("assertion", 23, 1)
    # A pre-instrumentation install failure may recover and then pass.
    run_case("recover", 0, 2)
    # Exhaustion must preserve the final underlying Gradle failure code.
    run_case("exhaust", 17, 3)
    print("PASS: failure status preserved; test failures are not retried; "
          "pre-instrumentation recovery is bounded to two retries.")

if __name__ == "__main__":
    main()
