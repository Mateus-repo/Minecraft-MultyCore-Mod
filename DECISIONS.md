# DECISIONS — Minecraft MultiCore Mod

> Decision log (short ADR). **Always record** when you choose between real alternatives, especially when *you* decided instead of asking.
> Read before reopening a topic. To change a decision: create a new one that **replaces it** (do not delete the old one).
> Status: `Accepted` · `Provisional` (assumed without the user's confirmation — revisit if it matters) · `Superseded by D-NNN`.

## Template

```
### D-NNN — Title  (YYYY-MM-DD) — Status
- **Context:** the problem and the constraints.
- **Options:** A) … B) … C) …
- **Analysis (5 angles):** precedent · intent · external evidence · risk/reversibility · simplicity — 1 line each.
- **Decision:** the chosen one and why.
- **Assumptions:** what I assumed without confirming.
- **How to revert:** steps/cost.
- **Consequences:** what changes; what becomes harder.
```

## Decisions

### D-001 — Use the repository template with management documents  (2026-10-21) — Accepted
- **Context:** a new project, developed with AI agents during long sessions.
- **Decision:** keep the project state in versioned `.md` files (`PROGRESS`, `TODO`, `DECISIONS`…) to survive context compression and session handoffs.
- **How to revert:** delete the management files and simplify `AGENTS.md`.

### D-002 — Modular, reusable, documented code by default  (2026-10-21) — Accepted
- **Context:** much code is written by agents in long sessions; without rules, duplication, `utils` junk drawers and modules nobody knows how to use appear.
- **Decision:** reuse before writing (`docs/MODULES.md` + `scripts/find-reuse.sh`); pure functions and modules with a minimal public API; structure by feature; everything public documented (docstring + module README). Skills `reusable-modules`, `project-structure`, `code-documentation`.
- **How to revert:** remove the three skills and the corresponding rules in `AGENTS.md` §5 and `docs/CONVENTIONS.md`.

### D-003 — Server-side first; the client is a later, separate milestone  (2026-10-21) — Accepted
- **Context:** the user wants the multithreading to work on Fabric servers (Linux/Windows) first, and only later try the client.
- **Options:** A) `environment: "*"` with client + server entrypoints; B) `environment: "server"` core with a `server` entrypoint, client later.
- **Analysis:** precedent — the client render thread has hard constraints and the user deprioritised it; intent — dedicated servers first; external evidence — every multithreaded server project starts server-only; risk — B means Mod Menu config only applies when a client is present (acceptable, documented); simplicity — B keeps the classpath and the threading core free of client concerns.
- **Decision:** B. Entrypoint `"server"` → `DedicatedServerModInitializer`. Client support is Phase 5 (feasibility report first).
- **How to revert:** switch `environment` to `*` and add a `client` entrypoint in a later commit.

### D-004 — Build stack and identity  (2026-10-21) — Accepted
- **Context:** need a reproducible Fabric build for 26.3.
- **Options:** A) copy the proven `ClientHarvestMod`/`RocketWithSpearMod` build files; B) start from the Fabric example template.
- **Analysis:** precedent — both sibling repos compile 26.3 today; external evidence — Loom 1.17-SNAPSHOT + Gradle 9.8.0 is the combo that produced a jar; risk — A is proven, B risks version drift; simplicity — A.
- **Decision:** Gradle 9.8.0 wrapper, Loom 1.17-SNAPSHOT, Java 25 toolchain, Fabric API 0.160.5+26.3, package `com.strefiz.multicore`, mod id `multicore`, archives name `multicore`.
- **How to revert:** adjust `gradle.properties` / `build.gradle` and regenerate the wrapper.

