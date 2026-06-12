package dev.mobileai.toolkit.composeguardrails

import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliOutputIntegrationTest {
    @Test
    fun `cli writes clean markdown report when output is provided`() {
        val tempRoot = Path.of("build", "tmp", "compose-guardrails-cli-test").toAbsolutePath().normalize()
        tempRoot.createDirectories()
        val tempDir = Files.createTempDirectory(tempRoot, "sample-")
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
        assertTrue(report.contains("- Analyzed path: `${relativePath(sourceFile)}`"))
        assertFalse(report.contains("> Task :"))
        assertFalse(report.contains("BUILD SUCCESSFUL"))
    }

    private fun relativePath(path: Path): String {
        val workspaceRoot = repositoryRoot()
        val normalized = path.toAbsolutePath().normalize()
        return if (normalized.startsWith(workspaceRoot)) {
            workspaceRoot.relativize(normalized).toString()
        } else {
            normalized.toString()
        }
    }

    private fun repositoryRoot(): Path {
        var current = Path.of("").toAbsolutePath().normalize()

        while (true) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current
            }

            val parent = current.parent ?: return current
            current = parent
        }
    }
}
