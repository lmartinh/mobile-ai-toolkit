#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=.github/scripts/compose-guardrails-lib.sh
source "$SCRIPT_DIR/compose-guardrails-lib.sh"

tool_root="$(cd "$SCRIPT_DIR/../.." && pwd -P)"
consumer_dir="$(mktemp -d)"
report_path="artifacts/compose-guardrails-report.md"

mkdir -p "$consumer_dir/src/main"
cat > "$consumer_dir/src/main/Sample.kt" <<'KOTLIN'
import androidx.compose.runtime.Composable

@Composable
fun Sample() {
    println("hello")
}
KOTLIN

if [[ -x "$consumer_dir/gradlew" ]]; then
  echo "FAIL: consumer repo should not provide its own Gradle wrapper" >&2
  exit 1
fi

MOBILE_AI_TOOLKIT_DIR="$tool_root" \
COMPOSE_GUARDRAILS_WORKSPACE="$consumer_dir" \
COMPOSE_GUARDRAILS_TARGET="src/main" \
COMPOSE_GUARDRAILS_REPORT_PATH="$report_path" \
COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY="false" \
COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS="false" \
COMPOSE_GUARDRAILS_WRITE_STEP_SUMMARY="false" \
MOBILE_AI_PROVIDER="fake" \
GITHUB_EVENT_NAME="push" \
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

echo "External repository simulation passed."
