# mobile-ai-toolkit

Open-source Kotlin monorepo for AI-assisted mobile development tools.

## Purpose
`mobile-ai-toolkit` provides practical CLI tools that analyze mobile codebases and enforce development guardrails using AI-assisted workflows.

The first tool is:
- `compose-guardrails`: analyzes Jetpack Compose code and reports violations of predefined guardrails.

## Repository Layout
- `tools/`: individual CLI tools and tool-specific logic.
- `shared/`: reusable components shared across tools (AI client abstractions, CLI helpers, reporting utilities).
- `docs/`: architecture and roadmap documentation.

## Design Principles
- Kotlin/JVM with Gradle Kotlin DSL.
- Simple, extensible monorepo structure for future tools.
- No hardcoded AI provider or API keys.
- Prompts stored as Markdown assets, not embedded in code.

## Status
This is an initialization-only skeleton. CLI implementation is intentionally deferred.
