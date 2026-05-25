# KMP Project Audit Report
## Summary
- Analyzed path: `examples/clean-kmp-library`
- Gradle files: 2
- Source sets: 4
- Kotlin source roots: 4
- Deterministic findings: 0
- AI findings: 1
- Provider: fake

## Detected Gradle Configuration
- Kotlin Multiplatform plugin: true
- Android target: true
- iOS target: true

## Source Sets
| Source set | Category |
| --- | --- |
| androidMain | android |
| commonMain | common |
| commonTest | common |
| iosMain | ios |

## Kotlin Source Roots
- `src/androidMain/kotlin`
- `src/commonMain/kotlin`
- `src/commonTest/kotlin`
- `src/iosMain/kotlin`

## Deterministic Findings
No deterministic findings found.

## AI Findings
### INFO - kmp.ai.source-set-clarity
**Title:** Review intermediate source-set intent  
**File:** `<project>`  
**Evidence:** `Scan summary lists intermediate source sets.`
Intermediate source sets should have clear ownership and purpose.

**Suggestion:** Document why each intermediate source set exists and which targets consume it.

## AI Warnings
No AI warnings.

## Limitations
- Detection is heuristic and text-based.
- AI findings should be manually reviewed.
- No Gradle AST or dependency graph resolution is performed.
