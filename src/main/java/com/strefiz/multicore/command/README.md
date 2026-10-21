# command

> The {@code /multicore} command: status, compat, regions and reload.

**Does not do:** gather the state itself (it comes from `StatusSource`), format text for other
uses, or change the configuration.

## Public API

Entry point: `command/MultiCoreCommand`.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `MultiCoreCommand` | `static void register(CommandDispatcher<CommandSourceStack>, StatusSource)` | Add the command tree to a dispatcher | — |
| | `static void registerEvents(StatusSource)` | Register via `CommandRegistrationCallback` | — |
| `StatusSource` | `mode()`, `workerThreads()`, `threadCount()`, `metrics()`, `threadTimesMs()`, `compatSummary()`, `regionSummary()` | Read-only runtime state | — |
| `StatusFormatter` | `static String status(StatusSource)` · `compat(String)` · `regions(String)` · `reloadWarning()` · `singleThreaded()` | Pure text formatting | — |

## Usage example

```java
CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
        MultiCoreCommand.register(dispatcher, statusSource));
```

## Dependencies

- **Allowed:** Fabric command API, Brigadier, Minecraft `Component`.
- **Forbidden:** reading the config directly, world classes.

## Notes and limitations

- All text is produced by the pure `StatusFormatter`, so it is unit-testable without a game.
- `compat` and `regions` are placeholders until Phase 2/3; they say so instead of lying.
- Permission checks are not implemented yet (the 26.3 permission API is new); see
  `ARCHITECTURE.md` › limitations.

## Tests

`./gradlew test --tests "com.strefiz.multicore.command.*"` — tests on `StatusFormatter` in
`src/test/java/com/strefiz/multicore/command/`.
