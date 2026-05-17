# Rule: compose.viewmodel-in-leaf-composable

- category: architecture
- goal: avoid coupling leaf/reusable UI to screen-level state holders.
- recommended severity: warning

## What to detect
- Leaf composable directly obtaining/depending on ViewModel.

## What not to detect
- Top-level screen entrypoint composables owning ViewModel wiring.

## Bad example
```kotlin
@Composable
fun ProductCard() { val vm: ProductVm = viewModel() }
```

## Improved example
```kotlin
@Composable
fun ProductCard(state: ProductCardState, onClick: () -> Unit) { }
```

## Guidance for actionable suggestions
- Suggest passing state/events from parent screen.

## False positive notes
- Screen root may legitimately use ViewModel.
