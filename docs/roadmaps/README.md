# Tool Roadmaps

This directory contains tool-specific roadmaps.

Current roadmaps:
- `compose-guardrails.md`
- `kmp-project-auditor.md`
- `github-pr-reviewer.md`

Guideline:
- Each tool under `tools/` should maintain its own roadmap file here.

Recommended implementation order:
- Keep `compose-guardrails` as the flagship tool and use its post-release roadmap for maintenance and selective improvements.
- Implement `kmp-project-auditor` before `github-pr-reviewer`.
- Use `github-pr-reviewer` after specialist tool reports are stable enough to reference from PR-level workflows.
