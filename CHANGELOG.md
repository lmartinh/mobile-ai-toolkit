# Changelog

## Unreleased
## v0.1.2
- Fix the release publication infrastructure by tracking the Gradle wrapper JAR in Git so tagged GitHub Actions runs can execute the build.
- Keep the release artifact naming and packaging flow stable for the next patch release.
- This patch release is the recommended release line after the earlier `v0.1.0` and `v0.1.1` recovery tags.

`v0.1.2` supersedes `v0.1.1` and `v0.1.0` for new users.

## v0.1.1
- Correct the first public release after the prematurely published `v0.1.0` tag.
- Produce stable `compose-guardrails-0.1.1.zip` and `compose-guardrails-0.1.1.tar` release artifacts without `SNAPSHOT` naming.
- Fix release packaging validation so ZIP and TAR launcher checks are reliable.
- Finalize the public release notes and recovery documentation.

`v0.1.1` supersedes `v0.1.0`, which was tagged before the final release packaging fixes were applied.

## v0.1.0
- First public release of `compose-guardrails` and the supporting monorepo baseline.
- Add the `mobile-ai guardrails check <path>` CLI with Kotlin file scanning and text-based Compose detection.
- Add deterministic prompt assets, structured finding parsing, and Markdown report generation.
- Add provider support for `fake`, OpenAI, Anthropic, and Gemini through `AiClient`.
- Add default, advanced, and all rule-set support for Compose guardrail analysis.
- Add a reusable GitHub Action, release packaging, and CI workflow support for report artifacts.
- Add README, contribution, security, changelog, issue template, and release checklist polish for open-source use.

Known limitations:
- Compose detection is heuristic/text-based and does not use an AST parser yet.
- AI findings may include false positives or false negatives and should be reviewed manually.
- PR comments are not implemented yet.
- SARIF and JSON output are not implemented yet.
- Paths with spaces are currently unsupported in CI scripts.
- Release artifacts are workflow ZIP/TAR uploads for now; GitHub Release publishing is not implemented.
