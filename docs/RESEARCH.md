# RESEARCH — Minecraft MultiCore Mod

> **Phase 0 deliverable.** Everything we know *before writing code*: prior art, the Minecraft 26.3
> server-tick anatomy, the shared-state surface, Java concurrency, Fabric's modding surface,
> compatibility strategy and risks. Written so the project never "loses its place" after a
> context reset.
>
> **Legend for status:** `✅ verified (read the code/docs)` · `🔶 documented by the community` ·
> `❓ needs a spike` · `⚠️ assumption`.
>
> Last updated: 2026-10-21

---

## 0. Executive summary

| Question | Answer |
|----------|--------|
| Can a Minecraft server run on multiple cores? | Yes, but **only by re-architecting the tick**, exactly like Folia/Moonrise do. There is no switch. |
| What gives the biggest, safest win first? | Infrastructure + offloading of *already-isolated* work (chunk I/O, light, random-tick candidate selection, pathfinding cache) + metrics. |
| What breaks mods? | Anything that assumes "one thread owns the world". Folia states compatibility ≈ 0 for unmodified Bukkit plugins; we must be equally honest for Fabric mods. |
| How do we keep legacy mods safe? | They never leave the main thread: their Fabric event callbacks are handed off to the main thread, and world access from worker threads is routed back. Mixin-based mods cannot be fully protected (documented limitation). |
| Is "N cores" a real setting? | Java cannot pin threads to cores. We expose **worker thread count**; CPU affinity is an optional Phase 4 extra (FFM/JNI or OS launcher flags). |
| Target | Fabric server-side mod for **Minecraft 26.3**, Java 25, Gradle 9.8.0, Loom 1.17-SNAPSHOT, Fabric API 0.160.5+26.3. |

**Bottom line:** the idea is technically sound and there is strong prior art. The hard part is not
"starting threads" — it is **defining ownership of mutable world state** and **keeping the legacy
mod ecosystem alive**. The roadmap is therefore: *infrastructure → compatibility layer →
regionised tick → polish → (client, feasibility only)*.

---

## 1. Scope and target versions

### 1.1 Target stack (locked)

| Component | Version | Source |
|-----------|---------|--------|
| Minecraft | **26.3** | `ClientHarvestMod/gradle.properties` (`minecraft_version=26.3`) |
| Fabric Loader | **0.19.5** | same, and the loader jar is cached locally |
| Fabric API | **0.160.5+26.3** | same |
| Fabric Loom | **1.17-SNAPSHOT** | same |
| Java | **25** (toolchain) | JDK `C:\Program Files\Java\jdk-25.0.4` is installed |
| Gradle | **9.8.0** (wrapper dist cached at `~/.gradle/wrapper/dists`) | the ClientHarvestMod build succeeded with 9.8.0 (its `.gradle/8.13` has no execution history) |
| Environment | **Server-side** (`DedicatedServerModInitializer`) | see §6.1 |
| OS | **Linux + Windows** (dedicated server) | user requirement |
| In-game config | **Mod Menu** (`com.terraformersmc:modmenu:21.0.0`, optional) | user requirement; pattern proven in `RocketWithSpearMod` |

⚠️ **Assumption:** the `26.3` / `0.19.5` / `0.160.5` combo is the one that compiles today (proven by
`ClientHarvestMod/build/libs/rightclick-replant-1.0.0.jar`). We reproduce its build files.

### 1.2 Out of scope for now (see `ROADMAP.md`)

- Client-side (rendering thread has hard constraints; integrated-server only later).
- NeoForge/Forge.
- Changing vanilla behaviour in any way that is observable to players.

---

## 2. Prior art — what already exists, what it does, and what it broke

This is the single most important input: we are **not** the first to try this, and every existing
project tells us where the traps are.

### 2.1 Folia (PaperMC) — the reference design ✅🔶

- Splits loaded chunks of every world into **independently ticking regions**; each region has its
  own tick loop executed on a thread pool, in parallel, at 20 TPS. **There is no main thread.**
