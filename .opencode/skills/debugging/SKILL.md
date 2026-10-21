---
name: debugging
description: Systematic method to diagnose and fix bugs, test failures, build errors, crashes and unexpected behavior. ALWAYS use when something fails, a test breaks, there is an error message, the result isn't the expected one, a fix doesn't work, or the user reports a problem. Reproduce, generate alternative hypotheses, isolate, fix the root cause and leave a regression test.
---

# Debugging

Don't guess. **Reproduce → observe → hypothesize → test → fix the cause → prove it.**

## 1. Reproduce

- **Minimal** command/steps that fail, deterministically. Without reproduction there's no fix — only hope.
- Record the exact command and the **full** output (don't paraphrase the error message).
- Intermittent? Look for what varies: data, timing, order, concurrency, environment, cache.

## 2. Read what the system tells you

- Stack trace from **bottom up** to your code; the full error message; logs around the moment.
- What changed? `git log -p`, `git diff`, `git stash` to compare. **Bisect** when "it worked yesterday": `git bisect start; git bisect bad; git bisect good <hash>` + test command.

## 3. Hypotheses — always more than one

Write **3–5 different** hypotheses before touching anything (use the spirit of the 5 angles):

1. Unexpected data/input · 2. State/execution order · 3. Environment/configuration/version · 4. Logic in my code · 5. Dependency/library/platform.

For each one: **what observation would confirm or rule it out?** Rank by (probability × low cost of testing). Test one at a time and **record** it in `BUGS.md` (`H1 ✗ ruled out: …`).

## 4. Isolate

- Reduce: strip code/data down to the minimum that still fails.
- Instrument: temporary logs/asserts/prints at the decision points (and **remove them** afterwards).
- Divide and conquer: is the problem before or after this point?
- Compare a working case with a failing one — the difference is the clue.

## 5. Fix the cause, not the symptom

- Ask "why?" until you reach something that, if changed, makes the bug impossible ("5 whys").
- Avoid *patches* that hide it (empty try/catch, `if` for the specific case, sleep, blind retry).
- Look for the **same pattern elsewhere** (grep) — bugs repeat.

## 6. Prove it

- Regression test that failed before and now passes (skill `testing-verification`).
- `bash scripts/check.sh` green; reproduce the original scenario and confirm.
- Record in `BUGS.md`: symptom, root cause, fix (commit), test. Commit `fix(scope): …` with `Refs: B-NNN`.

## When your fix didn't work

Say so clearly (in the record and in the final message) and add the **diagnosis**: what you tried, the actual output, which hypotheses are ruled out and why. **Don't repeat the same approach a 3rd time.** Change angle: another hypothesis, another tool, another level (network, OS, version, cache), or question the assumption that "the problem is where I'm looking".

## Common traps

| Symptom | Quick suspicion |
|---------|-----------------|
| "Works on my machine" | versions, environment variables, uncommitted files, absolute paths, case sensitivity |
| Only fails sometimes | concurrency, timing, test order, shared state, randomness without a *seed* |
| Changed without me touching it | dependency update, cache, data, clock/timezone |
| Huge confusing error | look for the **first** error line; the rest is a consequence |
| "Obvious" fix doesn't resolve it | you're looking in the wrong place; confirm your code is actually running (stale build? cache?) |
| Memory/CPU growing | unreleased resources, listeners, unbounded caches, event loops |

## Time limits

2 failed attempts on the same line of investigation → change strategy. 30+ min without progress → record the state in `BUGS.md` (reproduction, ruled-out hypotheses), mark the task blocked and move on. Never leave the repo broken.
