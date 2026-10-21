#!/usr/bin/env bash
# Full verification: detects the stack and runs lint + tests (+ build when it exists).
# To customize: create scripts/check.local.sh (it takes full priority).
# Exits with a non-zero code if something fails.
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

if [[ -f scripts/check.local.sh ]]; then exec bash scripts/check.local.sh; fi

fail=0
ran=0
step() { # step "name" command...
  local name="$1"; shift
  ran=$((ran+1))
  echo "▶ $name"
  if "$@"; then echo "  ✔ $name"; else echo "  ✘ $name"; fail=$((fail+1)); fi
}
has() { command -v "$1" >/dev/null 2>&1; }
npm_script() { [[ -f package.json ]] && grep -q "\"$1\"[[:space:]]*:" package.json; }
# 'npm init' creates a test script that fails on purpose: do not count it
npm_test_stub() { grep -Eq '"test"[[:space:]]*:[[:space:]]*"[^"]*no test specified' package.json; }
# pytest exits with 5 when there are no tests: not a failure
pytest_ok() { pytest -q; local c=$?; [[ $c -eq 0 || $c -eq 5 ]]; }

# Node / TypeScript
if [[ -f package.json ]]; then
  pm=npm; [[ -f pnpm-lock.yaml ]] && pm=pnpm; [[ -f yarn.lock ]] && pm=yarn; [[ -f bun.lockb || -f bun.lock ]] && pm=bun
  npm_script lint      && step "lint ($pm)"      $pm run lint
  npm_script typecheck && step "typecheck ($pm)" $pm run typecheck
  npm_script test      && ! npm_test_stub && step "test ($pm)" $pm run test
  npm_script build     && step "build ($pm)"     $pm run build
fi

# Python
if [[ -f pyproject.toml || -f requirements.txt || -f setup.py ]]; then
  has ruff   && step "ruff"   ruff check .
  has mypy   && [[ -f pyproject.toml ]] && grep -q '\[tool.mypy\]' pyproject.toml && step "mypy" mypy .
  has pytest && step "pytest" pytest_ok
fi

# Rust
if [[ -f Cargo.toml ]]; then
  step "cargo clippy" cargo clippy --all-targets -- -D warnings
  step "cargo test"   cargo test
fi

# Go
if [[ -f go.mod ]]; then
  step "go vet"  go vet ./...
  step "go test" go test ./...
fi

# .NET
if compgen -G "*.sln" >/dev/null || compgen -G "*.csproj" >/dev/null || compgen -G "*/*.csproj" >/dev/null || compgen -G "*/*/*.csproj" >/dev/null; then
  step "dotnet build" dotnet build --nologo -v q
  step "dotnet test"  dotnet test --nologo -v q --no-build
fi

# Godot (no tests by default: only a script check, if the binary exists)
if [[ -f project.godot ]]; then
  if has godot; then step "godot (load project)" godot --headless --quit --path .; else echo "ℹ Godot: 'godot' binary not found on PATH — manual verification needed."; fi
fi

# Structure, documentation and modularity (warnings only; see scripts/structure-check.sh)
[[ -x scripts/structure-check.sh || -f scripts/structure-check.sh ]] && bash scripts/structure-check.sh || true
echo

# The repo's own shell scripts
for f in scripts/*.sh; do step "bash -n $f" bash -n "$f"; done

echo
if (( ran == 0 )); then echo "No verification detected."; exit 0; fi
if (( fail > 0 )); then echo "FAILED: $fail of $ran checks."; exit 1; fi
echo "OK: $ran checks passed."