- Regionisation rules (from `docs.papermc.io/folia/reference/region-logic`):
  - A **region** = a set of owned chunk positions + a per-region data object (tick lists, entity
    lists, tick counters) only touched by the owning thread.
  - The regionizer works on **region sections** = an `N×N` grid of chunks (`N` a power of two,
    `regionSectionCoord = chunkCoord >> log2(N)`), so regions own *sections*, not raw chunks.
  - Four invariants:
    1. each existing chunk has exactly one owning region;
    2. every chunk within the merge radius of a chunk is owned by the same region;
    3. a **ticking region cannot grow** (it may not acquire new sections while ticking);
    4. a region is in exactly one state: `transient | ready | ticking | dead`.
  - Ticking regions create "transient" buffer regions around them; those merge back after the tick.
  - Merge/split adjust tick deadlines (redstone tick, current tick) with an offset so relative
    deadlines are preserved.
- Tick counters: `Current Tick` and `Redstone Time` are **per region**; `Global Game Time` and
  `Daylight Time` live in the **global region**. Region reads of global time use a copy taken at
  tick start so repeated reads agree.
- **Global region**: one always-running 20 TPS task for non-spatial state (game rules, global time,
  daylight, console commands, world border, weather).
- Inter-region ops use `EntityScheduler`, `RegionizedTaskQueue`, global-region task queue and
  `RegionizedData`; teleports are split into *transform* + *async place*.
- **What it broke:** the project itself says *"expect compatibility at 0"* and requires
  `folia-supported: true` in `plugin.yml`; without it the plugin refuses to load. Region-safe
  plugins must use `RegionScheduler`/`EntityScheduler`, use thread-safe collections, and never
  share plain `HashMap`/`ArrayList` across regions.
- Takeaway for us: **opt-in compatibility is the industry-accepted answer**, and the region
  section / merge-radius / transient-region machinery is the proven shape. Our Phase 3 copies the
  *model*, not the Paper patches.

### 2.2 C2ME (RelativityMC) — chunk concurrency ✅

- "Concurrent Chunk Management Engine": parallelises **chunk generation, I/O and loading**.
- Uses its own rewritten chunk I/O; `globalExecutorParallelism` sets the executor size.
- Ships `CheckedThreadLocalRandom` / "undefined behaviour sanitisation" to detect off-thread world
  random access that would corrupt worlds.
- **Compatibility:** datapacks that work on vanilla work; **custom world generators in mods may
  break** because they assume generation is single-threaded.
- Moonrise declares itself **incompatible** with C2ME (both replace the chunk system).
- Takeaway: offloading chunk *generation/IO* is a solved, valuable problem; **reuse or complement
  C2ME rather than duplicate it**, and add thread-safety instrumentation (§4.4).

### 2.3 Lithium (CaffeineMC) ✅

- General optimisation (client + server) that "does not change vanilla behaviour": entity
  movement/collisions/physics/tracking, chunk ticking/loading, block & entity retrieval,
  collections, AI, redstone, world gen. Compatible with most mods; per-optimisation config toggles.
- Takeaway: many single-threaded algorithmic wins already exist. Our mod should be **additive**, not
  a replacement for Lithium; and we must not regress its behaviour.

### 2.4 VMP (RelativityMC) ✅

- "Very Many Players": high-playercount server optimisation — area maps for packet sending/player
  lookups, caches for entity trackers, fluid state lookups, ingredient matching, entity iteration
  for collisions, ticket propagator. Also client time-source and networking improvements.
- Takeaway: the *non-world-state* server hot paths (packet fan-out, trackers, collision iteration)
  are already being optimised; our Phase 1 offload should not duplicate VMP.

### 2.5 Moonrise (Tuinity) ✅

- Dedicated/integrated-server optimisation: entity movement/collisions/physics/tracking, chunk
  ticking/loading/generation/saving, block/entity retrieval; responsiveness improvements (packet
  priority, lower worker-thread count on low-core systems, reduced TPS catch-up).
- Known conflicts: disables conflicting parts of Lithium/FerriteCore; incompatible with C2ME.
- Takeaway: on low-core machines the best move can be **fewer** worker threads; our config must make
  worker count a first-class knob with sensible defaults.

### 2.6 Other relevant projects 🔶

- **Paper async chunks** — async chunk loading/saving (I/O only, not generation).
- **Sodium** (client) — render-thread parallelism; shows client-side world-gen is already off the
  render thread (integrated server).
