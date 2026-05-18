# mobile-ai-toolkit

Open-source Kotlin monorepo for AI-assisted mobile development tools.

## Purpose
`mobile-ai-toolkit` provides practical CLI tools that analyze mobile codebases and enforce development guardrails using AI-assisted workflows.

The first tool is:
- `compose-guardrails`: analyzes Jetpack Compose code and reports guardrail findings.

## Current Command
- `mobile-ai guardrails check <path>`

Current implementation supports:
- Kotlin file discovery (`.kt` file or recursive directory scan).
- Text-based Compose candidate and `@Composable` detection.
- Prompt assembly from versioned Markdown assets.
- AI client abstraction with deterministic fake adapter.
- Structured finding parsing (`severity`, `rule_id`, `title`, `file_path`, `explanation`, `suggestion`, optional `code_example`).
- Stable Markdown report generation from parsed findings.

## Quick Start
Run from repository root:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check tools/compose-guardrails/examples/bad-compose-sample"
```

Rule-set examples:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set default"
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set advanced"
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set all"
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --output artifacts/compose-guardrails-report.md"
```

If `--rule-set` is omitted, the CLI uses the conservative `default` rule set.

Run explicitly with fake provider:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run with OpenAI provider:

```bash
MOBILE_AI_PROVIDER=openai \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gpt-4.1-mini \
./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test
```

## CI Integration
A baseline GitHub Actions workflow is provided at:
- `.github/workflows/compose-guardrails.yml`

Current CI behavior (report-only):
- Runs module tests.
- Runs `compose-guardrails` with `MOBILE_AI_PROVIDER=fake`.
- Uses `COMPOSE_GUARDRAILS_TARGET` and `COMPOSE_GUARDRAILS_RULE_SET` to configure scan scope and rules.
- Writes a clean Markdown report to `artifacts/compose-guardrails-report.md`.
- Uploads the report artifact (`compose-guardrails-report`).
- Findings do not fail the build yet.

For deterministic CI, keep `fake` provider.
For real-provider CI runs, use repository secrets for:
- `MOBILE_AI_PROVIDER`
- `MOBILE_AI_API_KEY`
- `MOBILE_AI_MODEL`


CI workflow knobs (environment variables):
- `COMPOSE_GUARDRAILS_TARGET` (default: `tools/compose-guardrails/src/main`)
- `COMPOSE_GUARDRAILS_RULE_SET` (default: `default`)
- `COMPOSE_GUARDRAILS_REPORT_PATH` (default: `artifacts/compose-guardrails-report.md`)
- `COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY` (default: `false`)
- `COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS` (default: `false`)
- `COMPOSE_GUARDRAILS_WRITE_STEP_SUMMARY` (default: `true`)

Behavior defaults remain non-breaking:
- fake provider
- default rule set
- report-only mode (no fail on findings)
- full configured target path analysis

Changed-files-only mode:
- Set `COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY=true` to analyze changed `.kt` files on pull requests.
- Changed-files-only mode analyzes only files inside `COMPOSE_GUARDRAILS_TARGET`.
- Files outside target scope are ignored (for example tests/examples when target is `src/main`).
- If no changed Kotlin files match the target scope, CI writes a clean no-files Markdown report and succeeds.
- On non-PR events, changed-files-only falls back to full analysis of `COMPOSE_GUARDRAILS_TARGET`.

Fail-on-findings mode:
- Set `COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS=true` to fail CI when the report contains findings.

Report artifact:
- Uploaded as `compose-guardrails-report`.

Step Summary:
- Includes provider, rule set, target path, changed-files-only status, report mode, report path, and fallback status.

Path handling note:
- CI uses absolute analysis paths to avoid Gradle module working-directory ambiguity.
- Current CI script rejects analysis/report paths containing whitespace with a clear error to avoid Gradle `--args` splitting issues.

## Repository Layout
- `tools/`: individual tools and tool-specific logic.
- `shared/`: reusable modules across tools.
- `docs/`: architecture and cross-repository documentation.
- `docs/roadmaps/`: per-tool roadmaps (`compose-guardrails` today, more tools later).

Reusable GitHub Action:
The reusable Action is designed for external repositories. It runs the tool checkout from `mobile-ai-toolkit` and analyzes the consumer repository workspace.

Safe first run:
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
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.0
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
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.0
        with:
          target: app/src/main
          rule-set: default
          provider: anthropic
          model: claude-3-5-sonnet
          api-key: ${{ secrets.MOBILE_AI_API_KEY }}
          report-path: artifacts/compose-guardrails-report.md
```

Changed-files-only PR mode:
```yaml
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.0
        with:
          target: .
          changed-files-only: true
          provider: fake
```

Fail-on-findings:
```yaml
      - id: compose-guardrails
        uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.0
        with:
          target: .
          provider: fake
          fail-on-findings: true
```

The reusable Action keeps `fake` and report-only behavior as the default. Step Summary is handled by the Action; artifact upload remains the caller’s responsibility.

## Design Principles
- Kotlin/JVM with Gradle Kotlin DSL.
- Keep architecture simple and modular.
- No hardcoded AI provider/API keys in core logic.
- Prompts are Markdown assets, never hardcoded in Kotlin.

## Runtime Configuration
Environment variables are used at runtime for provider selection:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required for real providers)
- `MOBILE_AI_MODEL` (required for real providers)

Security note:
- Never commit API keys or secrets to this repository.

GitHub Actions example:

```yaml
env:
  MOBILE_AI_PROVIDER: anthropic
  MOBILE_AI_API_KEY: ${{ secrets.MOBILE_AI_API_KEY }}
  MOBILE_AI_MODEL: ${{ secrets.MOBILE_AI_MODEL }}
```

Use `MOBILE_AI_PROVIDER=fake` for deterministic CI checks without external API calls.

Current provider-layer limitations:
- No streaming support yet.
- No retries/backoff yet.
- No chat history support.
- No live API tests in the automated test suite.

Guardrail quality note:
- AI findings should be treated as review assistance and validated manually by developers.
- Compose rule detection is heuristic/text-based (no AST parser yet).
- Default rules are intentionally conservative to reduce noisy findings.
- Advanced rules are exploratory and may produce noisier findings.

## Status
Milestones 1-11 are complete, including baseline GitHub Actions integration and a reusable GitHub Action for deterministic `compose-guardrails` checks and report artifacts.
