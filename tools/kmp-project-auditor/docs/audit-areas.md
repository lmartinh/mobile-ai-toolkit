# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.

Milestone 4 adds a prompt pipeline and fake AI review on top of deterministic findings.

## Planned Areas

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

## Milestone 4 Scope Reminder

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

No Markdown audit report generation, no CI integration, and no Gradle AST/dependency graph parsing are implemented in Milestone 4.
