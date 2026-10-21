---
name: auto-commit
description: Make small, frequent, well-described git commits (Conventional Commits), with secrets checking, and understand the auto commit plugin that saves checkpoints. ALWAYS use when you finish a logical unit of work, before switching tasks, when the user asks for /commit, "save", "commit", or when there are changes to commit. Covers messages, granularity, branches, push, what never to commit and how to recover from errors.
---

# Auto commit

Two complementary mechanisms:

1. **You** make commits with meaning (`scripts/commit.sh`) — the readable history.
2. **The plugin** `auto-commit.js` makes a `chore(wip): checkpoint` when the session goes idle — a safety net against lost work.

## When to commit

After **each logical unit** that leaves the repo in a coherent state: a small feature, a fix, a refactor, a docs update. Rule: if you'd have to write "and" in the message, it's probably two commits.

Before committing: `bash scripts/check.sh` green. Broken code doesn't go into a "real" commit (the automatic WIP checkpoint is the only exception — and that's exactly what it is).

## How

```bash
bash scripts/commit.sh "feat(settings): persist user preferences"            # everything that changed
bash scripts/commit.sh "docs: update roadmap after M1" ROADMAP.md PROGRESS.md  # only a few files
```

The script validates the format, stages, **blocks sensitive files and secret patterns** and commits. To split mixed changes, make several commits with specific paths (or `git add -p`).

## Messages — Conventional Commits (English)

`type(scope): summary in the imperative, lowercase, no period, ≤ 72 characters`

| Type | When |
|------|------|
| `feat` | new visible feature |
| `fix` | bug fix |
| `refactor` | changes structure without changing behavior |
| `perf` | improves performance |
| `test` | tests only |
| `docs` | documentation only |
| `style` | formatting (no logic) |
| `build` / `ci` | build, dependencies, pipeline |
| `chore` | maintenance, miscellaneous tasks |
| `revert` | reverts a commit |

Body (optional, when the *why* isn't obvious): blank line + 1–3 lines about **why** and trade-offs. Footer: `Refs: T-012, B-003`; breaking change: `feat(api)!: …` + `BREAKING CHANGE: …`.

Good examples: `fix(parser): handle empty config file` · `refactor(order): extract price calculation` · `test(cart): cover negative quantity`.
Bad: `update`, `fix stuff`, `wip`, `changes`.

## What never to commit

`.env*` (except `.env.example`), private keys, tokens, credentials, real data dumps, large generated binaries, `node_modules`, builds. If the script blocks: **remove the secret**, use an environment variable and update `.env.example`. If a secret **was already committed**: consider it compromised → record it in `QUESTIONS.md` (needs rotation by the user) and don't try to rewrite shared history without being asked.

## Branches

- Short work: on the current branch. Long/experimental work: `ai/<theme>` or `feat/<theme>`.
- Before risky operations (big refactor, migration): create a branch or a `before-<theme>` tag.
- Never `git push --force`, never rewrite shared history, never `reset --hard` with uncommitted work (the permissions in `opencode.json` already protect you).
- `git push` asks for confirmation; it isn't needed to work. (Want automatic push: `"push": true` in `.opencode/autocommit.json`.)

## WIP checkpoints

Commits `chore(wip): checkpoint (N files)` are normal. Don't "clean" them (squash/rebase) unless the user asks. When they do, interactive `git rebase -i` doesn't work in the agent: use `git reset --soft <base>` + new, organized commits.

## If something goes wrong

| Situation | Action |
|----------|------|
| Committed an extra file | `git reset --soft HEAD~1`, adjust the stage, recommit |
| Wrong message (last commit, not published) | `git commit --amend -m "…"` |
| Commit broke something | `git revert <hash>` (doesn't rewrite history) |
| Lost changes | `git reflog` + `git checkout <hash> -- file` |
| Plugin doesn't commit | `git status`; `.opencode/autocommit.json`; OpenCode log (secret detected?) |

## Turn off/tune the plugin

`.opencode/autocommit.json` → `{ "enabled": false }` or environment variable `AUTOCOMMIT=off`.
