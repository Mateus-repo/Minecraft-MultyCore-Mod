# Design — Minecraft MultiCore Mod

> Source of truth for the interface (web, desktop, mobile or game). Skill: `ui-design`.
> If the project has no UI, delete this file.

## Direction

- **Tone/personality:** _e.g.: welcoming, minimalist, "cozy"_
- **Platform and target sizes:** _e.g.: desktop 1280×720+, mobile 360×640+_
- **Visual references:** _to be filled in_

## Tokens

### Color

| Token | Value | Use |
|-------|-------|-----|
| `--color-bg` | `#…` | Base background |
| `--color-surface` | `#…` | Cards/panels |
| `--color-text` | `#…` | Main text (contrast ≥ 4.5:1 with `bg`) |
| `--color-text-muted` | `#…` | Secondary text |
| `--color-primary` | `#…` | Primary action |
| `--color-danger` / `--color-success` / `--color-warning` | `#…` | States |

### Typography

| Role | Family | Size/weight/line-height |
|-------|---------|--------------------------|
| Title | | |
| Body | | |
| Caption | | |

### Spacing (4/8 scale)

`4 · 8 · 12 · 16 · 24 · 32 · 48 · 64`

### Radii, shadows, borders

_to be defined_

### Motion

Duration 120–240 ms, smooth *easing*; respect "reduce motion".

## Components

| Component | Required states | Notes |
|------------|---------------------|-------|
| Button | normal · hover · focus · pressed · disabled · loading | |
| Text field | normal · focus · error · disabled | |
| List/table | empty · loading · error · with data | |

## Layout principles

_Hierarchy, grid, alignment, density._

## Accessibility

- Contrast ≥ 4.5:1 (text) / 3:1 (UI) · touch targets ≥ 44 px · keyboard/gamepad navigation · visible focus · don't rely on color alone.

## Design change log

| Date | Change | Reason |
|------|-----------|--------|
