# ROADMAP — Minecraft MultiCore Mod

> The project's direction: **why** and **when**. The user decides; the agent proposes and keeps the state up to date.
> Concrete tasks live in `TODO.md`. Loose ideas in `IDEAS.md`.

## Vision

A drop-in **Fabric mod** that lets a normal Minecraft **server** use more than one core, without
breaking the mods that were not written for multithreading. By default it runs in **SAFE** mode:
unmodified mods keep working on the main thread, while the vanilla work that is safe to split is
distributed over a configurable number of worker threads. Servers that verify their modpack can
switch to **AGGRESSIVE** mode for the full regionised tick (the Folia-style design).

## Principles (what we do NOT sacrifice)

- **Correctness over speed** — never corrupt a world; fall back to the main thread on any doubt.
- **Compatibility by default** — a mod that does not opt in is legacy and stays on the main thread.
- **Honesty** — document what is not protected (mixin-based mods, datapacks) and what is not
  deterministic (world generation).
- **Small, testable, documented code** — no god classes; every public API has a docstring and a
  module README.
- **No duplicate of existing work** — complement C2ME/Lithium/VMP, never replace them silently.

## Out of scope (for now)

- Client-side rendering parallelism (Phase 5: feasibility report only).
- NeoForge/Forge ports.
- Any change to vanilla gameplay/observable behaviour.
- Auto-detection of thread safety (impossible; opt-in only).

## Milestone status

Legend: `⬜ not started` · `🟦 in progress` · `✅ done` · `⏸ paused` · `❌ canceled`

| # | Milestone | Status | Measurable goal |
|---|-----------|--------|-----------------|
| M0 | Foundations | 🟦 | Repo template applied, build compiles for 26.3, `scripts/check.sh` green |
| M1 | Infrastructure + safe offload | ⬜ | Config, thread manager, metrics, `/multicore`, Mod Menu screen, 1+ safe offload |
| M2 | Mod compatibility layer | ⬜ | Detect mods, `threadSafe` flag, legacy hand-off queue, compat API + docs |
| M3 | Regionised tick (Folia-style) | ⬜ | Regions, scheduler, barrier, cross-region messages, determinism tests |
| M4 | Polish + Linux/Windows | ⬜ | Tested on both OSes, optional CPU affinity, tested-mods list |
| M5 | Client feasibility | ⬜ | Written viability report; no client code unless approved |

---

## M0 — Foundations

**Goal:** a reproducible, compiling Fabric 26.3 project.
**Completion criteria:**
- [ ] Gradle wrapper (9.8.0) committed; `./gradlew build` compiles a jar.
- [ ] `fabric.mod.json` with `server` entrypoint, `mixins` list, `suggests.modmenu`.
- [ ] `scripts/check.sh` runs and passes.
- [ ] `AGENTS.md` commands filled in; `ARCHITECTURE.md` describes the structure.
- [ ] Phase 0 research written to `docs/RESEARCH.md` and reviewed by the user.

## M1 — Infrastructure + safe offload (low risk)

**Goal:** the harness exists and gives *some* measurable win without touching mutable world state.
**Completion criteria:**
- [ ] JSON config (`workerThreads`, `mode`, whitelist/blacklist, debug) loaded at server start.
- [ ] Named worker pool (no common pool) with start/stop/shutdown handling.
- [ ] Task scheduler that runs offloaded work on workers and merges results on the main thread.
- [ ] At least one safe offload with a before/after benchmark (candidates: random-tick candidate
      precompute, pathfinding cache warm, POI lookups — spike S3 decides).
- [ ] Tick metrics (MSPT, per-thread time, per-region time) + `/multicore status`.
- [ ] Mod Menu config screen with a **restart-required** warning.
- [ ] `workerThreads = 0` is behaviourally equivalent to vanilla.

**Known risks:** the offload must not change RNG order (otherwise world behaviour changes).

## M2 — Mod compatibility layer

**Goal:** know which mods are legacy and keep them safe.
**Completion criteria:**
- [ ] Scan loaded mods via `FabricLoader.getAllMods()` + `custom.multicore.threadSafe`.
- [ ] Whitelist/blacklist config respected.
- [ ] Legacy callbacks handed off to the main thread (queue + barrier) in SAFE mode.
- [ ] Debug thread-check instrumentation + `/multicore compat dump`.
- [ ] Public `MultiCoreCompat` API documented for other authors.
- [ ] Regression test: a legacy "naughty" mod that writes world state does not crash SAFE mode.

## M3 — Regionised tick (high risk)

**Goal:** the Folia-style parallel tick, with a safe fallback.
**Completion criteria:**
- [ ] Region model (sections, merge radius, transient/ready/ticking/dead) implemented.
- [ ] Per-level region scheduler on the worker pool with an EDF-like start-time policy.
- [ ] Cross-region interactions go through message queues, never direct access.
- [ ] Barriers at tick boundaries; legacy callbacks run on the main thread.
- [ ] Automatic fallback to the main thread when a region has problematic interactions.
- [ ] Determinism/corruption test suite comparing against vanilla (same seed, same ticks).
- [ ] Stress test: no crash/corruption with popular mods in SAFE mode.

**Known risks:** deadlocks, world corruption, non-determinism (see `docs/RESEARCH.md` §8).

## M4 — Polish + Linux/Windows

**Goal:** ship it.
**Completion criteria:**
- [ ] Tested on a Linux dedicated server and a Windows dedicated server.
- [ ] Optional CPU affinity (behind a flag) or documented launcher flags.
- [ ] README with install/config/troubleshooting and a tested-mods table.
- [ ] `/multicore` command: status, regions, threads, legacy mods.

## M5 — Client feasibility

**Goal:** decide whether client support is worth it — **report first**.
**Completion criteria:**
- [ ] Written viability report (render thread constraints, integrated server vs dedicated).
- [ ] If approved, a separate milestone with its own plan.

---

## Roadmap change history

| Date | Change | Reason |
|------|-----------|--------|
| 2026-10-21 | Roadmap created (M0–M5) | Project started; user's enriched prompt (Phase 0–5) |
| 2026-10-21 | Added Mod Menu integration requirement (M1) | User requirement; proven in `RocketWithSpearMod` |
