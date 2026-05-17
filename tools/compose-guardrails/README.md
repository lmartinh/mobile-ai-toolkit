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
- Loads prompts from `prompts/` Markdown assets.
- Builds a deterministic prompt bundle.
- Sends prompt to `AiClient` abstraction (`fake` client by default).
- Parses AI response into structured findings.
- Renders a stable Markdown guardrails report.

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

Optional provider selector:

```bash
MOBILE_AI_CLIENT=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"
```

## Scope and Non-Goals (Current)
- Focus on analysis only.
- No code generation.
- No auto-fix behavior.
- No provider-specific coupling in core modules.
