#!/usr/bin/env bash

set -euo pipefail

ACTION_FILE=".github/actions/compose-guardrails/action.yml"

for token in \
  "name: Compose Guardrails" \
  "target:" \
  "rule-set:" \
  "provider:" \
  "model:" \
  "api-key:" \
  "report-path:" \
  "changed-files-only:" \
  "fail-on-findings:" \
  "write-step-summary:" \
  "MOBILE_AI_TOOLKIT_DIR:" \
  "COMPOSE_GUARDRAILS_WORKSPACE:" \
  "analysis-mode:" \
  "fallback-used:" \
  "report-path:" \
  "report-mode:" \
  "total-findings:" \
  "github.action_path" \
  "GITHUB_STEP_SUMMARY" \
  "Report artifact: caller responsibility" \
  "../../.." \
  "script_path=" \
  'bash "$script_path"'
do
  if ! grep -Fq -- "$token" "$ACTION_FILE"; then
    echo "Missing expected token in $ACTION_FILE: $token" >&2
    exit 1
  fi
done

if grep -Fq -- "actions/upload-artifact" "$ACTION_FILE"; then
  echo "Action should not upload artifacts itself" >&2
  exit 1
fi

echo "Compose Guardrails action manifest looks consistent."
