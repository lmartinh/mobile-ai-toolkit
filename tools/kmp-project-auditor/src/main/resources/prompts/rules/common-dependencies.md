# Rule Intent
Review obvious platform dependency leaks from common dependency scopes.

# Evidence To Use
- Provided Gradle snippets from common dependency blocks.
- Deterministic dependency leak findings.

# Do Not Report
- Dependency graph conclusions not visible in snippets.
- Version catalog alias resolution assumptions.

# False-Positive Notes
- Treat ambiguous dependency strings conservatively.
