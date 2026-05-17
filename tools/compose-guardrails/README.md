# compose-guardrails

Kotlin CLI tool (skeleton) for analyzing Jetpack Compose code against predefined development guardrails using AI-assisted review.

## Current Scope
- Minimal CLI command for Kotlin file discovery.
- Basic text-based Compose candidate detection.
- Prompt assets for future AI-based guardrail checks.
- Example input and expected Markdown report.

## Command
`mobile-ai guardrails check <path>`

Behavior:
- If `<path>` is a `.kt` file, it analyzes that file.
- If `<path>` is a directory, it recursively scans for `.kt` files.
- It prints a summary with analyzed path, number of Kotlin files, and file paths.
- It also prints Compose candidate files and detected `@Composable` functions.

Local development run:
- `./gradlew :tools:compose-guardrails:run --args="guardrails check <path>"`

## Planned Scope (v1)
- Run guardrail checks using prompt-driven analysis.
- Emit structured Markdown findings.

## Non-Goals (v1)
- Code generation.
- Auto-fixing source files.
- Provider-specific assumptions in core logic.
