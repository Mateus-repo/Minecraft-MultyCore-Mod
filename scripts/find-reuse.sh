#!/usr/bin/env bash
# Reuse before writing: search the catalog and the code for something that already does what you need.
# Usage: bash scripts/find-reuse.sh term [term...]     (e.g. find-reuse.sh chunk split batch)
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1
if [[ $# -eq 0 ]]; then echo "Usage: bash scripts/find-reuse.sh term [term...]" >&2; exit 1; fi

in_git=0; git rev-parse --is-inside-work-tree >/dev/null 2>&1 && in_git=1

for t in "$@"; do
  esc="$(printf '%s' "$t" | sed 's/[][\.*^$(){}?+|/]/\\&/g')"
  echo "=== '$t'"

  echo "--- catalog (docs/MODULES.md)"
  grep -n -i -F -- "$t" docs/MODULES.md 2>/dev/null | head -10

  echo "--- definitions in the code"
  pat="(function|def|fn|func|class|struct|enum|trait|interface|type|const|let|var|record)[[:space:]].*${esc}"
  if (( in_git )); then
    git grep --untracked -n -i -I -E "$pat" -- . ':!docs' ':!*.md' ':!.opencode' ':!*.lock' ':!*-lock.json' 2>/dev/null | head -25
  else
    grep -rn -i -I -E "$pat" . --exclude-dir=node_modules --exclude-dir=.git --exclude-dir=docs \
      --exclude-dir=.opencode --exclude='*.md' 2>/dev/null | head -25
  fi

  echo "--- file names"
  if (( in_git )); then git ls-files -co --exclude-standard; else find . -type f -not -path './.git/*' -not -path '*/node_modules/*'; fi \
    | grep -i -F -- "$t" | head -10
  echo
done
echo "Nothing relevant? Create the code in the right place (skill reusable-modules) and register it in docs/MODULES.md."