- **Starlight / ScalableLux** — rewritten light engines (threaded).
- **MultiPaper / ShreddedPaper** — multithreaded server cores; same "rewrite your plugin" caveat.

### 2.7 The gap we fill

Every project above is either (a) a **server fork** (Folia, MultiPaper) or (b) an **optimiser**
(C2ME/Lithium/VMP/Moonrise). **No one ships a *drop-in* multithreading mod for Fabric with a
legacy-compat mode.** That is our niche: a Fabric mod that can be installed on a normal server,
with a **SAFE mode** where unmodified mods keep working, and an **AGGRESSIVE mode** for servers that
opt in.

---

## 3. Minecraft 26.3 server-tick anatomy ✅

Verified by `javap` against the Loom-remapped merged jar
(`.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-6974b2190e/26.3/`).

### 3.1 The call chain

```
MinecraftServer.runServer()
  └─ tickServer(BooleanSupplier hasTimeLeft)
       ├─ tickConnection()                // network/packet loop
       ├─ processPacketsAndTick(boolean)  // player packets then tick
       └─ tickChildren(BooleanSupplier)   // <-- the interesting part
            └─ for each ServerLevel level:
                 level.tick(hasTimeLeft)
                 level.getChunkSource().tick(hasTimeLeft, true)   // chunk system
```

`MinecraftServer` also owns: `getTickCount()`, `getNextTickTime()`, `addTickable(Runnable)`,
`getAllLevels()`, `getLevel(ResourceKey)`, `levelKeys()`, `enforceGameTypeForPlayers(...)`,
`isUnderSpawnProtection(...)`.

`ServerLevel.tick(BooleanSupplier)` is the heart. Observed members of `ServerLevel` (selected):

| Member | Role | Thread-safety concern |
|--------|------|-----------------------|
| `players: List<ServerPlayer>` | players in this level | mutated on join/leave |
| `chunkSource: ServerChunkCache` | chunk system, already has executors | mixed |
| `entityTickList: EntityTickList` | entities to tick this tick | **mutable, main-thread only** |
| `entityManager: PersistentEntitySectionManager` | entity sections/lookups | **mutable** |
| `blockTicks / fluidTicks: LevelTicks` | scheduled block/fluid ticks | **mutable** |
| `blockEvents / blockEventsToReschedule: List<BlockEventData>` | block events | **mutable** |
| `navigatingMobs: Set<Mob>` + `isUpdatingNavigations: boolean` | pathfinding batch | **mutable** |
| `pathTypesByPosCache: PathTypeCache` | path type cache | **mutable** |
| `customSpawners: List<CustomSpawner>` | mob spawning | **mutable** |
| `gameEventDispatcher`, `portalForcer`, `structureManager`, `structureCheck` | world systems | **mutable** |
| `sleepStatus: SleepStatus`, weather data | world-global | **mutable** |
| `handlingTick: boolean` | re-entrancy guard | **mutable** |

`ServerLevel` public tick API: `tick(BooleanSupplier)`, `tickTime()`, `tickChunk(LevelChunk,int)`,
`tickNonPassenger(Entity)`, `tickThunder`, `tickPrecipitation`, `tickCustomSpawners(boolean)`,
`runBlockEvents()`, `tickBlock/tickFluid(BlockPos,...)`, `tickPassenger(...)`,
`startTickingChunk(LevelChunk)`, `waitForEntities(ChunkPos,int)`.

### 3.2 The chunk system

- `ServerChunkCache` owns a `ChunkTaskDispatcher` and a `MainThreadExecutor` (`ReentrantBlockableEventLoop`).
- `ChunkMap` owns `entityMap: Int2ObjectMap<TrackedEntity>`, `poiManager: PoiManager`,
  `ticketStorage`, `chunkStatusListener`, and the `Executor` for chunk tasks.
- `ChunkMap.tick(BooleanSupplier)` drives **entity ticking** (via entity sections) and
  `ChunkMap.prepareTickingChunk(...)` / `prepareEntityTickingChunk(...)` return
  `CompletableFuture<ChunkResult<LevelChunk>>` — **chunk generation is already async** and handed back
  to the main thread.
