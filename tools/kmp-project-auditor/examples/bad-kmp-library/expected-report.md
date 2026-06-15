# KMP Project Audit Report
## Summary
- Analyzed path: `examples/bad-kmp-library`
- Gradle files: 1
- Source sets: 3
- Kotlin source roots: 3
- Total findings: 26
- Deterministic findings: 26
- Additional AI findings: 0
- AI warnings: 0
- Provider: fake

## Detected Gradle Configuration
- KMP project shape: detected
- Kotlin Multiplatform plugin: detected
- Android target: inferred from source sets
- iOS target: inferred from source sets

## Source Sets
| Source set | Category |
| --- | --- |
| androidMain | android |
| commonMain | common |
| iosMain | ios |

## Kotlin Source Roots
- `src/androidMain/kotlin`
- `src/commonMain/kotlin`
- `src/iosMain/kotlin`

## Deterministic Findings
### WARNING - kmp.common.jvm-only-api
**Title:** JVM-only API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 10
**Evidence:** `import java.time.LocalDate`
commonMain should avoid JVM-only APIs when the module targets iOS/Native.

**Suggestion:** Move the implementation behind expect/actual, inject an abstraction, or use a multiplatform library.

### WARNING - kmp.common.jvm-only-api
**Title:** JVM-only API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 11
**Evidence:** `import javax.crypto.Cipher`
commonMain should avoid JVM-only APIs when the module targets iOS/Native.

**Suggestion:** Move the implementation behind expect/actual, inject an abstraction, or use a multiplatform library.

### WARNING - kmp.common.jvm-only-api
**Title:** JVM-only API usage in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 20
**Evidence:** `java.util.`
commonMain should avoid JVM-only APIs when the module targets iOS/Native.

**Suggestion:** Move the implementation behind expect/actual, inject an abstraction, or use a multiplatform library.

### WARNING - kmp.common.no-android-api
**Title:** Android API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Evidence:** `import android.content.Context`
commonMain should stay platform-neutral and avoid Android-specific imports.

**Suggestion:** Move Android-specific code to androidMain or hide it behind expect/actual.

### WARNING - kmp.common.no-android-api
**Title:** Android API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Evidence:** `import android.content.res.Resources`
commonMain should stay platform-neutral and avoid Android-specific imports.

**Suggestion:** Move Android-specific code to androidMain or hide it behind expect/actual.

### WARNING - kmp.common.no-android-api
**Title:** Android API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Evidence:** `import androidx.activity.compose.BackHandler`
commonMain should stay platform-neutral and avoid Android-specific imports.

**Suggestion:** Move Android-specific code to androidMain or hide it behind expect/actual.

### WARNING - kmp.common.no-ios-api
**Title:** iOS/Native API import in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Evidence:** `import platform.Foundation.NSString`
commonMain should avoid iOS/native-specific APIs.

**Suggestion:** Move iOS/native code to iosMain/native source sets or use expect/actual.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 5
**Evidence:** `import androidx.activity.compose.BackHandler`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 6
**Evidence:** `import androidx.compose.ui.platform.LocalContext`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 7
**Evidence:** `import androidx.compose.ui.platform.LocalLifecycleOwner`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 8
**Evidence:** `import androidx.compose.ui.platform.LocalView`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 23
**Evidence:** `LocalContext.current`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 24
**Evidence:** `LocalView.current`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.common-localcontext-usage
**Title:** Android-specific Compose platform access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 25
**Evidence:** `LocalLifecycleOwner.current`
commonMain contains Android-specific Compose/platform usage that should not be relied on as shared code.

**Suggestion:** Move platform behavior behind expect/actual, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### WARNING - kmp.compose.resources.android-res-in-common
**Title:** Android resource access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 4
**Evidence:** `android.content.res.Resources`
Android resource access was detected in common code.

**Suggestion:** Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.

### WARNING - kmp.compose.resources.android-res-in-common
**Title:** Android resource access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 27
**Evidence:** `.resources.getString(`
Android resource access was detected in common code.

**Suggestion:** Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.

### WARNING - kmp.compose.resources.android-res-in-common
**Title:** Android resource access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 28
**Evidence:** `.resources.getColor(`
Android resource access was detected in common code.

**Suggestion:** Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.

### WARNING - kmp.compose.resources.android-res-in-common
**Title:** Android resource access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 29
**Evidence:** `.resources.getDrawable(`
Android resource access was detected in common code.

**Suggestion:** Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.

### WARNING - kmp.compose.resources.android-res-in-common
**Title:** Android resource access in commonMain
**File:** `src/commonMain/kotlin/CommonPlatformLeaks.kt`
**Line:** 30
**Evidence:** `R.string.`
Android resource access was detected in common code.

**Suggestion:** Use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind expect/actual.

### WARNING - kmp.dependencies.common-platform-leak
**Title:** Platform-specific dependency in common dependencies
**File:** `build.gradle.kts`
**Evidence:** `implementation("androidx.core:core-ktx:1.17.0")`
commonMain dependencies should avoid Android-specific artifacts.

**Suggestion:** Move Android dependencies to androidMain-specific configurations.

### WARNING - kmp.expect-actual.missing-actual
**Title:** Expect declaration without matching actual
**File:** `src/commonMain/kotlin/PlatformContracts.kt`
**Line:** 4
**Evidence:** `expect fun platformName`
An expect fun named platformName was found in commonMain, but no matching actual declaration was detected in platform source sets.

**Suggestion:** Add a matching actual declaration in the relevant platform source set or remove the expect declaration if it is not needed.

### WARNING - kmp.expect-actual.orphan-actual
**Title:** Actual declaration without matching expect
**File:** `src/iosMain/kotlin/IosOrphanActual.kt`
**Line:** 3
**Evidence:** `actual class OrphanPlatformInfo`
An actual class named OrphanPlatformInfo was found in a platform source set, but no matching expect declaration was detected in commonMain.

**Suggestion:** Add a matching expect declaration in commonMain or remove the actual declaration if it is not needed.

### WARNING - kmp.expect-actual.orphan-actual
**Title:** Actual declaration without matching expect
**File:** `src/iosMain/kotlin/IosOrphanActual.kt`
**Line:** 4
**Evidence:** `actual fun orphanPlatformName`
An actual fun named orphanPlatformName was found in a platform source set, but no matching expect declaration was detected in commonMain.

**Suggestion:** Add a matching expect declaration in commonMain or remove the actual declaration if it is not needed.

### WARNING - kmp.source-sets.android-source-set-without-target
**Title:** Android source set without Android target
**File:** `<project>`
Android source sets were found, but no Android target declaration was detected.

**Suggestion:** Declare an Android target or remove Android-specific source sets.

### WARNING - kmp.source-sets.ios-source-set-without-target
**Title:** iOS source set without iOS target
**File:** `<project>`
iOS source sets were found, but no iOS target declaration was detected.

**Suggestion:** Declare iOS targets or remove iOS-specific source sets.

### INFO - kmp.tests.missing-common-test
**Title:** Missing commonTest source set
**File:** `<project>`
Shared code exists in commonMain but commonTest was not found.

**Suggestion:** Add commonTest to cover shared business logic with multiplatform tests.

## Additional AI Findings
No additional AI findings found.

## AI Warnings
No AI warnings.

## Limitations
- Detection is heuristic and text-based.
- AI findings should be manually reviewed.
- No Gradle AST or dependency graph resolution is performed.