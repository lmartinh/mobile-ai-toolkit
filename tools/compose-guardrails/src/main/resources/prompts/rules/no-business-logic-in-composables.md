# Rule: compose.no-business-logic-in-composables

- category: architecture
- goal: keep composables focused on UI rendering and event forwarding.
- recommended severity: warning

## What to detect
- Domain decisions, validation, pricing, permission checks, workflow branching inside composables.

## What not to detect
- Simple UI-only branching (visibility/style), simple callback forwarding.

## Bad example
```kotlin
@Composable
fun Checkout(total: Double) {
  val canPay = if (total > 100) total * 0.9 > 0 else total > 0
}
```

## Improved example
```kotlin
@Composable
fun Checkout(uiState: CheckoutUiState, onPay: () -> Unit) { /* render only */ }
```

## Guidance for actionable suggestions
- Point to exact logic block and suggest moving it to ViewModel/use-case.

## False positive notes
- Avoid flagging purely presentational conditionals.
