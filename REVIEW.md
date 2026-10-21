# REVIEW — Minecraft MultiCore Mod

> The agent's self-reviews (and external reviews). Run `/review` before closing a milestone, before big commits and after long blocks of autonomous work.
> Skill: `code-review`. For a second independent opinion use the `@reviewer` subagent; to attack a plan use `@critic`.

## Quick checklist (per review)

- [x] **Correctness:** does it do what the acceptance criteria ask for? Edge cases and errors handled?
- [x] **Tests:** cover the new behavior and the fixed bugs? Pass deterministically?
- [x] **Design:** clear responsibilities, small modules, low coupling, no obvious duplication?
- [x] **Readability:** names, structure, comments only where they explain the *why*?
- [x] **Security:** input validated, secrets out of the repository, dependencies justified? (`security-review`)
- [x] **Performance:** nothing clearly quadratic/blocking on hot paths?
- [ ] **UI/UX** (if applicable): states (empty/loading/error), contrast, focus, responsiveness? (`ui-design`) — Mod Menu screen not manually verified yet.
- [x] **Docs:** do README/ARCHITECTURE/CHANGELOG/CONVENTIONS reflect reality?
- [x] **Git:** small commits, clear messages, no files that don't belong?

## Severity of findings

`blocker` (do not close without fixing) · `major` · `minor` · `nit` · `idea` (goes to IDEAS.md)

## Reviews

### R-001 — Independent review of the M1 code + external (Claude) review  (2026-10-21) — Result: ⚠️ with caveats

- **Scope:** the whole `src/` (main + tests), the build files, `docs/RESEARCH.md`, `ROADMAP.md`, and the
  external review of the repo at commit `792a9b0`.

#### A. External review: which findings were real?

The external reviewer read the repo at `792a9b0`. Findings and their status **now**:

| # | Severity | External finding | Verdict | Status |
|---|----------|------------------|---------|--------|
| 1 | major | `fabric.mod.json` contradicted README/D-007 (`environment: "server"`, no `modmenu` entrypoint) | **Real** (already fixed in `792a9b0`) | ✅ fixed |
| 2 | major | Scheduler merged in **completion** order, not submission order, despite the docs | **Real** | ✅ fixed (see B2) |
| 3 | medium | `WorkerPool.submit` / `TaskScheduler.submit` swallowed exceptions (unread `Future`) | **Real** | ✅ fixed (see B3) |
| 4 | minor | No end-of-tick barrier; merges could land after the tick ended | **Real** | ✅ fixed (see B1) |
| 5 | minor | Docs stale (`PROGRESS.md` said "no build files", entrypoint was a stub) | **Real** | ✅ fixed |
| 6 | minor | README claimed integrated-server support while the entrypoint was `DedicatedServerModInitializer` | **Real** | ✅ fixed (see B4) |
| 7 | — | "SAFE mode protects fewer mods than the idea promised (mixins)" | **Real, and honest** | ✅ documented in README §Known limitations |

The external review was accurate and useful. It did **not** find anything we had not already
suspected in `docs/RESEARCH.md` §7.3 — but it confirmed it with evidence, which is worth a lot.

#### B. Fixes applied (with tests)

| # | Fix | Evidence |
|---|-----|----------|
| B1 | `TaskScheduler.drainToMain()` now **waits** for every offloaded task (`CountDownLatch` barrier) before applying merges, so the tick never ends with work in flight. | `TaskSchedulerTest.drainWaitsForSlowWork` |
| B2 | Merges are applied in **submission order** (the in-flight queue is FIFO and tasks carry a sequence number), not completion order. | `TaskSchedulerTest.mergesAreAppliedInSubmissionOrderEvenWhenWorkersFinishOutOfOrder` (4 workers, forced out-of-order completion) |
| B3 | `WorkerPool.submit(Runnable)` uses `execute`, not `submit`, so exceptions reach the worker's uncaught-exception handler. | `WorkerPool` source; `TaskSchedulerTest.workerFailureDoesNotMergeAndDoesNotPropagate` |
| B4 | Entrypoint switched to `"main"` / `ModInitializer`, so the integrated server (singleplayer/LAN) runs too and the Mod Menu screen is meaningful. | D-013; `fabric.mod.json`; `MultiCoreMod` |
| B5 | `StatusFormatter` now uses `Locale.ROOT` — the original `String.format` produced `12,50` under a pt-PT locale. | `StatusFormatterTest` (caught it) |

#### C. My own findings (independent)

