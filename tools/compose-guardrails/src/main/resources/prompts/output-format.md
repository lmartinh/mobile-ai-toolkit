# Output Format Prompt

Return **valid JSON** using this schema:

```json
{
  "findings": [
    {
      "severity": "error|warning|info",
      "rule_id": "compose.rule-id",
      "title": "Short finding title",
      "file_path": "relative/or/absolute/path.kt",
      "explanation": "Why this matters in this code",
      "suggestion": "Specific and realistic improvement",
      "code_example": "Optional short example"
    }
  ]
}
```

Rules:
- Every finding must include: `severity`, `rule_id`, `title`, `file_path`, `explanation`, `suggestion`.
- `code_example` is optional.
- Omit uncertain findings.
- Omit unsupported claims.
- Do not output invalid JSON.
- Do not wrap JSON in Markdown fences.
- If no findings exist, return exactly:
  `{ "findings": [] }`
