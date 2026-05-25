# kmp-project-auditor Agent Instructions

## Scope
- Keep the tool simple and focused on deterministic KMP project scanning.
- Support Android+iOS KMP library/project shapes first.
- Build foundations for later milestones without pre-implementing them.

## Current Milestone Constraints
- AI review pipeline is enabled in Milestone 4 through `AiClient`.
- Default provider must stay fake for safe deterministic local/CI usage.
- Do not add Gradle AST parsing in this phase.
- Do not add Kotlin compiler analysis in this phase.
- Do not call real external APIs in tests.

## Engineering Guidance
- Prefer explicit, testable scanner functions.
- Keep CLI responsibilities limited to argument parsing, input validation, and output.
- Keep findings evidence-based when findings are introduced in future milestones.
- Keep deterministic findings separate from AI findings in output.
- Ensure AI prompt assets stay in Markdown resources and JSON output parsing remains defensive.
- Document limitations clearly in README/docs when behavior is partial.
- Avoid company-specific conventions or hardcoded organizational rules.
