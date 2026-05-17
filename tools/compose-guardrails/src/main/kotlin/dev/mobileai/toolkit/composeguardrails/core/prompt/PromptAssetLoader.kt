package dev.mobileai.toolkit.composeguardrails.core.prompt

class PromptAssetLoader(
    private val classLoader: ClassLoader = PromptAssetLoader::class.java.classLoader,
    private val basePath: String = "prompts"
) {
    fun load(): PromptAssets {
        val reviewPrompt = readRequiredResource("$basePath/compose-review.md")
        val outputFormatPrompt = readRequiredResource("$basePath/output-format.md")
        val rulePrompts = loadRulePrompts()

        return PromptAssets(
            composeReview = reviewPrompt,
            outputFormat = outputFormatPrompt,
            rules = rulePrompts
        )
    }

    private fun loadRulePrompts(): List<RulePrompt> {
        val indexContent = readRequiredResource("$basePath/rules/index.txt")
        val fileNames = indexContent
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        require(fileNames.isNotEmpty()) { "No rule prompt files listed in $basePath/rules/index.txt" }

        return fileNames.sorted().map { fileName ->
            val resourcePath = "$basePath/rules/$fileName"
            RulePrompt(
                id = fileName.removeSuffix(".md"),
                content = readRequiredResource(resourcePath)
            )
        }
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

data class PromptAssets(
    val composeReview: String,
    val outputFormat: String,
    val rules: List<RulePrompt>
)

data class RulePrompt(
    val id: String,
    val content: String
)