| # | Severity | Where | Problem | Action | Status |
|---|----------|-------|---------|--------|--------|
| C1 | minor | `TaskScheduler` | In Phase 3, region threads would call `submitOffload` concurrently with the main thread's `drainToMain`, racing on the in-flight queue | Make the queue and drain thread-confined (document; revisit in Phase 3) | 📝 recorded (R3) |
| C2 | minor | `MultiCoreCommand` | No permission check (the 26.3 permission API is new) | Add in Phase 4 once the API settles | 📝 documented |
| C3 | nit | `MultiCoreConfig` | Fields are package-private for tests | Acceptable for a Gson data holder | ✔ accepted |
| C4 | idea | `OffloadRegistry` | The `Safety` filter is enforced by ordinal comparison — correct but implicit | Could be a `EnumMap` policy in Phase 3 | → IDEAS.md |
| C5 | idea | `MetricsRollup` | The first offload only aggregates the mod's own timings — it proves the pipeline, it is not a game speedup | Expected; Phase 3 is where the gain is | → documented |

#### D. What works well

- Modules are small, single-purpose and fully unit-tested without needing the game (config, pool,
  scheduler, metrics, command, rollup). The threading logic is **injectable**, which is why it is testable.
- The docs are unusually honest: the README states the mixin limitation up front, and `docs/RESEARCH.md`
  lists risks and prior art with sources (verified with `javap` against the real 26.3 jar).
- `workerThreads` instead of "cores", SAFE by default, and `workerThreads = 0` ≡ vanilla are the right
  architectural calls.

#### E. Honest feasibility and usefulness

- **M1 (what exists today):** useful *infrastructure* — config, a named worker pool, an offload
  pipeline with a real safety contract, lock-free metrics, a command, and a Mod Menu screen. It does
  **not** by itself make the server faster, because vanilla already runs chunk I/O, chunk generation
  and light updates on background threads (confirmed in `docs/RESEARCH.md` §3 and by C2ME/Lithium/VMP).
- **M2 (compatibility):** the weakest link. SAFE mode can only protect mods that go through Fabric
  events; the majority of Fabric mods use mixins, which run on vanilla's thread. The promise
  "unsupported mods keep working" is therefore **narrower than it sounds**. This is the biggest gap
  between the original idea and what is possible without a server fork, and it is now stated in the
  README.
- **M3 (regionised tick):** where the real gain is, and where the real risk is. Folia is a full server
  fork by a team; reproducing it with mixins is a different scale. A partial result (a few regions, in
  spread-out scenarios) is more realistic than full parity.
- **Useful for:** large servers with many players **spread out** and a **verified** modpack. For small
  servers, or players clustered together, the effect is small — with one region, one thread does all
  the work.

#### F. The "players far apart" question

> *"Quando os players estão todos muito longe uns dos outros, eles carregam várias chunks separadas —
> este projeto conseguiria otimizar bastante nesse caso?"*

**Yes — that is exactly the best case, but only with the regionised tick (M3), not with M1.**

Why it is the best case:

1. With spread-out players, the server's tick cost grows roughly linearly with the number of players,
   because each player brings its own loaded chunks to tick (entities, block ticks, random ticks,
   spawns). That is the bottleneck Folia was built for.
2. The groups do not touch each other, so there are no cross-region interactions and almost no
   race-condition risk between them.
3. When players are **together**, they form one region → **no gain at all**.

The ceiling is `min(workerThreads + 1, independentGroups)`:

| Setup | Ceiling (before overhead) |
|-------|---------------------------|
| Default (1 worker, 2 threads) | ≈ **2×** |
| 6 threads, 6+ distant groups, real cores | ≈ **5–6×** |
| Players together (one group) | ≈ **1×** |

Important caveats:

- **Chunk generation/IO is not our gain** — vanilla and C2ME already do it asynchronously. Our gain is
  the *tick of already-loaded chunks*, which is mutable main-thread state.
- Even with distant chunks, the world has **global structures** (`entityTickList`, `blockTicks`,
  `fluidTicks`, `blockEvents`, `navigatingMobs`, `pathTypesByPosCache`) that must be partitioned per
  group. The first M3 spike should measure how many of these actually block parallelism.
- A simpler viable variant: **group players by distance** and tick each distant group in parallel with
  a barrier at the end of the tick, instead of full Folia regions. That avoids independent tick loops
  (the hardest part of Folia) and is a natural stepping stone toward M3.

The benchmark must therefore have **two scenarios** (players together vs spread out); if the gain only
appears in the spread scenario, that tells us immediately who the mod is for. See `docs/BENCHMARKS.md`.

#### G. What I would change (agreed with the external review)

1. Fix the real bugs first — **done** (B1–B5).
2. Run the benchmark and use it as a **go/no-go** before M3 — **planned** (T-023, D-012).
3. If the profile shows the cost is entities/pathfinding, consider attacking **only that** first
   (async pathfinding / entity AI with a fork-join phase) instead of the full regionised tick. Less
   ambitious, much more likely to work — and it is still a valid Phase 3 entry point.
4. Keep the README's legacy-mod promise realistic — **done**.

- **Residual risks:** world corruption in M3; deadlocks (R3); non-determinism (R5); SAFE mode not
  covering mixin-based mods (documented); no manual in-game verification yet.
