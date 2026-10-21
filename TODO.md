# TODO — Minecraft MultiCore Mod

> The agent's task queue. Order = priority. A task = something that fits in a short session and has **verifiable acceptance criteria**.
> Format: `- [ ] T-NNN [P0–P3] [size S/M/L] Title — acceptance: …  (ref: ROADMAP M#, BUGS B-NNN)`
> P0 = blocks everything · P1 = current milestone · P2 = important · P3 = when there is time.
> L tasks (> ~2h) must be broken down before starting (skill `planning`).

## Now

- [ ] T-001 [P1] [M] Scaffold the Gradle/Fabric 26.3 project — acceptance: `build.gradle`, `settings.gradle`, `gradle.properties`, committed wrapper (9.8.0), `src/main/resources/fabric.mod.json` (server entrypoint + mixins + `suggests.modmenu`), `multicore.mixins.json`; `./gradlew build` produces `build/libs/multicore-*.jar`  (ref: ROADMAP M0)
- [ ] T-002 [P1] [M] Fill the management docs for the real stack — acceptance: `AGENTS.md` commands real, `ARCHITECTURE.md` describes modules, `docs/CONVENTIONS.md`/`MODULES.md`/`TESTING.md`/`GLOSSARY.md`/`DESIGN.md` have no placeholders, `scripts/check.sh` green  (ref: ROADMAP M0)
- [ ] T-003 [P1] [M] Phase 1.1 — config module — acceptance: JSON `config/multicore.json` with `workerThreads` (default 1), `mode` (SAFE/AGGRESSIVE), `modWhitelist`/`modBlacklist`, `debug`; load/save/round-trip + validation tested; applied at server start only  (ref: ROADMAP M1, D-006, D-008)
- [ ] T-004 [P1] [M] Phase 1.2 — worker thread manager — acceptance: named fixed pool (never the common pool), start on `SERVER_STARTING`, drain+stop on `SERVER_STOPPED`, unit test proves tasks run off the main thread  (ref: ROADMAP M1)
- [ ] T-005 [P1] [M] Phase 1.3 — offload/merge scheduler — acceptance: submit work to workers, results merged on the main thread before `END_SERVER_TICK`; no shared mutable world state touched off-thread; tests cover ordering  (ref: ROADMAP M1)
- [ ] T-006 [P1] [M] Phase 1.4 — metrics + `/multicore status` — acceptance: MSPT and per-thread time collected without locks on the hot path; command prints threads, mode, MSPT, legacy mods  (ref: ROADMAP M1)
- [ ] T-007 [P1] [M] Phase 1.5 — Mod Menu config screen — acceptance: `ModMenuIntegration` + screen + lang files; every control persists; a clear "restart required" notice is shown; works when Mod Menu present, no crash when absent  (ref: ROADMAP M1, D-007)
- [ ] T-008 [P1] [S] Phase 1.6 — first safe offload + benchmark — acceptance: spike S3 picks one offload (random-tick candidates / pathfinding cache / POI), a benchmark script compares MSPT before/after on the same seed, and the offload is disabled when it changes RNG order  (ref: ROADMAP M1, D-010)

## Next

- [ ] T-009 [P2] [M] Phase 2.1 — mod scan — acceptance: list loaded mods, read `custom.multicore.threadSafe`, classify legacy/compatible, honour whitelist/blacklist  (ref: ROADMAP M2, D-005)
- [ ] T-010 [P2] [M] Phase 2.2 — legacy hand-off — acceptance: legacy Fabric callbacks run on the main thread in SAFE mode; a "naughty" legacy mod test does not corrupt state  (ref: ROADMAP M2)
- [ ] T-011 [P2] [M] Phase 2.3 — `MultiCoreCompat` public API — acceptance: `runOnRegion`/`runOnMain`/`RegionLocal` documented, tested, listed in `docs/MODULES.md`  (ref: ROADMAP M2)
- [ ] T-012 [P2] [S] Phase 2.4 — instrumentation — acceptance: debug thread-check logs off-thread world access; `/multicore compat dump` reports it  (ref: ROADMAP M2)
- [ ] T-013 [P2] [L] Phase 3.1 — region model — acceptance: region sections, merge radius, four states, merge/split with deadline offsets; unit-tested in isolation  (ref: ROADMAP M3)
- [ ] T-014 [P2] [L] Phase 3.2 — region scheduler + barrier — acceptance: per-level regions tick in parallel on workers, main thread joins, legacy callbacks run after  (ref: ROADMAP M3)
- [ ] T-015 [P2] [L] Phase 3.3 — cross-region messages + fallback — acceptance: teleports/redstone/explosions across boundaries use queues; problematic regions merge back to main thread  (ref: ROADMAP M3)
- [ ] T-016 [P2] [M] Phase 3.4 — determinism/corruption tests — acceptance: same seed + same tick count produces identical world state vs vanilla in SAFE mode  (ref: ROADMAP M3)

## Later

- [ ] T-017 [P3] [M] Phase 4.1 — test on Linux + Windows dedicated servers — acceptance: documented results, no OS-specific crash  (ref: ROADMAP M4)
- [ ] T-018 [P3] [S] Phase 4.2 — optional CPU affinity — acceptance: flag enables FFM/JNI affinity or prints the correct launcher flags; clear no-op warning  (ref: ROADMAP M4, D-006)
- [ ] T-019 [P3] [M] Phase 4.3 — README + tested-mods table + `/multicore` subcommands  (ref: ROADMAP M4)
- [ ] T-020 [P3] [M] Phase 5.1 — client feasibility report — acceptance: written report, no client code merged  (ref: ROADMAP M5)

## Blocked (waiting for the user's answer — see QUESTIONS.md)

- [ ] T-021 [P1] [S] Confirm Phase 3 approach + Mod Menu restart flow — acceptance: user answers in `QUESTIONS.md`; then adjust M1/M3 tasks  (ref: QUESTIONS Q-001, Q-002)

## Done (last 20; the rest goes to PROGRESS.md › Archive)

- [x] T-000 [P1] [S] Apply the repository template — acceptance: template copied, placeholders replaced, first commit  (ref: ROADMAP M0)
- [x] T-000b [P1] [M] Phase 0 research — acceptance: `docs/RESEARCH.md` written from web + local jar inspection; Mod Menu requirement recorded  (ref: ROADMAP M0)
