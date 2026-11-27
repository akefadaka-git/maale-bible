#!/bin/sh
# Retry only transient dependency/repository transport failures. Never retry
# compilation, test, assertion, or other deterministic Gradle failures.
set -u

if [ "$#" -eq 0 ]; then
  echo "Usage: gradle-retry.sh <Gradle task and arguments...>" >&2
  exit 64
fi

max_attempts=3
delay_seconds="${GRADLE_RETRY_DELAY_SECONDS:-20}"
case "$delay_seconds" in
  ''|*[!0-9]*) echo "GRADLE_RETRY_DELAY_SECONDS must be a non-negative integer." >&2; exit 64 ;;
esac

log_file="$(mktemp)"
trap 'rm -f "$log_file"' EXIT HUP INT TERM
attempt=1

is_transient_repository_failure() {
  grep -Eiq \
    'status code (429|500|502|503|504)|HTTP/(1\.[01]|2) (429|500|502|503|504)|Too Many Requests|Internal Server Error|Bad Gateway|Service Unavailable|Gateway Timeout|Read timed out|Connection reset|Temporary failure in name resolution|UnknownHostException' \
    "$log_file"
}

while :; do
  echo "Gradle attempt ${attempt}/${max_attempts}: $*"
  if ./gradlew "$@" >"$log_file" 2>&1; then
    cat "$log_file"
    exit 0
  else
    result=$?
  fi

  cat "$log_file"
  if [ "$attempt" -ge "$max_attempts" ] || ! is_transient_repository_failure; then
    echo "::error::Gradle failed with exit code ${result}; no safe retry remains."
    exit "$result"
  fi

  wait_seconds=$((delay_seconds * attempt))
  echo "::warning::Transient repository failure detected; retrying in ${wait_seconds}s."
  sleep "$wait_seconds"
  attempt=$((attempt + 1))
done
