#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEVICE="$("${ROOT}/scripts/watch-device-id.sh" || true)"

if [[ -z "${DEVICE}" ]]; then
  echo "Watch not found. Run: ${ROOT}/scripts/watch-connect.sh" >&2
  exit 1
fi

echo "Tailing RadiaCode logs on ${DEVICE}" >&2
adb -s "${DEVICE}" logcat -v time \
  "RadiaCode:D" \
  "AndroidRuntime:E" \
  "System.err:W" \
  "*:S"
