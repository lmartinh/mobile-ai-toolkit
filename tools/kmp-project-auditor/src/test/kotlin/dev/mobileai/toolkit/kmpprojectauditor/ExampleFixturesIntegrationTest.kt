package dev.mobileai.toolkit.kmpprojectauditor

import dev.mobileai.toolkit.aiclient.FakeAiClient
import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalyzer
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ScanSummaryRenderer
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExampleFixturesIntegrationTest {
    private val scanner = ProjectScanner()
    private val deterministicAuditor = DeterministicKmpAuditor()
    private val aiAnalyzer = KmpAiAnalyzer(FakeAiClient())
    private val renderer = ScanSummaryRenderer()

    @Test
    fun `clean fixture output is deterministic and contains ai section`() {
        val fixturePath = Path.of("examples/clean-kmp-library")
        val result = scanner.scan(fixturePath)
        val deterministicFindings = deterministicAuditor.audit(result)
        val aiResult = aiAnalyzer.analyze(result, deterministicFindings)

        assertContentEquals(listOf("build.gradle.kts", "settings.gradle.kts"), result.gradleFiles)
        assertTrue(deterministicFindings.isEmpty())
        assertTrue(aiResult.findings.isNotEmpty())

        val summary = renderer.render(result, deterministicFindings, aiResult.findings, aiResult.warnings)
        assertEquals(-1, summary.indexOf('\\'))
        assertTrue(summary.contains("Deterministic findings:"))
        assertTrue(summary.contains("No deterministic findings found."))
        assertTrue(summary.contains("AI findings:"))
        assertTrue(summary.contains("kmp.ai.source-set-clarity"))
        assertTrue(summary.contains("Markdown reports are not generated yet."))
    }

    @Test
    fun `bad fixture output is deterministic and keeps deterministic findings separate from ai findings`() {
        val fixturePath = Path.of("examples/bad-kmp-library")
        val result = scanner.scan(fixturePath)
        val deterministicFindings = deterministicAuditor.audit(result)
        val aiResult = aiAnalyzer.analyze(result, deterministicFindings)

        assertContentEquals(listOf("build.gradle.kts"), result.gradleFiles)
        assertTrue(deterministicFindings.isNotEmpty())

        val summary = renderer.render(result, deterministicFindings, aiResult.findings, aiResult.warnings)
        assertEquals(-1, summary.indexOf('\\'))
        assertTrue(summary.contains("Deterministic findings:"))
        assertTrue(summary.contains("kmp.common.no-android-api"))
        assertTrue(summary.contains("kmp.common.no-ios-api"))
        assertTrue(summary.contains("AI findings:"))
        assertTrue(summary.contains("kmp.ai.source-set-clarity"))
    }
}
