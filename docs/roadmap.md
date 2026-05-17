# Roadmap

## Milestone 0: Repository Initialization
- Set up monorepo structure for tools and shared modules.
- Add baseline docs, prompts, examples, and agent instructions.

## Milestone 1: compose-guardrails CLI MVP (Analysis Only)
- Implement CLI argument parsing and input validation.
- Load prompt assets from `prompts/`.
- Add AI client interface contracts in `shared/ai-client`.
- Produce Markdown report output using `shared/report-common`.

## Milestone 2: Rule Expansion and Reliability
- Improve rule coverage and severity tuning.
- Add deterministic post-processing for stable report formatting.
- Add tests for prompt loading, argument validation, and report rendering.

## Milestone 3: Multi-Tool Foundation
- Add at least one additional tool under `tools/`.
- Expand shared modules based on real cross-tool reuse.
- Improve contributor workflow and CI checks.
