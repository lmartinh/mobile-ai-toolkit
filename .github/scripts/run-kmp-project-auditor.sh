#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
MOBILE_AI_TOOLKIT_DIR="${MOBILE_AI_TOOLKIT_DIR:-$(cd "$SCRIPT_DIR/../.." && pwd -P)}"
KMP_PROJECT_AUDITOR_WORKSPACE="${KMP_PROJECT_AUDITOR_WORKSPACE:-${GITHUB_WORKSPACE:-$(pwd -P)}}"
KMP_PROJECT_AUDITOR_TARGET="${KMP_PROJECT_AUDITOR_TARGET:-tools/kmp-project-auditor/examples/bad-kmp-library}"
KMP_PROJECT_AUDITOR_REPORT_PATH="${KMP_PROJECT_AUDITOR_REPORT_PATH:-artifacts/kmp-project-auditor-report.md}"
KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS="${KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS:-false}"
KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY="${KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY:-true}"
MOBILE_AI_PROVIDER="${MOBILE_AI_PROVIDER:-fake}"

resolve_in_workspace() {
  local input_path="$1"
  local workspace_root="$2"

  if [[ "$input_path" = /* ]]; then
    echo "$input_path"
  else
    echo "$workspace_root/$input_path"
  fi
}

normalize_abs() {
  local input_path="$1"
  local parent
  local name
  parent="$(cd "$(dirname "$input_path")" 2>/dev/null && pwd -P || dirname "$input_path")"
  name="$(basename "$input_path")"
  echo "$parent/$name"
}

assert_no_whitespace_path() {
  local value="$1"
  local label="$2"
  if [[ "$value" =~ [[:space:]] ]]; then
    echo "Error: $label contains whitespace, which is not supported in this CI script: $value" >&2
    exit 1
  fi
}

TARGET_ABS="$(normalize_abs "$(resolve_in_workspace "$KMP_PROJECT_AUDITOR_TARGET" "$KMP_PROJECT_AUDITOR_WORKSPACE")")"
REPORT_ABS="$(normalize_abs "$(resolve_in_workspace "$KMP_PROJECT_AUDITOR_REPORT_PATH" "$KMP_PROJECT_AUDITOR_WORKSPACE")")"
REPORT_DIR="$(dirname "$REPORT_ABS")"

assert_no_whitespace_path "$MOBILE_AI_TOOLKIT_DIR" "MOBILE_AI_TOOLKIT_DIR"
assert_no_whitespace_path "$KMP_PROJECT_AUDITOR_WORKSPACE" "KMP_PROJECT_AUDITOR_WORKSPACE"
assert_no_whitespace_path "$TARGET_ABS" "KMP_PROJECT_AUDITOR_TARGET"
assert_no_whitespace_path "$REPORT_ABS" "KMP_PROJECT_AUDITOR_REPORT_PATH"

mkdir -p "$REPORT_DIR"

report_mode="report-only"
if [[ "$KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS" == "true" ]]; then
  report_mode="fail-on-findings"
fi

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "report_path=$REPORT_ABS"
    echo "report_mode=$report_mode"
    echo "total_findings=0"
  } >> "$GITHUB_OUTPUT"
fi

set +e
MOBILE_AI_PROVIDER="$MOBILE_AI_PROVIDER" "$MOBILE_AI_TOOLKIT_DIR/gradlew" --quiet --console=plain --project-dir "$MOBILE_AI_TOOLKIT_DIR" :tools:kmp-project-auditor:run \
  --args="kmp audit ${TARGET_ABS} --output ${REPORT_ABS}"
audit_exit_code=$?
set -e

if [[ "$audit_exit_code" -ne 0 && ! -f "$REPORT_ABS" ]]; then
  cat > "$REPORT_ABS" <<EOF
# KMP Project Audit Report
## Status
The audit did not complete.
## Reason
The audit command failed before a full report was generated.
## Configuration
- Provider: \`$MOBILE_AI_PROVIDER\`
- Target: \`$TARGET_ABS\`
- Report path: \`$REPORT_ABS\`
## Notes
No full audit report was generated.
EOF
fi

if [[ ! -f "$REPORT_ABS" ]]; then
  echo "Error: report file was not created at $REPORT_ABS" >&2
  exit 1
fi

deterministic_findings="$(sed -n 's/^- Deterministic findings: //p' "$REPORT_ABS" | head -n 1 | tr -d '[:space:]')"
ai_findings="$(sed -n 's/^- AI findings: //p' "$REPORT_ABS" | head -n 1 | tr -d '[:space:]')"

if [[ -z "$deterministic_findings" || ! "$deterministic_findings" =~ ^[0-9]+$ ]]; then
  deterministic_findings=0
fi
if [[ -z "$ai_findings" || ! "$ai_findings" =~ ^[0-9]+$ ]]; then
  ai_findings=0
fi

total_findings="$((deterministic_findings + ai_findings))"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "total_findings=$total_findings"
  } >> "$GITHUB_OUTPUT"
fi

if [[ "$KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY" == "true" && -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "## KMP Project Auditor"
    echo ""
    echo "- Provider: \`$MOBILE_AI_PROVIDER\`"
    echo "- Target: \`$TARGET_ABS\`"
    echo "- Report path: \`$REPORT_ABS\`"
    echo "- Report mode: \`$report_mode\`"
    echo "- Artifact name: \`kmp-project-auditor-report\`"
    echo "- Total findings: \`$total_findings\`"
    echo ""
    echo "Default behavior uses fake provider and generates report artifacts without secrets."
  } >> "$GITHUB_STEP_SUMMARY"
fi

if [[ "$KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS" == "true" && "$total_findings" -gt 0 ]]; then
  echo "Failing because KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS=true and findings were detected: $total_findings" >&2
  exit 2
fi

if [[ "$audit_exit_code" -ne 0 ]]; then
  echo "Audit command failed with exit code $audit_exit_code." >&2
  exit "$audit_exit_code"
fi
