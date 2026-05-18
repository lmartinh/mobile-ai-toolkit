#!/usr/bin/env bash

set -euo pipefail

cg_normalize_rel_path() {
  local path="$1"
  path="${path#./}"
  while [[ "$path" == */ ]]; do
    path="${path%/}"
  done
  printf '%s\n' "$path"
}

cg_contains_whitespace() {
  local value="$1"
  [[ "$value" == *[[:space:]]* ]]
}

cg_assert_no_whitespace_path() {
  local path="$1"
  local label="$2"
  if cg_contains_whitespace "$path"; then
    echo "Error: ${label} contains whitespace and is not supported in CI path mode: $path" >&2
    return 1
  fi
}

cg_is_kotlin_source_file() {
  local file="$1"
  case "$file" in
    *.kt) return 0 ;;
    *) return 1 ;;
  esac
}

cg_is_within_target_scope() {
  local file_rel
  local target_rel

  file_rel="$(cg_normalize_rel_path "$1")"
  target_rel="$(cg_normalize_rel_path "$2")"

  [[ "$file_rel" == "$target_rel" || "$file_rel" == "$target_rel"/* ]]
}

cg_extract_total_findings() {
  local report_path="$1"
  grep -E "^- Total findings:" "$report_path" | sed -E 's/^- Total findings: ([0-9]+)$/\1/' | awk '{sum += $1} END {print sum+0}'
}

cg_write_no_files_report() {
  local report_path="$1"
  local target_rel="$2"

  cat > "$report_path" <<REPORT
# Compose Guardrails Report

## Summary
- Analyzed path: \`changed-files\`
- Kotlin files scanned: 0
- Total findings: 0
- Affected files: 0
- Findings by severity: error=0, warning=0, info=0

No changed Kotlin files were found inside configured target scope: \`$target_rel\`.
REPORT
}
