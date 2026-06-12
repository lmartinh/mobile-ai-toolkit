# KMP Project Auditor

> **Leer en otro idioma:** [English](README.md) · **Español**

`kmp-project-auditor` es una CLI en Kotlin para auditar proyectos mobile Kotlin Multiplatform.

Ayuda a los equipos a validar estructura de proyecto, límites entre source sets y separación de plataformas de forma temprana, antes de que la deriva de arquitectura se convierta en mantenimiento costoso. La herramienta combina checks deterministas con revisión opcional asistida por IA y genera reportes Markdown aptos para CI.

## Por qué lo usan los equipos

- Feedback más rápido de arquitectura en módulos y source sets KMP.
- Detección temprana de fugas de plataforma hacia código compartido (`commonMain`).
- Reportes repetibles para pull requests y validaciones de release.

Si eres nuevo en este repositorio, empieza por el [README raíz](../../README.md) y la [guía de arquitectura](../../docs/architecture.md).

Roadmap:
- [docs/roadmaps/kmp-project-auditor.md](../../docs/roadmaps/kmp-project-auditor.md)

## Comando

`kmp audit <path>`

Flags:
- `--output <path>` (escribe el reporte Markdown en archivo)

## Inicio rápido

Ejecuta desde la raíz del repositorio con rutas absolutas:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library --output $PWD/artifacts/kmp-project-auditor-report.md"
```

Ejecutar tests:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:kmp-project-auditor:test
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

Modo determinista para local/CI:

```bash
MOBILE_AI_PROVIDER=fake ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/tools/kmp-project-auditor/examples/clean-kmp-library"
```

Ejemplo con proveedor real:

```bash
MOBILE_AI_PROVIDER=anthropic \
MOBILE_AI_API_KEY=your_api_key \
MOBILE_AI_MODEL=claude-3-5-sonnet \
./gradlew :tools:kmp-project-auditor:run --args="kmp audit $PWD/path/to/your/kmp-project"
```

Seguridad:
- Nunca subas API keys.
- Usa secrets de CI para proveedores reales.

## Integración en CI

Ejemplo mínimo de GitHub Actions:

```yaml
name: KMP Project Auditor
on:
  pull_request:

permissions:
  contents: read

jobs:
  kmp-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - name: Run audit
        env:
          MOBILE_AI_PROVIDER: fake
        run: |
          ./gradlew :tools:kmp-project-auditor:run --args="kmp audit $GITHUB_WORKSPACE --output $GITHUB_WORKSPACE/artifacts/kmp-project-auditor-report.md"
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: kmp-project-auditor-report
          path: artifacts/kmp-project-auditor-report.md
```

Notas:
- La herramienta todavía no implementa modo fail-on-findings.
- Usa política a nivel workflow si quieres bloquear merges.

## Contrato de salida

El formato de reporte es Markdown-first con secciones deterministas + IA.

Secciones de alto nivel estables:
- `# KMP Project Auditor Report`
- `## Summary`
- `## Deterministic Findings`
- `## AI Findings`

Nota para automatización:
- Los nombres de encabezado anteriores se consideran estables.
- El contenido libre puede variar según proveedor/modelo.

## Rule Sets

`deterministic` (implementadas, mayor confianza):
- `kmp.common.no-android-api`: Detecta imports Android dentro de `commonMain`.
- `kmp.common.no-ios-api`: Detecta imports iOS/native dentro de `commonMain`.
- `kmp.tests.missing-common-test`: Marca proyectos con `commonMain` pero sin `commonTest`.
- `kmp.source-sets.android-target-without-source-set`: Detecta targets Android declarados sin source sets Android.
- `kmp.source-sets.ios-target-without-source-set`: Detecta targets iOS declarados sin source sets iOS.
- `kmp.source-sets.android-source-set-without-target`: Detecta source sets Android sin target Android declarado.
- `kmp.source-sets.ios-source-set-without-target`: Detecta source sets iOS sin target iOS declarado.
- `kmp.dependencies.common-platform-leak`: Detecta coordenadas Android obvias filtradas en dependencias de `commonMain`.

`ai-assisted` (implementadas, asesoras):
- `kmp.ai.source-set-clarity`: Revisa claridad e intención de source sets intermedios/custom.
- `kmp.project.structure`: Revisa señales de organización general de módulos/source sets KMP.
- `kmp.source-sets.intermediate-clarity`: Revisa propósito y ownership de source sets intermedios.
- `kmp.dependencies.platform-placement`: Revisa colocación sospechosa de dependencias cuando la evidencia es explícita.
- `kmp.resources.common-usage`: Revisa señales de uso de recursos compartidos cuando existe evidencia.
- `kmp.publishing.metadata`: Revisa señales de preparación para publicación cuando hay bloques de publishing.
- `kmp.api.public-surface-cleanliness`: Revisa señales generales de limpieza/estabilidad de API pública.
- `kmp.docs.consumer-setup`: Revisa posibles carencias en documentación de integración para consumidores.

`future` (documentadas, todavía no implementadas):
- `kmp.expect-actual.missing-actual`: Check planificado para `expect` sin implementaciones `actual` por plataforma.
- `kmp.expect-actual.unnecessary-expect`: Check planificado para sobreuso de abstracciones `expect`.
- `kmp.tests.source-set-coverage`: Check planificado para cobertura de tests más amplia entre source sets.

Catálogo completo y notas de detección:
- [docs/rules.md](docs/rules.md)

Guía de severidad:
- `warning`: riesgo probable de arquitectura/límites.
- `info`: recomendación asesora o de menor confianza.

## Qué analiza

- Validación de path de proyecto (`exists`, `is directory`).
- Archivos Gradle en raíz y módulos.
- Clasificación de source sets bajo `src/` (`common`, `android`, `ios`, `intermediate`, `custom`).
- Rutas Kotlin que siguen `src/*/kotlin`.
- Heurísticas textuales de targets/plugins (KMP, Android, iOS).

## Defaults seguros

- El scanner es read-only.
- El recorrido excluye directorios generados/internos (`build`, `.gradle`, `.idea`, `.kotlin`, `out`).
- El proveedor `fake` funciona sin API key/model.

## Troubleshooting

- `Path does not exist` o `not a directory`:
  - Confirma que `<path>` apunta a la raíz del proyecto KMP.
- Falta API key/model con proveedor real:
  - Configura `MOBILE_AI_API_KEY`; el modelo queda fijo por proveedor.
- Salida de IA vacía o malformada:
  - El parser aplica fallback seguro; revisa la sección AI Findings y re-ejecuta con `fake` para validar el comportamiento determinista.
- Findings poco útiles:
  - Revisa supuestos de layout en [docs/audit-areas.md](docs/audit-areas.md).

## Limitaciones

- Solo heurísticas de filesystem/texto (sin modelo completo AST/compiler).
- Sin análisis completo de dependency graph.
- Sin análisis de `expect`/`actual` todavía.
- Sin modo fail-on-findings todavía.
- Sin salidas SARIF/JSON todavía.
- Los findings de IA requieren revisión manual.

## Scope (actual)

- Solo análisis.
- Sin generación de código.
- Sin autofix.
