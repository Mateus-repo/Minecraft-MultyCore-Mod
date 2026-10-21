---
name: project-docs
description: How to read, write and maintain the project's management .md files (README, ROADMAP, TODO, PROGRESS, DECISIONS, QUESTIONS, BUGS, REVIEW, IDEAS, ARCHITECTURE, CHANGELOG, docs/*). ALWAYS use whenever you are about to create, update or consult any of these documents, record a decision, bug, idea, question or progress, or when you don't know where to store information. Also use at the end of every task to know exactly what to update.
---

# Project docs — where each thing goes and how to write it

The `.md` files are the **project's memory**: they survive context compression and new sessions. Code and docs change **in the same commit**.

## 1. Where do I put this?

| I have… | Goes to | Format |
|--------|----------|---------|
| A concrete task to do | `TODO.md` | `- [ ] T-NNN [P?] [S/M/L] Title — acceptance: …` |
| Something I started/finished | `TODO.md` (check it) + `PROGRESS.md` | diary entry |
| A choice between alternatives | `DECISIONS.md` | `D-NNN` (see template in the file) |
| A bug | `BUGS.md` | `B-NNN [P?]` |
| A doubt only the user can resolve | `QUESTIONS.md` | `Q-NNN` with default recommendation |
| "It would be nice if…" | `IDEAS.md` › Inbox | 1–2 lines, with date |
| Review finding | `REVIEW.md` | `R-NNN` + findings table |
| Change of direction/milestone | `ROADMAP.md` (propose; user confirms) | milestones table |
| Change visible to users | `CHANGELOG.md` › Unreleased | Added/Changed/Fixed |
| Structure/module change | `ARCHITECTURE.md` | modules table |
| Domain term | `docs/GLOSSARY.md` | table |
| New code convention | `docs/CONVENTIONS.md` | short rule + example |
| Visual decision / design token | `docs/DESIGN.md` | tokens/components |

If the information helps **decide what to do next** → `TODO`/`ROADMAP`. If it helps **understand what happened** → `PROGRESS`/`DECISIONS`. If it helps **not forget an idea** → `IDEAS`.

## 2. Writing rules

- **Verifiable facts, no flourishes.** "Tests: 42 ✅ / 1 ❌ (`test_parse_empty`)" > "almost everything working".
- **Stable IDs** (`T-001`, `D-001`, `B-001`, `Q-001`, `R-001`, `M1`). Increment the largest existing one; never reuse.
- **ISO dates** `YYYY-MM-DD`. Get the real date with `date`, don't guess.
- **Newest first** in diaries and history lists.
- **Cross-references:** `T-012` ← `B-003`; `D-004` in `ARCHITECTURE.md`; commit with short hash.
- **Short:** `Current state` ≤ 30 lines. Past ~400 lines → archive it.
- **Honesty:** if something failed or was assumed, write it. `DECISIONS.md` distinguishes `Accepted` from `Provisional`.
- **No forgotten placeholders** (`{{...}}`, stray `TODO`) in files ready to fill in.
- Docs and code disagree → **the code wins**; fix the doc.

## 3. Routines

### When finishing a task
1. `TODO.md`: check `[x]`, move to *Done*, add derived tasks.
2. `PROGRESS.md`: update *Current state*; diary entry if it was relevant.
3. If you made a non-trivial decision → `DECISIONS.md`.
4. If you found/fixed a bug → `BUGS.md`.
5. If there's a visible change → `CHANGELOG.md`. If the structure changed → `ARCHITECTURE.md`.
6. If ideas occurred to you → `IDEAS.md` (and **don't** implement them).
7. Commit.

### On session start
See skill `session-handoff` (Startup).

### When the user answers a question
Move `Q-NNN` to *Answered* with the answer and the date; apply it; if it changed something relevant, `DECISIONS.md`; unblock the tasks in `TODO.md › Blocked`.

### Idea triage (when the queue runs out)
For each idea: does it serve the `ROADMAP`? value/effort? duplicated? → **Promote** (create `T-NNN`), **Defer** or **Reject** (with a reason, so it isn't re-discussed).

## 4. Quick templates

```
- [ ] T-014 [P1] [M] Validate form input — acceptance: invalid fields show an error; 5 edge cases tested  (ref: M1)
```
```
### 2026-03-02 14:10 — Configuration parser  (duration: 1h 20m)
- Done: T-011, T-012
- Decisions: D-006 (TOML format)
- Findings: B-004 (crash with empty file) fixed
- State on exit: build ✅ · tests ✅ (57) · branch main · a1b2c3d
- Next step: T-013 error messages
```
```
### D-006 — Configuration format: TOML  (2026-03-02) — Provisional
- Context: we need human-readable config, with comments.
- Options: A) JSON B) YAML C) TOML
- Analysis: precedent (none) · intent (user edits by hand) · evidence (TOML has a standard parser in the stack) · risk (low, easy to convert) · simplicity (TOML)
- Decision: C
- Assumptions: user doesn't need deep nesting
- How to revert: convert with a script (~1h)
```
