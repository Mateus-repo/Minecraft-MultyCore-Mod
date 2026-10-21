# PROGRESS — Minecraft MultiCore Mod

> **The project's working memory.** Read at the start of each session and after each context compression.
> Keep *Current state* short and truthful (≤ 30 lines). The session log goes below, most recent first.

## Current state

- **Current milestone:** M0 — Foundations (Phase 0 done, M1 next)
- **Last update:** 2026-10-21
- **Build/test health:** ❓ no build files yet — nothing compiled
- **In progress:** T-001 (Gradle/Fabric scaffolding), T-002 (management docs)
- **Recommended next step:** run T-001, then T-003 (config module)
- **Blockers / open questions:** none blocking. Q-001 (Phase 3 approach) and Q-002 (Mod Menu restart flow) are open in `QUESTIONS.md`; M0/M1 do not depend on them.
- **Risks / relevant technical debt:** Phase 3 (regionised tick) can corrupt worlds if rushed — SAFE default, debug instrumentation, determinism tests first (see `docs/RESEARCH.md` §8). Build env uses Loom SNAPSHOT + a cached Gradle 9.8.0 dist.

## Session log

<!-- Entry template (copy upwards):

### YYYY-MM-DD HH:MM — short title  (duration: Xh Ym)
- **Done:** T-00X …; T-00Y …
- **Decisions:** D-00X (link in DECISIONS.md)
- **Findings / problems:** …
- **State on exit:** build ✅/❌ · tests ✅/❌ · branch · last commit
- **Next step:** …
-->

### 2026-10-21 — Phase 0: research + template + roadmap
- **Done:** copied the `Repo-Template` and ran `init.sh`; wrote `docs/RESEARCH.md` (Folia/C2ME/Lithium/VMP/Moonrise, 26.3 tick anatomy from the Loom-remapped jar, Java concurrency, Fabric surface, risks); filled `ROADMAP.md`, `TODO.md`, `DECISIONS.md`, `README.md`, `ARCHITECTURE.md`, `docs/CONVENTIONS.md`, `MODULES.md`, `TESTING.md`, `GLOSSARY.md`, `DESIGN.md`, `IDEAS.md`, `QUESTIONS.md`.
- **Decisions:** D-003…D-011 (server-first, stack, opt-in compatibility, threads-not-cores, Mod Menu, config-at-start, SAFE default, additive, language).
- **Findings / problems:** `bash` is not on PATH — invoke `C:\Program Files\Git\bin\bash.exe` with a script file (`bash -c` mangles args on this machine). Gradle is not on PATH either; the wrapper dist (9.8.0) is cached. Java 25 is installed.
- **User requirement added:** Mod Menu compatibility (proven in `RocketWithSpearMod`) + restart warning (D-007).
- **State on exit:** build ❌ (no build files) · tests ❌ · branch `main` · last commit `chore: initialize project from template`
- **Next step:** T-001 — scaffold the Gradle/Fabric 26.3 project and make `./gradlew build` work.

## Archive

_Old entries (> 20 sessions) summarized here._
