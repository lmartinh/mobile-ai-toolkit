# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.

Milestone 1 only scans structure and reports discovered project shape. It does not enforce the rules below yet.

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

## Milestone 1 Scope Reminder

Current behavior is scanner-only and deterministic:
- discover Gradle files
- discover source-set names
- discover Kotlin source roots
- summarize detected capabilities
