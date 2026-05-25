# kmp-project-auditor

`kmp-project-auditor` is a Kotlin CLI tool for auditing Kotlin Multiplatform mobile projects.

## Status

Milestone 5 is implemented: deterministic scanner/findings + fake AI review + Markdown audit reports.

## What It Scans Today

- Project path validation (exists, directory)
- Gradle files (settings/build files in root and modules)
- Source-set classification under `src/` (`common`, `android`, `ios`, `intermediate`, `custom`)
  - Source sets ending in `Main` or `Test` that are not common/Android/iOS-specific are treated as likely intermediate.
- Kotlin source roots matching `src/*/kotlin`
- Text-based Gradle target/plugin heuristics (KMP plugin, Android target, iOS target)
- Basic layout/capability summary and non-finding layout notes
- Deterministic findings for:
  - Android/iOS/native imports under `commonMain`
  - Missing `commonTest`
  - Android/iOS target-source-set mismatch
  - Obvious Android dependency leaks in `commonMain` dependency blocks
- Prompt asset loading and deterministic prompt composition for KMP review
- AI review through shared `AiClient` abstraction (fake provider default)
- Structured AI finding parsing with safe fallback on invalid/malformed responses
- Markdown audit report rendering with deterministic and AI findings sections
- `--output <path>` support to write Markdown report files
- Traversal excludes generated/internal directories (`build`, `.gradle`, `.idea`, `.kotlin`, `out`)

## What It Does Not Do Yet

- Full dependency graph checks
- AST/compiler-level platform API analysis
- `expect`/`actual` analysis
- CI/release integration for this tool
- Fail-on-findings behavior
- SARIF/JSON report outputs

## Usage

From repository root:

```bash
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/clean-kmp-library"
```

Write a Markdown report:

```bash
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library --output $PWD/artifacts/kmp-project-auditor-report.md"
```

You can also run against the incomplete sample:

```bash
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library"
```

## Safe Defaults

- Scanner is read-only.
- No network calls.
- No AI client/provider usage in Milestone 1.
- No AI client/provider usage in Milestone 2.
- No AI client/provider usage in Milestone 3.
- Fake AI provider is default in Milestone 4 and requires no API key/model.
- Real providers can be used through shared env vars (`MOBILE_AI_PROVIDER`, `MOBILE_AI_API_KEY`, `MOBILE_AI_MODEL`) if configured.

## Current Limitations

- Scanning is filesystem-pattern based only.
- Gradle detection is text-based heuristic matching (no Gradle model/AST parsing).
- Findings are conservative and heuristic-based (no Gradle AST or dependency graph resolution).
- AI findings are heuristic and evidence-based; review them manually.
- No CI integration for report upload yet.

## Roadmap Summary

Planned later milestones add project-level KMP audit rules, findings, and optional AI-assisted analysis. See `docs/audit-areas.md` and repository roadmap docs.
