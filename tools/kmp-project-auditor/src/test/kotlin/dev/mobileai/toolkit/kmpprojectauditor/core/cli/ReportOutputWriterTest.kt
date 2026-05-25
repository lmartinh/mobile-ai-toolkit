package dev.mobileai.toolkit.kmpprojectauditor.core.cli

import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReportOutputWriterTest {
    private val writer = ReportOutputWriter()

    @Test
    fun `writes report and creates parent directories`() {
        val tempDir = createTempDirectory()
        val outputPath = tempDir.resolve("reports/kmp/audit.md")

        writer.write("# report", outputPath)

        assertTrue(outputPath.exists())
        assertEquals("# report", outputPath.readText())
    }

    @Test
    fun `overwrites existing report`() {
        val tempDir = createTempDirectory()
        val outputPath = tempDir.resolve("report.md")
        outputPath.writeText("old")

        writer.write("new", outputPath)

        assertEquals("new", outputPath.readText())
    }

    @Test
    fun `fails clearly when path is invalid`() {
        val tempDir = createTempDirectory()
        val outputPath = tempDir.resolve("invalid")
        outputPath.writeText("file")

        val error = assertFailsWith<IllegalArgumentException> {
            writer.write("content", outputPath.resolve("report.md"))
        }

        assertTrue(error.message!!.contains("Unable to write report"))
    }
}
