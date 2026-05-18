# Rule: compose.unstable-parameters

- category: performance (advanced)
- goal: reduce avoidable recompositions from clearly unstable inputs.
- recommended severity: info

## What to detect
- Repeated recreation of heavyweight objects or callbacks in hot paths where recomposition impact is evident.

## What not to detect
- Routine lambda usage with no strong evidence of measurable impact.
- Blanket cases where wrapping in `remember` adds complexity without clear gain.

## Bad example
```kotlin
@Composable
fun Feed(items: List<Item>, onOpen: (Item) -> Unit) {
  val mapper = ItemMapper() // recreated each recomposition
  FeedList(items.map(mapper::map), onOpen)
}
```

## Improved example
```kotlin
@Composable
fun Feed(items: List<Item>, onOpen: (Item) -> Unit) {
  val mapper = remember { ItemMapper() }
  FeedList(items.map(mapper::map), onOpen)
}
```

## Guidance for actionable suggestions
- Report only when instability is concrete and likely impactful.
- Avoid broad "wrap all lambdas in remember" advice.

## False positive notes
- This rule is advanced and lower-confidence; omit uncertain findings.
