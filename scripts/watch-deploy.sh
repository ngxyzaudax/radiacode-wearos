#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEVICE="$("${ROOT}/scripts/watch-device-id.sh" || true)"

if [[ -z "${DEVICE}" ]]; then
  echo "Watch not found. Run: ${ROOT}/scripts/watch-connect.sh" >&2
  exit 1
fi

cd "${ROOT}"
./gradlew :wear:assembleDebug :watchface:assembleDebug
adb -s "${DEVICE}" install -r wear/build/outputs/apk/debug/wear-debug.apk
adb -s "${DEVICE}" install -r watchface/build/outputs/apk/debug/watchface-debug.apk
adb -s "${DEVICE}" shell am force-stop com.ngxyzaudax.radiacode || true
adb -s "${DEVICE}" shell am start \
  -n com.ngxyzaudax.radiacode/.presentation.MainActivity \
  -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  --activity-single-top
