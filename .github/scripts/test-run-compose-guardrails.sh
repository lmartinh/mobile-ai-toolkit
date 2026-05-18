#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=.github/scripts/compose-guardrails-lib.sh
source "$SCRIPT_DIR/compose-guardrails-lib.sh"

assert_true() {
  local name="$1"
  shift
  if "$@"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name" >&2
    exit 1
  fi
}

assert_false() {
  local name="$1"
  shift
  if "$@"; then
    echo "FAIL: $name" >&2
    exit 1
  else
    echo "PASS: $name"
  fi
}

assert_contains() {
  local name="$1"
  local haystack="$2"
  local needle="$3"
  if [[ "$haystack" == *"$needle"* ]]; then
    echo "PASS: $name"
  else
    echo "FAIL: $name" >&2
    exit 1
  fi
}

assert_file_contains() {
  local name="$1"
  local file="$2"
  local needle="$3"
  if grep -Fq -- "$needle" "$file"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name" >&2
    exit 1
  fi
}

assert_true "foo.kt is Kotlin" cg_is_kotlin_source_file "foo.kt"
assert_false "foo.txt is ignored" cg_is_kotlin_source_file "foo.txt"
assert_false "build.gradle.kts is ignored" cg_is_kotlin_source_file "build.gradle.kts"

assert_true "inside target is included" cg_is_within_target_scope "tools/compose-guardrails/src/main/kotlin/Foo.kt" "tools/compose-guardrails/src/main"
assert_true "workspace root target includes file" cg_is_within_target_scope "src/main/kotlin/Foo.kt" "."
assert_false "outside target is excluded" cg_is_within_target_scope "tools/compose-guardrails/src/test/kotlin/FooTest.kt" "tools/compose-guardrails/src/main"
assert_false "sibling path is excluded" cg_is_within_target_scope "tools/compose-guardrails/src/mainly/Foo.kt" "tools/compose-guardrails/src/main"
assert_false "examples path is excluded" cg_is_within_target_scope "tools/compose-guardrails/examples/bad-compose-sample/LoginScreen.kt" "tools/compose-guardrails/src/main"

missing_file="does/not/exist/Missing.kt"
if [[ -f "$missing_file" ]]; then
  echo "FAIL: missing-file precondition" >&2
  exit 1
fi
echo "PASS: missing/deleted file is absent and can be ignored by workflow filter"

tmp_dir="$(mktemp -d)"
report_file="$tmp_dir/report.md"
cg_write_no_files_report "$report_file" "tools/compose-guardrails/src/main"

assert_file_contains "no-files report has header" "$report_file" "# Compose Guardrails Report"
assert_file_contains "no-files report has zero findings" "$report_file" "- Total findings: 0"
assert_file_contains "no-files report mentions scope" "$report_file" "No changed Kotlin files were found inside configured target scope"

echo "All compose-guardrails changed-files helper tests passed."
