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
If there are no high-confidence findings, return: {"findings": []}.
