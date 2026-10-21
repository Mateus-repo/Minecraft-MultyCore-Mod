---
name: decide-without-asking
description: 5-angle decision method for resolving doubts on your own before asking the user. ALWAYS use whenever you feel like asking a question, have two or more options, ambiguous requirements, a design/library/structure choice, or when the user uses /decide. Forces you to analyze precedent, intent, external evidence, risk/reversibility and simplicity, and to record the decision. Only ends in a question to the user if it is a real blocker.
---

# Decide without asking — the 5 angles

Goal: turn "I don't know, I'll ask" into "I analyzed it 5 ways and decided — or I know exactly what to ask".

## Step 0 — Is it really a decision?

If the answer is in the code, the docs, `git log`, `DECISIONS.md` or the official documentation → **go read**. Many "doubts" end here.

## Step 1 — Frame the question and the options

One sentence for the question. 2–4 real options (include "do nothing" and "the simplest one").

## Step 2 — The 5 angles (1–3 lines each, per option when relevant)

1. **Precedent** — What does the repo already do? Is there a pattern, convention, previous decision (`DECISIONS.md`, `docs/CONVENTIONS.md`, `git log`, neighboring code)? Consistency almost always wins.
2. **Intent** — Which user goal does this serve (`README`, `ROADMAP`, `PROGRESS`, messages, earlier answers in `QUESTIONS.md`)? Which option would they pick? What is the *spirit* of the request, not just the letter?
3. **External evidence** — What do the official documentation, the ecosystem, benchmarks, known issues say? (skill `research-first`.) Don't assume APIs from memory.
4. **Risk and reversibility** — Worst case if it's wrong? Cost to undo (minutes vs. days)? Does it touch data, security, money, the public API? **Cheap and reversible → decide now.**
5. **Simplicity and cost** — Which one meets the requirements with fewer moving parts and less code? **Pre-mortem:** "a month from now this option failed — why?" For each option, the most likely reason.

*Optional for big decisions:* delegate the plan to the `@critic` subagent (devil's advocate) for a 6th look.

## Step 3 — Score and decide

| Option | Precedent | Intent | Evidence | Risk (↓ better) | Simplicity | Total |
|-------|-----------|----------|-----------|------------------|--------------|-------|
| A | | | | | | |

Scale 0–2 per angle (risk: 2 = low). It isn't math — it's a way of seeing whether **one** angle dominates or there's a tie.

- **One option clearly ahead (or ~70%+ confidence)** → decide.
- **Tie and reversible** → pick the simplest and most reversible; or run a **spike** ≤ 15 min to gain evidence.
- **Tie and irreversible/high impact** → possible real blocker (Step 5).

## Step 4 — Record and move on

Write it in `DECISIONS.md` (`D-NNN`): context, options, the 5 angles one line each, decision, **assumptions**, **how to revert**. Status `Provisional` if you assumed something about what the user wants. Then continue the work.

## Step 5 — When it really is a blocker

Only ask if, after the 5 angles, **one** of these holds:
- you need credentials/access/secrets you don't have;
- destructive, irreversible action or one with a financial cost;
- **product** ambiguity that substantially changes the outcome and angles 1–2 don't settle it;
- it contradicts an explicit instruction from the user;
- it goes outside the repo's scope (publishing, sending, deleting third-party data).

How to ask well (`QUESTIONS.md`, `Q-NNN`):
- Context in 1–2 lines and what has **already been tried** (the 5 angles, summarized).
- Closed options (A/B/C), not "what do you think?".
- **Default recommendation** + "I'll do this if there's no answer".
- What stays blocked and what you'll do in the meantime.
- Group several questions into one batch.

Then: mark dependent tasks as blocked and **move on to others**.

## Anti-patterns

- Asking to "confirm" something you already decided well.
- Asking what reading 3 files would resolve.
- Asking the same question 3 different ways.
- Deciding in silence (no record) — the decision disappears.
- Paralysis over small choices: names, order, formatting → decide in 10 seconds.
