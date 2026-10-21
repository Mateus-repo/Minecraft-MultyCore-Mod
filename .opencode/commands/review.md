---
description: Self-review of recent work → REVIEW.md (and fix the blockers)
---
Use the skill `code-review` to review: $ARGUMENTS (if empty: changes since the last milestone/review).

Git state:
!`git log --oneline -15`
!`git diff --stat HEAD~5..HEAD 2>/dev/null | tail -20`

Register the review in `REVIEW.md`, fix anything that's a simple `blocker`/`major`, open tasks in `TODO.md` for the rest and send ideas to `IDEAS.md`. For an independent second opinion, delegate to the subagent `@reviewer`.
