#!/usr/bin/env bash
# Usage: bash scripts/init.sh "Project Name" "Short description" "Stack"
# Replaces placeholders, initializes git and makes the first commit.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

NAME="${1:-}"
DESC="${2:-}"
STACK="${3:-to be defined}"
if [[ -z "$NAME" || -z "$DESC" ]]; then
  echo 'Usage: bash scripts/init.sh "Project Name" "Short description" "Stack"' >&2
  exit 1
fi
DATE="$(date +%Y-%m-%d)"

# escape \ & | for use in sed with the | delimiter
esc() { printf '%s' "$1" | sed -e 's/[\\&|]/\\&/g'; }
N="$(esc "$NAME")"; D="$(esc "$DESC")"; S="$(esc "$STACK")"

shopt -s nullglob
files=(./*.md docs/*.md)
for f in "${files[@]}"; do
  [[ "$(basename "$f")" == "TEMPLATE_GUIDE.md" ]] && continue
  sed -i.bak \
    -e "s|{{PROJECT_NAME}}|$N|g" \
    -e "s|{{DESCRIPTION}}|$D|g" \
    -e "s|{{STACK}}|$S|g" \
    -e "s|{{DATE}}|$DATE|g" "$f"
  rm -f "$f.bak"
done

# the template guide moves into docs/
if [[ -f TEMPLATE_GUIDE.md ]]; then mv TEMPLATE_GUIDE.md docs/TEMPLATE_GUIDE.md; fi
mkdir -p src && touch src/.gitkeep

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git init -q -b main 2>/dev/null || { git init -q && git checkout -q -b main; }
  echo "Git repository initialized."
fi

if [[ -n "$(git status --porcelain)" ]]; then
  git add -A
  git commit -q -m "chore: initialize project from template" && echo "First commit made."
fi

left="$(grep -l -e '{{PROJECT_NAME}}' -e '{{DESCRIPTION}}' -e '{{STACK}}' -e '{{DATE}}' ./*.md docs/*.md 2>/dev/null | grep -v 'docs/TEMPLATE_GUIDE.md' || true)"
if [[ -n "$left" ]]; then echo "Warning: placeholders left to replace in:"; echo "$left"; fi

cat <<MSG

Ready: $NAME
Next steps:
  1. opencode
  2. /bootstrap
  3. /autopilot 120
MSG
