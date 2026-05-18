# GitHub PR Reviewer Roadmap

## Product Description
`github-pr-reviewer` is a Kotlin CLI and GitHub Actions-oriented tool that reviews pull request diffs with AI and generates concise, structured review feedback.

Target users are development teams that want PR summaries, risk detection, and reviewer guidance without replacing human reviewers.

## Why This Tool Belongs Here
- It supports AI-assisted mobile development workflows at the pull request level.
- It can detect mobile-specific risk areas when Kotlin, Compose, Android, or KMP files change.
- It can reuse the same provider abstraction and report patterns as existing tools.
- It complements specialist tools by summarizing diffs and routing reviewer attention.

## Relationship To Compose Guardrails
- `compose-guardrails` analyzes Compose code against a rule catalog.
- `github-pr-reviewer` analyzes PR diffs and produces review summaries and risk guidance.
- It should not duplicate Compose guardrail logic in the MVP.
- Later, it may call `compose-guardrails` or link to its report when Compose files change.

## Proposed Repository Structure
```text
tools/github-pr-reviewer/
├── AGENTS.md
├── README.md
├── build.gradle.kts
├── docs/
│   └── ci.md
├── examples/
│   └── sample-pr-diff/
├── src/main/kotlin/
├── src/main/resources/prompts/
│   ├── pr-review.md
│   ├── output-format.md
│   └── rules/
└── src/test/kotlin/
```

## Initial Review Dimensions
- PR summary: concise explanation of what changed, based only on the diff.
- Changed files overview: grouped by source, tests, docs, build/CI, dependencies, and generated-like files.
- Risk areas: risky files, broad changes, public API changes, build configuration changes, and dependency changes.
- Missing tests: likely production changes without nearby test changes.
- Documentation impact: user-facing or public API changes without docs updates.
- Breaking-change risk: renamed/removed symbols, configuration changes, or changed public contracts when visible in the diff.
- Mobile-specific risks: Kotlin, Compose, Android, KMP, Gradle, and CI patterns that deserve reviewer attention.
- Reviewer focus checklist: short, prioritized list of where human reviewers should spend time.

## GitHub Actions Safety Model
- Default mode uses `fake` provider and does not require secrets.
- Default output is artifact plus GitHub Step Summary.
- PR comments are postponed and must remain opt-in if added later.
- Fail-on-findings is postponed or opt-in; the default must not block PRs.
- Forked PRs must not require secrets or expose tokens.
- Large diffs should be summarized and truncated with clear indicators.
- Generated files, lockfiles, and binary-like files should be ignored or summarized, not deeply reviewed.
- Findings should be concise, file-specific, and evidence-based.

## Initial Review Signals
- File path patterns for source, test, documentation, Gradle, GitHub Actions, and generated-like files.
- Git diff metadata for added, modified, renamed, deleted, and large files.
- Simple churn metrics such as changed file count and changed line count.
- Nearby test detection based on naming and directory conventions.
- Mobile stack detection from file extensions, Gradle files, package paths, and imports where available.
- Secret-safety constraints from GitHub event type and provider configuration.

## Milestone 1: Tool Skeleton And Diff Input
Status: planned
- Create the Gradle module and CLI command, for example `mobile-ai github review-pr`.
- Accept a diff file or changed-files directory as input.
- Print a deterministic summary with changed file count, file types, and basic categories.
- Keep default behavior local and provider-free.
- Add tests for diff input parsing and path validation.
- Document local usage with fixture diffs.

## Milestone 2: GitHub Actions Context Reader
Status: planned
- Add a small CI script for pull request context.
- Read changed files using git in GitHub Actions.
- Ignore deleted files and unsupported binary files.
- Produce a Markdown report artifact and Step Summary.
- Keep `fake` provider as the default.
- Avoid GitHub token usage unless needed for checkout or future features.

## Milestone 3: PR Summary Heuristics
Status: planned
- Build deterministic summaries for changed files, risk areas, tests, docs, and CI/config changes.
- Detect mobile-related changes by file paths and extensions.
- Flag likely missing tests using simple path comparisons.
- Keep output concise and evidence-based.
- Add tests with sample diffs.
- Document known limitations.