### D-005 — Default to legacy: a mod is thread-safe only if it says so  (2026-10-21) — Accepted
- **Context:** how to decide which mods may run on region threads.
- **Options:** A) try to auto-detect; B) opt-in via `fabric.mod.json` custom field.
- **Analysis:** precedent — Folia requires `folia-supported: true`; external evidence — auto-detection of thread safety is impossible; intent — unmodified mods must keep working; risk — authors must edit their metadata; simplicity — B is one line.
- **Decision:** a mod declaring `custom.multicore.threadSafe == true` is compatible; everything else is legacy and runs on the main thread. Reversible by changing one value.
- **How to revert:** treat all mods as legacy (remove the compat scan).

### D-006 — Expose worker-thread count, not "cores"  (2026-10-21) — Accepted
- **Context:** the user asked for "2 cores by default, up to 6". Java cannot pin threads to cores.
- **Options:** A) pretend to set cores; B) expose `workerThreads` and make affinity optional.
- **Analysis:** external evidence — no portable Java CPU-affinity API; Folia/Moonrise expose thread counts; intent — real, honest control; simplicity — B is a number plus an optional best-effort affinity.
- **Decision:** config has `workerThreads` (default `1` → 2 threads total: 1 main + 1 worker). CPU affinity is an optional Phase 4 flag using FFM/JNI or OS launcher flags, with a clear no-op warning.
- **How to revert:** remove the affinity code; keep `workerThreads`.

### D-007 — Integrate Mod Menu for in-game config, as an optional dependency  (2026-10-21) — Accepted
- **Context:** the user requires Mod Menu compatibility, proven in `RocketWithSpearMod`.
- **Options:** A) hard-depend on Mod Menu; B) `compileOnly` + `localRuntime` and a `suggests` entry.
- **Analysis:** precedent — `RocketWithSpearMod` does exactly B; external evidence — Mod Menu is a client mod and must be optional on servers; intent — no forced dependency; risk — the config screen is only reachable when a client is present.
- **Decision:** B. `fabric.mod.json` registers a `modmenu` entrypoint and `suggests.modmenu: "*"`. On a dedicated server the file + `/multicore` remain authoritative.
- **How to revert:** delete `compat/ModMenuIntegration`, the screen, and the `modmenu` entrypoint.

### D-008 — Configuration is a JSON file applied at server start  (2026-10-21) — Accepted
- **Context:** threading/region settings cannot change mid-tick safely.
- **Options:** A) hot-reload everything; B) apply at server start, warn on change.
- **Analysis:** external evidence — thread pools and region ownership are established at boot; intent — safety; risk — user confusion; simplicity — B.
- **Decision:** single JSON config (`config/multicore.json`) parsed once at `SERVER_STARTING`; the Mod Menu screen and `/multicore` only *persist* changes and display a **restart-required** notice.
- **How to revert:** add a reload path guarded by a debug flag.

### D-009 — SAFE mode is the default; AGGRESSIVE is opt-in  (2026-10-21) — Accepted
- **Context:** the user's core requirement is that unsupported mods keep working.
- **Decision:** default `mode = "SAFE"` (Fabric events dispatched on the main thread; world access from workers routed back). `mode = "AGGRESSIVE"` only for verified modpacks. With `workerThreads = 0` the mod is behaviourally equivalent to vanilla.
- **How to revert:** change the default in config.

### D-010 — Be additive; do not replace C2ME/Lithium/VMP  (2026-10-21) — Accepted
- **Context:** several optimisers already touch the same subsystems.
- **Decision:** Phase 1 offloads only what is still on the main thread and is safe; detect and disable overlaps with C2ME/Moonrise/VMP; never claim determinism (C2ME documents that world gen already varies run-to-run).
- **How to revert:** add an offload back; document the tested combination.

### D-011 — Project is documented in English; UI text in pt-PT/en-US  (2026-10-21) — Accepted
- **Context:** the user writes in Portuguese but the repo conventions and commits are English.
- **Decision:** management docs, docstrings and commits in English; in-game strings via `assets/multicore/lang/{en_us,pt_pt}.json` (Portuguese first, matching the sibling mods).
- **How to revert:** translate docs.
