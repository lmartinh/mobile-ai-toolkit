# kmp-project-auditor

`kmp-project-auditor` is a Kotlin CLI tool for auditing Kotlin Multiplatform mobile projects.

## Status

Milestone 1 is implemented: skeleton + deterministic project scanner.

## What It Scans Today

- Project path validation (exists, directory)
- Gradle files (settings/build files in root and modules)
- Source-set directory names under `src/`
- Kotlin source roots matching `src/*/kotlin`
- Basic capability summary (commonMain/commonTest/Android/iOS presence)
- Traversal excludes generated/internal directories (`build`, `.gradle`, `.idea`, `.kotlin`, `out`)

## What It Does Not Do Yet

- AI-based reviews
- Prompt composition/execution
- Deterministic audit findings
- Dependency placement checks
- Platform API leakage checks
- `expect`/`actual` analysis
- Markdown audit report generation
- CI/release integration for this tool

## Usage

From repository root:

```bash
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/clean-kmp-library"
```

You can also run against the incomplete sample:

```bash
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library"
```

## Safe Defaults

- Scanner is read-only.
- No network calls.
- No AI client/provider usage in Milestone 1.

## Current Limitations

- Scanning is filesystem-pattern based only.
- It does not parse Gradle/Kotlin semantics yet.
- It does not emit structured findings yet.

## Roadmap Summary

Planned later milestones add project-level KMP audit rules, findings, and optional AI-assisted analysis. See `docs/audit-areas.md` and repository roadmap docs.
