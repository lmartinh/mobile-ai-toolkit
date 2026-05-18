# Repository Agent Instructions

This repository hosts Kotlin-based AI-assisted mobile development tools in a monorepo.

## Scope and Goals
- Keep implementations simple, readable, and easy to extend.
- Prioritize analysis workflows over code generation for early versions.
- Preserve module boundaries across `tools/` and `shared/`.
- Treat `compose-guardrails` as the current flagship tool, but keep the repo ready for additional tools under `tools/`.

## Architecture Rules
- CLI modules only handle argument parsing, input validation, orchestration, and output formatting.
- Tool-specific core logic lives in the corresponding module under `tools/`.
- Reusable logic belongs under `shared/`.
- AI provider integrations must be behind interfaces; do not couple domain logic to provider SDKs.
- Prompt content must live in Markdown files, not hardcoded Kotlin strings.
- Keep GitHub Action and CI logic thin; prefer checked-in scripts over large inline YAML blocks.

## Coding Guidelines
- Use Kotlin/JVM and Gradle Kotlin DSL.
- Keep dependencies minimal; add only when required by an implemented feature.
- Prefer explicit, small abstractions over broad frameworks.
- Write tests for behavior-critical logic when implementation begins.
- Use `rg` / `rg --files` for search and file discovery when working in the repo.
- Prefer `apply_patch` for single-file edits.
- Do not revert user changes or touch unrelated work.
- Avoid destructive commands unless the user explicitly asks.

## Security and Configuration
- Never commit API keys, tokens, or secrets.
- Read runtime configuration from environment variables.
- Do not hardcode a single AI provider into core modules.
- Assume external CI users may run the reusable GitHub Action from another repository.
- Keep secrets out of logs, reports, and artifacts.

## Contribution Expectations
- Keep commits focused and atomic.
- Update docs when architecture, prompts, or workflows change.
- Validate examples and expected reports when changing guardrail behavior.
- When editing workflow or action code, validate both the internal workflow and the external-repository simulation path.
- Keep roadmaps current when milestone scope changes.

## Language Policy
- Use English for all commit messages.
- Use English for all code comments.
- Use English for all documentation and Markdown content.
