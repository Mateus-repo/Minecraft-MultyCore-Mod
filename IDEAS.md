# IDEAS — Minecraft MultiCore Mod

> Ideas inbox. **Golden rule: an idea is not a task.** Capture it in 1–2 lines and get on with the work.
> Triage (agent, when the `TODO.md` queue runs out — or the user, at any time):
> `Promote` → `TODO.md`/`ROADMAP.md` · `Defer` · `Reject` (with a reason — avoids discussing it again).
> Criteria for promoting: does it serve the `ROADMAP.md` **vision/milestone**? High value / low effort? Doesn't duplicate anything?

## Inbox

<!-- - (YYYY-MM-DD) [origin: agent/user] One-line idea — why it might be worth it -->

- (2026-10-21) [user] Support the **client** (integrated server + chunk meshing) — Phase 5; feasibility report first (see ROADMAP M5).
- (2026-10-21) [user] Integrate **Mod Menu** for in-game config with a restart warning — promoted to M1/T-007.
- (2026-10-21) [agent] Per-region CPU affinity (pin a region to a core) — M4/T-018, optional.
- (2026-10-21) [agent] Web/Spark-style live dashboard of regions and threads — probably overkill; `/multicore status` first.
- (2026-10-21) [agent] Deterministic mode (serialise ticks) for reproducible debugging — conflicts with the performance goal; defer.
- (2026-10-21) [agent] Replace the `Safety` ordinal comparison in `OffloadRegistry` with an explicit policy (`EnumMap<Safety, Boolean>`) — clearer, and easier to extend in Phase 3.

## Under evaluation

_Empty._

## Promoted

- (2026-10-21) Mod Menu in-game config + restart warning → `TODO.md` T-007 (D-007).
- (2026-10-21) Client support → `ROADMAP.md` M5 (report only).

## Deferred

_Empty._

## Rejected (with reason)

_Empty._