- `ThreadedLevelLightEngine` already runs light updates on a `ConsecutiveExecutor`/`ChunkTaskDispatcher`
  and only applies results on the main thread.

### 3.3 Scheduled ticks

`LevelTicks<T>` (used for both `Block` and `Fluid`) has `tick(long currentTick, int maxTicks,
BiConsumer<BlockPos,T> consumer)`, `schedule(...)`, `hasScheduledTick(...)`, `willTickThisTick(...)`,
`clearArea(...)`, `copyArea(...)`, `count()`. It is a **plain, unsynchronised** structure keyed by
`ChunkPos` containers.

### 3.4 What this means for us

- **Already parallel/isolated in vanilla:** chunk I/O, chunk generation, light updates. Do not rebuild.
- **Still main-thread and mutable:** entity ticking (`entityTickList`), block/fluid scheduled ticks,
  block events, pathfinding, mob spawning, game events, portals, world-global counters.
- Therefore Phase 3 must **take ownership of these structures per region**, exactly as Folia does.

---

## 4. Shared mutable state and the race-condition surface

This section is the "what can go wrong" list that drives the design.

### 4.1 The world-state ownership model

The only safe model is **ownership**: at any instant, for any given piece of world state, there is
exactly one thread allowed to *write* it, and other threads must communicate through a queue.

| State | Owner today | Owner after Phase 3 |
|-------|-------------|---------------------|
| Entity list / entity sections of a region | main thread | region thread |
| Block/fluid scheduled ticks of a region | main thread | region thread |
| Block events of a region | main thread | region thread |
| Pathfinding (`navigatingMobs`, path type cache) | main thread | region thread (or offloaded & merged) |
| Global game time / daylight / weather / game rules | main thread (global region) | global region |
| Chunk map / entity map (spatial index) | main thread | must be partitionable or locked |
| POI manager | main thread | region-partitioned or locked |
| Network/tracker state | main + Netty loops | unchanged (Netty already async) |

### 4.2 Cross-region interactions (the dangerous ones)

Any of these crossing a region boundary requires a **message**, never direct access:

