# Design — Minecraft MultiCore Mod

> Source of truth for the interface. The only UI is the **Mod Menu config screen** (a native
> Minecraft `Screen`). Skill: `ui-design`. The `/multicore` command text follows the same rules.

## Direction

- **Tone:** utilitarian and technical — this is a server-administration tool.
- **Platform:** Minecraft's built-in GUI (client, singleplayer/LAN/dev only; dedicated servers use
  the file + command).
- **References:** the sibling `RocketWithSpearMod` config screen (same native style).

## Screen layout

- Centred column of full-width controls, `240×20` each, `22` px apart (Minecraft grid scale).
- Title: `config.multicore.title`.
- Controls (toggles/sliders): `workerThreads`, `mode`, `debug`, and a whitelist/blacklist summary.
- A **persistent warning banner**: "Restart required" (`config.multicore.restartRequired`).
- A **Done** button returning to the parent screen.

## States

| State | Behaviour |
|-------|-----------|
| Default | Shows the persisted values. |
| Edited | Saves immediately; the warning banner is always visible. |
| Mod Menu absent | Screen is never constructed; nothing to show. |
| Error (bad JSON) | Config falls back to defaults; a warning is logged, not thrown at the user. |

## Color / typography

- Use Minecraft's built-in `Screen` rendering (dark background, default font) — do not invent a
  theme. Warning text uses the standard yellow `0xE0E04A`-ish error/warning tone of the vanilla
  GUI; primary actions use the default button style.

## Accessibility

- All controls are keyboard-navigable (vanilla `Screen` handles focus order).
- State is conveyed by text, not colour alone (e.g. "OFF" / "ON" labels).
- Text is translatable (`assets/multicore/lang/{en_us,pt_pt}.json`).

## Design change log

| Date | Change | Reason |
|------|-----------|--------|
| 2026-10-21 | Initial screen design | Mod Menu requirement (D-007) |
