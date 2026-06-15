# KMP Rule Catalog

This catalog documents the current audit rules for `kmp-project-auditor`.
Milestone 8 is release-readiness focused and keeps the rule set heuristic and text-based.

Rule status values:
- `deterministic`: implemented as text/path-based checks in the current tool.
- `ai-assisted`: reviewed through AI prompt guidance using provided evidence only.
- `future`: documented but not implemented yet.

## Deterministic Rules

### `kmp.common.no-android-api`
- Status: deterministic
- Category: platform boundaries
- Default severity: WARNING
- Detects: `import android.*` and Android-only `androidx.*` imports inside shared code in `commonMain` Kotlin files.
- Does not detect: Compose Multiplatform imports under `androidx.compose.*`, Android usage outside imports, or Android imports in `androidMain`.
- Evidence used: import lines in files under `src/commonMain/kotlin`.
- False-positive notes: import-based heuristic only.
- Example bad pattern: `import android.content.Context` in shared code in `commonMain`.
- Compose Multiplatform examples that are allowed in shared code in `commonMain`: `androidx.compose.runtime.Composable`, `androidx.compose.foundation.*`, `androidx.compose.material3.*`, and `androidx.compose.ui.*`.
- Android-only examples that should still be flagged in shared code in `commonMain`: `androidx.activity.*`, `androidx.appcompat.*`, `androidx.core.*`, and `androidx.lifecycle.*`.
- Better pattern: move Android-specific code to `androidMain` or use `expect`/`actual` boundaries.

### `kmp.common.no-ios-api`
- Status: deterministic
- Category: platform boundaries
- Default severity: WARNING
- Detects: `import platform.*` or `import kotlinx.cinterop.*` inside shared code in `commonMain`.
- Does not detect: platform imports in `iosMain`.
- Evidence used: import lines in files under `src/commonMain/kotlin`.
- False-positive notes: import-based heuristic only.
- Example bad pattern: `import platform.Foundation.NSString` in shared code in `commonMain`.
- Better pattern: move native/iOS-specific code to `iosMain` or native source sets.

### `kmp.common.jvm-only-api`
- Status: deterministic
- Category: platform boundaries
- Default severity: WARNING
- Detects: JVM-only imports and simple fully qualified JVM-only usages inside `commonMain`.
- Examples: `import java.time.LocalDate`, `import javax.crypto.Cipher`, `java.time.LocalDate.now()`, `kotlin.io.path.createTempDirectory()`.
- Does not detect: complex aliasing, compiler-inferred usages, or fully semantic API compatibility.
- Evidence used: import lines and simple fully qualified usages in files under `src/commonMain/kotlin`.
- False-positive notes: text-based heuristic only; comments are ignored but string-literal usage may still be noisy.
- Better pattern: move JVM-specific code behind `expect`/`actual`, inject an abstraction, or use a multiplatform library.

### `kmp.tests.missing-common-test`
- Status: deterministic
- Category: test readiness
- Default severity: INFO
- Detects: shared code exists in `commonMain` but `commonTest` is missing.
- Does not detect: test quality or coverage depth.
- Evidence used: discovered source-set directories.
- False-positive notes: some tiny modules intentionally skip shared tests.
- Example bad pattern: shared production code without shared tests.
- Better pattern: add `commonTest` for shared business logic.

### `kmp.source-sets.android-target-without-source-set`
- Status: deterministic
- Category: source-set alignment
- Default severity: WARNING
- Detects: Android target heuristics in Gradle, but no Android source set directory.
- Does not detect: whether the Android target is intentionally placeholder-only.
- Evidence used: Gradle text patterns + discovered source sets.
- False-positive notes: text-based Gradle heuristics can be conservative.

### `kmp.source-sets.ios-target-without-source-set`
- Status: deterministic
- Category: source-set alignment
- Default severity: WARNING
- Detects: iOS target heuristics in Gradle, but no iOS source set directory.
- Does not detect: intentionally incomplete prototypes.
- Evidence used: Gradle text patterns + discovered source sets.
- False-positive notes: text-based Gradle heuristics only.

### `kmp.source-sets.android-source-set-without-target`
- Status: deterministic
- Category: source-set alignment
- Default severity: WARNING
- Detects: Android source sets exist but Android target heuristics are missing.
- Does not detect: hidden targets in external convention plugins.
- Evidence used: source-set discovery + Gradle text patterns.
- False-positive notes: Gradle convention indirection may hide target declarations.
- Advice-only rule. A missing target can be a tooling limitation, not a project defect.

### `kmp.source-sets.ios-source-set-without-target`
- Status: deterministic
- Category: source-set alignment
- Default severity: WARNING
- Detects: iOS source sets exist but iOS target heuristics are missing.
- Does not detect: hidden target setup from convention plugins.
- Evidence used: source-set discovery + Gradle text patterns.
- False-positive notes: Gradle convention indirection may hide target declarations.
- Advice-only rule. A missing target can be a tooling limitation, not a project defect.

### `kmp.expect-actual.missing-actual`
- Status: deterministic
- Category: expect/actual
- Default severity: WARNING
- Detects: simple `expect` declarations in `commonMain` with no matching `actual` declaration in platform or intermediate source sets.
- Examples: `expect class Name`, `expect fun platformName()`, `expect val value`.
- Does not detect: full signature compatibility, annotations, visibility, overload sets, type parameters, or `androidUnitTest`/test-only handling.
- Evidence used: declaration lines in `commonMain` and matching declarations in non-common source sets.
- False-positive notes: heuristic text scan only.
- Better pattern: add the corresponding platform `actual` declaration or remove the unused `expect`.

