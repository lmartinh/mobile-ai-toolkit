# Security Policy

## Supported Versions
The repository is maintained on the current trunk and release-candidate line.

## Reporting a Vulnerability
Do not post secrets, API keys, or private repository details in public issues.

If you find a security problem:
- Use the GitHub Security Advisory flow if it is available for this repository.
- Otherwise, open a private maintainer channel or a normal issue that describes the problem without including secrets.

## What to Report
- Secret leakage in docs, scripts, workflows, or artifacts.
- Unsafe workflow permissions or trust boundaries.
- Provider configuration bugs that could expose API keys.
- Packaging or release behavior that could ship sensitive files.

## Safe Defaults
- `fake` provider is the default for CI and release validation.
- Real providers require `MOBILE_AI_PROVIDER`, `MOBILE_AI_API_KEY`, and `MOBILE_AI_MODEL`.
- Public pull requests should not rely on secrets being available.
