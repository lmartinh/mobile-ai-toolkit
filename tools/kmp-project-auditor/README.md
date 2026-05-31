# kmp-project-auditor

`kmp-project-auditor` is a Kotlin CLI for auditing Kotlin Multiplatform mobile projects.

If you are new to this repository, start with the root [README](../../README.md) and [architecture guide](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/kmp-project-auditor.md](../../docs/roadmaps/kmp-project-auditor.md)

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

Minimal GitHub Actions example:

```yaml
name: KMP Project Auditor
on:
  pull_request:

permissions:
  contents: read

jobs:
  kmp-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - name: Run audit
        env:
          MOBILE_AI_PROVIDER: fake
        run: |
          ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $GITHUB_WORKSPACE --output $GITHUB_WORKSPACE/artifacts/kmp-project-auditor-report.md"
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: kmp-project-auditor-report
          path: artifacts/kmp-project-auditor-report.md
```

Notes:
- The tool currently does not implement fail-on-findings mode.
- Use workflow-level policy if you want to gate merges.

## Output Contract

Report format is Markdown-first with deterministic + AI sections.

Stable top-level sections:
- `# KMP Project Auditor Report`
- `## Summary`
- `## Deterministic Findings`
- `## AI Findings`

Automation note:
- Heading names above are intended to remain stable.
- Free-text details can vary by provider/model.

## What It Scans

- Project path validation (`exists`, `is directory`).
- Gradle files in root and modules.
- Source-set classification under `src/` (`common`, `android`, `ios`, `intermediate`, `custom`).
- Kotlin roots matching `src/*/kotlin`.
- Text-based target/plugin heuristics (KMP, Android, iOS).
- Deterministic findings for:
  - Android/iOS/native imports under `commonMain`.
  - Missing `commonTest`.
  - Android/iOS target-source-set mismatch.
  - Obvious Android dependency leaks in `commonMain` dependency blocks.

Rule catalog:
- [docs/rules.md](docs/rules.md)

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
- No fail-on-findings mode yet.
- No SARIF/JSON outputs yet.
- AI findings require manual review.
