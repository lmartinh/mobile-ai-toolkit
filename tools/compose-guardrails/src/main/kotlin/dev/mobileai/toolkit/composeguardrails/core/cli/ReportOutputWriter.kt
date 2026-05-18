package dev.mobileai.toolkit.composeguardrails.core.cli

import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class ReportOutputWriter {
    fun write(markdownReport: String, outputPath: java.nio.file.Path?) {
        if (outputPath == null) {
            println()
            println(markdownReport)
            return
        }

        val parent = outputPath.parent
        if (parent != null && !parent.exists()) {
            parent.createDirectories()
        }
        outputPath.writeText(markdownReport)
    }
}
