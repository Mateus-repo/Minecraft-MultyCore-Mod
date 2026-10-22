# compat

> Mod compatibility: the Mod Menu config screen (Phase 1) and, now, the legacy/compatible mod
> classification and hand-off (Phase 2).

**Does not do:** decide whether a mod is thread-safe (Phase 2), or run offloaded work.

## Public API

Entry point: `compat/ModCompatibility` and `compat/ModClassifier`.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `ModClassifier.classify(metadata)` | `static ModClassification classify(ModMetadata)` | Pure: read `custom.multicore.threadSafe` | never throws |
| `ModCompatibility.scan()` | `static ModCompatibility scan()` | Scan all loaded mods via Fabric Loader | never throws |
| `ModCompatibility.isLegacy(id)` / `isCompatible(id)` | `boolean` | Classification honouring overrides | — |
| `ModCompatibility.forceLegacy(id)` / `forceCompatible(id)` | `void` | Override a mod's own declaration | — |
| `ModCompatibility.classifications()` | `Map<String, ModClassification>` | Read-only snapshot | — |
| `ModClassification` | `LEGACY` \| `COMPATIBLE` | Safety level | — |

## Usage example

```java
ModCompatibility compat = ModCompatibility.scan();
if (compat.isCompatible("some-mod")) {
    // may run on region threads in AGGRESSIVE mode
}
```

## Dependencies

- **Allowed:** Fabric Loader (`ModCompatibility.scan()` only).
- **Forbidden:** Minecraft world classes, the worker pool.

## Notes and limitations

- A mod is thread-safe only if it declares `custom.multicore.threadSafe: true` in its
  `fabric.mod.json`. Everything else is legacy (opt-in, like Folia).
- Whitelist/blacklist overrides are applied on top of the declaration.
- The classification is a snapshot taken at `SERVER_STARTING`; it does not change at runtime.

## Tests

`./gradlew test --tests "com.strefiz.multicore.compat.*"` — tests on `ModClassifier` and
`ModCompatibility` in `src/test/java/com/strefiz/multicore/compat/`.
