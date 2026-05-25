package dev.mobileai.toolkit.kmpprojectauditor.core.parsing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpAiFindingParserTest {
    private val parser = KmpAiFindingParser()

    @Test
    fun `parses valid json findings`() {
        val result = parser.parse(
            """
            {
              "findings": [
                {
                  "ruleId": "kmp.ai.example",
                  "severity": "WARNING",
                  "title": "Example",
                  "file": "src/commonMain/kotlin/A.kt",
                  "explanation": "x",
                  "suggestion": "y",
                  "evidence": "import android.content.Context"
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(1, result.findings.size)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `skips finding with missing required fields`() {
        val result = parser.parse("""{"findings":[{"ruleId":"x"}]}""")

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.any { it.contains("missing required fields") })
    }

    @Test
    fun `unknown severity defaults to info with warning`() {
        val result = parser.parse(
            """
            {"findings":[{"ruleId":"kmp.ai","severity":"CRITICAL","title":"t","file":"f","explanation":"e","suggestion":"s"}]}
            """.trimIndent()
        )

        assertEquals(1, result.findings.size)
        assertEquals("INFO", result.findings.first().severity.name)
        assertTrue(result.warnings.any { it.contains("invalid severity") })
    }

    @Test
    fun `malformed json does not crash`() {
        val result = parser.parse("{not-json")

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `parses multiple findings deterministically`() {
        val result = parser.parse(
            """
            {
              "findings": [
                {"ruleId":"b","severity":"INFO","title":"B","file":"b.kt","explanation":"e","suggestion":"s"},
                {"ruleId":"a","severity":"WARNING","title":"A","file":"a.kt","explanation":"e","suggestion":"s"}
              ]
            }
            """.trimIndent()
        )

        assertEquals(2, result.findings.size)
        assertEquals("b", result.findings[0].ruleId)
        assertEquals("a", result.findings[1].ruleId)
    }
}
