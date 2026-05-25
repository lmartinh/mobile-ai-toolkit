package dev.mobileai.toolkit.kmpprojectauditor.core.prompt

import dev.mobileai.toolkit.kmpprojectauditor.core.audit.DeterministicKmpAuditor
import dev.mobileai.toolkit.kmpprojectauditor.core.scan.ProjectScanner
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KmpPromptComposerTest {
    private val scanner = ProjectScanner()
    private val auditor = DeterministicKmpAuditor()

    @Test
    fun `prompt composition includes summary findings and snippets deterministically`() {
        val scanResult = scanner.scan(Path.of("examples/bad-kmp-library"))
        val deterministicFindings = auditor.audit(scanResult)
        val assets = KmpPromptAssetLoader().load()

        val composer = KmpPromptComposer(maxFiles = 4, maxCharsPerFile = 200, maxTotalChars = 5000)
        val bundle = composer.compose(assets, scanResult, deterministicFindings)

        assertTrue(bundle.promptText.contains("## Scan Summary"))
        assertTrue(bundle.promptText.contains("## Deterministic Findings"))
        assertTrue(bundle.promptText.contains("## Relevant File Snippets"))
        assertTrue(bundle.promptText.contains("build.gradle.kts"))
        assertTrue(bundle.promptText.contains("src/commonMain/kotlin/CommonOnly.kt"))
        assertTrue(bundle.snippetPaths.all { !it.contains('\\') })

        val bundle2 = composer.compose(assets, scanResult, deterministicFindings)
        assertEquals(bundle.promptText, bundle2.promptText)
        assertEquals(bundle.snippetPaths, bundle2.snippetPaths)
    }

    @Test
    fun `prompt composition respects truncation controls`() {
        val scanResult = scanner.scan(Path.of("examples/bad-kmp-library"))
        val assets = KmpPromptAssetLoader().load()
        val composer = KmpPromptComposer(maxFiles = 1, maxCharsPerFile = 20, maxTotalChars = 200)

        val bundle = composer.compose(assets, scanResult, emptyList())

        assertTrue(bundle.promptText.length <= 240)
        assertTrue(bundle.promptText.contains("<prompt-truncated>") || bundle.promptText.length <= 200)
    }

    @Test
    fun `finding linked kotlin snippets are prioritized over gradle snippets when budget is limited`() {
        val project = createTempDirectory()
        project.resolve("build.gradle.kts").writeText(
            """
            plugins { id("org.jetbrains.kotlin.multiplatform") }
            kotlin {
                ios()
                sourceSets {
                    commonMain.dependencies {
                        implementation("androidx.core:core-ktx:1.13.1")
                    }
                }
            }
            """.trimIndent()
        )
        (1..6).forEach { index ->
            project.resolve("module-$index/build.gradle.kts").apply {
                parent.createDirectories()
                writeText("plugins { kotlin(\"multiplatform\") }")
            }
        }
        project.resolve("src/commonMain/kotlin/Leak.kt").apply {
            parent.createDirectories()
            writeText(
                """
                import android.content.Context
                class Leak(val ctx: Context)
                """.trimIndent()
            )
        }

        val scanResult = scanner.scan(project)
        val deterministicFindings = auditor.audit(scanResult)
        val assets = KmpPromptAssetLoader().load()
        val composer = KmpPromptComposer(maxFiles = 2, maxCharsPerFile = 500, maxTotalChars = 5000)

        val bundle = composer.compose(assets, scanResult, deterministicFindings)

        assertTrue(bundle.snippetPaths.contains("src/commonMain/kotlin/Leak.kt"))
        assertTrue(bundle.snippetPaths.size <= 2)
        assertTrue(bundle.snippetPaths.all { !it.contains('\\') })
        assertEquals(bundle.snippetPaths, bundle.snippetPaths.distinct())
    }
}
