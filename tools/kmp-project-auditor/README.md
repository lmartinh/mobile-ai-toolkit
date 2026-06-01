# KMP Project Auditor

> **Read in another language:** **English** · [Español](README.es.md)

`kmp-project-auditor` is a Kotlin CLI for auditing Kotlin Multiplatform mobile projects.

It helps teams validate project structure, source-set boundaries, and platform separation early, before architecture drift becomes expensive maintenance work. The tool combines deterministic checks with optional AI-assisted review and produces CI-friendly Markdown reports.

## Why teams use it

- Faster architecture feedback on KMP modules and source sets.
- Early detection of platform leaks into shared code (`commonMain`).
- Repeatable reports for pull requests and release readiness checks.

If you are new to this repository, start with the root [README](../../README.md) and [architecture guide](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/kmp-project-auditor.md](../../docs/roadmaps/kmp-project-auditor.md)

Current status:
- Milestones 1-8 are implemented for first public release readiness.
- Rule catalog, examples, and expected Markdown reports are documented and versioned.

## Command

`kmp audit <path>`

Flags:
- `--output <path>` (write Markdown report to file)

## Quick Start

Run from repository root with absolute paths:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library --output $PWD/artifacts/kmp-project-auditor-report.md"
```

Run tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:kmp-project-auditor:test
```

Validate packaging:

```bash
./gradlew :tools:kmp-project-auditor:installDist :tools:kmp-project-auditor:distZip :tools:kmp-project-auditor:distTar
```

Run packaged CLI:

```bash
MOBILE_AI_PROVIDER=fake \
tools/kmp-project-auditor/build/install/kmp-project-auditor/bin/kmp-project-auditor \
kmp audit "$PWD/tools/kmp-project-auditor/examples/bad-kmp-library" \
--output "$PWD/artifacts/kmp-project-auditor-report.md"
```

## Runtime Configuration

Environment variables:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required for real providers)
- `MOBILE_AI_MODEL` (required for real providers)

Deterministic mode for local/CI:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/clean-kmp-library"
```

Real provider example:

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/path/to/your/kmp-project"
```

Security:
- Never commit API keys.
- Use CI secrets for real providers.

## Integration in CI

Repository-local workflow:
- `.github/workflows/kmp-project-auditor.yml`
- `.github/scripts/run-kmp-project-auditor.sh`
- Release tags workflow: `.github/workflows/release-kmp-project-auditor.yml`

Defaults are safe:
- `MOBILE_AI_PROVIDER=fake`
- report-only mode by default (`KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS=false`)
- no secrets required by default
- no PR comments
- no SARIF/JSON output

Artifacts and summary:
- Markdown artifact name: `kmp-project-auditor-report`
- default report path: `artifacts/kmp-project-auditor-report.md`
- GitHub Step Summary is written when `KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY=true`
- release package artifact name: `kmp-project-auditor-release-packages-<tag>`

Real providers:
- set `MOBILE_AI_PROVIDER`, `MOBILE_AI_API_KEY`, and `MOBILE_AI_MODEL` via GitHub Secrets
- real providers are opt-in and not the CI default

## Output Contract

Report format is Markdown-first with deterministic + AI sections.

Stable top-level sections:
- `# KMP Project Audit Report`
- `## Summary`
- `## Deterministic Findings`
- `## AI Findings`

Automation note:
- Heading names above are intended to remain stable.
- Free-text details can vary by provider/model.

## Rule Sets

`deterministic` (implemented, higher confidence):
- `kmp.common.no-android-api`: Detects Android imports inside `commonMain`.
- `kmp.common.no-ios-api`: Detects iOS/native imports inside `commonMain`.
- `kmp.tests.missing-common-test`: Flags projects with `commonMain` but missing `commonTest`.
- `kmp.source-sets.android-target-without-source-set`: Detects Android target declarations without Android source sets.
- `kmp.source-sets.ios-target-without-source-set`: Detects iOS target declarations without iOS source sets.
- `kmp.source-sets.android-source-set-without-target`: Detects Android source sets without matching Android target declarations.
- `kmp.source-sets.ios-source-set-without-target`: Detects iOS source sets without matching iOS target declarations.
- `kmp.dependencies.common-platform-leak`: Detects obvious Android dependency coordinates leaking into `commonMain` dependencies.

`ai-assisted` (implemented, advisory):
- `kmp.ai.source-set-clarity`: Reviews intermediate/custom source-set clarity and intent.
- `kmp.project.structure`: Reviews overall KMP module/source-set organization signals.
- `kmp.source-sets.intermediate-clarity`: Reviews purpose and ownership signals of intermediate source sets.
- `kmp.dependencies.platform-placement`: Reviews suspicious dependency placement when evidence is explicit.
- `kmp.resources.common-usage`: Reviews shared resource usage signals when resource evidence exists.
- `kmp.publishing.metadata`: Reviews publication-readiness signals when publishing blocks are present.
- `kmp.api.public-surface-cleanliness`: Reviews broad public API cleanliness/stability signals.
- `kmp.docs.consumer-setup`: Reviews likely gaps in consumer integration documentation.

`future` (documented, not implemented yet):
- `kmp.expect-actual.missing-actual`: Planned check for `expect` declarations missing platform `actual` implementations.
- `kmp.expect-actual.unnecessary-expect`: Planned check for overuse of `expect` abstractions.
- `kmp.tests.source-set-coverage`: Planned check for broader test coverage across source sets.

Full catalog and detection notes:
- [docs/rules.md](docs/rules.md)

Severity guidance:
- `warning`: likely architecture/boundary risk.
- `info`: lower-confidence or advisory recommendation.

## What It Scans

- Project path validation (`exists`, `is directory`).
- Gradle files in root and modules.
- Source-set classification under `src/` (`common`, `android`, `ios`, `intermediate`, `custom`).
- Kotlin roots matching `src/*/kotlin`.
- Text-based target/plugin heuristics (KMP, Android, iOS).

## Safe Defaults

- Scanner is read-only.
- Traversal excludes generated/internal directories (`build`, `.gradle`, `.idea`, `.kotlin`, `out`).
- `fake` provider works without API key/model.

## Troubleshooting

- `Path does not exist` or `not a directory`:
  - Confirm `<path>` points to the KMP project root.
- Missing API key/model with real provider:
  - Set both `MOBILE_AI_API_KEY` and `MOBILE_AI_MODEL`.
- Empty or malformed AI output:
  - Parser falls back safely; inspect AI findings section and re-run with `fake` to validate deterministic behavior.
- No useful findings:
  - Review project layout assumptions in [docs/audit-areas.md](docs/audit-areas.md).

## Limitations

- Filesystem/text heuristics only (no full AST/compiler model).
- No full dependency-graph analysis.
- No `expect`/`actual` analysis yet.
- No CLI `--fail-on-findings` flag (CI script supports opt-in fail mode via `KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS`).
- No SARIF/JSON outputs yet.
- No PR comments.
- No reusable external GitHub Action for this tool yet.
- AI findings require manual review.
- Paths with spaces are currently unsupported in CI scripts.

## Scope (Current)

- Analysis only.
- No code generation.
- No autofix behavior.
