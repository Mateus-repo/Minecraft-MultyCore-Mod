# Glossary — Minecraft MultiCore Mod

> One term, one meaning. Use these names in the code, the docs and in conversations.

| Term | Definition | Name in code |
|-------|-----------|----------------|
| **Worker thread** | A platform thread from our own pool that runs offloaded work. | `WorkerPool`, threads named `multicore-worker-N` |
| **Main thread** | The vanilla server tick thread. The only thread allowed to touch world state in SAFE mode. | `MultiCoreMod.MAIN_THREAD` |
| **SAFE mode** | Default. Fabric events dispatch on the main thread; workers never touch world state directly. | `ConfigMode.SAFE` |
| **AGGRESSIVE mode** | Opt-in. Region threads run world ticks; only mods declaring `threadSafe` may join. | `ConfigMode.AGGRESSIVE` |
| **Legacy mod** | A mod that did not declare `custom.multicore.threadSafe: true`. Runs on the main thread. | `ModClassification.LEGACY` |
| **Compatible mod** | A mod that declared `threadSafe: true`. May run on region threads. | `ModClassification.COMPATIBLE` |
| **Region** | A set of owned chunks plus a per-region data object, ticked by exactly one thread. | `region.Region` |
| **Region section** | An `N×N` chunk grid unit used by the regionizer. | `region.RegionSection` |
| **Transient region** | A buffer region around a ticking region; cannot tick, merges back after. | `RegionState.TRANSIENT` |
| **Owner thread** | The single thread allowed to write a given piece of world state. | `Region.ownerThread` |
| **Offload** | Work moved to a worker and merged back on the main thread before the tick ends. | `TaskScheduler.submitOffload` |
| **Hand-off** | Queuing a legacy callback to run on the main thread. | `LegacyHandoff.enqueue` |
| **MSPT** | Milliseconds per tick. The key performance metric (20 TPS = ≤ 50 ms). | `TickMetrics.mspt()` |
| **Region-local data** | Data stored per region so no concurrent access is needed. | `RegionLocal<T>` (Phase 2) |
| **Thread-safe declaration** | `"custom": { "multicore": { "threadSafe": true } }` in `fabric.mod.json`. | `ModCompatibility` |
| **Restart-required** | A config change that only takes effect after restarting the server. | config snapshot at `SERVER_STARTING` |
