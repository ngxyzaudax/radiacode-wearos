#!/usr/bin/env bash
set -euo pipefail

WATCH_IP="${WATCH_IP:-192.168.0.106}"
WATCH_PORT="${WATCH_PORT:-45671}"

adb kill-server >/dev/null 2>&1 || true
adb start-server

if [[ -n "${WATCH_PAIR_PORT:-}" && -n "${WATCH_PAIR_CODE:-}" ]]; then
  adb pair "${WATCH_IP}:${WATCH_PAIR_PORT}" "${WATCH_PAIR_CODE}"
fi

if [[ -n "${WATCH_PORT:-}" ]]; then
  adb connect "${WATCH_IP}:${WATCH_PORT}" || true
fi

sleep 1
adb devices -l | rg 'fresh7bs|SM_L300|List of devices' || adb devices -l
