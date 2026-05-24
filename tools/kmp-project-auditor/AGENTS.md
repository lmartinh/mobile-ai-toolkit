# kmp-project-auditor Agent Instructions

## Scope
- Keep the tool simple and focused on deterministic KMP project scanning.
- Support Android+iOS KMP library/project shapes first.
- Build foundations for later milestones without pre-implementing them.

## Current Milestone Constraints
- Do not add AI calls until the planned milestone enables them.
- Do not add Gradle AST parsing in this phase.
- Do not add Kotlin compiler analysis in this phase.
- Do not call real external APIs in tests.

## Engineering Guidance
- Prefer explicit, testable scanner functions.
- Keep CLI responsibilities limited to argument parsing, input validation, and output.
- Keep findings evidence-based when findings are introduced in future milestones.
- Document limitations clearly in README/docs when behavior is partial.
- Avoid company-specific conventions or hardcoded organizational rules.
