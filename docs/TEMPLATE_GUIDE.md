# Template guide

This repository is a **template** for starting programming projects with [OpenCode](https://opencode.ai) already set up: management documents, skills, commands, agents, *auto commit* and an autonomous mode.

## 1. Starting a new project

```bash
cp -r repo-template my-project && cd my-project     # or "Use this template" on GitHub
bash scripts/init.sh "My Project" "Short description" "Stack (e.g. TypeScript + React)"
opencode
```

`init.sh` replaces the placeholders (`{{PROJECT_NAME}}`, `{{DESCRIPTION}}`, `{{STACK}}`, `{{DATE}}`), initializes git if needed, moves this guide to `docs/TEMPLATE_GUIDE.md` and makes the first commit.

Then, inside OpenCode:

1. `/bootstrap` — the agent detects the stack, fills in the build/test commands, `ARCHITECTURE.md`, `docs/CONVENTIONS.md`, the first `ROADMAP.md` and `TODO.md`.
2. Review `ROADMAP.md` (it is your direction; the agent proposes, you decide).
3. `/autopilot 120` — and let it work.

## 2. How the agent works

- **Startup:** reads `AGENTS.md`, `PROGRESS.md`, `QUESTIONS.md`, `TODO.md`, `ROADMAP.md`.
- **Loop:** choose a task → plan → implement → verify → record → commit → repeat.
- **Autonomy:** it only stops for real blockers. Before asking, it analyzes the doubt from **5 angles** (precedent, intent, external evidence, risk/reversibility, simplicity) and decides, recording it in `DECISIONS.md`.
- **Questions:** go to `QUESTIONS.md` (in a batch, with a default recommendation). **Answer below the question** in the file, or in the chat; the agent reads it next session.
- **Ideas:** go to `IDEAS.md`; they do not become code without moving to `TODO.md`/`ROADMAP.md`.

## 3. Commands

| Command | What it does |
|---------|--------------|
| `/bootstrap` | Sets up the project (first time) |
| `/start` | Session startup ritual and start of work |
| `/plan <goal>` | Breaks a goal into tasks with acceptance criteria |
| `/autopilot [min]` | Long autonomous work (default: 120 min) |
| `/autopilot-stop` | Stops autopilot |
| `/decide <question>` | 5-angle analysis + recorded decision |
| `/review [scope]` | Self-review → `REVIEW.md` |
| `/structure [scope]` | Audits folders, documentation and reuse, and fixes them |
| `/module <name>` | Creates a new module (public API, README, tests) and registers it |
| `/commit` | Organised commit of pending work |
| `/handoff` | Closes the session: updates docs and summarizes |
| `/status` | Quick summary of the project state |

## 4. Auto commit

The plugin `.opencode/plugins/auto-commit.js` makes a `chore(wip): checkpoint` commit whenever the session goes idle and there are changes.

- Config in `.opencode/autocommit.json` (see below) or `AUTOCOMMIT=off` in the environment.
- Block sensitive files (`.env`, keys…) and secret patterns in the diff.
- Does **not** push by default (`"push": true` to enable).
- The "real" commits (with a good message) are made by the agent via the `auto-commit` skill / `scripts/commit.sh`.

```json
{ "enabled": true, "push": false, "minIntervalSeconds": 15, "blockSecrets": true, "message": "chore(wip): checkpoint" }
```

## 5. Autopilot (experimental)

`/autopilot` writes `.opencode/autopilot.state` and the `autopilot.js` plugin sends a continuation prompt again when the agent goes idle, until:

- the time budget runs out (default 120 min) — it sends one last message to do a handoff;
- `scripts/autopilot.sh stop "reason"` (the agent calls it when it finishes or gets blocked);
- a `STOP` file exists in the root;
- an error or an interruption from you (Esc) — it **stops on purpose**;
- an iteration limit (default 60).

If the plugin API changes and the plugin stops working, the rest stays valid: the agent still follows the autonomy protocol in `AGENTS.md`, it is just not woken up automatically.

## 6. Customising

- **Permissions:** `opencode.json` (`git push` asks for confirmation; `push --force`, `sudo`, `rm -rf /` denied).
- **Skills:** `.opencode/skills/<name>/SKILL.md`. The folder name must match `name:`. Keep it < 500 lines and make the descriptions "insistent" about *when* to use them.
- **Language:** the docs are in English; commits in English. Change `AGENTS.md` (section 9) if you prefer another language.
- **Global skills:** copy folders from `.opencode/skills/` to `~/.config/opencode/skills/` to use them in every project.

## 7. Structure, documentation and reuse

The template pushes the agent to write **modular and reusable** code, well organized and documented:

- **Skills:** `reusable-modules` (reuse before writing; pure functions; minimal API; the 1-2-3 rule), `project-structure` (where each thing lives; by feature; moving safely) and `code-documentation` (docstrings, per-module README, comments only for the why).
- **Catalog:** `docs/MODULES.md` lists reusable modules and functions. The agent consults it (and runs `scripts/find-reuse.sh`) before writing new code, and updates it in the same commit.
- **Module README template:** `docs/templates/MODULE_README.md`.
- **Audit:** `scripts/structure-check.sh` (large files, modules without a README, `utils/misc/old` folders, backups, placeholders). It only warns; `--strict` fails. It runs inside `scripts/check.sh`.
- **Commands:** `/module <name>` creates a full module; `/structure` audits and fixes.
- **Adjusting limits:** `MAX_FILE_LINES=400 bash scripts/structure-check.sh`.

## 8. Windows

The scripts are `bash` (Git Bash or WSL). The plugins are plain JavaScript and work on any system.

## 9. Troubleshooting

| Symptom | Check |
|---------|-------|
| Skill does not appear | `SKILL.md` in uppercase; frontmatter with `name` and `description`; `name` = folder name; restart OpenCode |
| Plugin does not run | File in `.opencode/plugins/`; restart; check the OpenCode log |
| Auto commit does not commit | `git status`; `.opencode/autocommit.json`; a secret detected (see the log) |
| Agent stops too early | Use `/autopilot`; confirm that `QUESTIONS.md` has no blockers |
