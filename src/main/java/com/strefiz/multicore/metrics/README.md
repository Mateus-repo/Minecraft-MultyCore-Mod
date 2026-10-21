# metrics

> Lock-free tick and per-thread timing metrics.

**Does not do:** decide what to measure (the mixin/command layers call `startTick`/`beginWork`), or
store world state.

## Public API

Entry point: `metrics/TickMetrics`.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `TickMetrics` | `void startTick()` · `void endTick()` | Measure one server tick | — |
| | `void beginWork(String)` · `void endWork(String)` | Measure a labelled unit of work | unmatched `endWork` is ignored |
| | `double mspt()` · `double averageMspt()` · `double maxMspt()` | Last / average / worst tick time | — |
| | `long tickCount()` · `Map<String,Double> threadTimesMs()` | Totals | — |
| | `Snapshot snapshot()` · `void reset()` | Immutable view / clear | — |
| `Snapshot` | record `(lastMspt, averageMspt, maxMspt, tickCount, threadTimesMs)` | Immutable metrics view | — |

## Usage example

```java
metrics.startTick();
try {
    // ... server tick ...
} finally {
    metrics.endTick();
}
```

## Dependencies

- **Allowed:** JDK (`LongAdder`, `LongAccumulator`, `ConcurrentHashMap`).
- **Forbidden:** Minecraft world classes, locks.

## Notes and limitations

- No locks: totals use adders/accumulators, per-thread time uses concurrent maps.
- Measurement itself is intentionally tiny; it is not a profiler.
- `endWork` without a matching `beginWork` is ignored.

## Tests

`./gradlew test --tests "com.strefiz.multicore.metrics.*"` — tests on the public API in
`src/test/java/com/strefiz/multicore/metrics/`.
