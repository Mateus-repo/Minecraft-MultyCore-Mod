---
name: refactoring-modular
description: Write modular code and refactor safely — clear responsibilities, small files and functions, low coupling, no duplication — without changing behavior. ALWAYS use when a file passes ~300 lines, a function passes ~40, there is duplication, tight coupling, "god objects", before adding a feature to confusing code, when designing the structure of a new module, or when the user asks to organize, clean up, or modularize code.
---

# Refactoring & modular code

Refactor = change **structure** while keeping **behavior**. If the behavior changes, it's not a refactor.

See also: `reusable-modules` (designing for reuse), `project-structure` (where everything lives), and `code-documentation` (READMEs and docstrings keeping up with the change).

## Principles of modular code

- **One responsibility per module/function** ("and" in the description = two modules).
- **Explicit boundaries:** each module exposes a small interface and hides the rest.
- **Dependencies in one direction** (e.g. `ui → app → domain`). The domain knows nothing about UI, DB, or framework.
- **Data in the middle, effects at the edges:** pure logic at the center (easy to test); I/O, network, UI, and global state at the margins.
- **Composition > inheritance.** **Names > comments.** **Small > clever.**
- References (not dogmas): function < ~40 lines · file < ~300 lines · ≤ 4 parameters · nesting ≤ 3 levels.

## Signs that it's time to refactor (code smells)

| Smell | Remedy |
|-------|---------|
| Giant function/file | Extract a function/module per responsibility |
| Duplication (3+ times) | Extract a shared function/component (not before 2–3 real cases) |
| Too many parameters | Options object / dedicated type |
| Repeated `if/switch` by type | Polymorphism / dispatch table |
| Vague names (`data`, `manager`, `utils`) | Rename for what it **is** or **does** |
| Growing `utils`/`helpers` folder | Move it to the module that owns the concept |
| Global state / singletons everywhere | Pass dependencies explicitly |
| Dependency cycles | Extract the shared part into a third module; invert the dependency |
| Comments explaining confusing code | Rewrite the code; keep comments only for the *why* |
| Dead code / forgotten flags | Delete it (git keeps the history) |

## Safe procedure

1. **Safety net:** are there tests covering the code? If not, write **characterization tests** (they pin down the current behavior) before touching anything. No tests possible → written manual verification.
2. `bash scripts/check.sh` green **before** starting. Clean baseline commit (or tag `before-refactor-<topic>`).
3. **Small steps**, each with a green check and its own commit (`refactor(scope): …`). Move → rename → extract → simplify, **one thing at a time**.
4. Never mix a refactor with a behavior change or bug fix in the same commit.
5. Large reorganization: **strangle** gradually (build the new path alongside, migrate the calls, remove the old one) instead of rewriting everything at once.
6. Update `ARCHITECTURE.md` (modules, dependencies) and `DECISIONS.md` if the structure changed.
7. If the check fails and you can't figure out why after ~2 attempts: `git revert`/`git checkout` the step and try a smaller one.

## Designing a new module

1. Write **what it does** and **what it doesn't do** in one sentence.
2. Define the minimal **public interface** (functions/types) before the implementation.
3. Decide the allowed dependencies (follow the rule in `ARCHITECTURE.md`).
4. Write the first test against the interface.
5. Typical structure: `modulo/` with `index` (public API), internal files by responsibility, tests alongside or in `tests/`.

## When NOT to refactor

- No tests and no way to verify → build the safety net first.
- Code that will be deleted/replaced soon.
- Halfway through a feature or bug — finish it, commit, then refactor.
- "For fun": only refactor with a reason (you're going to touch that spot anyway, it's slowing you down, it causes bugs).

## Scout rule

Leave the code **slightly better** than you found it in the areas you touch — but in separate commits and without widening the scope of the task. Bigger refactors → a dedicated task in `TODO.md`.
