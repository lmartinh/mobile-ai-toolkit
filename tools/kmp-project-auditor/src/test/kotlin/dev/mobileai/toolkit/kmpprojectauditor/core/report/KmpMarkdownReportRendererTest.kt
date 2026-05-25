package dev.mobileai.toolkit.kmpprojectauditor.core.report

import dev.mobileai.toolkit.aiclient.FakeAiClient
import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalyzer
import dev.mobileai.toolkit.kmpprojectauditor.core.analysis.KmpAiAnalysisResult
import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpMarkdownReportRendererTest {
    private val scanner = ProjectScanner()
    private val auditor = DeterministicKmpAuditor()
    private val renderer = KmpMarkdownReportRenderer()
    private val aiAnalyzer = KmpAiAnalyzer(FakeAiClient())

    @Test
    fun `clean fixture markdown report matches golden`() {
        val fixturePath = Path.of("examples/clean-kmp-library")
        val scanResult = scanner.scan(fixturePath)
        val deterministicFindings = auditor.audit(scanResult)
        val aiResult = aiAnalyzer.analyze(scanResult, deterministicFindings)

        val report = renderer.render(scanResult, deterministicFindings, aiResult)
        assertMatchesGolden(
            actual = report,
            fixturePath = fixturePath,
            goldenPath = fixturePath.resolve("expected-report.md")
        )
    }

    @Test
    fun `bad fixture markdown report matches golden`() {
        val fixturePath = Path.of("examples/bad-kmp-library")
        val scanResult = scanner.scan(fixturePath)
        val deterministicFindings = auditor.audit(scanResult)
        val aiResult = aiAnalyzer.analyze(scanResult, deterministicFindings)

        val report = renderer.render(scanResult, deterministicFindings, aiResult)
        assertMatchesGolden(
            actual = report,
            fixturePath = fixturePath,
            goldenPath = fixturePath.resolve("expected-report.md")
        )
    }

    @Test
    fun `renders explicit empty finding sections`() {
        val scanResult = scanner.scan(Path.of("examples/clean-kmp-library"))
        val aiResult = KmpAiAnalysisResult(findings = emptyList(), warnings = emptyList(), model = "fake", provider = "fake")
        val report = renderer.render(scanResult, emptyList(), aiResult)

        assertTrue(report.contains("No deterministic findings found."))
        assertTrue(report.contains("No AI findings found."))
        assertTrue(report.contains("No AI warnings."))
    }

    private fun assertMatchesGolden(actual: String, fixturePath: Path, goldenPath: Path) {
        val normalizedActual = normalizeForGolden(actual, fixturePath)
        val expected = goldenPath.toFile().readText().replace("\r\n", "\n").trimEnd()
        assertEquals(expected, normalizedActual)
    }

    private fun normalizeForGolden(actual: String, fixturePath: Path): String {
        val absoluteFixturePath = fixturePath.toAbsolutePath().normalize().toString().replace("\\", "/")
        val lines = actual.replace("\r\n", "\n").lines().map { line ->
            if (line.startsWith("- Analyzed path: `")) {
                "- Analyzed path: `${fixturePath.toString().replace("\\", "/")}`"
            } else {
                line.replace(absoluteFixturePath, fixturePath.toString().replace("\\", "/"))
            }
        }
        return lines.joinToString("\n").trimEnd()
    }
}
