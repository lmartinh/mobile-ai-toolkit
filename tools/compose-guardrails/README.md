# compose-guardrails

Kotlin CLI tool (skeleton) for analyzing Jetpack Compose code against predefined development guardrails using AI-assisted review.

## Current Scope
- Minimal CLI command for Kotlin file discovery.
- Basic text-based Compose candidate detection.
- Prompt asset loading and deterministic prompt composition pipeline.
- Prompt assets for future AI-based guardrail checks.
- Example input and expected Markdown report.

## Command
`mobile-ai guardrails check <path>`

Behavior:
- If `<path>` is a `.kt` file, it analyzes that file.
- If `<path>` is a directory, it recursively scans for `.kt` files.
- It prints a summary with analyzed path, number of Kotlin files, and file paths.
- It also prints Compose candidate files and detected `@Composable` functions.
- It validates and loads prompt Markdown assets from `prompts/`.
- It composes a deterministic review prompt bundle and sends it to an AI client abstraction.
- By default it uses a deterministic fake client (`MOBILE_AI_CLIENT=fake`).

Local development run:
- `./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"`

## Planned Scope (v1)
- Run guardrail checks using prompt-driven analysis.
- Emit structured Markdown findings.

## Non-Goals (v1)
- Code generation.
- Auto-fixing source files.
- Provider-specific assumptions in core logic.