## Milestone 4: Prompt Pipeline And Fake AI Review
Status: planned
- Add Markdown prompt assets for PR review and structured output.
- Compose prompts from diff summary, changed file metadata, and selected diff snippets.
- Use `AiClient` and `FakeAiClient`.
- Parse structured AI output into PR findings and summary sections.
- Keep prompts strict about not inventing files or behavior.
- Add prompt and parser tests without real API calls.

## Milestone 5: Markdown PR Review Report
Status: planned
- Generate a Markdown report with PR summary, risk areas, reviewer focus, missing tests, and documentation impact.
- Add `--output <path>` support from the first reporting milestone.
- Add Step Summary-friendly report sections.
- Keep report-only behavior as the default.
- Add golden report tests.
- Document expected output format.

## Milestone 6: GitHub Actions Integration
Status: planned
- Add a reusable script or action for external repositories.
- Support configurable target path, max diff size, report path, provider, and rule mode.
- Upload report artifacts.
- Write GitHub Step Summary.
- Keep fail-on-findings and PR comments disabled by default.
- Document forked PR and secret behavior.

## Milestone 7: Noise Control And Safety
Status: planned
- Add diff size limits and truncation indicators.
- Add file ignore patterns for generated files and lockfiles.
- Add confidence or evidence requirements for AI findings.
- Avoid duplicate or generic advice.
- Add tests for large diffs, ignored files, and no-review-needed cases.
- Keep human review as the stated decision point.

## Milestone 8: Release Readiness
Status: planned
- Add final README, CI quickstart, examples, and limitations.
- Add changelog entries.
- Validate report-only GitHub Actions usage in a sample repository.
- Confirm real-provider setup uses secrets and fake provider needs none.
- Prepare first public release after stable artifact and Step Summary behavior.

## MVP Definition
- Runs in GitHub Actions on pull requests.
- Builds a changed-files and diff summary.
- Generates a Markdown PR review report and Step Summary.
- Uses `fake` provider by default.
- Uses AI only through `AiClient`.
- Does not post PR comments or block PRs by default.

## First Public Release Definition
- External repositories can run the tool with a copy-paste workflow.
- Report artifact and Step Summary are stable and concise.
- Real providers work through existing environment variable configuration.
- Forked PR behavior and secret limitations are documented.
- PR comments remain postponed or explicitly opt-in only if implemented later.

## Shared Module Use
- Reuse `shared/ai-client` for provider access.
- Reuse `shared/report-common` if its model supports PR review findings cleanly.
- Do not reuse `compose-guardrails` internals in the MVP.
- Do not add a shared GitHub API module until token-backed features exist.
- Do not add a shared diff parser until another tool needs it.

## Explicitly Postponed
- Automatic PR comments.
- GitHub Checks annotations.
- GitHub App implementation.
- Blocking PRs by default.
- Full semantic code analysis.
- Full test impact analysis.
- Calling specialist tools automatically.
- Real API calls in tests.

## Risks And Mitigations
- AI review can become noisy: prefer concise reports and evidence-based prompts.
- Forked PRs restrict secrets: keep fake provider and report-only behavior safe by default.
- Large diffs can exceed model limits: summarize metadata first and cap snippets.
- Duplicate comments can annoy users: postpone comments until artifact and summary flow is stable.
- Generic advice reduces trust: require file-specific observations.

## Suggested Commit Or PR Sequence
- Add module skeleton, README, and diff fixtures.
- Add diff/churn scanner and tests.
- Add deterministic PR summary heuristics.
- Add prompt pipeline and fake AI integration.
- Add Markdown report generation.
- Add GitHub Actions script and documentation.
- Add noise controls and release-readiness docs.

## Recommended Tests Per Milestone
- Diff parser tests with added, modified, renamed, and deleted files.
- Changed-files tests for Kotlin, Markdown, Gradle, generated, and binary-like paths.
- Prompt asset integrity tests.
- Parser tests for structured AI output.
- Markdown report golden tests.
- CI script tests for pull_request and non-PR behavior.

## Recommended Documentation Per Milestone
- README quickstart from milestone 1.
- GitHub Actions usage once CI context reading exists.
- Output format once reporting exists.
- Forked PR and secrets documentation before public release.
- Limitations and noise-control guidance before v0.1.0.
