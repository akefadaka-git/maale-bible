#!/usr/bin/env python3
"""Regression tests for bounded retries of transient Gradle repository failures."""
from __future__ import annotations

import os
import subprocess
import tempfile
from pathlib import Path

SCRIPT = Path(__file__).with_name("gradle-retry.sh").resolve()

GRADLEW_STUB = r"""#!/bin/sh
count=0
[ ! -f "$TEST_GRADLE_COUNT" ] || count=$(cat "$TEST_GRADLE_COUNT")
count=$((count + 1))
printf '%s' "$count" > "$TEST_GRADLE_COUNT"
case "$TEST_SCENARIO:$count" in
  rate-limit:1|rate-limit:2|rate-limit-always:*)
    echo "Could not HEAD repository artifact. Received status code 429 from server: Too Many Requests"
    exit 1
    ;;
  rate-limit:*)
    echo "BUILD SUCCESSFUL"
    exit 0
    ;;
  compile:*)
    echo "e: Kotlin compilation failed: unresolved reference"
    exit 7
    ;;
esac
exit 99
"""


def run_case(scenario: str, expected_rc: int, expected_attempts: int) -> None:
    with tempfile.TemporaryDirectory(prefix="maale-gradle-retry-") as temp:
        root = Path(temp)
        gradlew = root / "gradlew"
        gradlew.write_text(GRADLEW_STUB, encoding="utf-8")
        gradlew.chmod(0o755)
        count_file = root / "gradle-count"
        env = os.environ.copy()
        env.update({
            "TEST_SCENARIO": scenario,
            "TEST_GRADLE_COUNT": str(count_file),
            "GRADLE_RETRY_DELAY_SECONDS": "0",
        })
        result = subprocess.run(
            ["sh", str(SCRIPT), ":app:testDebugUnitTest"],
            cwd=root,
            env=env,
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            check=False,
            timeout=30,
        )
        count = int(count_file.read_text(encoding="utf-8")) if count_file.exists() else 0
        if result.returncode != expected_rc or count != expected_attempts:
            raise AssertionError(
                f"{scenario}: rc={result.returncode} (expected {expected_rc}), "
                f"attempts={count} (expected {expected_attempts})\n{result.stdout}"
            )


def main() -> None:
    run_case("rate-limit", 0, 3)
    run_case("rate-limit-always", 1, 3)
    run_case("compile", 7, 1)
    print("PASS: HTTP 429 retries are bounded; deterministic compilation failures are not retried.")


if __name__ == "__main__":
    main()
