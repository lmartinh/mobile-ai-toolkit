# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.
Detailed rule metadata now lives in `docs/rules.md`.

Milestone 5 adds Markdown audit report generation on top of deterministic findings and fake AI review.

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

## Milestone 6 Scope Reminder

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

No CI integration, no fail-on-findings mode, and no Gradle AST/dependency graph parsing are implemented in Milestone 6.
