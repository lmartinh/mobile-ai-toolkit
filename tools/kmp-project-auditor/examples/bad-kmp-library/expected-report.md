# KMP Project Audit Report
## Summary
- Analyzed path: `examples/bad-kmp-library`
- Gradle files: 1
- Source sets: 1
- Kotlin source roots: 1
- Deterministic findings: 5
- AI findings: 1
- AI warnings: 0
- Provider: fake

## Detected Gradle Configuration
- Kotlin Multiplatform plugin: true
- Android target: false
- iOS target: true

## Source Sets
| Source set | Category |
| --- | --- |
| commonMain | common |

## Kotlin Source Roots
- `src/commonMain/kotlin`

## Deterministic Findings
### WARNING - kmp.common.no-android-api
**Title:** Android API import in commonMain  
**File:** `src/commonMain/kotlin/CommonOnly.kt`  
**Evidence:** `import android.content.Context`
commonMain should stay platform-neutral and avoid Android-specific imports.

**Suggestion:** Move Android-specific code to androidMain or hide it behind expect/actual.

### WARNING - kmp.common.no-ios-api
**Title:** iOS/Native API import in commonMain  
**File:** `src/commonMain/kotlin/CommonOnly.kt`  
**Evidence:** `import platform.Foundation.NSString`
commonMain should avoid iOS/native-specific APIs.

**Suggestion:** Move iOS/native code to iosMain/native source sets or use expect/actual.

### WARNING - kmp.dependencies.common-platform-leak
**Title:** Platform-specific dependency in common dependencies  
**File:** `build.gradle.kts`  
**Evidence:** `implementation("androidx.core:core-ktx:1.13.1")`
commonMain dependencies should avoid Android-specific artifacts.

**Suggestion:** Move Android dependencies to androidMain-specific configurations.

### WARNING - kmp.source-sets.ios-target-without-source-set
**Title:** iOS target without iOS source set  
**File:** `<project>`  
**Evidence:** `Detected in: build.gradle.kts`
iOS target declarations were detected, but no iOS source set directory was found.

**Suggestion:** Add iosMain/iosTest source sets or remove iOS target declarations.

### INFO - kmp.tests.missing-common-test
**Title:** Missing commonTest source set  
**File:** `<project>`  
Shared code exists in commonMain but commonTest was not found.

**Suggestion:** Add commonTest to cover shared business logic with multiplatform tests.

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
