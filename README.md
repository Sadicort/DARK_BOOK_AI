# DARK BOOK AI / DARK BOOK OS

DARK BOOK AI es una aplicación de escritorio modular para recopilar conocimiento, persistirlo, entrenar modelos locales y explorarlo desde una interfaz web embebida. Java 21 controla el ciclo de vida; Dark Book OS solo consume una API local; SQLite conserva datos estructurados; Obsidian recibe Markdown; Python genera artefactos de entrenamiento.

## Estado actual

La implementación incluye:

- ventana JavaFX con boot sequence y Dark Book OS;
- API local uniforme, enlazada a `127.0.0.1`;
- cuatro bases SQLite y migraciones idempotentes;
- scanner offline verificable y fuente Playwright para listas explícitas de URLs públicas de TikTok;
- dataset JSONL, notas Markdown, Obsidian, embeddings, memoria semántica y knowledge graph;
- predicción explicable, razonamiento respaldado por memoria y auto-learning por eventos;
- entrenador Python local que genera un clasificador por centroides;
- Dashboard, Scanner, Intelligence, Memory, Graph, Datasets, Models, Analytics, Terminal y Settings;
- contratos para Vision y Voice. Estos motores validan entradas, pero necesitan modelos Python para OCR, transcripción y clasificación reales.

El proyecto no utiliza Minecraft Forge, ForgeGradle ni código de mods. Todo el backend se desarrolla con Java 21 y Gradle.

## Requisitos

- JDK 21;
- Python 3 para el entrenamiento;
- conexión de red solo para resolver dependencias y para el modo Playwright;
- Windows, Linux o macOS para desarrollo; el empaquetado `packageDarkBook` genera una app-image de Windows.

## Ejecutar

```powershell
$env:JAVA_HOME = 'C:\ruta\a\jdk-21'
.\gradlew.bat run
```

La API sin ventana se inicia con `./gradlew.bat runHeadless`. El Dashboard queda en `http://127.0.0.1:17321`, pero la ejecución normal lo abre dentro de la ventana de la aplicación.

Para validar y empaquetar:

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat packageDarkBook
```

## Scanner real

El modo inicial es `demo` para probar todo el pipeline offline. Para usar Playwright, cambia `config/scanner.json` a `"mode":"playwright"` y añade URLs públicas en `seedUrls`. Instala Chromium una vez con la CLI de Playwright. La integración no inicia sesión, no evita CAPTCHA y no evade limitaciones del sitio.

## Documentación

- [Arquitectura implementada](docs/darkbook-architecture.md)
- [API local](docs/darkbook-api.md)
- [Arquitectura del frontend](docs/darkbook-frontend.md)
- [Estado y límites](docs/darkbook-status.md)
