#!/usr/bin/env bash
# Clock for the autonomous session. The agent does not feel time passing — use this.
#   bash scripts/clock.sh          -> shows elapsed / remaining
#   bash scripts/clock.sh reset    -> restarts
# Budget: autopilot.state (budget_min) > $AUTONOMY_BUDGET_MIN > 120
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
mkdir -p .opencode
F=.opencode/.session-start
now="$(date +%s)"

if [[ "${1:-}" == "reset" || ! -f "$F" ]]; then echo "$now" > "$F"; fi
start="$(cat "$F")"

budget="${AUTONOMY_BUDGET_MIN:-120}"
if [[ -f .opencode/autopilot.state ]]; then
  b="$(grep -E '^budget_min=' .opencode/autopilot.state | cut -d= -f2 || true)"
  s="$(grep -E '^start=' .opencode/autopilot.state | cut -d= -f2 || true)"
  a="$(grep -E '^active=' .opencode/autopilot.state | cut -d= -f2 || true)"
  [[ -n "$b" ]] && budget="$b"
  [[ "$a" == "1" && -n "$s" ]] && start="$s"
fi

el=$(( (now - start) / 60 ))
rem=$(( budget - el ))
echo "now: $(date '+%Y-%m-%d %H:%M')"
echo "elapsed: ${el} min · budget: ${budget} min · remaining: ${rem} min"
if (( rem <= 0 )); then echo "BUDGET SPENT → do a handoff (skill session-handoff)."; fi
