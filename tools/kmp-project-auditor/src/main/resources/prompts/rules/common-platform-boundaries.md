# Rule Intent
Review high-confidence platform-boundary issues in shared code.

# Evidence To Use
- Imports from files under `commonMain` snippets.
- Deterministic findings already reported for platform-boundary checks.

# Do Not Report
- Platform imports outside `commonMain`.
- Claims without explicit import evidence.

# False-Positive Notes
- Import-based detection is conservative; avoid speculative claims.
