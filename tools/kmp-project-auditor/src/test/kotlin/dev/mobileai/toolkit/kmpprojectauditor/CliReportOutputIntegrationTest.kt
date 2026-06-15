package dev.mobileai.toolkit.kmpprojectauditor

import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliReportOutputIntegrationTest {
    @Test
    fun `cli writes markdown report when output is provided`() {
        val tempDir = createTempDirectory()
        val outputPath = tempDir.resolve("artifacts/kmp-audit-report.md")
        outputPath.parent.createDirectories()

        main(
            arrayOf(
                "kmp",
                "audit",
                "examples/bad-kmp-library",
                "--output",
                outputPath.toString()
            )
        )

        assertTrue(outputPath.exists())
        val report = outputPath.readText()
        assertTrue(report.contains("# KMP Project Audit Report"))
        assertTrue(report.contains("## Deterministic Findings"))
        assertTrue(report.contains("## Additional AI Findings"))
        assertTrue(report.contains("## Limitations"))
        assertFalse(report.contains("> Task :"))
        assertFalse(report.contains("BUILD SUCCESSFUL"))

        val normalized = normalizeForGolden(report, "examples/bad-kmp-library")
        val expected = java.nio.file.Path.of("examples/bad-kmp-library/expected-report.md")
            .toFile()
            .readText()
            .replace("\r\n", "\n")
            .lines()
            .map { it.trimEnd() }
            .joinToString("\n")
            .trimEnd()
        assertEquals(expected, normalized)
    }

    private fun normalizeForGolden(report: String, fixturePath: String): String {
        val fixture = java.nio.file.Path.of(fixturePath).toAbsolutePath().normalize().toString().replace("\\", "/")
        return report
            .replace("\r\n", "\n")
            .lines()
            .map { line ->
                if (line.startsWith("- Analyzed path: `")) {
                    "- Analyzed path: `$fixturePath`"
                } else {
                    line.replace(fixture, fixturePath)
                }
            }
            .map { it.trimEnd() }
            .joinToString("\n")
            .trimEnd()
    }
}
