#!/usr/bin/env bash

set -euo pipefail

if (( $# == 0 )); then
    echo "Usage: $0 COMMAND [ARGUMENT]..." >&2
    exit 2
fi

: "${DISPLAY:?This script must run inside an X display session.}"

window_manager_log=$(mktemp "${RUNNER_TEMP:-/tmp}/tuitionbook-openbox.XXXXXX")
openbox --sm-disable >"$window_manager_log" 2>&1 &
window_manager_pid=$!

cleanup() {
    local command_status=$?
    trap - EXIT

    if kill -0 "$window_manager_pid" 2>/dev/null; then
        kill "$window_manager_pid" 2>/dev/null || true
    fi
    wait "$window_manager_pid" 2>/dev/null || true

    if (( command_status == 0 )); then
        rm -f "$window_manager_log" || true
    else
        echo "Openbox log:" >&2
        sed -n '1,200p' "$window_manager_log" >&2 || true
    fi

    exit "$command_status"
}
trap cleanup EXIT

window_manager_ready=false
for (( attempt = 1; attempt <= 100; attempt++ )); do
    if xprop -root _NET_SUPPORTING_WM_CHECK 2>/dev/null \
            | grep -Eq 'window id # 0x[[:xdigit:]]+'; then
        window_manager_ready=true
        break
    fi

    if ! kill -0 "$window_manager_pid" 2>/dev/null; then
        echo "::error::Openbox exited before publishing its window-manager readiness property." >&2
        exit 1
    fi

    sleep 0.1
done

if [[ "$window_manager_ready" != true ]]; then
    echo "::error::Openbox did not become ready within 10 seconds." >&2
    exit 1
fi

echo "Openbox is ready on DISPLAY=$DISPLAY."
"$@"
