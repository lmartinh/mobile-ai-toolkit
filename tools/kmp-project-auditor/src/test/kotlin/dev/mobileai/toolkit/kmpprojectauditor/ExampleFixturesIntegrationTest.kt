package dev.mobileai.toolkit.kmpprojectauditor

import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ScanSummaryRenderer
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExampleFixturesIntegrationTest {
    private val scanner = ProjectScanner()
    private val auditor = DeterministicKmpAuditor()
    private val renderer = ScanSummaryRenderer()

    @Test
    fun `clean fixture scan summary is deterministic`() {
        val fixturePath = Path.of("examples/clean-kmp-library")

        val result = scanner.scan(fixturePath)
        val findings = auditor.audit(result)

        assertContentEquals(
            listOf("build.gradle.kts", "settings.gradle.kts"),
            result.gradleFiles
        )
        assertContentEquals(
            listOf("androidMain", "commonMain", "commonTest", "iosMain"),
            result.sourceSets
        )
        assertContentEquals(
            listOf(
                "src/androidMain/kotlin",
                "src/commonMain/kotlin",
                "src/commonTest/kotlin",
                "src/iosMain/kotlin"
            ),
            result.kotlinSourceRoots
        )
        assertTrue(findings.isEmpty())

        val summary = renderer.render(result, findings)
        assertEquals(-1, summary.indexOf('\\'))
        val expectedSummary =
            """
            KMP Project Auditor
            Analyzed path: ${fixturePath.toAbsolutePath().normalize()}
            Gradle files:
            - build.gradle.kts
            - settings.gradle.kts
            Detected Gradle configuration:
            - Kotlin Multiplatform plugin: true
            - Android target: true
            - iOS target: true
            - KMP plugin detected in: build.gradle.kts
            - Android target detected in: build.gradle.kts
            - iOS target detected in: build.gradle.kts
            Source sets:
            - androidMain (android)
            - commonMain (common)
            - commonTest (common)
            - iosMain (ios)
            Kotlin source roots:
            - src/androidMain/kotlin
            - src/commonMain/kotlin
            - src/commonTest/kotlin
            - src/iosMain/kotlin
            Detected capabilities:
            - has commonMain: true
            - has commonTest: true
            - has Android source set: true
            - has iOS source set: true
            - has Android target: true
            - has iOS target: true
            - has intermediate source sets: false
            - has custom source sets: false
            Layout notes:
            - (none)
            Audit findings:
            No deterministic findings found.
            No AI findings are generated in Milestone 3.
            Markdown reports are not generated yet.
            """.trimIndent()

        assertEquals(expectedSummary, summary)
    }

    @Test
    fun `bad fixture scan summary is deterministic`() {
        val fixturePath = Path.of("examples/bad-kmp-library")

        val result = scanner.scan(fixturePath)
        val findings = auditor.audit(result)

        assertContentEquals(listOf("build.gradle.kts"), result.gradleFiles)
        assertContentEquals(listOf("commonMain"), result.sourceSets)
        assertContentEquals(listOf("src/commonMain/kotlin"), result.kotlinSourceRoots)

        val summary = renderer.render(result, findings)
        assertEquals(-1, summary.indexOf('\\'))
        val expectedSummary =
            """
            KMP Project Auditor
            Analyzed path: ${fixturePath.toAbsolutePath().normalize()}
            Gradle files:
            - build.gradle.kts
            Detected Gradle configuration:
            - Kotlin Multiplatform plugin: true
            - Android target: false
            - iOS target: true
            - KMP plugin detected in: build.gradle.kts
            - Android target detected in: (none)
            - iOS target detected in: build.gradle.kts
            Source sets:
            - commonMain (common)
            Kotlin source roots:
            - src/commonMain/kotlin
            Detected capabilities:
            - has commonMain: true
            - has commonTest: false
            - has Android source set: false
            - has iOS source set: false
            - has Android target: false
            - has iOS target: true
            - has intermediate source sets: false
            - has custom source sets: false
            Layout notes:
            - commonTest source set was not found.
            - iOS target detected but no iOS source set directory was found.
            Audit findings:
            - [WARNING] kmp.common.no-android-api - Android API import in commonMain
              File: src/commonMain/kotlin/CommonOnly.kt
              Evidence: import android.content.Context
              Explanation: commonMain should stay platform-neutral and avoid Android-specific imports.
              Suggestion: Move Android-specific code to androidMain or hide it behind expect/actual.
            - [WARNING] kmp.common.no-ios-api - iOS/Native API import in commonMain
              File: src/commonMain/kotlin/CommonOnly.kt
              Evidence: import platform.Foundation.NSString
              Explanation: commonMain should avoid iOS/native-specific APIs.
              Suggestion: Move iOS/native code to iosMain/native source sets or use expect/actual.
            - [WARNING] kmp.dependencies.common-platform-leak - Platform-specific dependency in common dependencies
              File: build.gradle.kts
              Evidence: implementation("androidx.core:core-ktx:1.13.1")
              Explanation: commonMain dependencies should avoid Android-specific artifacts.
              Suggestion: Move Android dependencies to androidMain-specific configurations.
            - [WARNING] kmp.source-sets.ios-target-without-source-set - iOS target without iOS source set
              File: <project>
              Evidence: Detected in: build.gradle.kts
              Explanation: iOS target declarations were detected, but no iOS source set directory was found.
              Suggestion: Add iosMain/iosTest source sets or remove iOS target declarations.
            - [INFO] kmp.tests.missing-common-test - Missing commonTest source set
              File: <project>
              Explanation: Shared code exists in commonMain but commonTest was not found.
              Suggestion: Add commonTest to cover shared business logic with multiplatform tests.
            No AI findings are generated in Milestone 3.
            Markdown reports are not generated yet.
            """.trimIndent()

        assertEquals(expectedSummary, summary)
    }
}
