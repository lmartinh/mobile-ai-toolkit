# Repository Agent Instructions

This repository hosts Kotlin-based AI-assisted mobile development tools in a monorepo.

## Scope and Goals
- Keep implementations simple, readable, and easy to extend.
- Prioritize analysis workflows over code generation for early versions.
- Preserve module boundaries across `tools/` and `shared/`.

## Architecture Rules
- CLI modules only handle argument parsing, input validation, orchestration, and output formatting.
- Tool-specific core logic lives in the corresponding module under `tools/`.
- Reusable logic belongs under `shared/`.
- AI provider integrations must be behind interfaces; do not couple domain logic to provider SDKs.
- Prompt content must live in Markdown files, not hardcoded Kotlin strings.

## Coding Guidelines
- Use Kotlin/JVM and Gradle Kotlin DSL.
- Keep dependencies minimal; add only when required by an implemented feature.
- Prefer explicit, small abstractions over broad frameworks.
- Write tests for behavior-critical logic when implementation begins.

## Security and Configuration
- Never commit API keys, tokens, or secrets.
- Read configuration from environment variables or external config files.
- Do not hardcode a single AI provider into core modules.

## Contribution Expectations
- Keep commits focused and atomic.
- Update docs when architecture, prompts, or workflows change.
- Validate examples and expected reports when changing guardrail behavior.

## Language Policy
- Use English for all commit messages.
- Use English for all code comments.
- Use English for all documentation and Markdown content.
