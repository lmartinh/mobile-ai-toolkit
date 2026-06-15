# Compose Guardrails

> **Leer en otro idioma:** [English](README.md) · **Español**

`compose-guardrails` es una CLI en Kotlin que analiza código Jetpack Compose con guardrails asistidos por IA y un enfoque agnóstico al proveedor.

Ayuda a los equipos a detectar antes problemas de arquitectura, manejo de estado, side-effects, accesibilidad y límites multiplataforma, antes de que lleguen a code review avanzado o producción. La herramienta está pensada para uso real en CI: escaneo determinista, findings estructurados y reporte Markdown estable.

## Por qué lo usan los equipos

- Ciclos de revisión más rápidos en codebases con mucho Compose.
- Aplicación más consistente de guardrails entre contribuidores.
- Reportes aptos para CI que pueden subirse como artefactos.

Si eres nuevo en este repositorio, empieza por el [README raíz](../../README.md) y la [guía de arquitectura](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/compose-guardrails.md](../../docs/roadmaps/compose-guardrails.md)

## Comando

`mobile-ai guardrails check <path>`

Flags:
- `--rule-set default|advanced|all` (por defecto: `default`)
- `--output <path>` (escribe el reporte Markdown en archivo)

## Inicio rápido

Ejecuta desde la raíz del repositorio con rutas absolutas:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

Ejecutar tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test
```

## Configuración en runtime

Variables de entorno:
- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (requerida solo para proveedores reales)
- `MOBILE_AI_MODEL` (fijo por proveedor)

| Proveedor | Modelo |
| --- | --- |
| `openai` | `gpt-4.1-mini` |
| `anthropic` | `claude-3-5-sonnet` |
| `gemini` | `gemini-1.5-pro` |

Ejemplos por proveedor:

```bash
MOBILE_AI_PROVIDER=openai \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gpt-4.1-mini \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

```bash
MOBILE_AI_PROVIDER=gemini \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=gemini-1.5-pro \
./gradlew :tools:compose-guardrails:run --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample"
```

Seguridad:
- Nunca subas API keys.
- Usa secrets de CI para proveedores reales.

## Integración en CI

Workflow:
- [.github/workflows/compose-guardrails.yml](../../.github/workflows/compose-guardrails.yml)

Comportamiento actual por defecto (report-first):
- Ejecuta tests.
- Ejecuta análisis.
- Sube artefacto del reporte.
- No falla por findings salvo que se habilite.

Action reutilizable:
- [.github/actions/compose-guardrails/action.yml](../../.github/actions/compose-guardrails/action.yml)

Uso mínimo externo:

```yaml
- id: compose-guardrails
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.5
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

Opciones comunes:
- `changed-files-only: true`
- `fail-on-findings: true`

## Contrato de salida

El formato de reporte es Markdown-first.

Secciones de alto nivel estables:
- `# Compose Guardrails Report`
- `## Summary`
- `## Parser Warnings` (opcional)
- `## Findings`

Campos estructurados de cada finding:
- `severity` (`error`, `warning`, `info`)
- `rule_id`
- `title`
- `file_path`
- `explanation`
- `suggestion`
- `code_example` (opcional)

Nota para automatización:
- Los nombres de encabezados anteriores se consideran estables.
- El texto libre de los findings puede variar según proveedor/modelo.

## Rule Sets

