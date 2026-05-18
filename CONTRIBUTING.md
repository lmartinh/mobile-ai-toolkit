# Contributing

Thanks for helping with `mobile-ai-toolkit`.

## Setup
- Use JDK 17.
- Run the full test suite before opening a pull request:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test
```

## Working Rules
- Keep changes small and focused.
- Update docs when architecture, prompts, workflows, or release behavior change.
- Validate examples and expected reports when changing guardrail behavior.
- Use English for commit messages, code comments, and Markdown docs.

## Compose Guardrails Changes
- Keep prompt content in Markdown files.
- Keep provider integrations behind shared abstractions.
- Preserve fake-provider and report-only defaults for CI-facing workflows.
- Re-run release packaging validation when changing packaging, prompt resources, or launch scripts:

```bash
.github/scripts/test-compose-guardrails-release-packaging.sh
```

- Re-run the helper validation scripts when touching CI or workflow behavior:
  - `.github/scripts/test-run-compose-guardrails.sh`
  - `.github/scripts/test-compose-guardrails-action.sh`
  - `.github/scripts/test-compose-guardrails-external.sh`
- Run the external-repo and packaging scripts when changing reusable action behavior or release flow.

## Pull Requests
- Describe the behavior change, the scope, and the validation you ran.
- Mention any user-facing workflow or documentation updates.
- Avoid including secrets, tokens, or API keys in issues or PRs.
- Use the issue templates when filing bugs, feature requests, or guardrail proposals.
