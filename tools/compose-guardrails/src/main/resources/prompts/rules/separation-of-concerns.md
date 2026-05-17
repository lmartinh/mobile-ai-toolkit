# Rule: compose.separation-of-concerns

## Goal
Maintain clear boundaries between UI rendering, state management, and domain/business operations.

## What to detect
- Domain operations (repository/use-case style work) triggered directly in composables.
- Composables shaping domain data aggressively instead of receiving UI-ready models.
- Navigation/business side effects mixed deeply with rendering logic.

## What not to detect
- Simple event forwarding (`onClick -> onAction(...)`).
- Pure display formatting directly related to rendering.

## Recommended severity
- `warning` by default.
- `error` when composable directly performs domain/data operations.

## Bad example
```kotlin
@Composable
fun OrdersScreen(repo: OrdersRepository) {
    val orders = repo.fetchOrders() // domain/data access in UI
    OrdersList(orders)
}
```

## Improved example
```kotlin
@Composable
fun OrdersScreen(state: OrdersUiState, onRefresh: () -> Unit) {
    OrdersList(state.orders)
    RefreshButton(onRefresh)
}
```

## Guidance for actionable suggestions
- Reference the exact mixed-responsibility code.
- Recommend pushing domain/state work to ViewModel/use-case.
- Suggest passing UI-ready state and callbacks into composables.
