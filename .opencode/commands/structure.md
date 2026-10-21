---
description: Audit folder organization, documentation and code reuse, then fix
---
Use the skills `project-structure`, `code-documentation` and `reusable-modules` to audit: $ARGUMENTS (if empty: all of the project's code).

Automatic warnings:
!`bash scripts/structure-check.sh`

Do, in this order, with small separate commits (never mix them with behavior changes):

1. **Structure** — fix the warnings (large files, catch-all folders, vague names) with `git mv`/splitting; `bash scripts/check.sh` green at every step.
2. **Reuse** — look for duplication (`bash scripts/find-reuse.sh` with terms from the suspicious functions); unify into `shared/<concept>/` or open `T-NNN` if it's large.
3. **Documentation** — a README in every module that lacks one, docstrings in exported functions with no contract, `docs/MODULES.md` and `ARCHITECTURE.md` in sync with the code.
4. Register what you did in `PROGRESS.md`; what didn't fit goes to `TODO.md`; ideas to `IDEAS.md`.
