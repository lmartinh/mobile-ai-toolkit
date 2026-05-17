# compose-guardrails

Kotlin CLI tool for Compose guardrail analysis using a provider-agnostic AI workflow.

## Command
`mobile-ai guardrails check <path>`

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
