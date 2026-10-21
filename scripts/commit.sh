#!/usr/bin/env bash
# Usage: bash scripts/commit.sh "feat(scope): message" [paths...]
# - validates Conventional Commits
# - stages (everything, or only the given paths)
# - blocks sensitive files and secret patterns
set -euo pipefail

msg="${1:-}"
shift || true
if [[ -z "$msg" ]]; then echo 'Usage: bash scripts/commit.sh "type(scope): message" [paths...]' >&2; exit 1; fi

re='^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-zA-Z0-9_./-]+\))?!?: .{3,}'
if ! [[ "$msg" =~ $re ]]; then
  echo "Message does not follow Conventional Commits: $msg" >&2
  echo "Examples: feat(ui): add settings screen | fix(parser): handle empty input | docs: update roadmap" >&2
  exit 2
fi

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "Not a git repository." >&2; exit 1; }

if [[ $# -gt 0 ]]; then git add -- "$@"; else git add -A; fi

if git diff --cached --quiet; then echo "Nothing to commit."; exit 0; fi

# sensitive files
bad_files="$(git diff --cached --name-only | grep -Ei '(^|/)(\.env(\.[^/]*)?|[^/]*\.pem|[^/]*\.p12|[^/]*\.pfx|id_rsa|id_ed25519|credentials\.json)$' | grep -Evi '\.env\.(example|sample|template)$' || true)"
if [[ -n "$bad_files" ]]; then
  echo "BLOCKED — sensitive files staged:" >&2; echo "$bad_files" >&2
  git reset -q
  exit 3
fi

# secret patterns in the added lines
pat='-----BEGIN [A-Z ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|ghp_[A-Za-z0-9]{36,}|github_pat_[A-Za-z0-9_]{50,}|(^|[^A-Za-z0-9])sk-[A-Za-z0-9_-]{32,}|xox[baprs]-[A-Za-z0-9-]{10,}'
added="$(git diff --cached -U0 --no-color | grep -E -e '^\+[^+]' || true)"
if printf '%s\n' "$added" | grep -Eq -e "$pat"; then
  echo "BLOCKED — possible secret in the diff. Remove it (use environment variables) and try again." >&2
  git reset -q
  exit 3
fi

git commit -q -m "$msg"
echo "Commit made: $(git log -1 --pretty='%h %s')"
