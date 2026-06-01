# KMP Project Auditor Roadmap

## Product Description
`kmp-project-auditor` is a Kotlin CLI tool that audits Kotlin Multiplatform mobile projects and reports architecture, source-set, dependency, publishing, and platform-boundary issues.

Target users are mobile and KMP developers building shared libraries or SDKs for Android and iOS.

## Why This Tool Belongs Here
- It fits the repository goal of AI-assisted mobile development workflows.
- It extends the toolkit beyond Compose UI review into KMP project health.
- It can reuse `AiClient` and report rendering patterns without coupling to `compose-guardrails`.
- It keeps mobile-specific knowledge in a dedicated tool instead of a generic PR reviewer.

## Relationship To Compose Guardrails
- `compose-guardrails` reviews Compose code and UI guardrails.
- `kmp-project-auditor` reviews project structure, source sets, Gradle configuration, and platform boundaries.
- The tools may both flag platform leakage, but the auditor should focus on project-level KMP concerns.
- Shared logic should be reused only when it is tool-agnostic, such as AI access and report models.

## Proposed Repository Structure
```text
tools/kmp-project-auditor/
├── AGENTS.md
├── README.md
├── build.gradle.kts
├── docs/
│   └── audit-areas.md
├── examples/
│   ├── bad-kmp-library/
│   └── clean-kmp-library/
├── src/main/kotlin/
├── src/main/resources/prompts/
│   ├── kmp-audit.md
│   ├── output-format.md
│   └── rules/
└── src/test/kotlin/
```

## Initial Audit Catalog
- `kmp.project.structure`: detect unclear root/module layout for an Android+iOS KMP library.
- `kmp.source-sets.required-mobile`: detect missing `commonMain`, `commonTest`, Android, or iOS source sets.
- `kmp.source-sets.intermediate-clarity`: detect suspicious or undocumented intermediate source-set usage.
- `kmp.common.no-android-api`: detect Android APIs imported from `commonMain`.
- `kmp.common.no-ios-api`: detect iOS, Darwin, or platform APIs imported from `commonMain`.
- `kmp.expect-actual.missing-actual`: detect likely `expect` declarations without platform `actual` declarations using lightweight text scans.
- `kmp.expect-actual.unnecessary-expect`: flag simple cases where `expect/actual` appears to wrap portable logic.
- `kmp.dependencies.common-platform-leak`: detect Android/iOS-specific dependencies declared in common source-set blocks.
- `kmp.dependencies.platform-placement`: detect common dependencies declared only in platform source sets when they look shared.
- `kmp.resources.common-usage`: review Compose Multiplatform resource usage in shared UI code.
- `kmp.publishing.metadata`: detect missing or suspicious publishing metadata for shared libraries.
- `kmp.api.public-surface-cleanliness`: flag broad public API exposure in shared modules.
- `kmp.tests.source-set-coverage`: detect missing common or platform-specific tests.
- `kmp.docs.consumer-setup`: detect missing consumer setup guidance for published KMP libraries.

Initial severities should be conservative: platform API leakage and dependency leakage can be warnings or errors when high-confidence; docs and publishing readiness should usually be info or warning.

## KMP-Specific Heuristic Inputs
- Directory names under `src/`, especially `commonMain`, `commonTest`, `androidMain`, `androidUnitTest`, `iosMain`, and intermediate source sets.
- Gradle Kotlin DSL text for `kotlin {}`, targets, source sets, dependencies, publishing, and Android configuration.
- Kotlin imports in common source sets.
- `expect` and `actual` declarations by file path and declaration name.
- Resource directory patterns for Compose Multiplatform where present.
- README or docs files near the project/module root.

## Milestone 1: Tool Skeleton And Project Scanning
Status: completed
- Create the Gradle module and CLI command, for example `mobile-ai kmp audit <path>`.
- Accept a project directory and validate that it exists.
- Detect Gradle files, Kotlin source roots, and common KMP directory names.
- Print a deterministic scan summary with no AI call.
- Add tests for project discovery and path validation.
- Document local usage with `fake` provider as the default.

## Milestone 2: Source-Set And Target Heuristics
Status: completed
- Detect `commonMain`, `commonTest`, `androidMain`, `iosMain`, and intermediate source sets.
- Detect likely Android and iOS target configuration from Gradle text.
- Report missing or suspicious source-set layouts.
- Keep detection text-based and conservative.
- Add fixtures for Android+iOS library layouts.
- Document supported KMP project shapes and limitations.

## Milestone 3: Deterministic KMP Findings
Status: completed
- Add deterministic checks for platform APIs in `commonMain`.
- Add checks for misplaced dependencies based on simple Gradle text heuristics.
- Add checks for missing test source sets and unclear module structure.
- Add internal finding models compatible with Markdown report output.
- Keep findings high-confidence and explain false-positive risk.
- Test every rule with small fixtures.

