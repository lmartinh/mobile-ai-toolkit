# Rule Intent
Review source-set and target alignment for Android/iOS in KMP modules.

# Evidence To Use
- Source-set list and categories.
- Detected Gradle configuration summary.
- Deterministic mismatch findings if present.

# Do Not Report
- Speculative structure problems without explicit mismatch evidence.
- Company-specific architecture constraints.
- Missing target/source-set pairs when the only signal is a heuristic layout note.

# False-Positive Notes
- Gradle target detection is text-based and may miss convention-plugin indirection.
- Prefer zero findings over absence-only advice when source-set ownership is ambiguous.
