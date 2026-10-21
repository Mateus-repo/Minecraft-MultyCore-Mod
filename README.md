# Minecraft MultiCore Mod

Fabric mod that parallelizes the Minecraft server tick across configurable worker threads while keeping legacy (non-multithreaded) mods single-threaded

> **Status:** in progress · **Stack:** Java 25 + Minecraft Fabric server-side mod · **Created:** 2026-10-21

## What it does

<!-- 2–4 sentences: the problem it solves and for whom. Fill in with /bootstrap. -->

## Features

- [ ] _to be filled in_

## Getting started

```bash
# 1. Install dependencies
# TODO

# 2. Run
# TODO

# 3. Tests
# TODO
```

## Repository structure

```
.
├── AGENTS.md          # rules for AI agents
├── ROADMAP.md         # vision and milestones
├── TODO.md            # task queue
├── PROGRESS.md        # current state and session log
├── DECISIONS.md       # technical decisions
├── QUESTIONS.md       # questions for the user
├── BUGS.md            # known bugs
├── REVIEW.md          # self-reviews
├── IDEAS.md           # out-of-scope ideas
├── ARCHITECTURE.md    # architecture
├── CHANGELOG.md       # version history
├── docs/              # conventions, testing, design, glossary, module catalog (MODULES.md)
├── scripts/           # utilities (check, commit, autopilot…)
├── .opencode/         # OpenCode skills, commands, agents and plugins
└── src/               # code (to be created)
```

## Development with OpenCode

```bash
opencode            # open at the repo root
/bootstrap          # (first time) fill in the docs and the project commands
/autopilot 120      # autonomous work for ~2h
```

See `docs/TEMPLATE_GUIDE.md` for the full workflow guide.

## Contributing

Commits follow [Conventional Commits](https://www.conventionalcommits.org/). Before submitting: `bash scripts/check.sh`.

## License

_To be defined._
