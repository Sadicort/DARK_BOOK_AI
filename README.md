# DARK BOOK AI / DARK BOOK OS

DARK BOOK AI es una aplicación de escritorio modular para recopilar conocimiento, persistirlo, entrenar modelos locales y explorarlo desde una interfaz web embebida. Java 21 controla el ciclo de vida; Dark Book OS solo consume una API local; SQLite conserva datos estructurados; Obsidian recibe Markdown; Python genera artefactos de entrenamiento.

## Estado actual

La implementación incluye:

- ventana JavaFX con boot sequence y Dark Book OS;
- API local uniforme, enlazada a `127.0.0.1`;
- cuatro bases SQLite y migraciones idempotentes;
- scanner con tres modos: `demo` offline verificable, `tiktok` que navega el feed *Para Ti* en un navegador real, y `playwright` para listas explícitas de URLs públicas de TikTok;
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

`config/scanner.json` → `mode` acepta tres valores (también editable desde la página Scanner o desde Settings › Scanner):

| Modo | Qué hace |
|---|---|
| `demo` | Fuente offline determinista; valida el pipeline completo sin cuenta ni red. Es el valor inicial. |
| `tiktok` | Abre el feed *Para Ti* en un navegador real, mira cada video un intervalo humano (`watchSecondsMin`–`watchSecondsMax`), extrae metadatos públicos y los envía al pipeline canónico. Omite anuncios y duplicados; ante un CAPTCHA espera `captchaPauseSeconds` y reintenta. |
| `playwright` | Procesa solo la lista fija de URLs públicas de `seedUrls` (sin feed, sin scroll). |

Ajustes del navegador en `config/playwright.json`:

- `channel` (`"chrome"` por defecto): usa el Chrome instalado en el sistema. Si no existe, cae a Chromium.
- `autoInstallBrowser` (`true`): si falta el Chromium de Playwright, lo instala en un proceso hijo la primera vez (necesita red esa vez).
- `headless` (`false` por defecto para `tiktok`): el feed en headless dispara detección/CAPTCHA con frecuencia.
- `userDataDir` (`"userdata"`): perfil persistente. El modo `tiktok` **no inicia sesión por sí mismo**; ejecuta una vez con `headless:false`, inicia sesión a mano en esa ventana y el perfil queda guardado para las siguientes ejecuciones.

La integración no evita CAPTCHA ni evade limitaciones del sitio. Úsala solo sobre cuentas y contenido para los que tengas autorización.

`ScannerManager` es resiliente: un fallo puntual de la fuente se reintenta con *backoff* exponencial y el scanner solo se detiene tras varios errores consecutivos; el último error se muestra en la página Scanner.

## Documentación

- [Arquitectura implementada](docs/darkbook-architecture.md)
- [API local](docs/darkbook-api.md)
- [Arquitectura del frontend](docs/darkbook-frontend.md)
- [Estado y límites](docs/darkbook-status.md)
