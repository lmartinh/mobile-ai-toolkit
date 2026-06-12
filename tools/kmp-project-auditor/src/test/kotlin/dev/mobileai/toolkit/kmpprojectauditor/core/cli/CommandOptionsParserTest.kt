package dev.mobileai.toolkit.kmpprojectauditor.core.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandOptionsParserTest {
    private val parser = CommandOptionsParser()

    @Test
    fun `parses kmp audit command`() {
        val options = parser.parse(arrayOf("kmp", "audit", "/tmp/project"))

        assertEquals("/tmp/project", options.projectPath.toString())
        assertEquals(null, options.outputPath)
    }

    @Test
    fun `parses kmp audit command with output`() {
        val options = parser.parse(arrayOf("kmp", "audit", "/tmp/project", "--output", "/tmp/report.md"))

        assertEquals("/tmp/project", options.projectPath.toString())
        assertEquals("/tmp/report.md", options.outputPath.toString())
    }

    @Test
    fun `parses external repository target path verbatim`() {
        val options = parser.parse(
            arrayOf(
                "kmp",
                "audit",
                "/home/runner/work/qr-guardian/qr-guardian"
            )
        )

        assertEquals("/home/runner/work/qr-guardian/qr-guardian", options.projectPath.toString())
        assertEquals(null, options.outputPath)
    }

    @Test
    fun `rejects invalid command`() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("guardrails", "check", "/tmp/project"))
        }
    }

    @Test
    fun `rejects invalid argument count`() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("kmp", "audit"))
        }
    }

    @Test
    fun `rejects missing output value`() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("kmp", "audit", "/tmp/project", "--output"))
        }
    }

    @Test
    fun `rejects unsupported extra args`() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(arrayOf("kmp", "audit", "/tmp/project", "--unknown"))
        }
    }
}
