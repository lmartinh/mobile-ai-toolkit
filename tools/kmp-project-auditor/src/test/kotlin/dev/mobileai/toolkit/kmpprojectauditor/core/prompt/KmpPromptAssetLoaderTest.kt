package dev.mobileai.toolkit.kmpprojectauditor.core.prompt

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KmpPromptAssetLoaderTest {
    @Test
    fun `loads base output and rules prompts`() {
        val assets = KmpPromptAssetLoader().load()

        assertTrue(assets.basePrompt.isNotBlank())
        assertTrue(assets.outputFormat.isNotBlank())
        assertTrue(assets.rules.isNotEmpty())
        assertTrue(assets.rules.map { it.id }.contains("common-platform-boundaries"))
    }

    @Test
    fun `missing prompt asset fails clearly`() {
        val loader = KmpPromptAssetLoader(basePath = "missing-prompts")

        assertFailsWith<IllegalArgumentException> {
            loader.load()
        }
    }
}
