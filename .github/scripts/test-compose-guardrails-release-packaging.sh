#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
tool_root="$(cd "$SCRIPT_DIR/../.." && pwd -P)"
project_dir="$tool_root/tools/compose-guardrails"
app_name="compose-guardrails"
install_dir="$project_dir/build/install/$app_name"
dist_dir="$project_dir/build/distributions"

"$tool_root/gradlew" --project-dir "$tool_root" \
  :tools:compose-guardrails:installDist \
  :tools:compose-guardrails:distZip \
  :tools:compose-guardrails:distTar

install_bin="$install_dir/bin/$app_name"

if [[ ! -x "$install_bin" ]]; then
  echo "FAIL: expected packaged launcher at $install_bin" >&2
  exit 1
fi

jar_file="$(find "$install_dir/lib" -maxdepth 1 -name "${app_name}-*.jar" | sort | head -n 1)"
if [[ -z "$jar_file" ]]; then
  echo "FAIL: packaged distribution is missing the application jar" >&2
  exit 1
fi

jar_contents="$(jar tf "$jar_file")"
for token in \
  "prompts/compose-review.md" \
  "prompts/output-format.md" \
  "prompts/rules/default-index.txt"
do
  if [[ "$jar_contents" != *"$token"* ]]; then
    echo "FAIL: packaged jar is missing prompt asset: $token" >&2
    exit 1
  fi
done

zip_file="$(find "$dist_dir" -maxdepth 1 -name '*.zip' | sort | head -n 1)"
tar_file="$(find "$dist_dir" -maxdepth 1 -name '*.tar' | sort | head -n 1)"

if [[ -z "$zip_file" || -z "$tar_file" ]]; then
  echo "FAIL: expected distZip and distTar artifacts" >&2
  exit 1
fi

if ! unzip -l "$zip_file" | grep -Fq -- "/bin/$app_name"; then
  echo "FAIL: distZip is missing packaged launcher" >&2
  exit 1
fi

if ! tar -tf "$tar_file" | grep -Fq -- "/bin/$app_name"; then
  echo "FAIL: distTar is missing packaged launcher" >&2
  exit 1
fi

tmp_report="$(mktemp)"
MOBILE_AI_PROVIDER=fake "$install_bin" guardrails check "$project_dir/examples/bad-compose-sample" --rule-set default --output "$tmp_report"

if ! grep -Fq -- "# Compose Guardrails Report" "$tmp_report"; then
  echo "FAIL: packaged CLI did not write a Markdown report" >&2
  exit 1
fi

if ! grep -Fq -- "Total findings:" "$tmp_report"; then
  echo "FAIL: packaged CLI report is missing summary content" >&2
  exit 1
fi

echo "Compose Guardrails distribution packaging passed."
