#!/usr/bin/env bash
# Benchmark harness for the MultiCore mod.
#
# This is a *skeleton*: it assumes a prepared server directory with Fabric 26.3, Fabric API,
# Spark and a player-simulator mod. It does not download anything (no network dependency).
#
# Usage:
#   benchmarks/benchmark.sh <server-dir> <scenario> <workers> <minutes>
#     scenario: together | spread
#     workers:  0 | 2 | 4 | 6
#
# See docs/BENCHMARKS.md for the full procedure and the results table.
set -euo pipefail

SERVER_DIR="${1:?server directory}"
SCENARIO="${2:?scenario: together|spread}"
WORKERS="${3:?workers: 0|2|4|6}"
MINUTES="${4:-10}"
MOD_JAR="$(ls build/libs/multicore-*.jar | grep -v sources | head -1)"

if [[ ! -d "$SERVER_DIR" ]]; then
  echo "Server directory not found: $SERVER_DIR" >&2
  exit 1
fi

CONFIG="$SERVER_DIR/config/multicore.json"
MODS="$SERVER_DIR/mods"

# Configure the mod for this run.
mkdir -p "$SERVER_DIR/config"
cat > "$CONFIG" <<JSON
{
  "workerThreads": $WORKERS,
  "mode": "SAFE",
  "debug": false,
  "modWhitelist": [],
  "modBlacklist": []
}
JSON

# Install or remove the mod depending on the worker count (0 = vanilla-equivalent but still installed).
cp "$MOD_JAR" "$MODS/"

echo "Run: scenario=$SCENARIO workers=$WORKERS minutes=$MINUTES"
echo "World: $SERVER_DIR"
echo
echo "Next steps (manual):"
echo "  1. Start the server:  (cd '$SERVER_DIR' && java -jar fabric-server-launch.jar)"
echo "  2. Spawn the simulated players ($SCENARIO)."
echo "  3. After ${MINUTES}m run:  /multicore status   and   /spark profiler --timeout 60"
echo "  4. Record MSPT / p95 / TPS in docs/BENCHMARKS.md"
echo
echo "To test the true vanilla baseline, remove $MODS/$(basename "$MOD_JAR") and repeat."
