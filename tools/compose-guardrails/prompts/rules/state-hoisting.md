# Rule: state-hoisting

## Intent
State should be hoisted to the lowest common owner when it is shared, reusable, or needs external control.

## Flag When
- Child composables own state that parent/siblings need to coordinate.
- Reusable components hide mutable state that should be caller-controlled.

## Preferred Pattern
Expose state and callbacks as parameters for stateless composables where practical.
