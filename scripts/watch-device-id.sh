#!/usr/bin/env bash
set -euo pipefail

watch_line="$(adb devices -l | rg 'model:SM_L300' | head -1 || true)"
if [[ -z "${watch_line}" ]]; then
  echo ""
  exit 1
fi
echo "${watch_line%%[[:space:]]device*}"
