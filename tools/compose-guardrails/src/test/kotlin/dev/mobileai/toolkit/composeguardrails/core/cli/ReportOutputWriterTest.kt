package dev.mobileai.toolkit.composeguardrails.core.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportOutputWriterTest {
    private val writer = ReportOutputWriter()

    @Test
    fun `writes report file when output path is provided`() {
        val tempDir = createTempDirectory()
        val outputPath = tempDir.resolve("nested/report.md")
        val markdown = "# Compose Guardrails Report\n\n- Total findings: 0"

        writer.write(markdown, outputPath)

        assertTrue(outputPath.exists())
        assertEquals(markdown, outputPath.readText())
    }

    @Test
    fun `prints to console when output path is omitted`() {
        val markdown = "# Compose Guardrails Report"
        val originalOut = System.out
        val output = ByteArrayOutputStream()

        try {
            System.setOut(PrintStream(output))
            writer.write(markdown, null)
        } finally {
            System.setOut(originalOut)
        }

        assertTrue(output.toString().contains(markdown))
    }
}
