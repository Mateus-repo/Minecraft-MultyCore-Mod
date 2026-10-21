---
name: project-bootstrap
description: Set up a new project or a template just copied — detect the stack, fill in commands in AGENTS.md, ARCHITECTURE.md, docs/CONVENTIONS.md, README, ROADMAP and the initial TODO, make sure scripts/check.sh works and get ready for autonomous work. ALWAYS use when the user asks for /bootstrap, when AGENTS.md still has "TODO" commands or {{...}} placeholders, when the repo is empty/new, or when adopting this template in an existing project.
---

# Project bootstrap

Goal: in ~15–30 min, go from "template with placeholders" to "project ready for autonomous work, with real commands and an initial plan".

## Steps

### 1. Get the lay of the land
- Is there code? `ls`, manifest files (`package.json`, `pyproject.toml`, `Cargo.toml`, `go.mod`, `*.csproj`/`*.sln`, `project.godot`, `pom.xml`, `build.gradle`…).
- `git log --oneline | head`, existing README, issues/user notes (`$ARGUMENTS` of the command).
- **Empty** project: use the description/stack from the placeholders in `README.md`/`AGENTS.md`. If the stack is `to be defined`, decide it with the 5 angles (`decide-without-asking`) based on the goal and record it in `DECISIONS.md` (e.g. `D-002 — Stack`).

### 2. Placeholders
If `{{PROJECT_NAME}}`, `{{DESCRIPTION}}`, `{{STACK}}`, `{{DATE}}` still exist in the root `.md`/`docs/`: run `bash scripts/init.sh "Name" "Description" "Stack"` (if you have the information) or replace them by hand. Dates via `date +%Y-%m-%d`.

### 3. Real commands
Find out and **actually test** every command (install, run, build, tests, lint). Fill in the table in `AGENTS.md §1` and `docs/TESTING.md`. If one doesn't exist, create the task in `TODO.md` to add it (e.g. set up a linter/formatter).

### 4. `scripts/check.sh`
`bash scripts/check.sh` detects common stacks. Run it: if it doesn't cover the project (monorepo, unusual stack), create `scripts/check.local.sh` with the real steps (lint → typecheck → test → build) and a non-zero `exit` on failure. It must be **fast** and **deterministic**.

### 5. Documents
- `ARCHITECTURE.md`: the real folder structure, modules, flows, dependencies (or the initial plan, if empty).
- `docs/CONVENTIONS.md`: formatter/linter, names and structure **deduced from the existing code**; don't impose what the project doesn't use. Stack section filled in.
- `README.md`: what it does, how to run it, structure. No inventing features that don't exist.
- `docs/DESIGN.md`: if it has UI, fill in the direction and the tokens from what exists (or propose them); if it has no UI, **delete** the file and remove it from the table in `AGENTS.md §3`.
- `docs/GLOSSARY.md`: domain terms already visible.
- `docs/MODULES.md`: if there's already code, catalog the existing reusable modules and functions; if it's empty, leave the header. Create `README.md` in the main modules (template `docs/templates/MODULE_README.md`).
- **Structure:** compare the real folders with the `project-structure` skill; if it's disorganized, **don't reorganize everything now** — create tasks in `TODO.md` (one per module). Define the dependency rule and where shared code lives in `ARCHITECTURE.md`.
- `.gitignore`/`.editorconfig`: adjust to the stack (e.g. `bin/`, `obj/`, `.godot/`).
- `.env.example` if there's per-environment configuration.

### 6. Roadmap and initial queue
- `ROADMAP.md`: vision in 1–3 sentences, principles, out of scope, M0 (foundations) and M1 (MVP) with measurable criteria. If the user hasn't set a direction, propose a **provisional version** and mark it as such (`DECISIONS.md` → `Provisional`).
- `TODO.md`: 5–15 S/M tasks with acceptance (skill `planning`), the first already in *Now*. Replace the `T-001` (bootstrap) task by marking it done.

### 7. Baseline
`bash scripts/check.sh` → record the state in `PROGRESS.md › Current state` (build/tests) and, if there are pre-existing failures, in `BUGS.md`. Commit: `chore: bootstrap project configuration`.

### 8. Questions (only the essential ones)
If, after the 5 angles, there's product ambiguity that blocks M1, write it in `QUESTIONS.md` with a default recommendation — and **continue** with what doesn't depend on it.

## "Done" criteria

- [ ] No `{{...}}` in documents ready to fill in; commands in `AGENTS.md` without `TODO` (or with an associated task).
- [ ] `bash scripts/check.sh` runs and the result is recorded.
- [ ] `ARCHITECTURE.md`, `ROADMAP.md`, `TODO.md` reflect reality.
- [ ] `docs/MODULES.md` filled in (or header ready) and structure/dependency rule defined.
- [ ] First task in *Now* has verifiable acceptance.
- [ ] Commit made; short summary to the user (stack, commands, milestones, questions).

## Adopting the template in an existing project

Copy `.opencode/`, `scripts/`, `opencode.json`, the management `.md` files and `docs/`. **Don't** overwrite existing README/.gitignore — *merge* them. Extract the project's current state into `PROGRESS.md`/`ARCHITECTURE.md` and turn the existing backlog into `TODO.md`.
