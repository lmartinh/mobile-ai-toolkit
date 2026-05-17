# Rule: remember-usage

## Intent
`remember` should be used carefully to avoid stale state, improper caching, or state resets across recompositions.

## Flag When
- `remember` is used for values that should come from ViewModel/state holder.
- `remember` keys are missing for values tied to changing inputs.
- Mutable UI state is created in deeply nested composables without clear ownership.

## Preferred Pattern
Keep long-lived and business-related state outside composables and scope `remember` to true UI-local concerns.
