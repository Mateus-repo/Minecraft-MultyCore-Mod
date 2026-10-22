# region

> Region model for the parallel tick (Phase 3). Folia-style: chunks are grouped into independently
> ticking regions, each owned by exactly one thread.

**Does not do:** tick Minecraft code directly (that is the mixin layer), or decide which regions
tick when (that is `RegionScheduler`).

## Public API

Entry point: `region/Region`, `region/RegionState`, `region/RegionSection`.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `Region` | `addSection(section)` · `removeSection(section)` · `tryMarkTicking()` · `markNotTicking()` | Region lifecycle; section ownership | `IllegalStateException` if ticking |
| `RegionState` | `TRANSIENT \| READY \| TICKING \| DEAD` | Region lifecycle state | — |
| `RegionSection` | `RegionSection(x,z)` · `static fromChunk(chunkX, chunkZ, chunkShift)` | Section coordinate | — |

## Usage example

```java
Region region = new Region();
region.addSection(RegionSection.fromChunk(100, 200, 4)); // 16x16 chunk sections
if (region.tryMarkTicking()) {
    // tick the region on this thread
    region.markNotTicking();
}
```

## Dependencies

- **Allowed:** JDK only.
- **Forbidden:** Minecraft classes, the worker pool, the compatibility layer.

## Notes and limitations

- A region is identified by its `id`; `equals`/`hashCode` are identity-based on id.
- `tryMarkTicking`/`markNotTicking` must be called by the same thread (the owner thread).
- A ticking region cannot add or remove sections (Folia invariant #3).

## Tests

`./gradlew test --tests "com.strefiz.multicore.region.*"` — unit tests in
`src/test/java/com/strefiz/multicore/region/`.
