# Compose Guardrails Roadmap

This roadmap keeps the repository simple and effective. `compose-guardrails` has reached its first recommended public release as `v0.1.2`, and the next focus is post-release maintenance plus selective improvements. Safe defaults remain `fake` provider, report-only behavior, and no secrets required for the first run.

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
- Postpone Maven Central and Gradle plugin publishing until after the first public release.

## Milestone 13: Open Source Polish
Status: done
- Rewrite the root README around quickstart, CI usage, providers, examples, and limitations.
- Add project badges for CI, license, and release packaging.
- Add `CONTRIBUTING.md` with setup, test, documentation, and English-only contribution guidance.
- Add `SECURITY.md` with secret handling and vulnerability reporting guidance.
- Update `CHANGELOG.md` for the first public release and release recovery notes.
- Add lightweight issue templates for bug reports, feature requests, and guardrail proposals.

## Milestone 14: First Public Release
Status: done
- Published `v0.1.2` as the first recommended public release.
- Kept earlier public tags immutable and superseded `v0.1.0` and `v0.1.1` with `v0.1.2`.
- Fixed release versioning so release artifacts are non-SNAPSHOT.
- Versioned the complete Gradle wrapper required by GitHub Actions.
- Validated ZIP/TAR release artifacts.
- Confirmed the packaged CLI runs outside the repository with `fake` provider.
- Confirmed the reusable GitHub Action remains usable from external repositories.

## Milestone 15: Post-v0.1.2 Improvements
Status: planned
- CI and GitHub Actions maintenance:
  - Update GitHub Actions ecosystem versions to address Node 20 deprecation warnings.
  - Add `shellcheck` to CI for `.github/scripts`.
  - Add higher-level script tests that simulate a real PR `git diff`.
  - Add exact-content tests for GitHub Step Summary.
  - Add validation for `write-step-summary=false`.
  - Add external workspace validation for `fail-on-findings`.
- Reusable Action and release workflow:
  - Consider switching the reusable GitHub Action to the packaged CLI distribution instead of the toolkit checkout and Gradle.
  - Consider adding GitHub Release publication after artifact validation.
  - Add a lightweight pull request template if needed.
  - Keep patch-release recovery guidance documented.
- Reporting and CI outputs:
  - Add optional JSON output.
  - Add optional SARIF output.
  - Improve `fail-on-findings` using structured output instead of Markdown parsing.
  - Improve multi-file changed-files report aggregation instead of concatenating per-file reports.
  - Consider optional PR comments with idempotent update behavior.
- Path handling and CLI robustness:
  - Improve support for paths with spaces.
  - Consider moving away from Gradle `--args` string tokenization where it affects CI/script reliability.
  - Keep documenting path limitations until fixed.
- Analysis quality and prompt efficiency:
  - Explore changed-line or context selection to reduce prompt size.
  - Add more real-world Compose/KMP examples.
  - Continue monitoring noisy rules and move them between default and advanced if needed.
  - Keep AST parsing out of scope until real usage justifies it.
  - Keep code generation out of scope until there is clear user demand.
