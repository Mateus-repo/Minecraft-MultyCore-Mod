---
name: ui-design
description: Design and review interfaces to a professional standard — visual hierarchy, spacing, typography, color and contrast, component states, accessibility, motion, and consistency via design tokens in docs/DESIGN.md. ALWAYS use when you create, change, or review any UI (web, desktop, mobile, games), when the user talks about visuals, layout, design, theme, "making it pretty", "improving the interface", or before writing UI code. Avoid generic, inconsistent, or inaccessible interfaces.
---

# UI design

A good UI is **clear, consistent, and predictable** first; pretty afterwards — and the beauty comes from discipline (scale, alignment, contrast), not decoration.

## Process

1. **Brief (2 min)** — who uses it, in what context (screen, mouse/touch/gamepad, in a hurry?), what the screen's **primary task** is, and the **tone** (calm, energetic, serious, "cozy"…). Write it in `docs/DESIGN.md › Direction`.
2. **Tokens before components** — define color, typography, spacing, radii, shadows, motion in `docs/DESIGN.md`. Code uses **tokens**, never loose values ("#3a7bd5", "13px").
3. **Structure before style** — sketch the screen's hierarchy (what's 1st, 2nd, 3rd) in text/ASCII; style only after.
4. **Components with all states** (see §5).
5. **Implement** reusing components; no repeated "one-off" styles.
6. **Review** with the checklist (§9) and **verify on the real screen** (small and large sizes). Record decisions in `docs/DESIGN.md › Log`.

If prior UI exists, **respect the existing system** (tokens, components, patterns) before introducing anything new.

## 1. Hierarchy

- One **focal point** per screen. Primary action prominent (color, size, position); secondary ones understated; destructive ones separated and confirmed.
- Hierarchy via **size, weight, contrast, space** — not via 6 different colors.
- Group by proximity (the law of proximity): space **within** a group < space **between** groups.
- Natural reading: left→right, top→bottom (adjust for RTL where applicable).

## 2. Spacing and layout

- **Fixed scale** (4/8): `4 · 8 · 12 · 16 · 24 · 32 · 48 · 64`. Never arbitrary values.
- More white space than seems necessary; density only in expert tools.
- **Align** to a grid/common axes; consistent margins; the same maximum reading width (45–75 characters per line).
- Responsive/adaptable: define 2–3 *breakpoints* (or target sizes), content reflows, no accidental horizontal scrolling. On desktop/games: test the minimum resolution **and** large/ultrawide screens.

## 3. Typography

- **1–2 families** at most; modular scale (e.g. 12 · 14 · 16 · 20 · 24 · 32).
- Body ≥ 16 px equivalent (≥ 14 in dense UIs); line height 1.4–1.6 for body text; headings 1.1–1.3.
- 2–3 weights (regular, medium, bold). All caps only in short labels.
- Short, actionable text: buttons with a **verb** ("Save", "Create order"), errors that say **what to do**, no technical jargon.

## 4. Color and contrast

- Small palette: **neutrals** (background, surface, borders, text) + **1 primary color** + **semantic** (success/warning/error/info). Neutrals do 80% of the work.
- **WCAG** contrast: normal text ≥ 4.5:1; large text and UI elements ≥ 3:1. Verify it for real (not "by eye").
- **Never color alone** to convey meaning (add icon/text/shape).
- Support light/dark theme where it makes sense, via tokens (don't duplicate styles).
- Avoid pure black/white in large areas; use neutrals tinted slightly with the primary color.

## 5. States (what separates amateur from professional)

Every interactive component defines: **normal · hover · focus (visible!) · pressed · disabled · loading · error · success**.
Every **data view** defines: **empty** (with guidance/action), **loading** (skeleton/spinner), **error** (with retry), **partial**, **lots of data** (pagination/scroll), **long text** (truncate/wrap).
Forms: visible labels (not just placeholders), clear validation next to the field, values preserved on error, submission state.

## 6. Accessibility (non-negotiable minimums)

- Navigable by **keyboard** (and gamepad, where applicable) with logical order and **visible focus**.
- Touch/click targets ≥ 44×44 px (≥ 24 px with spacing on dense desktop).
- Alt text on meaningful images; labels on controls; correct semantics (a button is a button, not a `div`).
- Respect "reduce motion"; nothing blinks > 3×/s; audio/video with controls.
- Text resizable without breaking the layout; don't block zoom.

## 7. Motion

Serves to **explain** (where it came from, where it's going), not to decorate. 120–240 ms; *ease-out* to enter, *ease-in* to exit; animate `transform`/`opacity`. Immediate feedback (< 100 ms) to input.

## 8. Style patterns and consistency

- One icon family, one base border radius (+1 variant), 2–3 elevation levels, borders **or** shadows (not both).
- Reusable components: button, field, card, modal, list, tabs, toast. Variants via properties, not copies.
- Same action = same look and same place everywhere.
- **Avoid the generic:** pick a direction and commit to it (typography with character, a coherent palette, one memorable detail) — but without sacrificing legibility or consistency.

## 9. Review checklist

- [ ] Is there a focal point and is the primary action obvious?
- [ ] Only values from the spacing/typography/color-token scales?
- [ ] Contrast verified (4.5:1 / 3:1)? No color-only information?
- [ ] All states defined (interaction **and** data)?
- [ ] Visible focus and keyboard/gamepad use OK?
- [ ] Texts short, clear, with verbs; actionable errors?
- [ ] Tested at minimum and large size; no overflow/clipping?
- [ ] Reuses existing components; no duplicated styles?
- [ ] `docs/DESIGN.md` updated (tokens/components/decision)?

## 10. Platform notes

- **Web:** semantic HTML, CSS variables for tokens, *mobile-first*, `prefers-color-scheme`/`prefers-reduced-motion`.
- **Desktop (WinForms/WPF/Qt/Electron…):** respect OS conventions, DPI scaling, keyboard shortcuts, resized-window states.
- **Mobile:** thumb zones, gestures with a visible alternative, *safe areas*, keyboard covering fields.
- **Games:** legibility at distance/in motion, contrast over varying backgrounds, anchoring to corners/safe area across different screens, gamepad navigation, configurable UI scale, audio/visual feedback for every action. Separate **data/logic** from **presentation** so you can test without rendering.

## Anti-patterns

Magic color/spacing values · 5 button sizes · light gray text on white · modal over modal · placeholder as a label · icons without text in important actions · blank empty states · slow animations that block use · "I'll get to accessibility later".
