---
description: Create a new module (structure, public API, README, tests) and register it in docs/MODULES.md
---
Use the skills `reusable-modules`, `project-structure` and `code-documentation` to create the module: $ARGUMENTS

1. **Reuse first:** `bash scripts/find-reuse.sh` with the module's terms. If something similar already exists, extend it instead of duplicating it.
2. **Location:** private within a feature, or `shared/<concept>/` if it's used by 2+ modules (follow `ARCHITECTURE.md` and `docs/CONVENTIONS.md`).
3. **Structure:** entry point with the minimal public API; internal files by responsibility; `README.md` (from `docs/templates/MODULE_README.md`); tests for the public API.
4. **Quality:** pure functions with explicit inputs/outputs, injected dependencies, docstrings in everything you export.
5. **Register:** a line in `docs/MODULES.md`; update the module table in `ARCHITECTURE.md`.
6. `bash scripts/check.sh` green → `bash scripts/commit.sh "feat(<module>): add <module> module"`.
