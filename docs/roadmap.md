# Roadmap

## Milestone 1: Repository Initialization
Status: done
- Set up monorepo structure for tools and shared modules.
- Add baseline docs, prompts, examples, and agent instructions.

## Milestone 2: CLI Skeleton and Kotlin File Scanning
Status: done
- Implement `mobile-ai guardrails check <path>` command flow.
- Validate single-file `.kt` inputs.
- Recursively scan `.kt` files for directory inputs.
- Add unit tests for scanning logic.

## Milestone 3: Compose Candidate Detection (Text-Based)
Status: done
- Detect Compose candidate files using lightweight text heuristics.
- Detect `@Composable` functions with approximate line references.
- Add tests for same-line and multi-line annotation patterns.

## Milestone 4: Prompt Pipeline
Status: done
- Load prompt Markdown assets from tool-local `prompts/`.
- Compose deterministic prompt bundles using base prompt + output format + rules + file context.
- Add validation for missing/empty prompt assets.
- Add tests for deterministic order and error cases.

## Milestone 5: AI Client Abstraction and Fake Adapter
Status: done
- Define provider-agnostic `AiClient` interface in `shared/ai-client`.
- Add deterministic `FakeAiClient` and factory selection.
- Integrate fake AI flow in `compose-guardrails`.
- Add unit/integration tests for contract behavior.

## Milestone 6: Structured Finding Parsing
Status: done
- Define finding model and severities in `shared/report-common`.
- Parse structured AI responses into typed findings.
- Handle invalid findings and severities with controlled warnings/fallbacks.
- Add parser tests for valid, invalid, and mixed payloads.

## Milestone 7: Markdown Report Generation
Status: next
- Generate stable Markdown reports from parsed findings.
- Include summary, per-file findings, and remediation guidance.
- Add golden tests for report rendering consistency.

## Milestone 8: First Real AI Provider Adapter
Status: planned
- Add one real provider implementation behind `AiClient`.
- Configure through environment variables only.
- Add adapter tests and clear configuration docs.

## Milestone 9: Guardrail Quality Expansion
Status: planned
- Improve rule set and prompt tuning.
- Expand examples and expected outputs.
- Reduce false positives with deterministic post-processing.

## Milestone 10: Basic GitHub Actions Integration
Status: planned
- Add CI workflow to run `compose-guardrails` in PRs.
- Persist Markdown report artifacts.
- Document CI usage and required environment configuration.
