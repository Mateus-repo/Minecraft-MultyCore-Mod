# compat

> Mod compatibility: the Mod Menu config screen (Phase 1) and, later, the legacy/compatible mod
> classification and hand-off (Phase 2).

**Does not do:** decide whether a mod is thread-safe (Phase 2), or run offloaded work.

## Public API

Entry point: `compat/ModMenuIntegration`.

| Symbol | Signature | Description | Errors |
|---------|------------|-----------|-------|
| `ModMenuIntegration` | `implements ModMenuApi` · `getModConfigScreenFactory()` | Exposes the config screen to Mod Menu | — |
| `MultiCoreConfigScreen` | `extends Screen` | Native config screen; persists on every change | never throws |

## Usage example

The screen is opened by Mod Menu; a user never calls it directly.

## Dependencies

- **Allowed:** Mod Menu API (optional), Minecraft client GUI, the `config` module.
- **Forbidden:** Minecraft server/world classes, the worker pool.

## Notes and limitations

- Mod Menu is **optional**: `compileOnly` + `localRuntime`, declared with `suggests`, never `depends`.
- The mod ships as `environment: "*"` so Mod Menu can see it; the actual work is server-side via the
  `server` entrypoint.
- Every change is saved immediately, but **thread/mode/region settings require a restart** (D-008);
  the screen says so.
- On a dedicated server there is no client GUI — the file and `/multicore` are authoritative.

## Tests

No unit tests yet (the screen needs a client). Verified manually — see `docs/TESTING.md`.
