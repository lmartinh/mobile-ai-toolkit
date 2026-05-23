# Changelog

## Unreleased
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
