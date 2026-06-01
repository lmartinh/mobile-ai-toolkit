# Release Checklist

Use this checklist before tagging `v0.1.3` or any later public patch release.

## Validation
- Run `./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test`.
- Run `./gradlew :tools:kmp-project-auditor:test`.
- Run `.github/scripts/test-run-compose-guardrails.sh`.
- Run `.github/scripts/test-run-kmp-project-auditor.sh`.
- Run `.github/scripts/test-compose-guardrails-action.sh`.
- Run `.github/scripts/test-compose-guardrails-external.sh`.
- Run `.github/scripts/test-compose-guardrails-release-packaging.sh`.
- Run `.github/scripts/test-kmp-project-auditor-release-packaging.sh`.
- Verify `gradlew`, `gradlew.bat` (if present), `gradle/wrapper/gradle-wrapper.properties`, and `gradle/wrapper/gradle-wrapper.jar` are tracked before tagging.
- Validate the release version mechanism by building with `-PreleaseVersion=0.1.3` or the tag-derived release value.
- Verify release ZIP/TAR artifact names do not include `SNAPSHOT`.
- Verify the packaged CLI works outside the repository.
- Verify `kmp-project-auditor` packaged distributions include prompt assets under `prompts/`.
- Verify `kmp-project-auditor` packaged CLI writes clean Markdown reports with `MOBILE_AI_PROVIDER=fake`.
- Verify the reusable GitHub Action works from an external test repository.

## Release Readiness
- Confirm `fake` provider is still the safe default.
- Confirm real-provider setup works through environment variables and GitHub Secrets.
- Confirm README, tool README, CHANGELOG, CONTRIBUTING, and SECURITY are current.
- Confirm release artifacts are generated as workflow artifacts, not GitHub Releases.
- Confirm `kmp-project-auditor` release packages are uploaded by `.github/workflows/release-kmp-project-auditor.yml`.
- Confirm no secrets, tokens, or API keys are committed.

## Tagging
- Verify the tag points to the intended commit before pushing it.
- Do not move or delete a public tag unless the maintainers explicitly decide to do so.
- If a public tag is published too early, prefer a patch release that supersedes it rather than rewriting history.
- Create and tag the next release only after validation passes.
