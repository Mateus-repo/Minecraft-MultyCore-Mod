---
name: autonomous-workflow
description: How to work autonomously during long sessions (~2 hours or more) without interrupting the user. ALWAYS use when the user asks for long/autonomous work, uses /autopilot, says "continue", "work on your own", "do whatever it takes", or when you have a task queue to work through. Covers the work loop, time management, checkpoints, what to do when the queue runs out, how to survive context compression and when (and only when) to stop.
---

# Autonomous workflow

**Goal:** deliver the most verified work possible, in ~2h sessions, **without** asking the user for input except on real blockers.

## Mindset

- You are the one responsible for the decision. The user delegated so they wouldn't be interrupted.
- A reasonable hunch, recorded and reversible, is worth more than a question that blocks for hours.
- But autonomy ≠ recklessness: verify everything, protect data, don't step outside scope.

## Setup (1 minute)

1. `bash scripts/session-start.sh` → context + clock.
2. Read `PROGRESS.md`, `QUESTIONS.md`, `TODO.md`, `ROADMAP.md`, `BUGS.md`.
3. Confirm the budget: `bash scripts/clock.sh` (default 120 min).
4. Define the **session goal** in 1–3 lines (e.g. "close M1: T-004..T-009") and write it at the top of today's entry in `PROGRESS.md`.

## Loop

```
repeat:
  task = top of TODO › Now   (else: ROADMAP; else: §"When the queue runs out")
  if task is L or vague → break it down (skill planning)
  implement in small steps
  verify (scripts/check.sh + acceptance criteria)  → skill testing-verification
  record (TODO, PROGRESS, DECISIONS/BUGS/CHANGELOG)    → skill project-docs
  commit                                               → skill auto-commit
  every ~25 min: checkpoint (PROGRESS › Current state) + bash scripts/clock.sh
  if < 10 min remain → start the handoff
```

Don't write messages to the user between tasks. Write files.

## When a doubt comes up

1. First try to resolve it **by reading** (code, docs, git log, DECISIONS).
2. Then skill `decide-without-asking` (5 angles) → decide and record.
3. Only if it's a **real blocker** → `QUESTIONS.md` (with a default recommendation) and **move on to another task**.

Real blocker = credentials/access you don't have · destructive/irreversible action or one with a cost · product ambiguity that changes the outcome a lot · contradicts an explicit instruction · goes outside the repo's scope.

## When a task fails or gets stuck

- 2 failed attempts on the same approach → stop, change strategy (skill `debugging`: alternative hypotheses).
- 30+ min on a single task with no measurable progress → record it in `BUGS.md`/`QUESTIONS.md`, mark the task as blocked, **move on** to another one.
- Never leave the repo broken when switching tasks: it either works, or it's on an isolated commit/branch, or it was reverted.

## When the queue runs out

Without inventing scope, in order: (1) test gaps · (2) `/review` the recent work and fix blockers · (3) refactor already-known smells · (4) sync docs ↔ code · (5) triage `IDEAS.md` (promote only what serves the ROADMAP). If none of this makes sense → stop.

## Surviving context compression

The context may be summarized halfway through. Defenses:
- `PROGRESS.md › Current state` always true (what I'm doing, what's left, test status).
- Each task has acceptance criteria **written** in `TODO.md`.
- Decisions in `DECISIONS.md`, not just in your head.
- After a compression: repeat the startup ritual (don't trust the summary).

## Stop (only in these cases)

- Time budget exhausted → handoff (skill `session-handoff`).
- Empty queue and nothing useful to do → `bash scripts/autopilot.sh stop "done"`.
- **Everything** that's left is blocked → record it in `QUESTIONS.md`, `bash scripts/autopilot.sh stop "blocked: <summary>"`.
- The user interrupted or asked you to stop.

## Final summary (≤ 10 lines)

Done (IDs) · Your decisions (D-NNN) · Blocked/questions (Q-NNN) · State (build/tests/branch) · Next step.
If something **didn't work**, say so and include the diagnosis (error, command, what you've already ruled out).
