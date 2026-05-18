#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=.github/scripts/compose-guardrails-lib.sh
source "$SCRIPT_DIR/compose-guardrails-lib.sh"

MOBILE_AI_TOOLKIT_DIR="${MOBILE_AI_TOOLKIT_DIR:-$(cd "$SCRIPT_DIR/../.." && pwd -P)}"
COMPOSE_GUARDRAILS_TARGET="${COMPOSE_GUARDRAILS_TARGET:-tools/compose-guardrails/src/main}"
COMPOSE_GUARDRAILS_RULE_SET="${COMPOSE_GUARDRAILS_RULE_SET:-default}"
COMPOSE_GUARDRAILS_REPORT_PATH="${COMPOSE_GUARDRAILS_REPORT_PATH:-artifacts/compose-guardrails-report.md}"
COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY="${COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY:-false}"
COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS="${COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS:-false}"
GITHUB_EVENT_NAME="${GITHUB_EVENT_NAME:-}"
GITHUB_BASE_REF="${GITHUB_BASE_REF:-}"
GITHUB_SHA="${GITHUB_SHA:-}"
COMPOSE_GUARDRAILS_WORKSPACE="${COMPOSE_GUARDRAILS_WORKSPACE:-${GITHUB_WORKSPACE:-$(pwd -P)}}"
MOBILE_AI_PROVIDER="${MOBILE_AI_PROVIDER:-fake}"

TARGET_REL="$(cg_normalize_rel_path "$COMPOSE_GUARDRAILS_TARGET")"
TARGET_ABS="$(cg_resolve_in_workspace "$TARGET_REL" "$COMPOSE_GUARDRAILS_WORKSPACE")"
REPORT_PATH="$(cg_resolve_in_workspace "$COMPOSE_GUARDRAILS_REPORT_PATH" "$COMPOSE_GUARDRAILS_WORKSPACE")"
REPORT_DIR="$(dirname "$REPORT_PATH")"

cg_assert_no_whitespace_path "$MOBILE_AI_TOOLKIT_DIR" "MOBILE_AI_TOOLKIT_DIR"
cg_assert_no_whitespace_path "$COMPOSE_GUARDRAILS_WORKSPACE" "COMPOSE_GUARDRAILS_WORKSPACE"
cg_assert_no_whitespace_path "$TARGET_ABS" "COMPOSE_GUARDRAILS_TARGET"
cg_assert_no_whitespace_path "$REPORT_PATH" "COMPOSE_GUARDRAILS_REPORT_PATH"

mkdir -p "$REPORT_DIR"

analysis_mode="target:${TARGET_REL}"
fallback_used="false"

run_guardrails_for_path() {
  local analysis_path="$1"
  local output_path="$2"

  cg_assert_no_whitespace_path "$analysis_path" "analysis path"
  cg_assert_no_whitespace_path "$output_path" "report output path"

  "$MOBILE_AI_TOOLKIT_DIR/gradlew" --project-dir "$MOBILE_AI_TOOLKIT_DIR" :tools:compose-guardrails:run \
    --args="guardrails check ${analysis_path} --rule-set ${COMPOSE_GUARDRAILS_RULE_SET} --output ${output_path}"
}

if [[ "$COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY" == "true" && "$GITHUB_EVENT_NAME" == "pull_request" ]]; then
  analysis_mode="changed-files:${TARGET_REL}"

  if [[ -z "$GITHUB_BASE_REF" || -z "$GITHUB_SHA" ]]; then
    echo "Error: pull_request changed-files mode requires GITHUB_BASE_REF and GITHUB_SHA." >&2
    exit 1
  fi

  git -C "$COMPOSE_GUARDRAILS_WORKSPACE" fetch --no-tags --depth=1 origin "$GITHUB_BASE_REF"

  changed_files=()
  while IFS= read -r changed_file; do
    changed_files+=("$changed_file")
  done < <(git -C "$COMPOSE_GUARDRAILS_WORKSPACE" diff --name-only "origin/${GITHUB_BASE_REF}...${GITHUB_SHA}")

  selected_files=()
  for file in "${changed_files[@]}"; do
    file_rel="$(cg_normalize_rel_path "$file")"
    file_abs="${COMPOSE_GUARDRAILS_WORKSPACE}/${file_rel}"

    if ! cg_is_kotlin_source_file "$file_rel"; then
      continue
    fi

    if ! cg_is_within_target_scope "$file_rel" "$TARGET_REL"; then
      continue
    fi

    if [[ ! -f "$file_abs" ]]; then
      continue
    fi

    cg_assert_no_whitespace_path "$file_abs" "changed file path"
    selected_files+=("$file_rel")
  done

  if [[ "${#selected_files[@]}" -eq 0 ]]; then
    cg_write_no_files_report "$REPORT_PATH" "$TARGET_REL"
  else
    : > "$REPORT_PATH"
    index=0
    for file_rel in "${selected_files[@]}"; do
      file_abs="${COMPOSE_GUARDRAILS_WORKSPACE}/${file_rel}"
      tmp_report="$(mktemp)"

      run_guardrails_for_path "$file_abs" "$tmp_report"

      {
        if [[ "$index" -gt 0 ]]; then
          echo
          echo "---"
          echo
        fi
        echo "## Changed file: \`$file_rel\`"
        echo
        cat "$tmp_report"
      } >> "$REPORT_PATH"

      index=$((index + 1))
    done
  fi
else
  if [[ "$COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY" == "true" && "$GITHUB_EVENT_NAME" != "pull_request" ]]; then
    fallback_used="true"
    analysis_mode="target-fallback-non-pr:${TARGET_REL}"
  fi

  run_guardrails_for_path "$TARGET_ABS" "$REPORT_PATH"
fi

total_findings="$(cg_extract_total_findings "$REPORT_PATH")"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "analysis-mode=${analysis_mode}"
    echo "fallback-used=${fallback_used}"
    echo "report-path=${REPORT_PATH}"
    echo "report-mode=$([[ "$COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS" == "true" ]] && echo "fail-on-findings" || echo "report-only")"
    echo "total-findings=${total_findings}"
  } >> "$GITHUB_OUTPUT"
fi
