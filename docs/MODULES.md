# MODULES — Minecraft MultiCore Mod

> **Catalog** of modules and functions/components that are **reusable**. The first thing to check **before writing new code** (skill `reusable-modules`) and the last thing to update **before closing a task** (skill `code-documentation`).
> Quick search: `bash scripts/find-reuse.sh <term> [term…]`.
> Rule: if it is exported and used (or usable) outside its file, it gets a row here. Code and catalog change in the **same commit**. When they disagree, the code wins.

## Modules

| Module | Path | Purpose (1 sentence) | Public API (summary) | Depends on |
|--------|---------|---------------------|----------------------|------------|
| _to be filled in_ | | | | |

## Reusable functions, types and components

Group by **concept** (text, dates, validation, files, HTTP…), not by file.

| Name | Path | Signature | What it does | Pure? | Errors | Used by |
|------|---------|------------|-----------|-------|-------|-----------|
| _to be filled in_ | | | | | | |

<!-- Example row:
| `chunk` | `src/shared/lists/chunk.ts` | `chunk<T>(items: T[], size: number): T[][]` | Splits a list into fixed-size blocks | yes | `RangeError` if `size` ≤ 0 | `billing`, `export` |
-->

## Known duplications (to resolve)

_Similar code that has not been unified yet, with the associated task (`T-NNN`). Empty is the goal._

## Catalog conventions

- Remove the row when you remove the code. Don't leave orphan entries.
- Signature or errors changed → update the row **and** the docstring **and** the module's README.
- A private function (used only inside its own module) does **not** go here.
