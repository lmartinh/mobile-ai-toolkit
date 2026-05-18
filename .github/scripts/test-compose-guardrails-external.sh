#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=.github/scripts/compose-guardrails-lib.sh
source "$SCRIPT_DIR/compose-guardrails-lib.sh"

tool_root="$(cd "$SCRIPT_DIR/../.." && pwd -P)"
temp_root="$(mktemp -d)"
origin_dir="$temp_root/origin.git"
consumer_dir="$temp_root/consumer"
report_path="artifacts/compose-guardrails-report.md"

git init --bare "$origin_dir" >/dev/null
git clone "$origin_dir" "$consumer_dir" >/dev/null

cd "$consumer_dir"
git config user.name "Codex"
git config user.email "codex@example.com"

mkdir -p app/src/main app/src/test docs
cat > app/src/main/Included.kt <<'KOTLIN'
import androidx.compose.runtime.Composable

@Composable
fun Included() {
    println("base")
}
KOTLIN

cat > app/src/main/Deleted.kt <<'KOTLIN'
fun Deleted() = Unit
KOTLIN

cat > app/src/test/Outside.kt <<'KOTLIN'
fun Outside() = Unit
KOTLIN

cat > docs/notes.txt <<'TEXT'
base notes
TEXT

git add .
git commit -m "base" >/dev/null
git branch -M main
git push origin main >/dev/null

cat > app/src/main/Included.kt <<'KOTLIN'
import androidx.compose.runtime.Composable

@Composable
fun Included() {
    println("changed")
}
KOTLIN

cat > app/src/test/Outside.kt <<'KOTLIN'
fun Outside() = println("changed outside target")
KOTLIN

cat > docs/notes.txt <<'TEXT'
changed notes
TEXT

rm app/src/main/Deleted.kt
mkdir -p app/src/main
cat > app/src/main/README.txt <<'TEXT'
non-kotlin inside target
TEXT

git add -A
git commit -m "change" >/dev/null
current_sha="$(git rev-parse HEAD)"

MOBILE_AI_TOOLKIT_DIR="$tool_root" \
COMPOSE_GUARDRAILS_WORKSPACE="$consumer_dir" \
COMPOSE_GUARDRAILS_TARGET="app/src/main" \
COMPOSE_GUARDRAILS_REPORT_PATH="$report_path" \
COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY="true" \
COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS="false" \
COMPOSE_GUARDRAILS_WRITE_STEP_SUMMARY="false" \
MOBILE_AI_PROVIDER="fake" \
GITHUB_EVENT_NAME="pull_request" \
GITHUB_BASE_REF="main" \
GITHUB_SHA="$current_sha" \
"$SCRIPT_DIR/run-compose-guardrails.sh"

resolved_report_path="$(cg_resolve_in_workspace "$report_path" "$consumer_dir")"

if [[ ! -f "$resolved_report_path" ]]; then
  echo "FAIL: expected report at $resolved_report_path" >&2
  exit 1
fi

if ! grep -Fq -- "# Compose Guardrails Report" "$resolved_report_path"; then
  echo "FAIL: report is missing Markdown header" >&2
  exit 1
fi

if ! grep -Fq -- "Total findings:" "$resolved_report_path"; then
  echo "FAIL: report is missing summary content" >&2
  exit 1
fi

if ! grep -Fq -- "Changed file: \`app/src/main/Included.kt\`" "$resolved_report_path"; then
  echo "FAIL: changed target Kotlin file was not analyzed" >&2
  exit 1
fi

for excluded in "Outside.kt" "Deleted.kt" "notes.txt" "README.txt"; do
  if grep -Fq -- "$excluded" "$resolved_report_path"; then
    echo "FAIL: report unexpectedly contains excluded file marker: $excluded" >&2
    exit 1
  fi
done

if [[ -x "$consumer_dir/gradlew" ]]; then
  echo "FAIL: consumer repo should not provide its own Gradle wrapper" >&2
  exit 1
fi

echo "External repository changed-files simulation passed."
