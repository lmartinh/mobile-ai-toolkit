package dev.mobileai.toolkit.composeguardrails.core

import java.nio.file.Files
import java.nio.file.Path

class ComposeCandidateDetector {
    fun detect(kotlinFiles: List<Path>): List<ComposeFileAnalysis> {
        return kotlinFiles.map { filePath ->
            val content = Files.readString(filePath)
            analyzeFile(filePath, content)
        }
    }

    private fun analyzeFile(filePath: Path, content: String): ComposeFileAnalysis {
        val lines = content.lines()
        val hasComposeImport = lines.any { it.trim().startsWith("import androidx.compose") }

        val composableFunctions = mutableListOf<ComposableFunctionCandidate>()
        var waitingComposableFunction = false

        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trim()

            if (line.contains("@Composable")) {
                waitingComposableFunction = true
                if (line.contains("fun ")) {
                    extractFunctionName(line)?.let { functionName ->
                        composableFunctions += ComposableFunctionCandidate(
                            functionName = functionName,
                            line = index + 1
                        )
                    }
                    waitingComposableFunction = false
                }
                return@forEachIndexed
            }

            if (waitingComposableFunction) {
                if (line.startsWith("fun ") || line.contains(" fun ")) {
                    extractFunctionName(line)?.let { functionName ->
                        composableFunctions += ComposableFunctionCandidate(
                            functionName = functionName,
                            line = index + 1
                        )
                    }
                    waitingComposableFunction = false
                    return@forEachIndexed
                }

                if (line.isNotBlank() && !line.startsWith("@")) {
                    waitingComposableFunction = false
                }
            }
        }

        return ComposeFileAnalysis(
            filePath = filePath,
            hasComposeImport = hasComposeImport,
            composableFunctions = composableFunctions
        )
    }

    private fun extractFunctionName(functionLine: String): String? {
        val match = FUNCTION_REGEX.find(functionLine) ?: return null
        return match.groupValues[1]
    }

    companion object {
        private val FUNCTION_REGEX = Regex("""\bfun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""")
    }
}

data class ComposeFileAnalysis(
    val filePath: Path,
    val hasComposeImport: Boolean,
    val composableFunctions: List<ComposableFunctionCandidate>
) {
    val isComposeCandidate: Boolean
        get() = hasComposeImport || composableFunctions.isNotEmpty()
}

data class ComposableFunctionCandidate(
    val functionName: String,
    val line: Int
)
