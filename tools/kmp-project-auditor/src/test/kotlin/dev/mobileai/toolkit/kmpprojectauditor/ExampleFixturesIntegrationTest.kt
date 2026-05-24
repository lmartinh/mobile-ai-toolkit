package dev.mobileai.toolkit.kmpprojectauditor

import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ScanSummaryRenderer
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ExampleFixturesIntegrationTest {
    private val scanner = ProjectScanner()
    private val renderer = ScanSummaryRenderer()

    @Test
    fun `clean fixture scan summary is deterministic`() {
        val fixturePath = Path.of("examples/clean-kmp-library")

        val result = scanner.scan(fixturePath)

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

        val summary = renderer.render(result)
        assertEquals(-1, summary.indexOf('\\'))
        val expectedSummary =
            """
            KMP Project Auditor
            Analyzed path: ${fixturePath.toAbsolutePath().normalize()}
            Gradle files:
            - build.gradle.kts
            - settings.gradle.kts
            Source sets:
            - androidMain
            - commonMain
            - commonTest
            - iosMain
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
            No audit findings are generated in Milestone 1.
            """.trimIndent()

        assertEquals(expectedSummary, summary)
    }

    @Test
    fun `bad fixture scan summary is deterministic`() {
        val fixturePath = Path.of("examples/bad-kmp-library")

        val result = scanner.scan(fixturePath)

        assertContentEquals(listOf("build.gradle.kts"), result.gradleFiles)
        assertContentEquals(listOf("commonMain"), result.sourceSets)
        assertContentEquals(listOf("src/commonMain/kotlin"), result.kotlinSourceRoots)

        val summary = renderer.render(result)
        assertEquals(-1, summary.indexOf('\\'))
        val expectedSummary =
            """
            KMP Project Auditor
            Analyzed path: ${fixturePath.toAbsolutePath().normalize()}
            Gradle files:
            - build.gradle.kts
            Source sets:
            - commonMain
            Kotlin source roots:
            - src/commonMain/kotlin
            Detected capabilities:
            - has commonMain: true
            - has commonTest: false
            - has Android source set: false
            - has iOS source set: false
            No audit findings are generated in Milestone 1.
            """.trimIndent()

        assertEquals(expectedSummary, summary)
    }
}
