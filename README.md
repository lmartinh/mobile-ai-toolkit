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

## Quick Start
Run from repository root:

```bash
./gradlew :tools:compose-guardrails:run --args="guardrails check tools/compose-guardrails/examples/bad-compose-sample"
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

## Status
Milestones 1-6 are complete (foundation, scanner, Compose detection, prompt pipeline, AI abstraction, structured parser). Next is Markdown report generation.
