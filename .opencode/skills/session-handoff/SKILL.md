---
name: session-handoff
description: Session start and end rituals to avoid losing context — startup (read PROGRESS, QUESTIONS, TODO, ROADMAP), checkpoints, pauses, and the final handoff (update docs, verify, commit, summarize). ALWAYS use at the start of any work session, after the context has been compressed, when the user asks for /start, /handoff, or /status, when the time budget is running out, or when you're about to stop for any reason.
---

# Session handoff

The next session (you after a context compression, or another agent) only knows what is **written in the files**.

## Startup (≈ 2 min)

1. `bash scripts/session-start.sh` — date, branch, git, recent commits, state, clock.
2. `PROGRESS.md` → *Current state* and the latest journal entry.
3. `QUESTIONS.md` → **any new answers?** (`Answer:` field filled in). Apply them, move to *Answered*, unblock tasks.
4. `TODO.md` → *Now* and *Blocked*; `ROADMAP.md` → current milestone; `BUGS.md` → open P0/P1.
5. Skim `DECISIONS.md`; `ARCHITECTURE.md` if you'll touch structure.
6. If `git status` shows uncommitted changes that aren't yours from this session (e.g. a WIP checkpoint): understand what they are before continuing.
7. Run `bash scripts/check.sh` to learn the **real** state (don't trust "it was green").
8. Set the session goal (1–3 lines) in `PROGRESS.md` and **get started**.

## During: checkpoints (every ~25 min or at the end of each task)

- `TODO.md` and `PROGRESS.md › Current state` updated (what I'm doing, what's left, build health).
- Commit (`auto-commit`).
- `bash scripts/clock.sh` — how much time is left?

## Pause/unexpected interruption

If you're stopping without a planned end (error, limit, blocker): **commit what's stable**, and write in `PROGRESS.md › Current state` exactly **where you left off and what the next concrete step is** (command to run, file to edit). If the repo is mid-something, say which file/branch.

## End of session / handoff (≈ 5 min)

1. Safely finish what's half-done — or isolate it (branch/stash/WIP commit) and document it.
2. `bash scripts/check.sh`. If it fails and you can't fix it: `BUGS.md` + say so in the summary.
3. Update: `TODO.md` · `PROGRESS.md` (*Current state* + new journal entry) · `DECISIONS.md` · `BUGS.md` · `CHANGELOG.md` · `ROADMAP.md` (milestone states) · `IDEAS.md`.
4. Quick triage of `QUESTIONS.md`: questions still needed? grouped? with a default recommendation?
5. Final commit (`bash scripts/commit.sh "docs: handoff <date>" ...` or logical commits).
6. If autopilot is running: `bash scripts/autopilot.sh stop "<reason>"`.
7. Summary to the user (≤ 10 lines).

### Journal entry (template)

```
### YYYY-MM-DD HH:MM — short title  (duration: Xh Ym)
- Goal: …  → Outcome: achieved / partial (why)
- Done: T-0xx, T-0yy
- Decisions: D-0xx (summary)
- Findings/problems: …
- State on exit: build ✅/❌ · tests ✅/❌ (n) · branch · hash
- Next concrete step: …
```

### Final summary to the user

1. **Done** (IDs + 1 line each)  2. **I decided** (D-NNN, for you to validate)  3. **I need you** (Q-NNN, with my recommendation)  4. **State** (build/tests/branch)  5. **Next step**.
If something **didn't work**, say so plainly with the diagnosis (error, command, what you've already ruled out).

## Recovering after context compression

Don't trust the summary — repeat **Startup**. The true state is: `.md` files + `git log` + `git status` + `scripts/check.sh`.
