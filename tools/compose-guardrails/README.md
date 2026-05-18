# compose-guardrails

Kotlin CLI tool for Compose guardrail analysis using a provider-agnostic AI workflow.

## Command
`mobile-ai guardrails check <path>`

Optional rule-set flag:
- `--rule-set default`
- `--rule-set advanced`
- `--rule-set all`

If omitted, `default` is used.

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
./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run with explicit default rules:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set default"
```

Run with advanced rules only:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set advanced"
```

Run with all rules:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check <path> --rule-set all"
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
MOBILE_AI_PROVIDER=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run with OpenAI provider:

```bash
MOBILE_AI_PROVIDER=openai \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gpt-4.1-mini \
./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run with Anthropic provider:

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

Run with Gemini provider:

```bash
MOBILE_AI_PROVIDER=gemini \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gemini-1.5-pro \
./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
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