### `kmp.expect-actual.orphan-actual`
- Status: deterministic
- Category: expect/actual
- Default severity: WARNING
- Detects: simple `actual` declarations in platform or intermediate source sets with no matching `expect` in `commonMain`.
- Does not detect: full signature compatibility, annotations, visibility, overload sets, or test-only handling.
- Evidence used: declaration lines in non-common source sets and matching declarations in `commonMain`.
- False-positive notes: heuristic text scan only.
- Better pattern: add the matching `expect` in `commonMain` or remove the orphan `actual`.

### `kmp.dependencies.common-platform-leak`
- Status: deterministic
- Category: dependency placement
- Default severity: WARNING
- Detects: obvious Android dependency coordinates in `commonMain` dependency scopes.
- Does not detect: alias/version-catalog indirection or full dependency graph semantics.
- Evidence used: Gradle text in `commonMain.dependencies {}` or `commonMainImplementation(...)`.
- False-positive notes: string-pattern based heuristic.
- Example bad pattern: `implementation("androidx.core:core-ktx:...")` in `commonMain.dependencies`.
- Better pattern: move Android artifacts to Android-specific source-set dependency scopes.

### `kmp.compose.common-localcontext-usage`
- Status: deterministic
- Category: Compose platform boundaries
- Default severity: WARNING
- Detects: Android-specific Compose platform access in `commonMain`, such as `LocalContext.current`, `LocalView.current`, `LocalLifecycleOwner.current`, and `BackHandler`.
- Does not detect: generic `androidx.compose.*` imports that are valid in Compose Multiplatform.
- Evidence used: explicit imports and simple `Local*` platform access lines under `src/commonMain/kotlin`.
- False-positive notes: narrow denylist by design.
- Better pattern: move platform behavior behind `expect`/`actual`, pass dependencies from platform code, or use a multiplatform-safe abstraction.

### `kmp.compose.resources.android-res-in-common`
- Status: deterministic
- Category: Compose resources / platform boundaries
- Default severity: WARNING
- Detects: Android resource access in `commonMain`, such as `R.string.*`, `R.drawable.*`, `LocalContext.current.resources`, and `.resources.getString(...)`.
- Does not detect: Compose Multiplatform resource access via generated `Res.*` APIs.
- Evidence used: explicit import/resource access lines under `src/commonMain/kotlin`.
- False-positive notes: heuristic text scan only; avoids broad `R` matching unless the Android resource shape is explicit.
- Better pattern: use Compose Multiplatform resources, pass platform-specific values from platform source sets, or move the access behind `expect`/`actual`.

## AI-Assisted Rules

### `kmp.ai.source-set-clarity`
- Status: ai-assisted
- Category: source-set clarity
- Default severity: INFO
- Detects: potentially unclear intermediate source-set intent.
- Does not detect: strict structural errors.
- Evidence used: scan summary, deterministic findings, selected snippets.
- False-positive notes: suggestions require human review.
- AI output is intentionally conservative and should avoid turning heuristic layout notes into strong findings.

### `kmp.project.structure`
- Status: ai-assisted
- Category: project structure
- Default severity: INFO
- Detects: unclear KMP module/source-set organization signals.
- Does not detect: one true architecture.
- Evidence used: provided scan summary and snippets.
- AI output is advisory only.

### `kmp.source-sets.intermediate-clarity`
- Status: ai-assisted
- Category: source sets
- Default severity: INFO
- Detects: unclear purpose or ownership of intermediate/custom source sets.
- Does not detect: correctness from source-set existence alone.
- Evidence used: source-set list and contextual snippets.

### `kmp.dependencies.platform-placement`
- Status: ai-assisted
- Category: dependencies
- Default severity: INFO
- Detects: suspicious dependency placement when evidence is explicit.
- Does not detect: full dependency graph truth.
- Evidence used: provided Gradle snippets only.

### `kmp.resources.common-usage`
- Status: ai-assisted
- Category: resources
- Default severity: INFO
- Detects: potential shared-resource usage issues when resource evidence exists.
- Does not detect: full Compose resource correctness without explicit evidence.

### `kmp.publishing.metadata`
- Status: ai-assisted
- Category: publishing
- Default severity: INFO
- Detects: publication-readiness gaps when publishing blocks are visible.
- Does not detect: publication platform compliance guarantees.

### `kmp.api.public-surface-cleanliness`
- Status: ai-assisted
- Category: public API
- Default severity: INFO
- Detects: broad public-surface risk signals from visible code snippets.
- Does not detect: complete API stability analysis.

### `kmp.docs.consumer-setup`
- Status: ai-assisted
- Category: documentation
- Default severity: INFO
- Detects: likely missing consumer integration guidance signals.
- Does not detect: full docs completeness.

## Future Rules

### `kmp.expect-actual.unnecessary-expect`
- Status: future
- Category: expect/actual
- Default severity: INFO
- Planned intent: detect overuse of `expect` abstraction where unnecessary.
- Why future: high false-positive risk without deeper semantic analysis.

### `kmp.tests.source-set-coverage`
- Status: future
- Category: tests
- Default severity: INFO
- Planned intent: broader source-set test coverage guidance beyond `commonTest` existence.
- Why future: requires deeper project/test semantics.
