# Tests — Minecraft MultiCore Mod

> Verification strategy. Skill: `testing-verification`.

## Philosophy

- Test **behavior**, not implementation. Fast, deterministic, independent tests.
- Every fixed bug gets a regression test.
- Pyramid: many unit · some integration · few end-to-end.
- Minecraft is not available in a plain JUnit test, so **the threading/region logic is designed as
  pure, injectable units** that can be tested without the game. Anything that needs a real
  `ServerLevel` is verified **manually** with written steps.

## Commands

| What | Command |
|-------|---------|
| All | `bash scripts/check.sh` |
| Unit | `./gradlew test` |
| Build | `./gradlew build` |
| One test class | `./gradlew test --tests "com.strefiz.multicore.config.MultiCoreConfigTest"` |
| Coverage | _not configured yet (Phase 1.3)_ |

## What must have tests

- **Config:** load/save/round-trip, defaults, invalid values (negative threads, unknown mode),
  missing file, malformed JSON.
- **Regionizer:** the four invariants (single owner, merge radius, ticking regions don't grow, four
  states); merge/split deadline offsets; split inheritance.
- **TaskScheduler:** results are merged on the main thread *before* the tick ends; ordering is
  preserved for same-region tasks; exceptions in workers are captured, not lost.
- **Metrics:** MSPT and per-thread timers update correctly; no allocation on the hot path.
- **Compat:** `threadSafe` true/false/missing → COMPATIBLE/LEGACY; whitelist/blacklist precedence.

## What is verified manually

- Running a **dedicated server** with the mod: no crash, `/multicore status` shows the expected
  threads and MSPT.
- **SAFE mode** with a legacy mod that writes world state: no corruption (see task T-010).
- `workerThreads = 0` behaves like vanilla.
- **Mod Menu** screen opens, persists, shows the restart warning, and does not crash when Mod Menu
  is absent.
- Linux and Windows (Phase 4).

### Manual procedure (template)

1. Fresh dedicated server, Fabric 26.3 + Fabric API + this mod (+ test mods).
2. Copy `config/multicore.json` to a known baseline.
3. Run `java -jar fabric-server-launch.jar`, let it tick N minutes with a bot load.
4. Record: `/multicore status`, TPS, crash logs, world integrity (`/spark` or a save/reload diff).
5. Compare against the same run without the mod.

## Test data

- Unit tests use in-memory strings/objects only; no real worlds, no personal data.
- Manual runs use throwaway worlds in `run/`.

## Known issues (flaky, slow)

- Full server runs are slow; keep automated tests headless and pure.
