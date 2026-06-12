<div align="center">
  <img src="docs/assets/mobile-ai-toolkit-hero.png" alt="mobile-ai-toolkit — herramientas de análisis de código mobile asistidas por IA" width="100%">
</div>

# Mobile AI Toolkit

> **Leer en otro idioma:** [English](README.md) · **Español**

[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-blue)](.github/workflows/compose-guardrails.yml)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Release Packaging](https://img.shields.io/badge/Release%20Packaging-compose--guardrails-orange)](docs/roadmaps/compose-guardrails.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-JVM%20%2B%20KMP-7F52FF)](https://kotlinlang.org/)
[![Status](https://img.shields.io/badge/Status-Active%20Development-2ea44f)](docs/roadmaps/README.md)

Herramientas de revisión de código asistidas por IA para proyectos Kotlin, Jetpack Compose y Kotlin Multiplatform.

`mobile-ai-toolkit` es un monorepo open source en Kotlin para tooling práctico de desarrollo mobile. Combina experiencia en ingeniería mobile con análisis asistido por IA para ayudar a los equipos a inspeccionar codebases reales en lugar de tratar la IA como un envoltorio genérico de un chat.

El repositorio se centra en flujos de trabajo con Jetpack Compose, Compose Multiplatform y Kotlin Multiplatform. Sus CLIs analizan codebases mobile, detectan riesgos de arquitectura y mantenibilidad, y generan reportes Markdown accionables que funcionan en desarrollo local y en GitHub Actions.

La IA se usa como ayuda de revisión, no como sustituto de los revisores humanos. El tooling está diseñado para ser seguro por defecto, con una abstracción compartida de proveedor, análisis guiado por prompts, soporte determinista con `fake` para tests y CI, y salidas fáciles de inspeccionar y comparar.

## Orientado a equipos mobile

- Patrones de estado y side effects en Compose que merecen revisión con conocimiento de arquitectura.
- Arquitectura de UI y límites de flujo de datos que se difuminan con facilidad en codebases grandes.
- Problemas de accesibilidad y mantenibilidad en Compose que se benefician de un análisis estructurado.
- Límites de source sets en Kotlin Multiplatform y fugas de APIs de plataforma en código compartido.
- Reportes de revisión compatibles con CI que pueden leerse sin abrir el codebase completo.

## Aspectos técnicos

- Desarrollo de CLIs en Kotlin/JVM con puntos de entrada pequeños y centrados.
- Conocimiento de arquitectura mobile y Jetpack Compose aplicado al análisis de código.
- Conocimiento de la estructura de proyectos Kotlin Multiplatform, incluidos los límites de source sets.
- Abstracción de proveedor de IA a través de un `AiClient` compartido.
- Prompts almacenados como recursos Markdown en lugar de strings hardcodeados.
- Parsing estructurado de findings y renderizado de reportes Markdown.
- Valores seguros por defecto en CI con soporte determinista para el proveedor `fake`.
- GitHub Actions reutilizables que pueden ejecutarse desde repositorios externos.
- Disciplina de documentación y de empaquetado de releases mantenida en el control de versiones.

## Qué hay en este repositorio

`mobile-ai-toolkit` es un ecosistema de herramientas enfocadas para equipos mobile y del código compartido que las soporta:

| Componente | Qué hace | Dónde leer más |
| --- | --- | --- |
| `compose-guardrails` | Revisa código Jetpack Compose con guardrails de arquitectura, estado, side effects, accesibilidad y límites de Kotlin Multiplatform, y luego genera reportes Markdown. | [tools/compose-guardrails/README.md](tools/compose-guardrails/README.md) |
| `kmp-project-auditor` | Audita estructura de proyectos Kotlin Multiplatform, límites de source sets y fugas entre plataformas. | [tools/kmp-project-auditor/README.md](tools/kmp-project-auditor/README.md) |
| `shared/ai-client` | Abstracción agnóstica de proveedor IA usada por las herramientas. | [shared/ai-client](shared/ai-client) |
| `shared/report-common` | Esquema compartido de findings y utilidades para renderizar reportes Markdown. | [shared/report-common](shared/report-common) |

## Inicio rápido

Ejecuta desde la raíz del repositorio.

1. Compose guardrails (proveedor `fake` determinista):

```bash
MOBILE_AI_PROVIDER=fake \
  ./gradlew :tools:compose-guardrails:run \
  --args="guardrails check $PWD/tools/compose-guardrails/examples/bad-compose-sample --rule-set default --output $PWD/artifacts/compose-guardrails-report.md"
```

2. Auditoría KMP:

```bash
MOBILE_AI_PROVIDER=fake \
  ./gradlew :tools:kmp-project-auditor:run \
  --args="kmp audit $PWD/tools/kmp-project-auditor/examples/bad-kmp-library --output $PWD/artifacts/kmp-project-auditor-report.md"
```

3. Ejecutar tests principales:

```bash
./gradlew :shared:ai-client:test :shared:report-common:test :tools:compose-guardrails:test :tools:kmp-project-auditor:test
```

## Estado actual

| Área | Estado |
| --- | --- |
| CLI `compose-guardrails` | Baseline estable |
| CLI `kmp-project-auditor` | Baseline estable |
| Capa compartida de proveedores IA | Baseline estable |
| Reportes Markdown | Baseline estable |
| Análisis AST/compiler-level | Planificado |
| Paridad SARIF/JSON entre herramientas | Planificado |

## Configuración en runtime

La configuración del proveedor se lee desde variables de entorno:

- `MOBILE_AI_PROVIDER` (`fake`, `openai`, `anthropic`, `gemini`)
- `MOBILE_AI_API_KEY` (requerida para proveedores reales)
- `MOBILE_AI_MODEL` (valor de modelo usado por los proveedores reales)

Para ejecuciones deterministas en local y CI, usa `MOBILE_AI_PROVIDER=fake`.

Seguridad:
- Nunca subas API keys o tokens al repositorio.
- Guarda secretos en variables de entorno o GitHub Secrets.

La configuración específica de cada proveedor, incluidos los permisos recomendados para la API key de OpenAI, está documentada en [docs/provider-configuration.md](docs/provider-configuration.md).

## CI y GitHub Action

Workflow base actual:
- [.github/workflows/compose-guardrails.yml](.github/workflows/compose-guardrails.yml)
- [.github/workflows/kmp-project-auditor.yml](.github/workflows/kmp-project-auditor.yml)
- [.github/workflows/manual-ai-tools-examples.yml](.github/workflows/manual-ai-tools-examples.yml)

Workflows de release actuales:
- [.github/workflows/release-compose-guardrails.yml](.github/workflows/release-compose-guardrails.yml)
- [.github/workflows/release-kmp-project-auditor.yml](.github/workflows/release-kmp-project-auditor.yml)

Comportamiento actual orientado a reporte por defecto:
- Ejecuta tests.
- Ejecuta `compose-guardrails`.
- Sube el artefacto del reporte Markdown.
- Mantiene `fail-on-findings` como opción opt-in.

Action reutilizable para repositorios externos:
- [.github/actions/compose-guardrails/action.yml](.github/actions/compose-guardrails/action.yml)

Workflow de pruebas manuales de ejemplo:
- Se ejecuta mediante `workflow_dispatch` contra proyectos de ejemplo del repositorio.
- Permite elegir el ref de checkout, el proveedor, la herramienta y el comportamiento de `fail-on-findings`.
- Usa `fake` por defecto, por lo que no requiere secrets.
- Los proveedores reales requieren GitHub Secrets para la API key del proveedor (`OPENAI_API_KEY`, `ANTHROPIC_API_KEY` o `GEMINI_API_KEY`); el modelo por defecto lo selecciona la implementación del proveedor.
- Mantiene el enfoque report-first y no comenta en PRs.

Uso mínimo:

```yaml
- id: compose-guardrails
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

Para opciones completas (`changed-files-only`, `fail-on-findings`, comportamiento del step summary), ver:
- [tools/compose-guardrails/README.md](tools/compose-guardrails/README.md)

## Cómo funciona

1. Elige una herramienta y un path objetivo (`compose` UI code o la raíz de un proyecto KMP).
2. El scanner recopila evidencia local determinista desde archivos fuente.
3. Los prompts se ensamblan a partir de archivos Markdown versionados con reglas.
4. El proveedor de IA seleccionado revisa solo la evidencia recopilada.
5. Los findings se parsean a un esquema estricto y se renderizan en reportes Markdown.

Este diseño mantiene el comportamiento determinista cuando es posible y aísla la lógica específica del proveedor detrás de interfaces.

## Arquitectura

- Los módulos CLI manejan el parseo de argumentos, la orquestación y la salida.
- La lógica core específica de cada herramienta vive en `tools/<tool-name>/`.
- Las abstracciones compartidas viven en `shared/`.
- Los proveedores de IA están detrás de interfaces (`AiClient`), nunca acoplados a la lógica de dominio.
- Los prompts son archivos Markdown, no strings hardcodeados en Kotlin.

Leer:
- [Architecture guide](docs/architecture.md)

## Estructura del repositorio

- `tools/`: CLIs y lógica de análisis específica de cada herramienta.
- `shared/`: módulos reutilizables de cliente IA y reporting.
- `docs/`: arquitectura, checklists y roadmaps.
- `artifacts/`: reportes de ejemplo generados.

## Roadmaps y documentación del proyecto

- [Compose Guardrails Roadmap](docs/roadmaps/compose-guardrails.md)
- [KMP Project Auditor Roadmap](docs/roadmaps/kmp-project-auditor.md)
- [Roadmaps Index](docs/roadmaps/README.md)
- [Release Checklist](docs/release-checklist.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

Comportamiento actual por defecto (orientado a reporte):
- Ejecuta tests.
- Ejecuta `compose-guardrails`.
- Sube artefacto del reporte Markdown.
- Mantiene `fail-on-findings` como opción opt-in.

Action reutilizable para repos externos:
- [.github/actions/compose-guardrails/action.yml](.github/actions/compose-guardrails/action.yml)

Uso mínimo:

```yaml
- id: compose-guardrails
  uses: your-org/mobile-ai-toolkit/.github/actions/compose-guardrails@v0.1.2
  with:
    target: .
    provider: fake
    rule-set: default
    report-path: artifacts/compose-guardrails-report.md
```

Para opciones completas (`changed-files-only`, `fail-on-findings`, comportamiento del step summary), ver:
- [tools/compose-guardrails/README.md](tools/compose-guardrails/README.md)

## Cómo funciona

1. Elige una herramienta y path objetivo (UI en Compose o raíz de proyecto KMP).
2. El scanner recopila evidencia local determinista desde archivos fuente.
3. Los prompts se ensamblan desde archivos Markdown versionados con reglas.
4. El proveedor IA seleccionado revisa únicamente la evidencia recopilada.
5. Los findings se parsean a un esquema estricto y se renderizan como reportes Markdown.

Este diseño mantiene el comportamiento determinista cuando es posible y aísla la lógica específica de proveedores detrás de interfaces.

## Arquitectura

- Los módulos CLI manejan parseo de argumentos, orquestación y salida.
- La lógica core de cada herramienta vive en `tools/<tool-name>/`.
- Las abstracciones compartidas viven en `shared/`.
- Los proveedores de IA están detrás de interfaces (`AiClient`), nunca acoplados a la lógica de dominio.
- Los prompts son archivos Markdown, no strings hardcodeados en Kotlin.

Leer:
- [Architecture guide](docs/architecture.md)

## Estructura del repositorio

- `tools/`: CLIs y lógica de análisis específica de cada herramienta.
- `shared/`: módulos reutilizables de cliente IA y reporting.
- `docs/`: arquitectura, checklists y roadmaps.
- `artifacts/`: reportes de ejemplo generados.

## Roadmaps y documentación del proyecto

- [Compose Guardrails Roadmap](docs/roadmaps/compose-guardrails.md)
- [KMP Project Auditor Roadmap](docs/roadmaps/kmp-project-auditor.md)
- [Roadmaps Index](docs/roadmaps/README.md)
- [Release Checklist](docs/release-checklist.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## Hoja de ruta

1. Mejorar precisión de análisis (mover checks clave de heurísticas de texto a análisis estático más robusto).
2. Expandir soporte de salidas machine-readable (SARIF/JSON) entre herramientas.
3. Profundizar integración con CI para gating de reportes y flujos de equipo.
4. Añadir nuevos analizadores mobile en `tools/` reutilizando la misma arquitectura compartida.

## Limitaciones actuales

- Gran parte de la detección sigue siendo heurística/textual (no análisis completo de AST o compilador).
- Los findings de IA pueden contener falsos positivos/falsos negativos.
- Los formatos de reporte son principalmente Markdown; SARIF/JSON aún no están completos en todas las herramientas.

## Licencia

Este proyecto está licenciado bajo MIT. Ver [LICENSE](LICENSE).
