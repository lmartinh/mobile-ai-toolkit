# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.

Milestone 3 adds a first deterministic rule subset on top of structure and target heuristics.

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

## Milestone 3 Scope Reminder

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

No AI calls, no Markdown audit report generation, and no Gradle AST/dependency graph parsing are implemented in Milestone 3.