- Entity (or player) moving/teleporting from region A to region B.
- Redstone / block updates / explosions propagating across a boundary.
- `copyArea` / structure moves spanning regions.
- Portal / dimension travel.
- Chunk load/unload near a boundary (invariant #2/#3).
- A legacy mod touching another region's data from the main thread.

### 4.3 Mod data structures

Folia's guidance applies verbatim: plugin/mod-held `HashMap`/`ArrayList` shared across regions are a
data-corruption source; use `ConcurrentHashMap`, atomics, or **region-local** storage. We must expose
the same guidance in our API (a `RegionLocal<T>` accessor).

### 4.4 Instrumentation ("undefined behaviour sanitisation")

C2ME ships `CheckedThreadLocalRandom`. We will ship, in debug mode:
- a **thread-check on world access** that logs/throws when a non-owner thread touches region data;
- a **legacy-mod access report** (`/multicore compat dump`) listing which mod touched what.

This is what makes SAFE mode trustworthy instead of merely "usually works".

---

## 5. Java concurrency model for this project

### 5.1 Threads vs cores ✅

- Java has **no portable API to pin a thread to a core**. `Thread.setPriority` is a hint only.
- The JVM maps Java threads to OS threads (1:1) unless virtual threads are used. **Virtual threads are
  unsuitable here**: our work is CPU-bound and long-lived with no blocking, so platform threads are
  correct (and virtual threads cannot be pinned either).
- CPU affinity must come from either:
  1. the **OS launcher** (`taskset`/`numactl` on Linux, `start /affinity` on Windows) — recommended,
     zero dependency; or
  2. **native code** (JNI or the Java 22+ **Foreign Function & Memory API**) calling
     `sched_setaffinity` (Linux) / `SetThreadAffinityMask` (Windows).
- **Decision (D-006):** expose `workerThreads` in config; provide affinity as an *optional, best-effort*
  Phase 4 feature behind a flag, with a clear warning that it may be a no-op on some platforms.

### 5.2 Primitives we will use

| Need | Primitive | Note |
|------|-----------|------|
| Worker pool | `ExecutorService` (fixed pool, named threads, `ThreadFactory`) | **never** the common pool |
| Per-tick fan-out + join | `CyclicBarrier` / `CountDownLatch` / `CompletableFuture.allOf` | barrier aligns with tick boundary |
| Cross-thread hand-off | `LinkedBlockingQueue<Runnable>` + drain on main thread | for legacy callbacks |
| Region-local data | custom `RegionLocal<T>` | mirrors Folia's `RegionizedData` |
| Metrics | `LongAdder`, `LongAccumulator`, lock-free ring buffers | no locks on the hot path |
| Config reload | `volatile` immutable config object | single-writer |

### 5.3 Scheduling: EDF-like, per region

Folia schedules regions on an earliest-start-time-first executor (EDF-like) so a region that falls
behind does not drag the others. Our scheduler will implement the same idea for Phase 3
(`region-deadline` based), and a simple work-stealing queue for Phase 1 offloads.

---

## 6. Fabric modding surface (verified against the actual 26.3 jars) ✅

### 6.1 Entrypoints

| Interface | Method | Entrypoint key | Use |
|-----------|--------|----------------|-----|
| `net.fabricmc.api.ModInitializer` | `onInitialize()` | `main` | common init (both sides) |
| `net.fabricmc.api.DedicatedServerModInitializer` | `onInitializeServer()` | `server` | **our entrypoint** |
| `EnvType` | `CLIENT` / `SERVER` | — | environment checks |

### 6.2 Events we need (`fabric-lifecycle-events-v1` 4.1.9)

- `ServerLifecycleEvents.SERVER_STARTING / SERVER_STARTED / SERVER_STOPPING / SERVER_STOPPED`
  (`ServerStarting(Server)`, `ServerStarted(Server)`, `ServerStopping(Server)`, `ServerStopped(Server)`).
- `ServerTickEvents.START_SERVER_TICK(MinecraftServer)` / `END_SERVER_TICK` /
  `START_LEVEL_TICK(ServerLevel)` / `END_LEVEL_TICK(ServerLevel)`.

These are **before/after dispatch** events: not cancellable, called synchronously on whatever thread
vanilla is on. This is the hook that lets us (a) measure and (b) drive our scheduler.

### 6.3 Commands (`fabric-command-api-v2` 3.1.2)

- `CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> { ... })`
- Build commands with Brigadier (`LiteralArgumentBuilder`, `Commands.literal(...).requires(...)`).

### 6.4 Mixins

- Standard SpongePowered Mixin; declare in `src/main/resources/<modid>.mixins.json`, with a refmap
  when targeting non-obvious members.
- `@Inject`, `@Redirect`, `@WrapOperation` (Fabric's `mixins` from Fabric API / official mixin extras)
  to hook `MinecraftServer.tickServer` / `tickChildren` / `ServerLevel.tick`.
- **Mixin-based mods run inside vanilla on whatever thread vanilla is on** — this is the fundamental
  limitation of SAFE mode (see §7.3).

### 6.5 Reading mod metadata for compatibility (`fabric-loader` 0.19.5)

```java
FabricLoader.getInstance().getAllMods();                 // Collection<ModContainer>
ModContainer.getMetadata();                              // ModMetadata
ModMetadata.containsCustomValue(String);                 // boolean
ModMetadata.getCustomValue(String);                      // CustomValue
CustomValue.getAsObject()  -> CvObject  (Iterable<Entry<String,CustomValue>>)
CustomValue.getAsString()  -> String
CustomValue.getAsNumber()  -> Number
CustomValue.getAsBoolean() -> boolean
```

`fabric.mod.json` supports an arbitrary `"custom"` object, so a mod can declare:

```json
"custom": { "multicore": { "threadSafe": true } }
```

⚠️ **Assumption (D-005):** a mod that does *not* declare `threadSafe: true` is treated as **legacy**,
even if the author simply forgot. This mirrors Folia's `folia-supported` rule and is the only way to
be safe by default. Legacy mods keep working on the main thread.

### 6.7 In-game configuration via Mod Menu (TerraformersMC) ✅

**User requirement:** the mod must be compatible with
[Mod Menu](https://github.com/TerraformersMC/ModMenu) so the user can edit the configuration
in-game, with a clear **"restart Minecraft to apply"** warning.

We already have a **working, proven implementation** in the user's own project
`C:\Users\Strefiz\Documents\GitHub\RocketWithSpearMod`. The exact pattern to copy:

1. **Build (`build.gradle`)** — add the Terraformers maven and depend on Mod Menu only for
   compile/runtime-of-the-dev-game (it must *not* be a hard dependency):
   ```groovy
   repositories { maven { name = "TerraformersMC"; url = "https://maven.terraformersmc.com/" } }
   dependencies {
       compileOnly "com.terraformersmc:modmenu:21.0.0"
       localRuntime "com.terraformersmc:modmenu:21.0.0"   // or runtimeOnly for the run config
   }
   ```
2. **`fabric.mod.json`** — register the entrypoint and *suggest* (not require) Mod Menu:
   ```json
   "entrypoints": { "modmenu": ["com.strefiz.multicore.compat.ModMenuIntegration"] },
   "suggests": { "modmenu": "*" }
   ```
3. **Integration class** implements `com.terraformersmc.modmenu.api.ModMenuApi`:
   ```java
   public class ModMenuIntegration implements ModMenuApi {
       @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return ModConfigScreen::new; }
   }
   ```
4. **Screen** extends `net.minecraft.client.gui.screens.Screen`, renders toggles/sliders, and
   persists on every change (see `RocketWithSpearMod/config/ModConfigScreen.java`).
5. **Config** is a plain JSON file in the Minecraft `config/` dir, loaded lazily and written on
   every change (see `RocketWithSpearMod/config/ModConfig.java`). Gson is bundled with Minecraft, so
   there is no extra dependency.

**Important nuance for a server-side mod (documented, D-007):**

- The *authoritative* configuration of a **dedicated server** is the file on the server
  (`config/multicore.json`) plus the `/multicore` command; there is no client GUI there.
- The Mod Menu screen is useful for the **integrated server / singleplayer / LAN** case (client and
  server share the `config/` folder) and for development.
- **Changing threading/region settings requires a full restart of the server** (and of the game in
  singleplayer). The screen must state this clearly and must *not* claim the change is live. The
  config object is only swapped at server start (see D-008).
- A `environment: "server"` mod is loaded on the dedicated server and on the server side of an
  integrated server; the `modmenu` entrypoint is a client entrypoint, so it is only exercised when
  a client is present (singleplayer/LAN/dev). On a dedicated server the entrypoint simply never
  runs. This is intentional.

### 6.6 Reading the mod's own identity from a listener (Phase 2)

To distinguish legacy vs compatible listeners at *dispatch* time we will map a listener object to a
mod via its class's `ProtectionDomain` → `CodeSource` → jar path → `ModContainer`. This requires a
mixin on Fabric's event implementation to capture registrations (spike planned, §9).

---

## 7. Compatibility strategy (the core of the product)

### 7.1 Two modes

| Mode | Who runs where | When to use |
|------|----------------|-------------|
| **SAFE** (default) | All Fabric events are dispatched on the **main thread**; region ticks run on workers but hand off legacy callbacks; world access from workers is routed back. | Normal servers with unmodified mods. |
| **AGGRESSIVE** | Events dispatch on the region thread; mods must opt in (`threadSafe: true`). Legacy mods are expected to break. | Servers where the author has verified every mod. |

### 7.2 Why "SAFE" is the right default

- Folia's own conclusion: opt-in compatibility, compatibility ≈ 0 otherwise. The user's requirement
  ("mods without support keep working") demands the same.
- SAFE mode must be **equivalent to vanilla when `workerThreads = 0`** (see acceptance criteria).

### 7.3 Honest limitations (must be in the README)

1. **Mixin-based mods cannot be protected.** If a mod mixes into `Entity.tick()` and runs on a
   region thread, it will race. SAFE mode protects *Fabric-event-based* mods.
2. **Datapacks** that assume main-thread execution may also misbehave.
3. **World corruption is possible** if a legacy mod writes to another region's data from the main
   thread while that region ticks. Our debug instrumentation aims to *detect* this, not prevent it.
4. Not a substitute for testing on your specific modpack.

### 7.4 The compatibility API (Phase 2)

- A public `MultiCoreCompat` API:
  - `boolean isThreadSafe()` on our own context;
  - `RegionLocal<T>` for region-local storage;
  - `MultiCoreCompat.runOnRegion(Entity/BlockPos, Runnable)` / `runOnMain(Runnable)`;
  - a `fabric.mod.json` declaration `"multicore": { "threadSafe": true }` for mod authors.

---

## 8. Risk register

| # | Risk | Severity | Mitigation |
|---|------|----------|------------|
| R1 | Regionised tick corrupts world state | **Critical** | SAFE default; debug thread-check; determinism tests; never merge a refactor with a behaviour change; fall back to main thread on any anomaly. |
| R2 | Legacy mixin-based mods race and crash | High | Document; SAFE mode protects events; provide `runOnMain`; report incompatible mods. |
| R3 | Deadlocks between region threads (cross-region locks) | High | Never hold a region lock while waiting for another region; use async messages only; impose a global ordering on multi-region ops. |
| R4 | Performance regression on low-core machines | Medium | Configurable worker count; default conservative; Moonrise's "fewer threads on low-core" lesson. |
| R5 | Non-determinism (C2ME already warns world gen varies run-to-run) | Medium | Do not claim determinism; document; compare against vanilla by seed only loosely. |
| R6 | Incompatibility with C2ME / Moonrise / VMP | Medium | Detect and disable overlapping subsystems; document tested combos. |
| R7 | Fabric event dispatch hand-off becomes a bottleneck | Medium | Only hand off in SAFE mode; compatible mods opt into region-thread events. |
| R8 | Snapshots/renames of vanilla methods | Medium | Pin to 26.3; re-verify mixins each release; keep mixins small. |
| R9 | Build environment (Loom SNAPSHOT, no Gradle on PATH) | Low/Medium | Cache Gradle 9.8.0 dist; commit the wrapper; document Java 25 toolchain. |
| R10 | Mod Menu config edits are not live (need restart) and a dedicated server has no client GUI | Low | Warn in the screen; document file + `/multicore` as authoritative on servers; apply config only at server start. |

---

## 9. Open questions / planned spikes

- **S1 (≤15 min):** can a mixin on Fabric's `ArrayBackedEvent` implementation capture listener→mod
  mapping reliably? Needed for per-listener (not per-event) SAFE hand-off. If not, fall back to
  "hand off all Fabric events in SAFE mode".
- **S2:** exact body of `MinecraftServer.tickChildren` in 26.3 (decompile or infer from bytecode) to
  know the precise loop we are replacing.
- **S3:** which specific offloads give measurable MSPT wins on 26.3 without touching world state
  (candidate: random-tick candidate precompute, pathfinding cache warm, POI lookups).
- **S4:** is FFM CPU affinity feasible on both Linux and Windows without a native helper?

---

## 10. Sources

- Folia overview — <https://docs.papermc.io/folia/reference/overview/>
- Folia region logic — <https://docs.papermc.io/folia/reference/region-logic/>
- Folia migration guide (DeepWiki) — <https://deepwiki.com/PaperMC/Folia/3.4-migration-guide>
- C2ME — <https://github.com/RelativityMC/C2ME-fabric>
- Lithium — <https://github.com/CaffeineMC/lithium>
- VMP — <https://modrinth.com/mod/vmp-fabric>
- Moonrise — <https://github.com/Tuinity/Moonrise>
- Fabric lifecycle events README — <https://github.com/fabricmc/fabric-api/blob/26.2/fabric-lifecycle-events-v1/README.md>
- Fabric events — <https://docs.fabricmc.net/develop/events/>
- Fabric mixin intro — <https://wiki.fabricmc.net/tutorial:mixin_introduction>
- `fabric.mod.json` spec — <https://docs.fabricmc.net/develop/loader/fabric-mod-json>
- Java virtual threads — <https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html>
- CPU affinity via JNI — <https://medium.com/@shirshakupadhayay182/breaking-out-of-the-jvm-implementing-cpu-affinity-with-jni-96d7a7720626>
- Local evidence: `ClientHarvestMod/` (working 26.3 Fabric build), Loom-remapped `minecraft-merged-…-26.3.jar`, cached Fabric API/loader jars.
