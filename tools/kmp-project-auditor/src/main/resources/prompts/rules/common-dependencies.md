Review common dependencies for obvious platform leaks:
- Android artifacts (androidx.*, com.android.*, android.*) in commonMain dependency scopes.
Report only when dependency evidence is explicit.
