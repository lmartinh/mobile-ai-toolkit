# mobile-ai-toolkit

Open-source Kotlin monorepo for AI-assisted mobile development tools.

## Purpose
`mobile-ai-toolkit` provides practical CLI tools that analyze mobile codebases and enforce development guardrails using AI-assisted workflows.

The first tool is:
- `compose-guardrails`: analyzes Jetpack Compose code and reports violations of predefined guardrails.

## Current Command
- `mobile-ai guardrails check <path>` (implemented in `tools/compose-guardrails`): scans a `.kt` file or directory and prints discovered Kotlin files.

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
Initial monorepo setup is complete, with a minimal `compose-guardrails` CLI that scans Kotlin files, detects Compose candidates, and runs through a fake AI client abstraction.
