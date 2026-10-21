# config

> Parses, validates and persists the mod configuration (`config/multicore.json`).

**Does not do:** read the file from the game (use `ConfigStore.defaultStore()`), apply the config
to a running server (that is `MultiCoreMod`), or decide what a setting means (that is the modules).

## Public API

Entry point: `config/ConfigStore`. Everything else is internal and **should not** be imported from outside.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `ConfigStore` | `ConfigStore(Path file)` | Store bound to an explicit JSON file | — |
| `ConfigStore.defaultStore()` | `static ConfigStore defaultStore()` | Store at `config/multicore.json` | — |
| `load()` | `MultiCoreConfig load()` | Load + normalize + persist; defaults on any error | never throws |
| `save(config)` | `void save(MultiCoreConfig config)` | Write the normalized config | never throws |
| `MultiCoreConfig` | `int workerThreads()`, `ConfigMode mode()`, `boolean debug()`, `List<String> modWhitelist()`, `List<String> modBlacklist()`, `int threadCount()`, `boolean multithreaded()`, `MultiCoreConfig normalized()` | Read the settings | — |
| `ConfigMode` | `SAFE \| AGGRESSIVE`, `static ConfigMode fromString(String)` | Execution mode | unknown value → `SAFE` |

## Usage example

```java
MultiCoreConfig config = ConfigStore.defaultStore().load();
if (config.multithreaded()) {
    int workers = config.workerThreads(); // main thread is added on top
}
```

## Dependencies

- **Allowed:** Gson (bundled with Minecraft), SLF4J, Fabric Loader (`defaultStore()` only).
- **Forbidden:** Minecraft world classes, the worker pool, the compatibility layer.

## Notes and limitations

- The config is a **snapshot applied at server start** (D-008); changes need a restart.
- Malformed values are replaced by defaults and written back, so the file is always valid.
- `MultiCoreConfig` has no Fabric/Minecraft dependency on purpose, so it is unit-testable.

## Tests

`./gradlew test --tests "com.strefiz.multicore.config.*"` — tests on the public API in
`src/test/java/com/strefiz/multicore/config/`.
