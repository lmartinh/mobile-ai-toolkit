# Rule Intent
Review high-confidence platform-boundary issues in shared code.

# Evidence To Use
- Imports from files under `commonMain` snippets.
- Deterministic findings already reported for platform-boundary checks.

# Do Not Report
- Platform imports outside `commonMain`.
- Claims without explicit import evidence.
- Compose Multiplatform imports under `androidx.compose.*` in shared code in `commonMain` when they are the only Android-like signal.

# False-Positive Notes
- Import-based detection is conservative; avoid speculative claims.
- `androidx.compose.runtime.*`, `androidx.compose.foundation.*`, `androidx.compose.material3.*`, and `androidx.compose.ui.*` are valid in shared code in `commonMain`.
- Only flag Android-only imports in shared code in `commonMain`, such as `android.*`, `androidx.activity.*`, `androidx.appcompat.*`, `androidx.core.*`, and `androidx.lifecycle.*`.
