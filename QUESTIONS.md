# QUESTIONS — Minecraft MultiCore Mod

> Questions for the **user**. Only **(real) blockers** go here (see `AGENTS.md` §4.3) — after the 5 angles have been analyzed.
> The agent asks **in batches**, gives a **default recommendation** and keeps working on what does not depend on the answer.
> **User:** reply under `Answer:` (one line is enough). The agent reads them at startup.

## Template

```
### Q-NNN — Short question  (YYYY-MM-DD) — Priority: high/medium/low
- **Context:** why this matters.
- **Already tried (5 angles):** precedent: … · intent: … · evidence: … · risk: … · simplicity: …
- **Options:** A) … B) …
- **Default recommendation:** A, because …  (this is what I will do if there is no answer)
- **Blocks:** T-00X (or "nothing, just fine-tuning")
- **Meanwhile:** what I will do.
- **Answer:** (user)
```

## Open

### Q-001 — Confirm the Phase 3 approach (regionised tick) before I build it  (2026-10-21) — Priority: high
- **Context:** Phase 3 is the only milestone that changes how the *world tick* itself runs. It is the highest-risk part (world corruption, deadlocks) and the one that determines the project's architecture.
- **Already tried (5 angles):** precedent — Folia's regionised model is the proven shape (docs read, see `docs/RESEARCH.md` §2.1) · intent — you want "the rest of vanilla distributed by cores while legacy mods stay on one core" · evidence — Folia requires opt-in and accepts ~0 compatibility; we invert it with SAFE mode · risk — Phase 3 can corrupt worlds if rushed, so it must come after M1+M2 · simplicity — copying Folia's model (sections, merge radius, transient regions, EDF schedule) is the least-invention path.
- **Options:** A) full Folia-style regionisation with SAFE fallback (recommended); B) start smaller — parallelise only across dimensions, then regions later; C) abandon regionisation and keep only the safe offloads.
- **Default recommendation:** A for the long term, but **implement B first** inside M3 (cross-dimension parallelism is a safe stepping stone and gives early wins).
- **Blocks:** T-013…T-016 (Phase 3 tasks).
- **Meanwhile:** I build M0 + M1 (config, thread manager, metrics, Mod Menu, first safe offload) — none of it depends on this answer.
- **Answer:** (user)

### Q-002 — Mod Menu + restart flow  (2026-10-21) — Priority: medium
- **Context:** you asked for Mod Menu compatibility and a "restart to apply" warning. Thread/region settings cannot change mid-tick safely, so they must be applied at server start.
- **Already tried (5 angles):** precedent — `RocketWithSpearMod` already does Mod Menu + a native `Screen` with a save-on-change config · intent — in-game config editing · evidence — Mod Menu is a client mod; a dedicated server has no client GUI · risk — if I let the screen claim changes are live, users will change thread counts mid-run and break the server · simplicity — persist immediately, display a persistent "Restart required" banner.
- **Options:** A) screen persists + shows a restart banner; server applies config only at start (recommended); B) allow hot-reload of a small safe subset (logging/debug) and restart for the rest.
- **Default recommendation:** A (with B as a later refinement behind a debug flag).
- **Blocks:** T-007 (and the exact behaviour of T-003).
- **Meanwhile:** nothing — T-003/T-007 implement A.
- **Answer:** (user)

## Answered

_None._
