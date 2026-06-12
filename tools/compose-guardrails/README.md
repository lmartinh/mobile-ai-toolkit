# Compose Guardrails

> **Read in another language:** **English** · [Español](README.es.md)

`compose-guardrails` is a Kotlin CLI that analyzes Jetpack Compose code with provider-agnostic AI-assisted guardrails.

It helps teams catch architecture, state-management, side-effect, accessibility, and multiplatform boundary issues early, before they become expensive review or production problems. The tool is designed for practical CI usage: deterministic input scanning, structured findings, and stable Markdown reporting.

## Why teams use it

- Faster review loops for Compose-heavy codebases.
- More consistent guardrail enforcement across contributors.
- CI-friendly reports that can be shared as artifacts.

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
- `MOBILE_AI_API_KEY` (required only for real providers)
- `MOBILE_AI_MODEL` (fixed by provider)

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
  - Set `MOBILE_AI_API_KEY`; the model is fixed by provider.
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
