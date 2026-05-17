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

## Structured Parsing Strategy
- AI responses are parsed from JSON using `kotlinx.serialization`.
- Parser behavior is resilient to invalid payloads and produces warnings instead of crashing.

## Security Note
- Runtime secrets such as API keys must be supplied via environment variables.
- Never commit API keys or tokens to the repository.

## Evolution Approach
- Start with minimal abstractions and add complexity only when needed.
- Prefer clear seams over deep framework layering.
