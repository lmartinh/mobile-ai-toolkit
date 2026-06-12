Return only JSON with this shape:
{
  "findings": [
    {
      "ruleId": "kmp.ai.example",
      "severity": "INFO",
      "title": "Short title",
      "file": "relative/path.kt",
      "explanation": "Why this matters.",
      "suggestion": "What to do.",
      "evidence": "Concrete evidence from provided context."
    }
  ]
}

Allowed severities: ERROR, WARNING, INFO.
Do not include findings that duplicate deterministic findings already provided in context.
If there are no additional high-confidence findings, return: {"findings": []}.
