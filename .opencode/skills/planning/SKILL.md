---
name: planning
description: Break down goals, features or milestones into small, ordered, verifiable tasks, with acceptance criteria, priority and size. ALWAYS use when the user asks to "plan", "/plan", for a new feature, a milestone, a big (L) or vague task, when the TODO.md queue is empty, or before starting any work with more than ~3 steps. Produces entries ready for TODO.md and, if needed, proposals for ROADMAP.md.
---

# Planning

A good plan is a task list an agent can **start without questions** and **prove it finished**.

## Process

1. **Goal** — 1 sentence: what observable result exists at the end? For whom?
2. **Context** — read `ROADMAP.md`, `ARCHITECTURE.md`, `DECISIONS.md`, relevant code. Don't plan in a vacuum.
3. **Scope** — what is **not** included (goes to `IDEAS.md` if it's tempting).
4. **Slice vertically** — each task delivers value or *verifiable* progress end to end (e.g. "create and list items") instead of loose layers ("do the whole database"). First a **walking skeleton** (the thinnest slice that works), then thicken it.
4b. **Reuse** — for each task that creates code: what already exists that I can reuse (`docs/MODULES.md`, `scripts/find-reuse.sh`)? Which module does the new code live in (skill `project-structure`)? Include the docs/README/`MODULES.md` entry in the task.
5. **Risks first** — what could kill the plan (unknown API, performance, integration)? Do a **spike** of ≤ 30 min early. Record the result.
6. **Order** — dependencies → risk → value → effort. P0/P1 tasks on top.
7. **Size** — S (< 30 min) · M (30–90 min) · L (> 90 min → **break down further**). No L in `Now`.
8. **Acceptance criteria** — verifiable by command or explicit manual steps.
9. **Write** in `TODO.md` and, if it changes direction, propose in `ROADMAP.md`.

## Task format

```
- [ ] T-021 [P1] [M] Save settings to a file — acceptance: restarting the app keeps the settings; an invalid file falls back to the defaults with a warning; 3 new tests  (ref: M1, D-004)
```

**Good criteria:** "`scripts/check.sh` green and the command `x --help` lists the `--out` option" · "with an empty list the text 'No items' appears" · "300 ms or less for 10k lines (benchmark in `tests/bench`)".
**Bad criteria:** "works well", "clean code", "improve UX".

## Checklist of a good plan

- [ ] Every task is S or M, with verifiable acceptance.
- [ ] There's an end-to-end slice early (walking skeleton).
- [ ] Unknown risks have a spike before the tasks that depend on them.
- [ ] Tests and docs are part of the tasks (not "later").
- [ ] Tasks that create code say what they reuse and where the new code will live.
- [ ] Dependencies between tasks are explicit (`depends on T-0xx`).
- [ ] Nothing out of scope got in; ideas went to `IDEAS.md`.
- [ ] You know which the first task is and can start it now.

## Plans for milestones

For each milestone in `ROADMAP.md`: measurable goal · completion criteria (checkboxes) · known risks · dependencies. Detail only the current and the next one; the rest stay as one-line goals.

## Replanning

If reality diverged (estimate off by ×2, requirement changed, new discovery): stop, update `TODO.md`, record the why in `PROGRESS.md` (and `DECISIONS.md` if the approach changed), and continue. A stale plan is worse than no plan.
