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
    val hasKmpSourceSetShape: Boolean = hasCommonMain && (hasAndroidSourceSet || hasIosSourceSet)
    val hasKmpContext: Boolean = gradleHeuristics.hasKmpPlugin || hasKmpSourceSetShape

    fun kmpContextStatusLabel(): String = when {
        gradleHeuristics.hasKmpPlugin -> "detected"
        hasKmpSourceSetShape -> "inferred from source sets"
        else -> "not detected"
    }

    fun kmpPluginStatusLabel(): String = if (gradleHeuristics.hasKmpPlugin) {
        "detected"
    } else {
        "not detected"
    }

    fun androidTargetStatusLabel(): String = when {
        gradleHeuristics.hasAndroidTarget -> "detected"
        hasKmpSourceSetShape && hasAndroidSourceSet -> "inferred from source sets"
        else -> "not detected"
    }

    fun iosTargetStatusLabel(): String = when {
        gradleHeuristics.hasIosTarget -> "detected"
        hasKmpSourceSetShape && hasIosSourceSet -> "inferred from source sets"
        else -> "not detected"
    }
}
