---
name: testing-verification
description: Prove the work is really done — write tests, run scripts/check.sh, define reproducible manual verification, and apply the Definition of Done. ALWAYS use before marking a task as complete, after fixing a bug, when adding non-trivial logic, when no tests exist, or when something "seems to work". Nothing is done without evidence.
---

# Testing & verification

> "I think it works" ≠ "I verified that it works".

## Definition of Done (summary)

Acceptance criterion met **with evidence** · `bash scripts/check.sh` green · new/updated tests · affected docs updated · `TODO`/`PROGRESS` updated · commit made · known regressions recorded in `BUGS.md`.

## Cycle

1. **Before coding:** reread the acceptance criterion. How will I prove it? Write that down first (a test, or manual steps).
2. **Test first when possible** — especially for bugs: reproduce with a test that **fails**, fix it, watch it pass (regression guaranteed).
3. **Implement** the minimum to pass.
4. **Run** `bash scripts/check.sh` (and the specific test).
5. **Verify real behavior** — run the app/command/screen, not just the tests. Green tests with a broken app happen.
6. **Record the evidence** in `PROGRESS.md` (number of tests, manual command, relevant output).

## What tests to write

| Type | When | Characteristics |
|------|--------|-----------------|
| Unit | pure logic, rules, parsing, calculations | fast, no I/O, many |
| Integration | modules together, DB, files, own API | a few, isolated, test data |
| End-to-end | critical user flows | few, stable |
| Regression | every fixed bug | name the bug (`test_B004_empty_config`) |

Best practices: one behavior per test · names that describe the behavior (`returns_error_when_input_is_empty`) · **Arrange–Act–Assert** pattern · no order/time/network dependence · minimal data · mocks only at the boundaries · edge cases (empty, null, maximum, negative, unicode, concurrency).

## When it can't be automated (UI, games, hardware)

- Write **reproducible manual steps** in `docs/TESTING.md` or in the task: "1. open X · 2. click Y · 3. expected: Z".
- Extract the logic into pure, testable functions and keep the UI layer **thin**.
- For web UI, consider component/E2E tests; for games, logic/simulation tests without rendering.
- If you **cannot** verify (no environment, no tools), say so **explicitly** in the summary — "implemented, **not verified**: reason" — and open a task in `TODO.md`.

## Failures

- **Test fails:** read the whole error; don't "adjust the test to pass" unless the test is objectively wrong (and justify it). Skill `debugging`.
- **Flaky test:** don't ignore it or rerun it until it passes — find the cause (time, order, shared state, network); record it in `docs/TESTING.md › Known issues` and `BUGS.md`.
- **Can't fix it now:** never delete/skip tests to make the check pass without recording it in `BUGS.md` and in the commit.

## Anti-patterns

- Checking `[x]` because you wrote the code.
- Tests that assert what the code does (a mirror) instead of what it **should** do.
- Coverage as a goal in itself (cover behavior, not lines).
- Verification only on the "happy path".
- Declaring success based on a command that didn't run or whose output you didn't read.
