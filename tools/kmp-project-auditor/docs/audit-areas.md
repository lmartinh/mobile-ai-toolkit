# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.
Detailed rule metadata now lives in `docs/rules.md`.

Milestone 7 adds repository-local CI integration on top of Markdown reports and fake AI defaults.

## Planned Areas (High Level)

- Project structure
- Required mobile source sets
- Intermediate source-set clarity
- Android APIs in commonMain
- iOS/Darwin/platform APIs in commonMain
- `expect`/`actual` usage
- Dependency placement
- Compose Multiplatform resources
- Publishing metadata
- Public API cleanliness
- Test source-set coverage
- Consumer setup documentation

## Milestone 7 Scope Reminder

Current behavior is deterministic and heuristic-based:
- discover Gradle files
- discover and classify source-set names
- discover Kotlin source roots
- detect likely KMP/Android/iOS target declarations from Gradle text
- summarize detected capabilities and layout notes
- produce deterministic findings for:
  - Android/iOS/native imports in `commonMain`
  - missing `commonTest`
  - Android/iOS target-source-set mismatch
  - obvious Android dependency leaks in `commonMain` dependencies
- load prompt assets and compose deterministic AI review context
- run AI analysis through shared `AiClient` (fake provider by default)
- parse structured AI findings separately from deterministic findings
- render a deterministic Markdown report
- support `--output <path>` for report files
- maintain a documented rule catalog aligned with deterministic checks, AI guidance, and future rules
- run a repository-local GitHub Actions workflow with fake provider defaults
- upload Markdown report artifacts and write a Step Summary

No reusable external action, no release packaging, no SARIF/JSON, and no Gradle AST/dependency graph parsing are implemented in Milestone 7.
