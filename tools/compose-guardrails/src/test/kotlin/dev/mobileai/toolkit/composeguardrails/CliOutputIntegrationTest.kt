package dev.mobileai.toolkit.composeguardrails

import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliOutputIntegrationTest {
    @Test
    fun `cli writes clean markdown report when output is provided`() {
        val tempDir = createTempDirectory()
        val sourceFile = tempDir.resolve("SampleScreen.kt")
        sourceFile.writeText(
            """
            import androidx.compose.runtime.Composable

            @Composable
            fun SampleScreen() {
                println("sample")
            }
            """.trimIndent()
        )

        val outputDir = tempDir.resolve("reports")
        outputDir.createDirectories()
        val outputFile = outputDir.resolve("compose-guardrails-report.md")

        main(
            arrayOf(
                "guardrails",
                "check",
                sourceFile.toString(),
                "--rule-set",
                "default",
                "--output",
                outputFile.toString()
            )
        )

        assertTrue(outputFile.exists())

        val report = outputFile.readText()
        assertTrue(report.contains("# Compose Guardrails Report"))
        assertTrue(report.contains("## Summary"))
        assertFalse(report.contains("> Task :"))
        assertFalse(report.contains("BUILD SUCCESSFUL"))
    }
}
