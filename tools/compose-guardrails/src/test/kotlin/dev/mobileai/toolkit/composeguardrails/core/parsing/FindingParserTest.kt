package dev.mobileai.toolkit.composeguardrails.core.parsing

import dev.mobileai.toolkit.report.FindingSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FindingParserTest {
    private val parser = FindingParser()

    @Test
    fun `parse accepts exact empty findings payload`() {
        val parsed = parser.parse("""{"findings": []}""")

        assertTrue(parsed.findings.isEmpty())
        assertTrue(parsed.warnings.isEmpty())
    }

    @Test
    fun `parse treats json object without findings as malformed`() {
        val parsed = parser.parse("""{"message":"could not infer findings"}""")

        assertTrue(parsed.findings.isEmpty())
        assertTrue(parsed.warnings.any { it.contains("Invalid JSON AI response") })
    }

    @Test
    fun `parse supports escaped quotes in strings`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "warning",
                  "rule_id": "compose.quote-check",
                  "title": "Escaped \"quote\" title",
                  "file_path": "LoginScreen.kt",
                  "explanation": "Use of escaped \"quotes\" should parse.",
                  "suggestion": "Keep JSON valid."
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals("Escaped \"quote\" title", parsed.findings.single().title)
    }

    @Test
    fun `parse supports braces and brackets inside code example`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "error",
                  "rule_id": "compose.code-example",
                  "title": "Complex example",
                  "file_path": "A.kt",
                  "explanation": "Example contains brackets.",
                  "suggestion": "Keep as text.",
                  "code_example": "if (x) { listOf(1, 2, 3).forEach { println(it) } }"
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals(FindingSeverity.ERROR, parsed.findings.single().severity)
        assertTrue(parsed.findings.single().codeExample!!.contains("{ listOf(1, 2, 3)"))
    }

    @Test
    fun `parse supports multiline code examples`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "warning",
                  "rule_id": "compose.multiline",
                  "title": "Multiline code",
                  "file_path": "B.kt",
                  "explanation": "Code spans lines.",
                  "suggestion": "Extract function.",
                  "code_example": "val state = remember { mutableStateOf(0) }\nif (state.value > 0) {\n    println(state.value)\n}"
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertTrue(parsed.findings.single().codeExample!!.contains("\nif (state.value > 0)"))
    }

    @Test
    fun `parse accepts missing optional code example`() {
        val raw = """
            {
              "findings": [
                {
                  "severity": "info",
                  "rule_id": "compose.optional",
                  "title": "No code example",
                  "file_path": "C.kt",
                  "explanation": "Optional field omitted.",
                  "suggestion": "No action needed."
                }
              ]
            }
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertNull(parsed.findings.single().codeExample)
    }

    @Test
    fun `parse defaults invalid severity to info with warning`() {
        val raw = """
            {"findings": [{
              "severity": "critical",
              "rule_id": "compose.rule",
              "title": "Bad",
              "file_path": "A.kt",
              "explanation": "x",
              "suggestion": "y"
            }]}
        """.trimIndent()

        val parsed = parser.parse(raw)

        assertEquals(1, parsed.findings.size)
        assertEquals(FindingSeverity.INFO, parsed.findings.single().severity)
        assertTrue(parsed.warnings.any { it.contains("invalid severity") })
    }

    @Test
    fun `parse handles invalid json gracefully`() {
        val parsed = parser.parse("{\"findings\": [ { invalid }")

        assertTrue(parsed.findings.isEmpty())
        assertTrue(parsed.warnings.any { it.contains("Invalid JSON AI response") })
    }
}
