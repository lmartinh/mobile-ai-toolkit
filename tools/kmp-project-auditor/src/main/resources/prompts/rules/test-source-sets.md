# Rule Intent
Review shared test readiness for `commonMain` modules.

# Evidence To Use
- Presence/absence of `commonTest` in source-set summary.
- Deterministic `missing-common-test` finding when present.

# Do Not Report
- Broad test coverage quality claims.
- Assertions about specific test frameworks without evidence.

# False-Positive Notes
- Some tiny modules intentionally defer shared tests.