`default` (recomendado para CI rutinario):
- `compose.no-business-logic-in-composables`: Detecta lógica de negocio/dominio filtrada en composables de UI.
- `compose.state-hoisting`: Verifica que el estado se eleve al caller cuando la propiedad local no es necesaria.
- `compose.viewmodel-in-leaf-composable`: Marca uso de ViewModel directamente en componentes hoja de UI.
- `compose.unidirectional-data-flow`: Valida flujo de datos unidireccional y patrón de eventos hacia arriba.
- `compose.no-side-effects-in-composition`: Detecta side-effects ejecutados durante la composición.
- `compose.effect-key-quality`: Revisa si las claves de efectos son estables y semánticamente correctas.
- `compose.lazy-list-keys`: Asegura keys estables en listas `Lazy*` para evitar glitches de recomposición.
- `compose.missing-modifier-parameter`: Marca composables que deberían exponer `Modifier` y no lo hacen.
- `compose.modifier-parameter-position`: Fuerza `Modifier` como parámetro temprano (normalmente el primer parámetro opcional de UI).
- `compose.missing-content-description`: Detecta descripciones de accesibilidad faltantes en elementos visuales relevantes.
- `compose.clickable-without-semantics`: Marca UI clickable sin contexto semántico/accesible.
- `compose.android.collect-as-state-with-lifecycle`: Recomienda collection de estado con awareness de lifecycle en Android.
- `compose.android.context-leak-risk`: Detecta posibles referencias de larga vida a `Context` de Android.
- `compose.multiplatform.no-android-api-in-common`: Marca uso de APIs Android en `commonMain`.
- `compose.multiplatform.platform-specific-ui-leak`: Detecta detalles de UI específicos de plataforma filtrados a APIs compartidas.
- `compose.multiplatform.public-api-cleanliness`: Revisa que las APIs públicas compartidas sean neutrales de plataforma y estables.

`advanced` (opt-in, con más ruido):
- `compose.expensive-work-in-composition`: Detecta cálculos pesados o asignaciones dentro de rutas de composición.
- `compose.unstable-parameters`: Marca parámetros probablemente inestables que causan recomposiciones extra.
- `compose.derived-state-usage`: Sugiere `derivedStateOf` cuando el estado computado se recalcula repetidamente.
- `compose.large-composable`: Detecta composables demasiado grandes, más difíciles de testear y mantener.
- `compose.hardcoded-dimensions-and-colors`: Marca constantes de UI hardcodeadas que deberían venir de tema/tokens.
- `compose.missing-preview`: Detecta composables sin cobertura de preview para feedback visual rápido.
- `compose.preview-with-real-dependencies`: Marca previews conectadas a dependencias reales en lugar de fakes de preview.
- `compose.multiplatform.resources-usage`: Revisa patrones de acceso a recursos compartidos en UI multiplataforma.

Guía de severidad:
- `error`: riesgo alto de corrección/arquitectura con alta confianza.
- `warning`: problema importante de mantenibilidad/diseño.
- `info`: recomendación de mejora de menor riesgo.

## Packaging de release

Construir distribución instalable:

```bash
./gradlew :tools:compose-guardrails:installDist
```

Construir archivos:

```bash
./gradlew :tools:compose-guardrails:distZip :tools:compose-guardrails:distTar
```

Ejecutar launcher empaquetado:

```bash
MOBILE_AI_PROVIDER=fake \
./tools/compose-guardrails/build/install/compose-guardrails/bin/compose-guardrails guardrails check tools/compose-guardrails/examples/bad-compose-sample
```

## Troubleshooting

- `Invalid rule-set value`:
  - Usa solo `default`, `advanced` o `all`.
- `Missing API key/model` con proveedor real:
  - Configura `MOBILE_AI_API_KEY`; el modelo queda fijo por proveedor.
- Rutas con espacios en CI:
  - Los scripts actuales de CI las rechazan para evitar problemas de splitting en Gradle `--args`.
- Salida de IA vacía o malformada:
  - El parser reporta warnings; revisa `## Parser Warnings` y re-ejecuta con `fake` para validación determinista.

## Limitaciones

- La detección es heurística/textual (sin parser AST todavía).
- Los findings de IA pueden incluir falsos positivos/falsos negativos.
- No hay publicación de comentarios en PR implementada.
- La salida SARIF/JSON no está completamente implementada.
- La capa de proveedores aún no soporta streaming/retries/history.

## Scope (actual)

- Solo análisis.
- Sin generación de código.
- Sin autofix.
