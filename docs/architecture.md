# Monorepo Architecture

## Overview
This repository is a Kotlin/JVM monorepo for AI-assisted mobile development tooling.

## Modules
- `tools/compose-guardrails`
- Purpose: tool-specific analysis logic, prompt orchestration, and CLI entrypoint wiring.

- `shared/ai-client`
- Purpose: provider-agnostic interfaces and abstractions for AI interactions.

- `shared/report-common`
- Purpose: reusable report structures and output helpers.

## Responsibility Boundaries
- CLI code parses inputs, validates arguments, invokes analysis, and prints reports.
- Tool module contains Compose guardrail analysis behavior.
- Shared modules contain cross-tool reusable contracts/utilities.
- AI-provider-specific adapters must remain isolated behind interfaces.

## Prompt Strategy
- Prompt assets are stored as Markdown resources under each tool (`src/main/resources/prompts`).
- Rules are separate prompt files to keep behavior modular and reviewable.
- Kotlin code loads prompt assets from classpath resources, not from repository-relative paths.
- Tool-specific prompt formation details should live with the tool itself. For `compose-guardrails`, see [tools/compose-guardrails/docs/prompt-system.md](../tools/compose-guardrails/docs/prompt-system.md).

## Shared Prompt Pipeline
AI-assisted tools in this repository should follow the same baseline prompt pipeline unless there is a strong reason not to:

- Keep prompt Markdown under the tool module in `src/main/resources/prompts/`.
- Load prompt assets deterministically from the classpath.
- Compose a final prompt from:
  - a base instruction prompt
  - an explicit output contract
  - rule/catalog guidance
  - scan context and evidence snippets
- Keep the output contract strict and parser-friendly.
- Treat malformed AI output as invalid and surface parser warnings rather than silently accepting it as empty.
- Keep `fake` provider behavior deterministic so tests and CI do not require real API calls.
- Prefer tool-local prompt docs and tests over shared abstractions unless duplication becomes real maintenance cost.

Current examples:
- `compose-guardrails`: see [tools/compose-guardrails/docs/prompt-system.md](../tools/compose-guardrails/docs/prompt-system.md)
- `kmp-project-auditor`: prompt assets and catalog behavior are documented in [tools/kmp-project-auditor/README.md](../tools/kmp-project-auditor/README.md) and [tools/kmp-project-auditor/docs/rules.md](../tools/kmp-project-auditor/docs/rules.md)

## Structured Parsing Strategy
- AI responses are parsed from JSON using `kotlinx.serialization`.
- Parser behavior is resilient to invalid payloads and produces warnings instead of crashing.

## Security Note
- Runtime secrets such as API keys must be supplied via environment variables.
- Never commit API keys or tokens to the repository.

## Evolution Approach
- Start with minimal abstractions and add complexity only when needed.
- Prefer clear seams over deep framework layering.
