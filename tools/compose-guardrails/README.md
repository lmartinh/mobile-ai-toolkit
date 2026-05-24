# compose-guardrails

Kotlin CLI tool for Compose guardrail analysis using a provider-agnostic AI workflow.

Tool roadmap:
- `docs/roadmaps/compose-guardrails.md`

## Command
`mobile-ai guardrails check <path>`

Optional flags:
- `--rule-set default`
- `--rule-set advanced`
- `--rule-set all`
- `--output <path>`

If `--rule-set` is omitted, `default` is used.
If `--output` is provided, the Markdown report is written to that file.

## Current Behavior
- Accepts either:
  - one `.kt` file
  - one directory (recursive `.kt` scan)
- Detects Compose candidates using text heuristics.
- Detects `@Composable` function candidates with approximate line numbers.
- Loads prompts from classpath Markdown resources (`src/main/resources/prompts`).
- Builds a deterministic prompt bundle.
- Sends prompt to `AiClient` abstraction.
- Parses AI JSON response into structured findings using `kotlinx.serialization`.
- Renders a stable Markdown guardrails report.

Supported providers:
- `fake`
- `openai`
- `anthropic`
- `gemini`

## Structured Finding Schema
- `severity`: `error | warning | info`
- `rule_id`
- `title`
- `file_path`
- `explanation`
- `suggestion`
- `code_example` (optional)

## Supported Guardrails
Default rule set (enabled by default, conservative):
- `compose.no-business-logic-in-composables`
- `compose.state-hoisting`
- `compose.viewmodel-in-leaf-composable`
- `compose.unidirectional-data-flow`
- `compose.no-side-effects-in-composition`
- `compose.effect-key-quality`
- `compose.lazy-list-keys`
- `compose.missing-modifier-parameter`
- `compose.modifier-parameter-position`
- `compose.missing-content-description`
- `compose.clickable-without-semantics`
- `compose.android.collect-as-state-with-lifecycle` (`Android-only`)
- `compose.android.context-leak-risk` (`Android-only`)
- `compose.multiplatform.no-android-api-in-common` (`Multiplatform/commonMain`)
- `compose.multiplatform.platform-specific-ui-leak` (`Multiplatform/commonMain`)
- `compose.multiplatform.public-api-cleanliness` (`Multiplatform/commonMain`)

Advanced rule set (opt-in, lower-confidence/noise-prone):
- `compose.expensive-work-in-composition`
- `compose.unstable-parameters`
- `compose.derived-state-usage`
- `compose.large-composable`
- `compose.hardcoded-dimensions-and-colors`
- `compose.missing-preview`
- `compose.preview-with-real-dependencies`
- `compose.multiplatform.resources-usage` (`Multiplatform/commonMain`)

## Rule Catalog Assessment
- The default catalog is intentionally limited to higher-signal Compose, Android, accessibility, and commonMain boundary rules.
- The advanced catalog contains exploratory or noisier checks, especially performance, preview, design-system, and multiplatform resource guidance.
- Use `default` for routine code review and CI-friendly runs.
- Use `advanced` when you want deeper review coverage and can tolerate more subjective or lower-confidence findings.
- Use `all` when you explicitly want both sets in a single analysis pass.

## Severity Guidance
- `error`: high-confidence issue with clear correctness/architecture risk.
- `warning`: important maintainability or design issue.
- `info`: lower-risk improvement recommendation.

## Current Output
The command prints an execution summary and a Markdown report with:
- `# Compose Guardrails Report`
- `## Summary`
- `## Parser Warnings` (when applicable)
- `## Findings` grouped by file

## Local Run
```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

When running from the repository root, prefer absolute paths based on `$PWD` for `--args` values.

Run with explicit default rules:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default"
```

Run with advanced rules only:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set advanced"
```

Run with all rules:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set all"
```

Write report to a file:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

## CLI Rule-Set Assessment
- `mobile-ai guardrails check <path>` remains valid and uses the conservative `default` rule set.
- `--rule-set default` loads only the default catalog.
- `--rule-set advanced` loads only the advanced catalog.
- `--rule-set all` loads the union of default and advanced rules without duplicates.
- Invalid rule-set values fail fast with a clear error listing the supported values.
- Rule-set selection is implemented directly in the CLI argument parser and prompt loader; there is no additional config layer to maintain.

Run with fake provider:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Run with OpenAI provider:

```bash
MOBILE_AI_PROVIDER=openai \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gpt-4.1-mini \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Run with Anthropic provider:

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Run with Gemini provider:

```bash
MOBILE_AI_PROVIDER=gemini \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gemini-1.5-pro \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Environment variables used at runtime:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required for `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_MODEL` (required for `openai`, `anthropic`, `gemini`)

Security:
- Never commit API keys to the repository.

GitHub Actions example:

```yaml
env:
  MOBILE_AI_PROVIDER: anthropic
  MOBILE_AI_API_KEY: ${{ secrets.MOBILE_AI_API_KEY }}
  MOBILE_AI_MODEL: ${{ secrets.MOBILE_AI_MODEL }}
```

For deterministic CI checks without external calls, set `MOBILE_AI_PROVIDER=fake`.

## Release Packaging
Build an installable distribution:

```bash
./gradlew :tools:compose-guardrails:installDist
```

Build release archives:

