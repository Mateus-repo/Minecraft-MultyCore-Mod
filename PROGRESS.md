# PROGRESS — Minecraft MultiCore Mod

> **The project's working memory.** Read at the start of each session and after each context compression.
> Keep *Current state* short and truthful (≤ 30 lines). The session log goes below, most recent first.

## Current state

- **Current milestone:** M1 — Infrastructure + safe offload (in progress)
- **Last update:** 2026-10-21
- **Build/test health:** ✅ `./gradlew build` green · ✅ `scripts/check.sh` green (10 checks) · ✅ JUnit green
- **In progress:** T-009 done (offload registry + first safe offload + benchmark plan); starting Phase 2 (mod compatibility)
- **Recommended next step:** T-010 — scan loaded mods and classify them as legacy/compatible
- **Blockers / open questions:** Q-001 (Phase 3 approach) and Q-002 (Mod Menu restart flow) are open in `QUESTIONS.md`; they do not block M1/M2.
- **Risks / relevant technical debt:** Phase 3 (regionised tick) can corrupt worlds if rushed — SAFE default, debug instrumentation, determinism tests first (see `docs/RESEARCH.md` §8). SAFE mode protects Fabric-event mods but **not** mixin-based mods (documented). No CPU affinity yet (M4).

## Session log

<!-- Entry template (copy upwards):

### YYYY-MM-DD HH:MM — short title  (duration: Xh Ym)
- **Done:** T-00X …; T-00Y …
- **Decisions:** D-00X (link in DECISIONS.md)
- **Findings / problems:** …
- **State on exit:** build ✅/❌ · tests ✅/❌ · branch · last commit
- **Next step:** …
-->

### 2026-10-21 — M1: infrastructure + offload + Mod Menu + external review fixes
- **Done:** T-003 config · T-004 worker pool · T-005 scheduler · T-006 metrics + `/multicore` · T-007 Mod Menu screen · T-008 wired the entrypoint · T-009 offload registry + first safe offload + `docs/BENCHMARKS.md`.
- **External review (Claude):** confirmed the plan is honest and the base is solid; found real bugs. Fixed: (1) entrypoint now `main`/`ModInitializer` so singleplayer/integrated server works; (2) `TaskScheduler` now **waits** for offloaded work and merges in **submission order** (was completion order); (3) `WorkerPool.submit` uses `execute` so exceptions are no longer swallowed; (4) stale docs updated. See `REVIEW.md`.
- **Decisions:** D-012 (go/no-go benchmark after M1), D-013 (main entrypoint).
- **State on exit:** build ✅ · tests ✅ · branch `main` · last commit `docs: add independent review and go/no-go plan`.
- **Next step:** T-010 — mod compatibility scan.

### 2026-10-21 — Phase 0: research + template + roadmap
- **Done:** copied the `Repo-Template` and ran `init.sh`; wrote `docs/RESEARCH.md`; filled `ROADMAP.md`, `TODO.md`, `DECISIONS.md`, `README.md`, `ARCHITECTURE.md` and the `docs/*` files.
- **Decisions:** D-003…D-011.
- **Findings / problems:** `bash` is not on PATH — invoke `C:\Program Files\Git\bin\bash.exe` with a script file. Gradle is not on PATH; the wrapper dist (9.8.0) is cached. Java 25 is installed.
- **State on exit:** build ❌ (no build files) · tests ❌ · branch `main` · last commit `chore: initialize project from template`.

## Archive

_Old entries (> 20 sessions) summarized here._
