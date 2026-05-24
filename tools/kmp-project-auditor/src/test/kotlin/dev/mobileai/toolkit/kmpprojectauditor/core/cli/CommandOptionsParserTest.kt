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
}
