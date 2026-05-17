# Compose Guardrails

Initial guardrails for `compose-guardrails`:

1. `no-business-logic-in-composables`
- Keep business decisions out of composable functions.

2. `state-hoisting`
- Hoist shareable state to an appropriate owner.

3. `remember-usage`
- Use `remember` only for local UI concerns with correct keys/ownership.

4. `previews`
- Provide meaningful `@Preview` coverage for key UI states.

These guardrails are intentionally narrow for v1 and will evolve based on real project usage.
