# Compose Guardrails Report

## Summary
- Total files analyzed: 1
- Total findings: 3
- Findings by severity: high=1, medium=2, low=0

## Findings

### Finding 1
- Rule ID: `no-business-logic-in-composables`
- Severity: `high`
- File: `LoginScreen.kt`
- Line: `22`
- Why: Validation and domain-style access rules are implemented directly in the composable click handler.
- Remediation: Move credential/domain validation to a ViewModel or domain use case, and pass result state to the composable.

### Finding 2
- Rule ID: `state-hoisting`
- Severity: `medium`
- File: `LoginScreen.kt`
- Line: `14`
- Why: Form state is owned inside the screen composable, reducing external control and testability.
- Remediation: Expose state and callbacks as parameters or source them from a state holder.

### Finding 3
- Rule ID: `previews`
- Severity: `medium`
- File: `LoginScreen.kt`
- Line: `1`
- Why: No `@Preview` composables are provided for this screen.
- Remediation: Add preview functions for default and error states.
