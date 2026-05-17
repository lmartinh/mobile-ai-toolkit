# Output Format Prompt

Generate a Markdown report with the following sections in order:

1. `# Compose Guardrails Report`
2. `## Summary`
- Total files analyzed
- Total findings
- Findings by severity (`high`, `medium`, `low`)
3. `## Findings`

For each finding, include:
- Rule ID
- Severity
- File path
- Approximate line reference (if available)
- Why this violates the guardrail
- Suggested remediation

If no findings exist, output:
- `## Findings`
- `No guardrail violations detected.`
