---
name: code-documentation
description: Document code and the project in layers — docstrings on everything public, a README in each module, a docs/MODULES.md catalog, comments only for the why, usage examples — and keep documentation always in sync with the code. ALWAYS use when creating or changing functions, classes, components or exported modules, when creating a module folder, before calling a task done, when the user asks you to document, comment on or explain the code, and when reviewing outdated documentation.
---

# Code documentation

Document **everything another part of the program (or another person) will use**. Code without a written contract isn't reusable: it forces readers into the implementation.

## The 4 layers

| Layer | Where | Content | Required for |
|--------|------|----------|------------------|
| 1. Project | `README.md`, `ARCHITECTURE.md` | What it is, how to run it, how it's organized | Always |
| 2. Module | `<module>/README.md` (template: `docs/templates/MODULE_README.md`) | Purpose, what it **doesn't** do, public API, example, dependencies, notes | Every module/folder with ≥ 3 code files |
| 3. Symbol | Docstring/documentation comment next to the function/class/type | Full contract (below) | Everything **exported/public** |
| 4. Line | Comment in the code | The non-obvious **why** (decision, workaround, pitfall) | Only when needed |

Plus the **catalog** `docs/MODULES.md`: one line per reusable function/module (skill `reusable-modules`).

## Contract of a public function

Write, in this order (omit what doesn't apply):

1. **One sentence** in the imperative/present: what it does (not how).
2. **Parameters:** name, type, meaning, constraints (e.g. `> 0`, non-empty), default values.
3. **Returns:** type and meaning, including special cases (empty, `null`, not found).
4. **Errors:** which failures can occur and how they are signaled (exception/`Result`/code).
5. **Side effects:** I/O, mutated state, network, time, randomness. If it's **pure**, say so.
6. **Minimal runnable example** (the best test of the API; ideally a doctest/tested example).
7. Complexity or limits **only** if relevant (e.g. O(n²), thread-safety).

### Format per language (use the idiomatic one for the stack)

```ts
/**
 * Splits `items` into blocks of size `size`.
 * Pure. Does not modify the original array.
 *
 * @param items List to split.
 * @param size  Size of each block (integer > 0).
 * @returns List of blocks; the last one may be smaller. Empty list → `[]`.
 * @throws {RangeError} if `size` is not a positive integer.
 * @example chunk([1,2,3,4,5], 2) // [[1,2],[3,4],[5]]
 */
```

```python
def chunk(items: list[T], size: int) -> list[list[T]]:
    """Splits `items` into blocks of size `size`. Pure.

    Args:
        items: List to split.
        size: Size of each block (int > 0).
    Returns:
        List of blocks; the last one may be smaller. Empty -> [].
    Raises:
        ValueError: if `size` is not a positive integer.
    Example:
        >>> chunk([1, 2, 3, 4, 5], 2)
        [[1, 2], [3, 4], [5]]
    """
```

Rust: `///` with sections `# Errors`, `# Panics`, `# Examples` (doctests). Go: comment starting with the name (`// Chunk splits…`). C#: `/// <summary>`, `<param>`, `<returns>`, `<exception>`. GDScript: `##` above the function.

## Module README

Copy `docs/templates/MODULE_README.md` and fill in: purpose (1 sentence) · what it **doesn't** do · public API (table) · usage example · allowed dependencies · decisions/limitations · how to test. Max ~1 screen. Create it **when you create the module**, not afterwards.

## Comments in the code

- Explain the **why**, never repeat the **what**. Comment describing confusing code → rewrite the code.
- Mark workarounds with context: `// Workaround: API X returns 200 with empty body (see B-004)`.
- `TODO` in code only with a reference to a task: `// TODO(T-031): …`.
- No commented-out code: git keeps the history.

## Keep in sync (part of the Definition of Done)

In the **same commit** that changes behavior or the public API:
- [ ] Docstring updated (signature, errors, example still runs).
- [ ] Module README reflects the API.
- [ ] `docs/MODULES.md` with the new/changed/removed entry.
- [ ] `CHANGELOG.md` if the change is visible to users.
- [ ] `ARCHITECTURE.md` if the structure or the dependencies changed.

Docs and code disagree → **the code wins**; fix the doc. Never leave docs that lie.

## Language

Follow the language already used in the code. For a new project: identifiers in English; docstrings and README in the language set in `docs/CONVENTIONS.md` (default PT-PT). Be consistent.

## Quick review (self-verification)

1. Someone who has never seen the module can use it with just the README + docstrings?
2. Do the examples actually run? (test them)
3. Any exported symbols without a docstring? (`grep` the exports and confirm)
4. Do the names say what they do without needing the comment?

## Anti-patterns

Docstring that only repeats the name (`/** Gets the user. */`) · bulk-generated documentation with no real content · README listing features that don't exist · apologetic comments · examples that no longer compile · docs only in chat and not in the repo.
