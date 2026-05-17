# Rule: no-business-logic-in-composables

## Intent
Composable functions should focus on UI rendering and user interaction wiring, not domain/business decision logic.

## Flag When
- Complex conditional business rules are embedded directly in composables.
- Data transformation, pricing rules, permission decisions, or workflow branching happen in UI functions.

## Preferred Pattern
Move business logic to ViewModel/use-case/domain layers and pass derived UI state into composables.
