# compose-guardrails Agent Instructions

This module contains the first tool in the monorepo: Compose guardrail analysis.

## Tool Intent
- Analyze Jetpack Compose source code for guardrail violations.
- Produce deterministic, structured reports.
- Focus on analysis only; do not implement code generation workflows.

## Module Boundaries
- Keep CLI-facing concerns outside core analysis logic.
- Keep guardrail logic and prompt orchestration inside this module.
- Reuse shared contracts/utilities from `shared/` when appropriate.

## Prompt and Rule Management
- Load prompts from `prompts/` Markdown files.
- Keep rule prompts granular under `prompts/rules/`.
- Update examples and expected reports when rule behavior changes.

## AI Integration Rules
- Depend on shared abstractions for AI interactions.
- Do not hardcode provider endpoints, models, or API keys.
- Keep provider-specific implementations out of this module unless explicitly scoped.

## Output Expectations
- Prefer stable Markdown/text report output for v1.
- Include file references, rule IDs, severity, and remediation guidance.

## Language Policy
- Use English for all commit messages.
- Use English for all code comments.
- Use English for all documentation and Markdown content.
