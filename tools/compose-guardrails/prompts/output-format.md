# Output Format Prompt

Return **valid JSON** with this structure:

```json
{
  "findings": [
    {
      "severity": "error|warning|info",
      "rule_id": "compose.rule-id",
      "title": "Short finding title",
      "file_path": "relative/or/absolute/path.kt",
      "explanation": "Why this violates the rule",
      "suggestion": "Actionable fix guidance",
      "code_example": "Optional short code example"
    }
  ]
}
```

Rules:
- Return JSON only, without Markdown fences in the final answer.
- Include only evidence-based findings.
- Omit `code_example` when not needed.
- If no findings exist, return `{ "findings": [] }`.
