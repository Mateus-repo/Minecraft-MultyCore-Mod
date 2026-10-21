#!/usr/bin/env bash
# Audit of structure, documentation and modularity.
# Only WARNINGS (exit 0) by default. With --strict (or STRICT_STRUCTURE=1) it fails if there are warnings.
# Usage: bash scripts/structure-check.sh [--strict]
# Variables: MAX_FILE_LINES (default 300)
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

STRICT=0
[[ "${1:-}" == "--strict" || "${STRICT_STRUCTURE:-}" == "1" ]] && STRICT=1
MAX="${MAX_FILE_LINES:-300}"
CODE_EXT='(js|jsx|mjs|cjs|ts|tsx|py|rs|go|java|kt|cs|c|cc|cpp|h|hpp|rb|php|swift|gd|lua|vue|svelte)'
warns=0
warn() { warns=$((warns+1)); echo "  ⚠ $*"; }

# project file list (respects .gitignore when there is git)
if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  raw="$(git ls-files -co --exclude-standard)"
else
  raw="$(find . -type f -not -path './.git/*' -not -path '*/node_modules/*' -not -path '*/.venv/*' \
         -not -path '*/dist/*' -not -path '*/build/*' -not -path '*/target/*' | sed 's|^\./||')"
fi
files=""
while IFS= read -r f; do [[ -n "$f" && -f "$f" ]] && files+="$f"$'\n'; done <<<"$raw"

code="$(printf '%s' "$files" | grep -E "\.${CODE_EXT}$" | grep -Ev '(^|/)(node_modules|vendor|dist|build|generated|migrations)/|\.min\.' || true)"
src_code="$(printf '%s\n' "$code" | grep -E '^(src|app|lib)/' || true)"

echo "▶ Structure and documentation"

# 1. code files that are too large
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  n=$(wc -l < "$f" | tr -d ' ')
  (( n > MAX )) && warn "$f has $n lines (> $MAX): split by responsibility (skill refactoring-modular)"
done <<<"$code"

# 2. modules (folders in src/app/lib with >= 3 code files) without README.md
mods="$(printf '%s\n' "$src_code" | grep -Ev '(^|/)(tests?|__tests__|fixtures)/' \
  | awk -F/ 'NF>2{d=$1; for(i=2;i<NF;i++) d=d"/"$i; c[d]++} END{for(k in c) if(c[k]>=3) print k}' | sort)"
while IFS= read -r d; do
  [[ -z "$d" ]] && continue
  [[ -f "$d/README.md" ]] || warn "module '$d' has no README.md (template: docs/templates/MODULE_README.md)"
done <<<"$mods"

# 3. junk-drawer folders and vague names
vague="$(printf '%s' "$files" | awk -F/ '{for(i=1;i<NF;i++) print $i}' | sort -u \
  | grep -Ei '^(utils?|helpers?|misc|stuff|temp|tmp|new|old|final|copy|backup|test2)$' || true)"
while IFS= read -r d; do
  [[ -z "$d" ]] && continue
  warn "folder '$d': vague/junk-drawer name — organize by concept (skill project-structure)"
done <<<"$vague"

# 4. backup / copy files
bk="$(printf '%s' "$files" | grep -Ei '(\.(bak|orig|rej|swp)$|~$|(_old|_copy|_backup|-copy)\.[a-z0-9]+$)' || true)"
while IFS= read -r f; do [[ -n "$f" ]] && warn "backup/copy file: $f (git keeps the history)"; done <<<"$bk"

# 5. forgotten placeholders in .md files
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  case "$f" in .opencode/*|docs/templates/*|TEMPLATE_GUIDE.md|docs/TEMPLATE_GUIDE.md) continue;; esac
  grep -qE '\{\{(PROJECT_NAME|DESCRIPTION|STACK|DATE)\}\}' "$f" && warn "placeholder left to replace in $f"
done <<<"$(printf '%s' "$files" | grep -E '\.md$' || true)"

# 6. module catalog still empty when there is already code
if [[ -n "$src_code" && -f docs/MODULES.md ]] && grep -q '_to be filled in_' docs/MODULES.md; then
  warn "docs/MODULES.md is still empty and there is already code in src/ (skill code-documentation)"
fi

echo
if (( warns == 0 )); then echo "OK: no structure warnings."; exit 0; fi
echo "$warns warning(s)."
(( STRICT )) && exit 1
exit 0
