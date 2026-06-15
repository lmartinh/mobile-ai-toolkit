# Planned Audit Areas

This document outlines planned audit areas for `kmp-project-auditor`.
Detailed rule metadata now lives in `docs/rules.md`.

Milestone 8 adds release-readiness packaging and tag workflow validation on top of repository-local CI integration.

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

## Milestone 8 Scope Reminder

Current behavior is deterministic and heuristic-based:
- discover Gradle files
- discover and classify source-set names
- discover Kotlin source roots
- detect likely KMP/Android/iOS target declarations from Gradle text
- summarize detected capabilities and layout notes
- produce deterministic findings for:
  - Android/iOS/native imports in shared code in `commonMain`
  - JVM-only imports/usages in shared code in `commonMain`
  - missing `commonTest`
  - Android/iOS target-source-set mismatch
  - simple `expect`/`actual` mismatches
  - Android-specific Compose platform access in shared code
  - Android resource access in shared code
  - obvious Android dependency leaks in `commonMain` dependencies
  - Compose Multiplatform imports under `androidx.compose.*` remain valid in shared code in `commonMain`
- load prompt assets and compose deterministic AI review context
- run AI analysis through shared `AiClient` (fake provider by default)
- parse structured AI findings separately from deterministic findings
- render a deterministic Markdown report
- support `--output <path>` for report files
- maintain a documented rule catalog aligned with deterministic checks, AI guidance, and future rules
- run a repository-local GitHub Actions workflow with fake provider defaults
- upload Markdown report artifacts and write a Step Summary
- validate installDist/distZip/distTar packaging for the tool
- validate packaged prompt resources and packaged CLI execution with fake provider
- run a tag-triggered release workflow that uploads ZIP/TAR package artifacts

No reusable external action, no SARIF/JSON, and no Gradle AST/dependency graph parsing are implemented in Milestone 8.
