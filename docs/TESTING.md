# Tests — Minecraft MultiCore Mod

> Verification strategy. Skill: `testing-verification`.

## Philosophy

- Test **behavior**, not implementation. Fast, deterministic, independent tests.
- Every fixed bug gets a regression test.
- Pyramid: many unit · some integration · few end-to-end.
- No automated tests possible (e.g. UI, game)? Define **reproducible manual verification** (written steps) and record it in the task.

## Commands

| What | Command |
|-------|---------|
| All | `bash scripts/check.sh` |
| Unit | `TODO` |
| Integration | `TODO` |
| One test | `TODO` |
| Coverage | `TODO` |

## What must have tests

- Domain logic and business rules · parsing/validation · calculations · edge cases and errors.

## What is verified manually

_List of UI flows/visual flows + steps._

## Test data

_Where they are, how they're generated; no real/personal data._

## Known issues (flaky, slow)

_None._
