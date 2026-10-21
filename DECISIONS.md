# DECISIONS — Minecraft MultiCore Mod

> Decision log (short ADR). **Always record** when you choose between real alternatives, especially when *you* decided instead of asking.
> Read before reopening a topic. To change a decision: create a new one that **replaces it** (do not delete the old one).
> Status: `Accepted` · `Provisional` (assumed without the user's confirmation — revisit if it matters) · `Superseded by D-NNN`.

## Template

```
### D-NNN — Title  (YYYY-MM-DD) — Status
- **Context:** the problem and the constraints.
- **Options:** A) … B) … C) …
- **Analysis (5 angles):** precedent · intent · external evidence · risk/reversibility · simplicity — 1 line each.
- **Decision:** the chosen one and why.
- **Assumptions:** what I assumed without confirming.
- **How to revert:** steps/cost.
- **Consequences:** what changes; what becomes harder.
```

## Decisions

### D-001 — Use the repository template with management documents  (2026-10-21) — Accepted
- **Context:** a new project, developed with AI agents during long sessions.
- **Decision:** keep the project state in versioned `.md` files (`PROGRESS`, `TODO`, `DECISIONS`…) to survive context compression and session handoffs.
- **How to revert:** delete the management files and simplify `AGENTS.md`.

### D-002 — Modular, reusable, documented code by default  (2026-10-21) — Accepted
- **Context:** much code is written by agents in long sessions; without rules, duplication, `utils` junk drawers and modules nobody knows how to use appear.
- **Decision:** reuse before writing (`docs/MODULES.md` + `scripts/find-reuse.sh`); pure functions and modules with a minimal public API; structure by feature; everything public documented (docstring + module README). Skills `reusable-modules`, `project-structure`, `code-documentation`.
- **How to revert:** remove the three skills and the corresponding rules in `AGENTS.md` §5 and `docs/CONVENTIONS.md`.
