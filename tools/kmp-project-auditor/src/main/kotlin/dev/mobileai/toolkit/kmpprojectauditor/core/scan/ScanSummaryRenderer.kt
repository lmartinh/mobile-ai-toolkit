package dev.mobileai.toolkit.kmpprojectauditor.core.scan

class ScanSummaryRenderer {
    fun render(result: ProjectScanResult): String {
        val lines = mutableListOf<String>()
        lines += "KMP Project Auditor"
        lines += "Analyzed path: ${result.analyzedPath}"
        lines += "Gradle files:"
        lines += formatList(result.gradleFiles)
        lines += "Source sets:"
        lines += formatList(result.sourceSets)
        lines += "Kotlin source roots:"
        lines += formatList(result.kotlinSourceRoots)
        lines += "Detected capabilities:"
        lines += "- has commonMain: ${result.hasCommonMain}"
        lines += "- has commonTest: ${result.hasCommonTest}"
        lines += "- has Android source set: ${result.hasAndroidSourceSet}"
        lines += "- has iOS source set: ${result.hasIosSourceSet}"
        lines += "No audit findings are generated in Milestone 1."
        return lines.joinToString(separator = "\n")
    }

    private fun formatList(items: List<String>): String {
        return if (items.isEmpty()) "- (none)" else items.joinToString("\n") { "- $it" }
    }
}
