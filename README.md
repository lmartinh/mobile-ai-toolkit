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

## Repository Layout
- `tools/`: individual tools and tool-specific logic.
- `shared/`: reusable modules across tools.
- `docs/`: architecture and roadmap.

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

## Status
Milestones 1-8.5 are complete (including OpenAI, Anthropic, and Gemini adapters behind `AiClient`). Next is guardrail quality expansion.
