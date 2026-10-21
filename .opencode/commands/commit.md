---
description: Tidy commits (Conventional Commits) of the pending work
---
Use the skill `auto-commit`.

Current state:
!`git status --short`

Run `bash scripts/check.sh` first. If it passes, split the changes into logical commits (one per unit) with `bash scripts/commit.sh "type(scope): message" paths...`. If it fails, don't commit broken code — fix it or register it in `BUGS.md`. Extra context: $ARGUMENTS
