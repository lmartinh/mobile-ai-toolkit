# compose-guardrails Agent Instructions

This module contains the first tool in the monorepo: Compose guardrail analysis.

## Tool Intent
- Analyze Jetpack Compose source code for guardrail violations.
- Produce deterministic, structured reports.
- Focus on analysis only; do not implement code generation workflows.
- Keep the MVP conservative and easy to reason about.

## Module Boundaries
- Keep CLI-facing concerns outside core analysis logic.
- Keep guardrail logic and prompt orchestration inside this module.
- Reuse shared contracts/utilities from `shared/` when appropriate.
- Keep provider-specific code behind shared interfaces.
- If a change touches the reusable GitHub Action, remember it must work from external repositories.

## Prompt and Rule Management
- Load prompts from Markdown assets under `src/main/resources/prompts/`.
- Keep rule prompts granular under `src/main/resources/prompts/rules/`.
- Update examples and expected reports when rule behavior changes.
- Keep default rules conservative and advanced rules opt-in.
- Keep example directories and expected reports aligned with the current rule catalog.

## AI Integration Rules
- Depend on shared abstractions for AI interactions.
- Do not hardcode provider endpoints, models, or API keys.
- Keep provider-specific implementations out of this module unless explicitly scoped.
- Default CI behavior should remain `fake` provider and report-only unless explicitly overridden.

## Output Expectations
- Prefer stable Markdown/text report output for v1.
- Include file references, rule IDs, severity, and remediation guidance.
- Keep report formatting compatible with existing example fixtures and workflow summaries.

## Validation Expectations
- Update or add tests when changing scanning, prompts, rules, or report formatting.
- If changed-files logic or the reusable GitHub Action changes, validate the external-workspace path as well as the in-repo workflow.
- Keep shell scripts executable and avoid brittle path assumptions.

## Language Policy
- Use English for all commit messages.
- Use English for all code comments.
- Use English for all documentation and Markdown content.
