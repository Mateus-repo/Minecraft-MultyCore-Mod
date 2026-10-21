---
name: reusable-modules
description: Write code in modules and functions reusable enough that any other part of the program can call them — reuse before writing, pure functions with explicit inputs and outputs, injected dependencies, minimal public API, promotion to shared when a second consumer appears, and registration in docs/MODULES.md. ALWAYS use before creating a new function, class, component, hook, or module, when you're about to copy and paste code, when a function depends on global state, when the user asks for modular, reusable, or decoupled code, and when designing a module's interface.
---

# Reusable modules

Goal: every piece of code is **useful outside the place it was born**. Any part of the program should be able to call it without knowing how it works inside — and without dragging half the project along.

## 1. Reuse before writing (mandatory)

Before creating any function/class/component:

1. `bash scripts/find-reuse.sh <terms>` (use 2–3 synonyms: `chunk split batch`).
2. Read `docs/MODULES.md` (the catalog) and the `shared/` folder.
3. Decide:
   - **Exists and fits** → use it. Don't reimplement.
   - **Exists and almost fits** → generalize it (add a parameter with a default, or extract the common part), with tests proving the old callers are unchanged. Don't copy.
   - **Doesn't exist** → create it in the right place (section 3) and register it in the catalog.
4. Found existing duplication? Don't resolve it mid-task: create a `T-NNN` in `TODO.md` (or resolve it in a separate `refactor` commit).

## 2. Designing a reusable function

| Property | Rule |
|-------------|-------|
| **Pure whenever possible** | Same inputs → same output; no I/O, no clock, no hidden randomness |
| **Explicit inputs** | Everything it needs comes as a parameter. No globals, singletons, or environment variables read inside |
| **Predictable output** | Returns a value (doesn't mutate arguments). Clear return type; no surprise `null`/exception |
| **One thing only** | Describable in a sentence without "and". Verb + object (`parseDuration`, `groupByDay`) |
| **Injected dependencies** | Clock, files, HTTP, DB enter as a parameter/interface → testable and swappable |
| **Contained parameters** | ≤ 4; beyond that, an options object with sensible defaults |
| **No boolean flags that change behavior** | Two clearly named functions > `doX(data, true)` |
| **Consistent errors** | One strategy across the project (typed exceptions **or** `Result`), documented |
| **Context-agnostic** | Knows nothing about UI, framework, routes, or its caller |
| **Idempotent / safe to repeat** | Where it makes sense (e.g. normalize, validate, write with a key) |

Effects (I/O, network, state, UI) go to the **edges**; the core is pure logic. An edge function is thin: read → call the core → write.

```ts
// ✗ Hard to reuse: global, I/O and logic mixed together
function saveReport() {
  const rows = db.query("select * from sales")       // global
  const total = rows.reduce((a, r) => a + r.value, 0)
  fs.writeFileSync(CONFIG.out, `Total: ${total}`)      // global + I/O
}

// ✓ Reusable pieces
export const sumBy = <T>(items: T[], pick: (x: T) => number): number =>
  items.reduce((acc, x) => acc + pick(x), 0)                       // pure
export const formatTotal = (total: number): string => `Total: ${total}`  // pure
export async function saveReport(src: SalesSource, out: Writer) {  // thin edge
  out.write(formatTotal(sumBy(await src.all(), (r) => r.value)))
}
```

## 3. Where new code lives (the 1-2-3 rule)

- **1 consumer:** it stays **inside the feature**, private (not exported).
- **2nd consumer appears:** **promote** it to `shared/<concept>/` (move with `git mv`, export it, document it, register it in `docs/MODULES.md`).
- **3rd case with variations:** only now do you generalize/parameterize. Until then, two small copies beat one wrong abstraction.
- Never in generic `utils`/`helpers`: organize **by concept** (`shared/dates/`, `shared/validation/`). See `project-structure`.

## 4. Designing a reusable module

1. **Purpose in one sentence** + what it **doesn't** do (goes in the module's README).
2. **Minimal public API** in a single entry point (`index`, `__init__`, `mod.rs`, exports). Everything else is internal. Importers go **only** through the entry point, never to internal files.
3. **Declared and allowed dependencies** (rule in `ARCHITECTURE.md`); no cycles; the module never imports its users.
4. **Contract tested:** tests against the public API (not internal details) — that way you can change the inside freely.
5. **Explicit configuration** (parameters/options object), not read from files or the environment inside.
6. **Stability:** changing the public API = a deliberate change (update callers, README, `MODULES.md`, `CHANGELOG`). Prefer adding over changing.

**Transplant test:** can you copy the module's folder into another project, carrying only the imports listed in the README, and use it? If not, there's hidden coupling — find it and cut it.

## 5. Register in the catalog

When creating/changing/removing a reusable function or module → a line in `docs/MODULES.md`: name · path · signature · what it does (1 line) · pure? · errors · used by. Without a register, nobody finds it and it will be rewritten (see `code-documentation`).

## Checklist before calling it done

- [ ] I looked before writing (`find-reuse.sh` + catalog); nothing duplicated.
- [ ] Each new function does one thing, has explicit inputs/outputs, and reads no hidden state.
- [ ] Effects stayed at the edges; the core is pure and tested.
- [ ] Only what's needed is exported; single entry point.
- [ ] Docstring + module README + line in `docs/MODULES.md`.
- [ ] Tests against the public API, including edge cases.
- [ ] Passes the transplant test (or is registered as failing it).

## Anti-patterns

`utils.ext` with 40 loose functions · function that reads global config · boolean flag that inverts behavior · copy-paste with small variations · trivial wrapper with no value · an "internal framework" before there are 3 cases · module that imports the code that calls it · exporting everything "just in case" · generalizing with 8 optional parameters to serve a hypothetical case.
