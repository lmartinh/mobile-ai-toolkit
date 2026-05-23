# Release Checklist

Use this checklist before tagging `v0.1.0`.

## Validation
- Run `./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test`.
- Run `.github/scripts/test-run-compose-guardrails.sh`.
- Run `.github/scripts/test-compose-guardrails-action.sh`.
- Run `.github/scripts/test-compose-guardrails-external.sh`.
- Run `.github/scripts/test-compose-guardrails-release-packaging.sh`.
- Validate the release version mechanism by building with `-PreleaseVersion=0.1.0` or the tag-derived release value.
- Verify release ZIP/TAR artifact names do not include `SNAPSHOT`.
- Verify the packaged CLI works outside the repository.
- Verify the reusable GitHub Action works from an external test repository.

## Release Readiness
- Confirm `fake` provider is still the safe default.
- Confirm real-provider setup works through environment variables and GitHub Secrets.
- Confirm README, tool README, CHANGELOG, CONTRIBUTING, and SECURITY are current.
- Confirm release artifacts are generated as workflow artifacts, not GitHub Releases.
- Confirm no secrets, tokens, or API keys are committed.

## Tagging
- Create and tag `v0.1.0` only after validation passes.
