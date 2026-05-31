# compose-guardrails

`compose-guardrails` is a Kotlin CLI that analyzes Jetpack Compose code with provider-agnostic AI-assisted guardrails.

If you are new to this repository, start with the root [README](../../README.md) and [architecture guide](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/compose-guardrails.md](../../docs/roadmaps/compose-guardrails.md)

## Command

`mobile-ai guardrails check <path>`

Flags:
- `--rule-set default|advanced|all` (default: `default`)
- `--output <path>` (write Markdown report to file)

## Quick Start

Run from repository root with absolute paths:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

Run tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test
```

## Runtime Configuration

Environment variables:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required for real providers)
- `MOBILE_AI_MODEL` (required for real providers)

Provider examples:

```bash
MOBILE_AI_PROVIDER=openai \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gpt-4.1-mini \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

```bash
MOBILE_AI_PROVIDER=gemini \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gemini-1.5-pro \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Security:
- Never commit API keys.
- Use CI secrets for real providers.

## Integration in CI

Workflow:
- [.github/workflows/compose-guardrails.yml](../../.github/workflows/compose-guardrails.yml)

Current default behavior is report-first:
- Runs tests.
- Runs analysis.
- Uploads report artifact.
- Does not fail on findings unless enabled.

Reusable Action:
- [.github/actions/compose-guardrails/action.yml](../../.github/actions/compose-guardrails/action.yml)

Minimal external usage:

```yaml
- id: compose-guardrails
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

Common options:
- `changed-files-only: true`
- `fail-on-findings: true`

## Output Contract

Report format is Markdown-first.

Stable top-level sections:
- `# Compose Guardrails Report`
- `## Summary`
- `## Parser Warnings` (optional)
- `## Findings`

Structured finding fields:
- `severity` (`error`, `warning`, `info`)
- `rule_id`
- `title`
- `file_path`
- `explanation`
- `suggestion`
- `code_example` (optional)

Automation note:
- Heading names above are intended to remain stable.
- Free-text content inside findings can vary by provider/model.

## Rule Sets

`default` (recommended for routine CI):
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
- `compose.android.collect-as-state-with-lifecycle`
- `compose.android.context-leak-risk`
- `compose.multiplatform.no-android-api-in-common`
- `compose.multiplatform.platform-specific-ui-leak`
- `compose.multiplatform.public-api-cleanliness`

`advanced` (opt-in, noisier):
- `compose.expensive-work-in-composition`
- `compose.unstable-parameters`
- `compose.derived-state-usage`
- `compose.large-composable`
- `compose.hardcoded-dimensions-and-colors`
- `compose.missing-preview`
- `compose.preview-with-real-dependencies`
- `compose.multiplatform.resources-usage`

Severity guidance:
- `error`: high-confidence correctness/architecture risk.
- `warning`: important maintainability/design issue.
- `info`: lower-risk recommendation.

## Release Packaging

Build installable distribution:

```bash
./gradlew :tools:compose-guardrails:installDist
```

Build archives:

```bash
./gradlew :tools:compose-guardrails:distZip :tools:compose-guardrails:distTar
```

Run packaged launcher:

```bash
MOBILE_AI_PROVIDER=fake \
./tools/compose-guardrails/build/install/compose-guardrails/bin/compose-guardrails guardrails check tools/compose-guardrails/examples/bad-compose-sample
```

## Troubleshooting

- `Invalid rule-set value`:
  - Use only `default`, `advanced`, or `all`.
- `Missing API key/model` with real provider:
  - Set both `MOBILE_AI_API_KEY` and `MOBILE_AI_MODEL`.
- Paths with spaces in CI:
  - Current CI scripts reject them to avoid Gradle `--args` splitting issues.
- Empty or malformed AI output:
  - The parser reports warnings; review `## Parser Warnings` and re-run with `fake` for deterministic validation.

## Limitations

- Detection is heuristic/text-based (no AST parser yet).
- AI findings can include false positives/false negatives.
- PR comment posting is not implemented.
- SARIF/JSON output is not fully implemented.
- Provider layer currently has no streaming/retries/history support.

## Scope (Current)

- Analysis only.
- No code generation.
- No autofix behavior.
