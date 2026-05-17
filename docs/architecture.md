# Monorepo Architecture

## Overview
This repository is a Kotlin/JVM monorepo for AI-assisted mobile development tooling.

## Modules
- `tools/compose-guardrails`
- Purpose: tool-specific analysis logic, prompt orchestration, and CLI entrypoint wiring.

- `shared/ai-client`
- Purpose: provider-agnostic interfaces and abstractions for AI interactions.

- `shared/cli-common`
- Purpose: reusable CLI helpers (argument models, validation helpers, command utilities).

- `shared/report-common`
- Purpose: reusable report structures and output helpers.

## Responsibility Boundaries
- CLI code parses inputs, validates arguments, invokes analysis, and prints reports.
- Tool module contains Compose guardrail analysis behavior.
- Shared modules contain cross-tool reusable contracts/utilities.
- AI-provider-specific adapters must remain isolated behind interfaces.

## Prompt Strategy
- Prompt assets are stored in Markdown under each tool.
- Rules are separate prompt files to keep behavior modular and reviewable.
- Kotlin code should load prompt files at runtime/package time, not embed prompt text.

## Evolution Approach
- Start with minimal abstractions and add complexity only when needed.
- Prefer clear seams over deep framework layering.