```bash
./gradlew :tools:compose-guardrails:distZip :tools:compose-guardrails:distTar
```

Run the packaged launcher:

```bash
MOBILE_AI_PROVIDER=fake \
./tools/compose-guardrails/build/install/compose-guardrails/bin/compose-guardrails guardrails check tools/compose-guardrails/examples/bad-compose-sample
```

Release workflow:
- `.github/workflows/release-compose-guardrails.yml`
- Runs tests on tagged `v*` pushes.
- Validates the installed distribution and packaged prompt resources.
- Uploads ZIP/TAR artifacts for the release build.
- Local development keeps `0.1.0-SNAPSHOT`; the release workflow passes the tag-derived stable version into Gradle so release archives do not include `SNAPSHOT`.
- GitHub Release publishing is not implemented yet; the current release path uses workflow artifacts.

## GitHub Actions
Workflow:
- `.github/workflows/compose-guardrails.yml`

Behavior (report-only for now):
- runs `:shared:ai-client:test`, `:shared:report-common:test`, and `:tools:compose-guardrails:test`
- runs `compose-guardrails` with fake provider and writes a clean report using `--output`
- uploads `artifacts/compose-guardrails-report.md` as `compose-guardrails-report`
- findings do not fail the build yet

Default CI configuration:
- `MOBILE_AI_PROVIDER=fake`
- `COMPOSE_GUARDRAILS_TARGET=tools/compose-guardrails/src/main`
- `COMPOSE_GUARDRAILS_RULE_SET=default`
- `COMPOSE_GUARDRAILS_REPORT_PATH=artifacts/compose-guardrails-report.md`
- `COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY=false`
- `COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS=false`
- `COMPOSE_GUARDRAILS_WRITE_STEP_SUMMARY=true`

Behavior notes:
- workflow is report-only by default
- fail-on-findings is opt-in
- changed-files-only is opt-in and applies on pull_request events
- changed-files-only includes only changed `.kt` files inside `COMPOSE_GUARDRAILS_TARGET`
- files outside target scope are ignored
- when no matching changed Kotlin files are found, workflow writes a clean no-files report and succeeds
- when changed-files-only is enabled outside pull_request, workflow falls back to target analysis
- report artifact name: `compose-guardrails-report`
- step summary includes provider, rule set, target path, changed-files-only status, report mode, report path, and fallback status

Path handling note:
- CI executes analysis with absolute paths.
- Current CI script rejects analysis/report paths with whitespace and exits with a clear error to avoid Gradle `--args` splitting issues.

Reusable Action:
```yaml
name: Compose Guardrails
on:
  pull_request:

permissions:
  contents: read

jobs:
  compose-guardrails:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
        with:
          target: .
          rule-set: default
          provider: fake
          report-path: artifacts/compose-guardrails-report.md
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: compose-guardrails-report
          path: ${{ steps.compose-guardrails.outputs.report-path }}
```

`fake` is deterministic scaffolding for CI validation, not a real AI review.

Real provider:
```yaml
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
        with:
          target: app/src/main
          rule-set: default
          provider: openai
          model: gpt-4.1-mini
          api-key: ${{ secrets.MOBILE_AI_API_KEY }}
          report-path: artifacts/compose-guardrails-report.md
```

Changed-files-only PR mode:
```yaml
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
        with:
          target: .
          changed-files-only: true
          provider: fake
```

Fail-on-findings:
```yaml
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
        with:
          target: .
          provider: fake
          fail-on-findings: true
```

Use this Action when you want the same safe defaults in another repository. It keeps report-only behavior by default, writes GitHub Step Summary entries internally, and leaves artifact upload to the caller. It still uses the toolkit checkout and Gradle; switching the action to the packaged distribution is future work.
For forked pull requests, GitHub may withhold secrets depending on repository settings, so `fake` remains the safest default for public or untrusted PRs. Real providers still require GitHub Secrets.

The reusable Action is validated against a separate consumer workspace, and changed-files-only mode is scoped to `COMPOSE_GUARDRAILS_TARGET` inside that workspace.

Real providers in CI should be configured with secrets:
- `MOBILE_AI_PROVIDER`
- `MOBILE_AI_API_KEY`
- `MOBILE_AI_MODEL`

## Scope and Non-Goals (Current)
- Focus on analysis only.
- No code generation.
- No auto-fix behavior.
- No provider-specific coupling in core modules.

Provider limitations (current):
- No streaming.
- No retries/backoff.
- No chat history.
- No live API tests in CI; use `fake` provider for deterministic checks.
- Rule detection is heuristic/text-based (no AST parser yet).
- Advanced rules are intentionally conservative and can still be noisy.

Examples overview:
- `examples/business-logic-sample`
- `examples/state-hoisting-sample`
- `examples/side-effect-sample`
- `examples/lazy-list-sample`
- `examples/android-lifecycle-sample`
- `examples/multiplatform-commonmain-sample`
- `examples/effect-key-quality-sample`
- `examples/viewmodel-in-leaf-composable-sample`
- `examples/clickable-without-semantics-sample`
- `examples/modifier-parameter-position-sample`
- `examples/expensive-work-in-composition-sample`
- `examples/clean-compose-sample` (expected zero findings)

Recommendation:
- Validate AI findings manually before applying changes.

PR comments are not implemented in this milestone; GitHub Step Summary is used instead.
