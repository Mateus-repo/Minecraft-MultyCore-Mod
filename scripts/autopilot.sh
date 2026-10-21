#!/usr/bin/env bash
# Controls autopilot mode (used by the plugin .opencode/plugins/autopilot.js).
#   bash scripts/autopilot.sh start [minutes]   (default 120)
#   bash scripts/autopilot.sh stop "reason"
#   bash scripts/autopilot.sh status
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
mkdir -p .opencode
S=.opencode/autopilot.state
cmd="${1:-status}"

case "$cmd" in
  start)
    mins="${2:-120}"
    rm -f STOP
    printf 'active=1\nstart=%s\nbudget_min=%s\ncount=0\nmax=60\nwrapup=0\nsession=\nreason=\n' "$(date +%s)" "$mins" > "$S"
    bash scripts/clock.sh reset >/dev/null 2>&1 || true
    echo "autopilot active: ${mins} min"
    ;;
  stop)
    reason="${2:-finished}"
    if [[ -f "$S" ]]; then
      sed -i.bak -e 's/^active=.*/active=0/' -e "s|^reason=.*|reason=${reason//|/ }|" "$S" && rm -f "$S.bak"
    else
      printf 'active=0\nreason=%s\n' "${reason//|/ }" > "$S"
    fi
    echo "autopilot stopped: $reason"
    ;;
  status)
    [[ -f "$S" ]] && cat "$S" || echo "autopilot: never started"
    ;;
  *)
    echo "Usage: autopilot.sh start [min] | stop \"reason\" | status" >&2; exit 1 ;;
esac
