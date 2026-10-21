# MODULES — Minecraft MultiCore Mod

> **Catalog** of modules and functions/components that are **reusable**. The first thing to check **before writing new code** (skill `reusable-modules`) and the last thing to update **before closing a task** (skill `code-documentation`).
> Quick search: `bash scripts/find-reuse.sh <term> [term…]`.
> Rule: if it is exported and used (or usable) outside its file, it gets a row here. Code and catalog change in the **same commit**. When they disagree, the code wins.

## Modules

| Module | Path | Purpose (1 sentence) | Public API (summary) | Depends on |
|--------|---------|---------------------|----------------------|------------|
| `config` | `config/` | parse/validate/persist the JSON config | `ConfigStore.load()/save()`, `MultiCoreConfig.normalized()`, `ConfigMode` | — |
| `thread` | `thread/` | own the worker threads and identify the main thread | `WorkerPool.start()/submit()/shutdown()`, `MainThread.isCurrent()` | — |
| _planned_ | `thread/TaskScheduler` | offload work and merge results on the main thread | `submitOffload(...)`, `drainToMain()` | `thread` |
| _planned_ | `metrics/TickMetrics` | MSPT + per-thread timing, lock-free | `startTick()`, `endTick()`, `mspt()`, `snapshot()` | `thread` |
| _planned_ | `command/MultiCoreCommand` | `/multicore` subcommands | `register(CommandDispatcher, CommandBuildContext)` | `config`, `metrics`, `compat` |
| _planned_ | `compat/ModCompatibility` | classify mods from `fabric.mod.json` | `classify(String)`, `isLegacy(String)` | `config` |
| _planned_ | `compat/LegacyHandoff` | queue legacy callbacks for the main thread | `enqueue(Runnable)`, `drain()` | `config` |
| _planned_ | `region/Regionizer` | Folia-style region maintenance | `addChunk`, `removeChunk`, `tryMarkTicking`, `markNotTicking` | `config` |
| _planned_ | `region/RegionScheduler` | run regions in parallel and join | `tickRegions(...)`, `awaitBarrier()` | `thread`, `compat` |

> Replace `_planned_` rows with real rows (and real signatures) as the code lands. Do not keep a row
> that no longer exists.

## Reusable functions, types and components

Group by **concept**, not by file. _(Empty until the first module is implemented.)_

| Name | Path | Signature | What it does | Pure? | Errors | Used by |
|------|---------|------------|-----------|-------|-------|-----------|
| `ConfigStore` | `config/ConfigStore.java` | `ConfigStore(Path)` · `static defaultStore()` · `MultiCoreConfig load()` · `void save(MultiCoreConfig)` | JSON config load/save in `config/multicore.json`; defaults on any error | no (I/O) | never throws | `MultiCoreMod`, Mod Menu screen |
| `MultiCoreConfig` | `config/MultiCoreConfig.java` | `workerThreads()`, `mode()`, `debug()`, `modWhitelist()`, `modBlacklist()`, `threadCount()`, `multithreaded()`, `normalized()` | Config data + validation | `normalized()` yes, rest no | — | `WorkerPool`, `ModCompatibility` |
| `ConfigMode` | `config/ConfigMode.java` | `SAFE` \| `AGGRESSIVE` · `static fromString(String)` | Execution mode; unknown → `SAFE` | yes | — | `MultiCoreConfig` |
| `WorkerPool` | `thread/WorkerPool.java` | `WorkerPool(String,int)` · `start()` · `boolean submit(Runnable)` · `submitAll(...)` · `shutdown()` | Named worker pool with lifecycle | no | never throws | `MultiCoreMod`, `TaskScheduler` |
| `MainThread` | `thread/MainThread.java` | `static capture()` · `static boolean isCurrent()` · `static Thread get()` | Identify the server main thread | yes | — | all modules |

## Known duplications (to resolve)

_None yet._

## Catalog conventions

- Remove the row when you remove the code. Don't leave orphan entries.
- Signature or errors changed → update the row **and** the docstring **and** the module's README.
- A private function (used only inside its own module) does **not** go here.
