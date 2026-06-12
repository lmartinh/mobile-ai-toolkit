<div align="center">
  <img src="docs/assets/mobile-ai-toolkit-hero.png" alt="mobile-ai-toolkit — AI-assisted mobile code analysis tools" width="100%">
</div>

# Mobile AI Toolkit

> **Read in another language:** **English** · [Español](README.es.md)

[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-blue)](.github/workflows/compose-guardrails.yml)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Release Packaging](https://img.shields.io/badge/Release%20Packaging-compose--guardrails%20%26%20kmp--project--auditor-orange)](docs/release-checklist.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-JVM%20%2B%20KMP-7F52FF)](https://kotlinlang.org/)
[![Status](https://img.shields.io/badge/Status-Active%20Development-2ea44f)](docs/roadmaps/README.md)

AI-assisted code review tools for Kotlin, Jetpack Compose, and Kotlin Multiplatform projects.

`mobile-ai-toolkit` is an open-source Kotlin monorepo for practical mobile development tooling. It combines mobile engineering expertise with AI-assisted analysis to help teams inspect real codebases instead of treating AI as a generic wrapper around a chat model.

The repository focuses on Jetpack Compose, Compose Multiplatform, and Kotlin Multiplatform workflows. Its CLIs analyze mobile codebases, surface architecture and maintainability risks, and produce actionable Markdown reports that work in local development and in GitHub Actions.

AI is used as a review aid, not a replacement for human reviewers. The tooling is designed to stay safe by default, with a shared provider abstraction, prompt-driven analysis, deterministic fake-provider support for tests and CI, and outputs that are easy to inspect and diff.

## Built for mobile engineers

- Compose state and side-effect patterns that deserve architecture-aware review.
- UI architecture and data-flow boundaries that are easy to blur in larger mobile codebases.
- Compose accessibility and maintainability issues that benefit from structured analysis.
- Kotlin Multiplatform source-set boundaries and platform API leaks in shared code.
- CI-friendly review reports that can be read without opening the codebase.

## Technical highlights

- Kotlin/JVM CLI development with small, focused tool entry points.
- Mobile architecture and Jetpack Compose knowledge applied to code analysis.
- Kotlin Multiplatform project structure knowledge, including source-set boundaries.
- AI provider abstraction through a shared `AiClient`.
- Prompt assets stored as Markdown resources rather than hardcoded strings.
- Structured finding parsing and Markdown report rendering.
- Safe CI defaults with deterministic `fake` provider support.
- Reusable GitHub Actions that can run from external repositories.
- Documentation discipline and release packaging workflows that are kept in version control.

## What is in this repository?

`mobile-ai-toolkit` is an ecosystem of focused tooling for mobile teams and the shared code that powers it:

| Component | What it does | Where to read more |
| --- | --- | --- |
| `compose-guardrails` | Reviews Jetpack Compose code against architecture, state, side-effect, accessibility, and Kotlin Multiplatform boundary guardrails, then renders Markdown reports. | [tools/compose-guardrails/README.md](tools/compose-guardrails/README.md) |
| `kmp-project-auditor` | Audits Kotlin Multiplatform project structure, source-set boundaries, and common platform leaks. | [tools/kmp-project-auditor/README.md](tools/kmp-project-auditor/README.md) |
| `shared/ai-client` | Provider-agnostic AI client abstraction used by the tools. | [shared/ai-client](shared/ai-client) |
| `shared/report-common` | Shared finding schema and Markdown report rendering utilities. | [shared/report-common](shared/report-common) |

## Quick Start

Run from repository root.

1. Compose guardrails (deterministic fake provider):

```bash
MOBILE_AI_PROVIDER=fake \
  ./gradlew :tools:compose-guardrails:run \
  --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

2. KMP project audit:

```bash
MOBILE_AI_PROVIDER=fake \
  ./gradlew :tools:kmp-project-auditor:run \
  --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library --output $PWD/artifacts/kmp-project-auditor-report.md"
```

3. Run core tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test :tools:kmp-project-auditor:test
```

## Current Status

| Area | Status |
| --- | --- |
| `compose-guardrails` CLI | Stable baseline |
| `kmp-project-auditor` CLI | Stable baseline |
| Shared AI provider layer | Stable baseline |
| Markdown reporting | Stable baseline |
| AST/compiler-level analysis | Planned |
| SARIF/JSON parity across tools | Planned |

## Runtime Configuration

Provider settings are read from environment variables:

- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (required for real providers)
- `MOBILE_AI_MODEL` (required for real providers in the current implementation)

For deterministic local and CI runs, use `MOBILE_AI_PROVIDER=fake`.

Security:
- Never commit API keys or tokens.
- Keep secrets in environment variables or CI secrets.

Provider-specific setup, including OpenAI API key permissions, is documented in [docs/provider-configuration.md](docs/provider-configuration.md).

## CI and GitHub Action

Current baseline workflows:
- [.github/workflows/compose-guardrails.yml](.github/workflows/compose-guardrails.yml)
- [.github/workflows/kmp-project-auditor.yml](.github/workflows/kmp-project-auditor.yml)
- [.github/workflows/manual-ai-tools-examples.yml](.github/workflows/manual-ai-tools-examples.yml)

Current release workflows:
- [.github/workflows/release-compose-guardrails.yml](.github/workflows/release-compose-guardrails.yml)
- [.github/workflows/release-kmp-project-auditor.yml](.github/workflows/release-kmp-project-auditor.yml)

Current behavior is report-first by default:
- Runs tests.
- Runs `compose-guardrails`.
- Uploads Markdown report artifact.
- Keeps fail-on-findings opt-in.

Reusable action for external repositories:
- [.github/actions/compose-guardrails/action.yml](.github/actions/compose-guardrails/action.yml)

Manual example testing workflow:
- Runs through `workflow_dispatch` against repository example projects.
- Uses the branch selected in the GitHub Actions "Use workflow from" dropdown as the checkout source.
- Lets you select the provider, tool, and fail-on-findings behavior.
- Defaults to `fake`, which needs no secrets.
- Real providers require GitHub Secrets for the provider API key (`OPENAI_API_KEY`, `ANTHROPIC_API_KEY`, or `GEMINI_API_KEY`) and use workflow convenience defaults for `MOBILE_AI_MODEL`.
- Direct CLI usage and reusable actions should pass `MOBILE_AI_MODEL` explicitly when using a real provider.
- It is report-first by default, uploads Markdown artifacts, and writes a compact run summary without Gradle-generated job summary noise.
- It does not comment on PRs.

Minimal usage:

```yaml
- id: compose-guardrails
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.3
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

For complete options (`changed-files-only`, `fail-on-findings`, step summary behavior), see:
- [tools/compose-guardrails/README.md](tools/compose-guardrails/README.md)

## How It Works

1. Choose a tool and target path (`compose` UI code or KMP project root).
2. The scanner collects deterministic local evidence from source files.
3. Prompt assets are assembled from versioned Markdown rule files.
4. The selected AI provider reviews only the collected evidence.
5. Findings are parsed into a strict schema and rendered to Markdown reports.

This design keeps behavior deterministic where possible and isolates provider-specific logic behind interfaces.

## Architecture

- CLI modules handle argument parsing, orchestration, and output.
- Tool-specific core logic lives under `tools/<tool-name>/`.
- Shared abstractions live under `shared/`.
- AI providers are behind interfaces (`AiClient`), never coupled to domain logic.
- Prompt assets are Markdown files, not hardcoded Kotlin strings.

Read:
- [Architecture guide](docs/architecture.md)

## Repository Layout

- `tools/`: tool CLIs and tool-specific analysis logic.
- `shared/`: reusable AI client and report modules.
- `docs/`: architecture, checklists, and roadmap docs.
- `artifacts/`: generated sample reports.

## Roadmaps and Project Docs

- [Compose Guardrails Roadmap](docs/roadmaps/compose-guardrails.md)
- [KMP Project Auditor Roadmap](docs/roadmaps/kmp-project-auditor.md)
- [Roadmaps Index](docs/roadmaps/README.md)
- [Release Checklist](docs/release-checklist.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## Roadmap

1. Improve analysis precision (move key checks from text heuristics to stronger static analysis).
2. Expand machine-readable output support (SARIF/JSON) across tools.
3. Deepen CI integration patterns for report gating and team workflows.
4. Add new mobile-focused analyzers under `tools/` using the same shared architecture.

## Current Limitations

- Most detection is currently heuristic/text-based (not full AST or compiler analysis).
- AI findings can contain false positives/false negatives.
- Report formats are Markdown-first; SARIF/JSON are not fully available across tools.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
