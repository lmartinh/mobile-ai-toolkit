#!/usr/bin/env bash

set -euo pipefail

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

ROOT_DIR="$(pwd -P)"
TMP_DIR="$(mktemp -d)"
REPORT_PATH="$TMP_DIR/reports/kmp-report.md"
STEP_SUMMARY_PATH="$TMP_DIR/step-summary.md"
GITHUB_OUTPUT_PATH="$TMP_DIR/github-output.txt"

MOBILE_AI_TOOLKIT_DIR="$ROOT_DIR" \
KMP_PROJECT_AUDITOR_WORKSPACE="$ROOT_DIR" \
KMP_PROJECT_AUDITOR_TARGET="tools/kmp-project-auditor/examples/bad-kmp-library" \
KMP_PROJECT_AUDITOR_REPORT_PATH="$REPORT_PATH" \
KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS="false" \
KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY="true" \
GITHUB_STEP_SUMMARY="$STEP_SUMMARY_PATH" \
GITHUB_OUTPUT="$GITHUB_OUTPUT_PATH" \
MOBILE_AI_PROVIDER="fake" \
.github/scripts/run-kmp-project-auditor.sh

assert_true "report file created" test -f "$REPORT_PATH"
assert_file_contains "report has markdown title" "$REPORT_PATH" "# KMP Project Audit Report"
assert_file_contains "report has deterministic findings section" "$REPORT_PATH" "## Deterministic Findings"
assert_file_contains "report has ai findings section" "$REPORT_PATH" "## AI Findings"
assert_true "report does not include gradle logs" bash -c "! grep -Fq '> Task :' '$REPORT_PATH'"
assert_true "step summary created" test -f "$STEP_SUMMARY_PATH"
assert_file_contains "step summary has tool name" "$STEP_SUMMARY_PATH" "## KMP Project Auditor"
assert_file_contains "github output has report path" "$GITHUB_OUTPUT_PATH" "report_path="
assert_file_contains "github output has report mode" "$GITHUB_OUTPUT_PATH" "report_mode=report-only"

EXTERNAL_WORKSPACE="$TMP_DIR/external-workspace"
mkdir -p "$EXTERNAL_WORKSPACE"
mkdir -p "$EXTERNAL_WORKSPACE/shared/src/commonMain/kotlin"
cat > "$EXTERNAL_WORKSPACE/build.gradle.kts" <<'EOF'
plugins {
  kotlin("multiplatform")
}
EOF
cat > "$EXTERNAL_WORKSPACE/shared/src/commonMain/kotlin/Shared.kt" <<'EOF'
class Shared
EOF

mkdir -p "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/shared/ai-client/src/main/kotlin"
mkdir -p "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/shared/report-common/src/main/kotlin"
mkdir -p "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/tools/kmp-project-auditor/src/main/kotlin"
mkdir -p "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/tools/kmp-project-auditor/examples/bad-kmp-library/src/commonMain/kotlin"
cat > "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/gradlew" <<'EOF'
#!/usr/bin/env sh
exit 0
EOF
cat > "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/settings.gradle.kts" <<'EOF'
rootProject.name = "mobile-ai-toolkit"
EOF
cat > "$EXTERNAL_WORKSPACE/mobile-ai-toolkit/tools/kmp-project-auditor/examples/bad-kmp-library/src/commonMain/kotlin/Fake.kt" <<'EOF'
import android.content.Context
EOF

EXTERNAL_REPORT_PATH="$TMP_DIR/external-workspace-report.md"
MOBILE_AI_TOOLKIT_DIR="$ROOT_DIR" \
KMP_PROJECT_AUDITOR_WORKSPACE="$EXTERNAL_WORKSPACE" \
KMP_PROJECT_AUDITOR_TARGET="." \
KMP_PROJECT_AUDITOR_REPORT_PATH="$EXTERNAL_REPORT_PATH" \
KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS="false" \
KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY="false" \
MOBILE_AI_PROVIDER="fake" \
.github/scripts/run-kmp-project-auditor.sh

assert_true "external workspace report created" test -f "$EXTERNAL_REPORT_PATH"
assert_file_contains "external workspace analyzed path" "$EXTERNAL_REPORT_PATH" "$EXTERNAL_WORKSPACE"
assert_true "external workspace report excludes toolkit checkout paths" bash -c "! grep -Fq 'mobile-ai-toolkit/tools' '$EXTERNAL_REPORT_PATH'"
assert_true "external workspace report excludes toolkit source roots" bash -c "! grep -Fq 'mobile-ai-toolkit/shared' '$EXTERNAL_REPORT_PATH'"

FAIL_REPORT_PATH="$TMP_DIR/reports/kmp-failure-report.md"
set +e
MOBILE_AI_TOOLKIT_DIR="$ROOT_DIR" \
KMP_PROJECT_AUDITOR_WORKSPACE="$ROOT_DIR" \
KMP_PROJECT_AUDITOR_TARGET="missing-kmp-project" \
KMP_PROJECT_AUDITOR_REPORT_PATH="$FAIL_REPORT_PATH" \
KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS="false" \
KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY="false" \
MOBILE_AI_PROVIDER="fake" \
.github/scripts/run-kmp-project-auditor.sh
FAIL_EXIT_CODE=$?
set -e

if [[ "$FAIL_EXIT_CODE" -eq 0 ]]; then
  echo "FAIL: missing target should return non-zero exit" >&2
  exit 1
fi

assert_true "fallback report file created" test -f "$FAIL_REPORT_PATH"
assert_file_contains "fallback report status section" "$FAIL_REPORT_PATH" "## Status"
assert_file_contains "fallback report reason section" "$FAIL_REPORT_PATH" "## Reason"
assert_true "fallback report does not include gradle logs" bash -c "! grep -Fq '> Task :' '$FAIL_REPORT_PATH'"

echo "All kmp-project-auditor CI script tests passed."