## Milestone 4: Prompt Pipeline And Fake AI Review
Status: completed
- Add Markdown prompt assets for KMP audit review and output format.
- Compose prompts from scan summaries, deterministic findings, and relevant file snippets.
- Use `AiClient` and `FakeAiClient` for deterministic local and CI behavior.
- Parse structured AI findings using the existing JSON parsing approach where practical.
- Add prompt asset integrity tests.
- Do not call real external APIs in tests.

## Milestone 5: Markdown Audit Report
Status: completed
- Generate a Markdown report with project summary, deterministic findings, AI findings, and recommendations.
- Include severity, rule id, file path, explanation, suggestion, and confidence where useful.
- Add `--output <path>` support from the first reporting milestone.
- Add golden report tests.
- Document report-only behavior as the safe default.

## Milestone 6: Rule Catalog And Examples
Status: completed
- Add initial audit areas for source sets, platform boundaries, dependency placement, publishing readiness, API cleanliness, tests, and docs.
- Create bad and clean KMP example projects.
- Add expected reports for examples.
- Keep Android+iOS KMP libraries as the first supported shape.
- Do not enforce company-specific conventions.
- Document each rule with what to detect, what not to detect, and false-positive notes.

## Milestone 7: CI Integration
Status: completed
- Add a GitHub Actions workflow for running the auditor with `fake` provider.
- Upload the Markdown report artifact.
- Add Step Summary output.
- Keep fail-on-findings opt-in.
- Support configurable target path and report path.
- Add robust report-path handling for CI artifact upload.
- Generate a fallback Markdown report when the audit fails before producing a full report.
- Document usage with real providers through GitHub Secrets.

## Milestone 8: Release Readiness
Status: planned
- Finalize the tool README with quickstart, local usage, CI usage, providers, examples, limitations, and rule catalog links.
- Add or update changelog entries for `kmp-project-auditor`.
- Add release packaging if it can reuse the existing `compose-guardrails` Gradle application distribution pattern safely.
- Verify packaged distributions include prompt resources and can run outside the repository checkout.
- Verify the CI workflow can run on a sample KMP project with `fake` provider.
- Verify real-provider configuration works through environment variables and GitHub Secrets.
- Keep report-only behavior as the default.
- Do not add a reusable external GitHub Action unless explicitly split into a later milestone.

## MVP Definition
- CLI scans a KMP Android+iOS project directory.
- Detects source sets and common platform-boundary issues with heuristics.
- Produces a Markdown audit report.
- Uses `fake` provider by default.
- Uses AI only through `AiClient`.
- Does not generate code.

## First Public Release Definition
- Can run in GitHub Actions on a real KMP library project.
- Produces useful report artifacts and Step Summary.
- Has documented rule catalog, examples, and known limitations.
- Supports at least one real AI provider through existing shared configuration.
- Keeps report-only behavior as the default.

## Shared Module Use
- Reuse `shared/ai-client` for provider access.
- Reuse `shared/report-common` only if its finding model remains general enough.
- Do not create a shared Gradle scanner until both this tool and another tool need it.
- Do not extract prompt loading into shared code until duplication becomes meaningful.

## Explicitly Postponed
- Full Gradle AST parsing.
- Full dependency graph resolution.
- Kotlin compiler or KSP analysis.
- Code generation or autofix.
- Maven Central publishing checks beyond lightweight heuristics.
- Company-specific architecture rules.
- Swift/Objective-C source analysis.
- SARIF/JSON outputs.
- PR comments.
- Reusable external GitHub Action for `kmp-project-auditor`.

## Risks And Mitigations
- Gradle configuration is highly variable: start with conservative text heuristics and document limitations.
- False positives can reduce trust: prefer fewer high-confidence findings.
- KMP projects differ between apps and libraries: focus v1 on Android+iOS libraries.
- AI can invent architecture advice: prompt for evidence-based findings only and keep deterministic checks visible.

## Suggested Commit Or PR Sequence
- Add module skeleton, docs, and example fixtures. ✅
- Add project/source-set scanner and tests. ✅
- Add deterministic KMP checks. ✅
- Add prompt pipeline and fake AI integration. ✅
- Add Markdown report generation. ✅
- Add examples, rule docs, and expected reports. ✅
- Add safe CI integration with report artifacts and Step Summary. ✅
- Add release-readiness docs and changelog.

## Recommended Tests Per Milestone
- Scanner tests for project roots, source sets, and Gradle file discovery.
- Rule tests using minimal fixture projects.
- Prompt asset integrity tests.
- Parser tests for structured AI responses.
- Markdown report golden tests.
- CI script tests for report path and safe defaults.

## Recommended Documentation Per Milestone
- README quickstart from milestone 1.
- Supported project shapes after source-set scanning.
- Rule catalog once deterministic checks exist.
- CI usage once workflow support exists.
- Release notes and limitations before public release.
