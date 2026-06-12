package dev.mobileai.toolkit.kmpprojectauditor.core.analysis

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.AiResponse
import dev.mobileai.toolkit.aiclient.FakeAiClient
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.KmpFindingSeverity
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpAiAnalyzerTest {
    private val scanner = ProjectScanner()
    private val deterministicAuditor = DeterministicKmpAuditor()

    @Test
    fun `uses ai client abstraction and works with fake client`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))
        val deterministicFindings = deterministicAuditor.audit(scanResult)

        val result = KmpAiAnalyzer(FakeAiClient()).analyze(scanResult, deterministicFindings)

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.isEmpty())
        assertEquals("fake", result.provider)
    }

    @Test
    fun `parses valid structured ai response from client`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))

        val client = object : AiClient {
            override fun generate(request: AiRequest): AiResponse {
                return AiResponse(
                    content = """
                        {
                          "findings": [
                            {
                              "ruleId": "kmp.ai.example",
                              "severity": "INFO",
                              "title": "Example",
                              "file": "src/commonMain/kotlin/Shared.kt",
                              "explanation": "Why it matters.",
                              "suggestion": "Do the thing.",
                              "evidence": "evidence"
                            }
                          ]
                        }
                    """.trimIndent(),
                    model = "mock-model",
                    metadata = mapOf("provider" to "mock")
                )
            }
        }

        val result = KmpAiAnalyzer(client).analyze(scanResult, emptyList())

        assertEquals(1, result.findings.size)
        assertTrue(result.warnings.isEmpty())
        assertEquals("mock", result.provider)
    }

    @Test
    fun `filters compose multiplatform false positives and caps advisory severities`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))

        val client = object : AiClient {
            override fun generate(request: AiRequest): AiResponse {
                return AiResponse(
                    content = """
                        {
                          "findings": [
                            {
                              "ruleId": "kmp.common.no-android-api",
                              "severity": "ERROR",
                              "title": "Android API import in commonMain",
                              "file": "src/commonMain/kotlin/CommonCode.kt",
                              "explanation": "This looks like an Android API leak.",
                              "suggestion": "Move it.",
                              "evidence": "import androidx.compose.material3.MaterialTheme"
                            },
                            {
                              "ruleId": "kmp.project.structure",
                              "severity": "ERROR",
                              "title": "Heuristic structure note",
                              "file": "<project>",
                              "explanation": "Layout hints at a possible mismatch.",
                              "suggestion": "Review target declarations.",
                              "evidence": "Android source sets found but no Android target declaration was detected."
                            },
                            {
                              "ruleId": "kmp.common.no-android-api",
                              "severity": "ERROR",
                              "title": "Android API import in commonMain",
                              "file": "src/commonMain/kotlin/CommonCode.kt",
                              "explanation": "This is a real Android-only import.",
                              "suggestion": "Move it.",
                              "evidence": "import android.content.Context"
                            }
                          ]
                        }
                    """.trimIndent(),
                    model = "mock-model",
                    metadata = mapOf("provider" to "mock")
                )
            }
        }

        val result = KmpAiAnalyzer(client).analyze(scanResult, emptyList())

        assertEquals(2, result.findings.size)
        assertTrue(result.findings.none { it.evidence == "import androidx.compose.material3.MaterialTheme" })
        assertTrue(result.findings.any { it.evidence == "import android.content.Context" && it.severity == KmpFindingSeverity.WARNING })
        assertTrue(result.findings.any { it.ruleId == "kmp.project.structure" && it.severity == KmpFindingSeverity.INFO })
    }

    @Test
    fun `handles malformed ai response gracefully`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))

        val malformedClient = object : AiClient {
            override fun generate(request: AiRequest): AiResponse {
                return AiResponse(content = "not-json", model = "mock-model", metadata = mapOf("provider" to "mock"))
            }
        }

        val result = KmpAiAnalyzer(malformedClient).analyze(scanResult, emptyList())

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `handles empty ai response gracefully`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))

        val emptyClient = object : AiClient {
            override fun generate(request: AiRequest): AiResponse {
                return AiResponse(content = "   ", model = "mock-model", metadata = mapOf("provider" to "mock"))
            }
        }

        val result = KmpAiAnalyzer(emptyClient).analyze(scanResult, emptyList())

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }
}
