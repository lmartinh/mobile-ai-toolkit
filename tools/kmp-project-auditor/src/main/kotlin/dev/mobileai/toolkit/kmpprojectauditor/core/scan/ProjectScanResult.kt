package dev.mobileai.toolkit.kmpprojectauditor.core.scan

import java.nio.file.Path

enum class SourceSetKind {
    COMMON,
    ANDROID,
    IOS,
    INTERMEDIATE,
    CUSTOM
}

data class SourceSetSummary(
    val name: String,
    val kind: SourceSetKind
)

data class GradleHeuristicsSummary(
    val hasKmpPlugin: Boolean,
    val hasAndroidTarget: Boolean,
    val hasIosTarget: Boolean,
    val kmpPluginFiles: List<String>,
    val androidTargetFiles: List<String>,
    val iosTargetFiles: List<String>
)

data class ProjectScanResult(
    val analyzedPath: Path,
    val gradleFiles: List<String>,
    val sourceSets: List<String>,
    val sourceSetSummaries: List<SourceSetSummary>,
    val kotlinSourceRoots: List<String>,
    val gradleHeuristics: GradleHeuristicsSummary,
    val layoutNotes: List<String>
) {
    val hasCommonMain: Boolean = sourceSets.contains("commonMain")
    val hasCommonTest: Boolean = sourceSets.contains("commonTest")
    val hasAndroidSourceSet: Boolean = sourceSetSummaries.any { it.kind == SourceSetKind.ANDROID }
    val hasIosSourceSet: Boolean = sourceSetSummaries.any { it.kind == SourceSetKind.IOS }
    val hasAndroidTarget: Boolean = gradleHeuristics.hasAndroidTarget
    val hasIosTarget: Boolean = gradleHeuristics.hasIosTarget
    val hasIntermediateSourceSets: Boolean = sourceSetSummaries.any { it.kind == SourceSetKind.INTERMEDIATE }
    val hasCustomSourceSets: Boolean = sourceSetSummaries.any { it.kind == SourceSetKind.CUSTOM }
}
