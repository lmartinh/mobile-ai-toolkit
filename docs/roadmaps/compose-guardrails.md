# Compose Guardrails Roadmap

This roadmap keeps the repository simple and effective. The current focus is open source polish for the first public release, while keeping `fake` provider and report-only behavior as safe defaults.

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
- Parse structured AI responses into typed findings using a JSON parser.
- Handle invalid findings and severities with controlled warnings/fallbacks.
- Add parser tests for valid, invalid, and mixed payloads.

## Milestone 7: Markdown Report Generation
Status: done
- Generate stable Markdown reports from parsed findings.
- Include summary, per-file findings, and remediation guidance.
- Add golden tests for report rendering consistency.

## Milestone 8: Real AI Provider Adapters
Status: done
- Add real provider implementations behind `AiClient`.
- Support OpenAI, Anthropic, and Gemini.
- Configure providers through environment variables only.
- Keep `FakeAiClient` available for tests and deterministic CI.
- Add adapter tests and clear configuration docs.

## Milestone 9: Guardrail Quality Expansion
Status: done (stabilized in 9.2)
- Improve rule set and prompt tuning.
- Split rules into conservative default rules and advanced opt-in rules.
- Expand examples and expected outputs.
- Reduce noise through clearer rule guidance and prompt constraints.

## Milestone 10: GitHub Actions Integration
Status: done (stabilized in 10.4)
- Add CI workflow to run `compose-guardrails` in PRs.
- Persist clean Markdown report artifacts.
- Add configurable target path, rule set, and report path.
- Add changed-files-only mode scoped to the configured target.
- Add opt-in fail-on-findings behavior.
- Add GitHub Step Summary.
- Keep `fake` provider and report-only behavior as safe defaults.
- Document CI usage and required environment configuration.

## Milestone 11: Reusable GitHub Action
Status: done (stabilized in 11.1)
- Create a reusable GitHub Action for `compose-guardrails`.
- Use a composite Action that runs the tool from the `mobile-ai-toolkit` checkout while analyzing the consumer workspace.
- Expose simple inputs for target path, rule set, provider, model, api-key, report path, changed-files-only, fail-on-findings, and summary behavior.
- Keep `fake` provider as the default so no secrets are required for the first run.
- Keep report-only behavior as the default, with fail-on-findings opt-in.
- Add a copy-paste GitHub Actions example for external repositories.
- Validate the Action manifest and use it from the built-in workflow.

## Milestone 12: Release Packaging
Status: done
- Configure repeatable Gradle application distributions for the CLI.
- Produce release ZIP/TAR artifacts for tagged versions.
- Ensure packaged distributions include prompt resources.
- Add a release workflow that runs tests and uploads artifacts.
- Document the packaged CLI path for local installs and release workflows.
- Postpone Maven Central and Gradle plugin publishing until after v0.1.0.

## Milestone 13: Open Source Polish
Status: done
- Rewrite the root README around quickstart, CI usage, providers, examples, and limitations.
- Add project badges for CI, license, and release packaging.
- Add `CONTRIBUTING.md` with setup, test, documentation, and English-only contribution guidance.
- Add `SECURITY.md` with secret handling and vulnerability reporting guidance.
- Update `CHANGELOG.md` for the first public release.
- Add lightweight issue templates for bug reports, feature requests, and guardrail proposals.

## Milestone 14: v0.1.0 Release Candidate
Status: done
- Define and run a v0.1.0 release checklist.
- Verify a third-party repository can run the reusable GitHub Action.
- Confirm default CI behavior remains fake-provider, report-only, and secret-free.
- Confirm real-provider CI setup works through GitHub Secrets.
- Tag v0.1.0 only after docs, changelog, release artifacts, and examples are aligned.

## Milestone 15: Post-v0.1.0 Improvements
Status: planned
- Consider optional PR comments after the first release.
- Consider SARIF and JSON outputs for richer CI integrations.
- Improve fail-on-findings using structured output instead of Markdown parsing.
- Improve path handling for spaces in Gradle-driven CI runs.
- Explore prompt-size reductions through changed-line or context selection.
- Keep AST parsing and code generation out of scope until real usage justifies them.
