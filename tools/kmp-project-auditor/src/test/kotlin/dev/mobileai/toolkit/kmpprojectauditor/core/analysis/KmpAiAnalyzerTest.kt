package dev.mobileai.toolkit.kmpprojectauditor.core.analysis

import dev.mobileai.toolkit.aiclient.AiClient
import dev.mobileai.toolkit.aiclient.AiRequest
import dev.mobileai.toolkit.aiclient.AiResponse
import dev.mobileai.toolkit.aiclient.FakeAiClient
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
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

        assertTrue(result.findings.isNotEmpty())
        assertTrue(result.warnings.isEmpty())
        assertEquals("fake", result.provider)
    }

    @Test
    fun `handles ai client failure gracefully`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))

        val failingClient = object : AiClient {
            override fun generate(request: AiRequest): AiResponse {
                error("boom")
            }
        }

        val result = KmpAiAnalyzer(failingClient).analyze(scanResult, emptyList())

        assertTrue(result.findings.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }
}
