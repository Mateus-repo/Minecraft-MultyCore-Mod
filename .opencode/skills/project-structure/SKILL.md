---
name: project-structure
description: Organize the project's folders and files clearly and predictably — where everything lives, structure by feature, clean root, tests mirroring the code, consistent names, and how to move or rename safely. ALWAYS use when creating a new project or module, when creating folders or files, when you don't know where to put something, when a folder has too many files, when utils, helpers or misc folders appear, or when the user asks to organize, tidy up, or restructure the project.
---

# Project structure

Objective: any person (or agent) should be able to **guess where something lives** in < 10 seconds and **know where to put** something new without thinking.

## Principles

1. **By feature/domain, not by technical type.** `src/billing/` (with everything billing-related) beats scattered `src/controllers/`, `src/models/`, `src/views/`. Start simple (flat) and group when there are 2+ features.
2. **Predictable:** the same pattern in every module. If one module has `index`, `types`, `README.md` and `tests`, all of them do.
3. **Clean root:** at the root only manifests, governance docs, config, and top-level folders (`src/`, `tests/`, `docs/`, `scripts/`, `assets/`). Loose code and data at the root = clutter.
4. **Shallow:** ≤ 3–4 levels deep. A folder with 1 file is rarely justified; a folder with > ~10 sibling files asks for subdivision by concept.
5. **Dependencies in one direction** (rule in `ARCHITECTURE.md`): `app/ui → features → core`; `infra` implements what `core` needs. Never the other way around.
6. **Tests mirroring the code:** `src/a/b.ext` ↔ `tests/a/b.test.ext` (or tests alongside, depending on the stack — one convention across the whole project).
7. **Generated ≠ source:** builds, caches, local data, and secrets out of git (`.gitignore`).

## Base layout (language-agnostic)

```
src/
  core/            # pure business rules: no I/O, no framework
  <feature>/       # one folder per feature
    index.*        # the module's public API (and nothing else)
    <internal files by responsibility>
    README.md      # what it does, API, examples (docs/templates/MODULE_README.md)
  shared/          # reusable by 2+ modules, organized BY CONCEPT
    text/  dates/  validation/  http/ ...
  infra/           # adapters to the outside: DB, network, files, APIs
  app/             # entry point and composition (wires the pieces together)
tests/             # mirrors src/
docs/              # CONVENTIONS, MODULES, TESTING, DESIGN, GLOSSARY
scripts/           # development utilities
assets/            # images, fonts, static data (if applicable)
```

## Adapting to the stack (infer from what exists; don't impose)

| Stack | Typical structure |
|-------|------------------|
| Node / TypeScript | `src/<feature>/index.ts`, `tests/` or `*.test.ts` alongside; `src/shared/` |
| Python (package) | `src/<package>/<module>/__init__.py` (public API), mirrored `tests/` |
| Python web (Flask/FastAPI) | `app/` with `blueprints|routers/<feature>/`, `services/` (logic), `templates/`, `static/`, `tests/` |
| Rust | `src/<mod>/mod.rs`, crates in `crates/` in a workspace if it grows |
| Go | `cmd/<app>/`, `internal/<package>/`, `pkg/` only if truly meant for export |
| .NET | one solution, projects per layer/feature, `*.Tests` per project |
| Godot | `scenes/`, `scripts/`, `assets/`, `autoload/`; group by entity (`player/`: scene + script + assets) |
| Front-end web | `components/<Name>/`, `pages|routes/`, `hooks|composables/`, `lib/`, `styles/` |

## Where do I put this?

| I have… | Goes to |
|--------|----------|
| Business rule with no I/O | `core/` (or the feature's module, if only that feature uses it) |
| Function used by 1 feature | Inside that feature (private) |
| Function used by 2+ features | `shared/<concept>/` (see the `reusable-modules` skill) |
| Access to DB / network / disk / external API | `infra/` (or `adapters/`) behind an interface |
| Configuration and constants | `config/` or a single config module; never scattered |
| Shared types/models | Next to the owning module; `shared/types` only if cross-cutting |
| Development script | `scripts/` (documented in the README) |
| Documentation | `docs/` (per module: `README.md` in the module folder) |
| Test data | `tests/fixtures/` |

## Names

- Name folders and files for what they **are**, in the stack's convention (see `docs/CONVENTIONS.md`); consistent across the whole repo.
- **Forbidden** as folder/file names: `misc`, `stuff`, `temp`, `new`, `old`, `final`, `copy`, `backup`, `test2`. History lives in git.
- `utils/` and `helpers/` are **junk drawers**: accept them only as `shared/<concept>/`. If you find a junk drawer, distribute its contents to the owning modules.
- File named after what it exports (`parse-config.ts` exports `parseConfig`). One main concept per file.

## When to create / split

- **Create a module folder** when there are ≥ 2 files that change together and form a concept.
- **Split a file** > ~300 lines or with 2+ responsibilities (skill `refactoring-modular`).
- **Split a folder** > ~10 sibling files: group by concept (not by file type).
- **Don't** build structure for a hypothetical future "just in case": structure follows real code.

## Moving / renaming safely

1. `bash scripts/check.sh` green first.
2. `git mv` (preserves history); update imports/references with a global search (`grep -rn "old-name"`).
3. **Only move in the commit** (`refactor(structure): move X to Y`), with no behavior change.
4. `bash scripts/check.sh` green after. Update `ARCHITECTURE.md`, the module README, and `docs/MODULES.md` (paths) in the same commit.
5. Large reorganization: do it per module, one commit at a time.

## Audit

`bash scripts/structure-check.sh` flags: oversized files, modules without README, junk-drawer folders, forbidden names, `.bak/.orig` files, forgotten placeholders. Run it at the end of each work block and during end-of-queue triage (`/structure`).

## Anti-patterns

Structure by technical type that forces touching 5 folders per feature · `utils` folder that keeps growing · code at the root · two folders for the same concept · relative paths like `../../../..` (a sign the module is in the wrong place or a missing alias) · committed generated files · `file_old.ext` copies.
