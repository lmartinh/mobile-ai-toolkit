# Rule: previews

## Intent
Composable screens/components should include useful `@Preview` coverage where feasible for UI iteration and regression awareness.

## Flag When
- Public-facing composables have no preview variants.
- Preview data is unrealistic or does not exercise meaningful UI states.

## Preferred Pattern
Add focused previews for default, edge, and error/loading states when relevant.
