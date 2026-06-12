#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
repo_root="$(cd "$SCRIPT_DIR/../.." && pwd -P)"
project_dir="$repo_root/tools/kmp-project-auditor"
app_name="kmp-project-auditor"
install_dir="$project_dir/build/install/$app_name"
dist_dir="$project_dir/build/distributions"
release_version="${RELEASE_VERSION:-}"
release_version="${release_version#v}"

if [[ -n "$release_version" ]]; then
  "$repo_root/gradlew" --quiet --console=plain --project-dir "$repo_root" \
    "-PreleaseVersion=$release_version" \
    :tools:kmp-project-auditor:installDist \
    :tools:kmp-project-auditor:distZip \
    :tools:kmp-project-auditor:distTar
else
  "$repo_root/gradlew" --quiet --console=plain --project-dir "$repo_root" \
    :tools:kmp-project-auditor:installDist \
    :tools:kmp-project-auditor:distZip \
    :tools:kmp-project-auditor:distTar
fi

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
  "prompts/kmp-audit.md" \
  "prompts/output-format.md" \
  "prompts/rules/index.txt"
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

zip_listing="$(unzip -l "$zip_file")"
if [[ "$zip_listing" != *"/bin/$app_name"* ]]; then
  echo "FAIL: distZip is missing packaged launcher" >&2
  exit 1
fi

tar_listing="$(tar -tf "$tar_file")"
if [[ "$tar_listing" != *"/bin/$app_name"* ]]; then
  echo "FAIL: distTar is missing packaged launcher" >&2
  exit 1
fi

if [[ -n "$release_version" ]]; then
  if [[ "$(basename "$zip_file")" == *"SNAPSHOT"* || "$(basename "$tar_file")" == *"SNAPSHOT"* ]]; then
    echo "FAIL: release-version build should not produce SNAPSHOT archive names" >&2
    exit 1
  fi
fi

tmp_outside_repo="$(mktemp -d)"
tmp_report="$tmp_outside_repo/kmp-project-auditor-report.md"
bad_example="$project_dir/examples/bad-kmp-library"

(
  cd "$tmp_outside_repo"
  MOBILE_AI_PROVIDER=fake "$install_bin" kmp audit "$bad_example" --output "$tmp_report"
)

if [[ ! -f "$tmp_report" ]]; then
  echo "FAIL: packaged CLI did not create report file" >&2
  exit 1
fi

if ! grep -Fq -- "# KMP Project Audit Report" "$tmp_report"; then
  echo "FAIL: packaged CLI did not write Markdown report content" >&2
  exit 1
fi

if ! grep -Fq -- "## Deterministic Findings" "$tmp_report"; then
  echo "FAIL: packaged report is missing deterministic findings section" >&2
  exit 1
fi

if ! grep -Fq -- "## Additional AI Findings" "$tmp_report"; then
  echo "FAIL: packaged report is missing Additional AI Findings section" >&2
  exit 1
fi

if grep -Fq -- "> Task :" "$tmp_report"; then
  echo "FAIL: packaged report contains Gradle logs" >&2
  exit 1
fi

echo "kmp-project-auditor distribution packaging passed."
