package dev.mobileai.toolkit.kmpprojectauditor.core.prompt

class KmpPromptAssetLoader(
    private val classLoader: ClassLoader = KmpPromptAssetLoader::class.java.classLoader,
    private val basePath: String = "prompts"
) {
    fun load(): KmpPromptAssets {
        val basePrompt = readRequiredResource("$basePath/kmp-audit.md")
        val outputFormat = readRequiredResource("$basePath/output-format.md")
        val ruleFiles = loadRuleIndex("$basePath/rules/index.txt")

        val rules = ruleFiles.sorted().map { fileName ->
            KmpRulePrompt(
                id = fileName.removeSuffix(".md"),
                content = readRequiredResource("$basePath/rules/$fileName")
            )
        }

        return KmpPromptAssets(
            basePrompt = basePrompt,
            outputFormat = outputFormat,
            rules = rules
        )
    }

    private fun loadRuleIndex(resourcePath: String): List<String> {
        val content = readRequiredResource(resourcePath)
        val files = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        require(files.isNotEmpty()) { "No rule files listed in $resourcePath" }
        return files
    }

    private fun readRequiredResource(resourcePath: String): String {
        val content = classLoader.getResourceAsStream(resourcePath)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?.trim()

        require(!content.isNullOrBlank()) { "Required prompt resource is missing or empty: $resourcePath" }
        return content
    }
}

data class KmpPromptAssets(
    val basePrompt: String,
    val outputFormat: String,
    val rules: List<KmpRulePrompt>
)

data class KmpRulePrompt(
    val id: String,
    val content: String
)
