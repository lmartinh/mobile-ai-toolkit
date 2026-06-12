# Compose Guardrails

> **Read in another language:** **English** · [Español](README.es.md)

AI-assisted guardrails for Jetpack Compose and Compose Multiplatform code.

`compose-guardrails` is an AI-assisted CLI for reviewing Jetpack Compose and Compose Multiplatform code against practical mobile architecture guardrails.

It combines deterministic source scanning, versioned Markdown prompt assets, provider-agnostic AI analysis, structured finding parsing, and Markdown report rendering. AI is used as a review aid, not a replacement for human code review.

The tool helps teams catch architecture, state, side-effect, accessibility, maintainability, and Kotlin Multiplatform boundary risks early. It is designed for practical CI usage with safe defaults and stable Markdown reporting.

## Prompt System

Prompt formation is documented in [docs/prompt-system.md](docs/prompt-system.md).
That page explains where prompt assets live, how the final prompt is composed, how rule catalogs are selected, how the JSON output contract works, and how to modify prompts safely.
The shared repository pattern is summarized in [docs/architecture.md](../../docs/architecture.md#shared-prompt-pipeline).

## What it checks

- State hoisting and unidirectional data flow.
- Side effects and effect keys.
- Recomposition and stability risks.
- Large or hard-to-review composables.
- Accessibility and semantics.
- Lazy list keys.
- Modifier usage.
- ViewModel usage in leaf composables.
- Android API leaks in common code.
- Compose Multiplatform resource usage.

## When to use it

- Before opening a PR.
- In CI as a Markdown report artifact.
- During Compose refactors.
- When reviewing shared Compose Multiplatform UI.
- As a lightweight architecture review aid.

If you are new to this repository, start with the root [README](../../README.md) and [architecture guide](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/compose-guardrails.md](../../docs/roadmaps/compose-guardrails.md)

## Command

Installed launcher:

```bash
compose-guardrails guardrails check <path>
```

Gradle during development:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set default --output artifacts/compose-guardrails-report.md"
```

Flags:
- `--rule-set default|advanced|all` (default: `default`)
- `--output <path>` (write Markdown report to file)

## Quick Start

Run from repository root with absolute paths:

```bash
MOBILE_AI_PROVIDER=fake \
  ./gradlew :tools:compose-guardrails:run \
  --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

Run tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test
```

## Runtime Configuration

Environment variables:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required only for real providers)
- `MOBILE_AI_MODEL` (required for real providers in the current implementation)

| Provider | Model |
| --- | --- |
| `openai` | `gpt-4.1-mini` |
| `anthropic` | `claude-3-5-sonnet` |
| `gemini` | `gemini-1.5-pro` |

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

For provider-specific setup and key permissions, see [Provider configuration](../../docs/provider-configuration.md).

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
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.4
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

Common options:
- `changed-files-only: true`
- `fail-on-findings: true`

## Example report

Reports are written in Markdown so they can be reviewed locally or uploaded as CI artifacts.

The report includes:
- analyzed path
- summary
- parser warnings when present
- findings
- severity
- rule id
- file path
- explanation
- suggestion
- optional code example

Example:
- [tools/compose-guardrails/examples/bad-compose-sample/expected-report.md](tools/compose-guardrails/examples/bad-compose-sample/expected-report.md)

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
- `compose.no-business-logic-in-composables`: Detects business/domain logic leaking into UI composables.
- `compose.state-hoisting`: Checks that state is hoisted to callers when local ownership is not required.
- `compose.viewmodel-in-leaf-composable`: Flags ViewModel usage directly inside leaf UI components.
- `compose.unidirectional-data-flow`: Validates one-way data flow and event-up patterns.
- `compose.no-side-effects-in-composition`: Detects side effects executed directly during composition.
- `compose.effect-key-quality`: Checks whether effect keys are stable and semantically correct.
- `compose.lazy-list-keys`: Ensures `Lazy*` lists use stable keys to avoid recomposition glitches.
- `compose.missing-modifier-parameter`: Flags composables that should expose a `Modifier` but do not.
- `compose.modifier-parameter-position`: Enforces `Modifier` as an early parameter (typically first optional UI param).
- `compose.missing-content-description`: Detects missing accessibility descriptions for meaningful visual elements.
- `compose.clickable-without-semantics`: Flags clickable UI that lacks semantic/accessibility context.
- `compose.android.collect-as-state-with-lifecycle`: Recommends lifecycle-aware state collection on Android.
- `compose.android.context-leak-risk`: Detects potential long-lived references to Android `Context`.
- `compose.multiplatform.no-android-api-in-common`: Flags Android API usage inside `commonMain`.
- `compose.multiplatform.platform-specific-ui-leak`: Detects platform-specific UI details leaking into shared UI APIs.
- `compose.multiplatform.public-api-cleanliness`: Checks that shared public APIs remain platform-neutral and stable.

`advanced` (opt-in, noisier):
- `compose.expensive-work-in-composition`: Detects heavy calculations or allocations inside composition paths.
- `compose.unstable-parameters`: Flags parameters likely to be unstable and cause extra recompositions.
- `compose.derived-state-usage`: Suggests `derivedStateOf` when computed state is repeatedly recalculated.
- `compose.large-composable`: Detects oversized composables that are harder to test and maintain.
- `compose.hardcoded-dimensions-and-colors`: Flags hardcoded UI constants that should come from theme/design tokens.
- `compose.missing-preview`: Detects composables lacking preview coverage for fast visual feedback.
- `compose.preview-with-real-dependencies`: Flags previews wired to real dependencies instead of preview-safe fakes.
- `compose.multiplatform.resources-usage`: Reviews shared resource-access patterns in multiplatform UI code.

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
  - Set `MOBILE_AI_API_KEY` for real providers.
  - `MOBILE_AI_MODEL` is required by the current implementation for real providers.
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
