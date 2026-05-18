#!/usr/bin/env bash

set -euo pipefail

ACTION_FILE=".github/actions/compose-guardrails/action.yml"

for token in \
  "name: Compose Guardrails" \
  "target:" \
  "rule_set:" \
  "provider:" \
  "report_path:" \
  "changed_files_only:" \
  "fail_on_findings:" \
  "write_step_summary:" \
  "analysis_mode:" \
  "fallback_used:" \
  "report_path:" \
  "report_mode:" \
  "total_findings:" \
  "../../scripts/run-compose-guardrails.sh"
do
  if ! grep -Fq -- "$token" "$ACTION_FILE"; then
    echo "Missing expected token in $ACTION_FILE: $token" >&2
    exit 1
  fi
done

echo "Compose Guardrails action manifest looks consistent."
