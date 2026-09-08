# Migración a DARK_BOOK_AI

Fecha: 2026-09-08

La implementación desarrollada originalmente dentro de `menu.discri` fue integrada en
esta carpeta. El código nuevo de Dark Book OS es la aplicación principal y el árbol
Java anterior se conserva para mantener disponibles el scanner, los analizadores y sus
modelos auxiliares.

## Decisiones de seguridad

- La base histórica `database/darkbook.db` se conserva sin cambios. Las bases modulares
  nuevas (`videos.db`, `memory.db`, `analytics.db` y `settings.db`) se crean por separado.
- `.obsidian/`, `userdata/` y `screenshots/` se conservan en su ubicación original.
- El estado previo de Gradle, IntelliJ, el código fuente y los archivos principales está
  respaldado en `legacy/pre-migration-2026-09-08/previous-target/`.
- Los restos del experimento Minecraft Forge de `menu.discri` fueron eliminados por no
  pertenecer a DARK BOOK AI. El proyecto activo utiliza exclusivamente Java 21, Gradle,
  JavaFX y los servicios definidos en la arquitectura oficial.
- `DatabaseManager` y `VideoRepository` incluyen una capa de compatibilidad para el
  scanner anterior, sin mezclar su esquema con el Storage Engine modular.

## Proyecto activo

- Entrada de escritorio: `darkbook.DarkBookLauncher`.
- Entrada sin interfaz: `darkbook.DarkBookHeadless`.
- Frontend: `web/`.
- Pruebas: `gradlew.bat test`.
- Empaquetado para Windows: `gradlew.bat packageDarkBook`.

Los directorios `build/`, `.gradle/` y `.gradle-user/` son artefactos generados y pueden
regenerarse; no contienen código fuente del producto.

## Unificación de arquitectura (2026-09-08)

El árbol Java anterior que se había conservado (segunda clase `main`
`darkbook.DarkBook`, paquetes `core/`, `analysis/`, `collector/`, el bot `TrendScanner`
con `ScrollEngine`/`VideoObserver`/`VideoPlayer`/`WatchTimer`, los detectores de
`intelligence/*` salvo `ContentAnalyzer`, los repositorios SQLite legacy
`DatabaseInitializer`/`AudioRepository`/`HashtagRepository`/`SessionRepository`/
`StatisticsRepository`/`SearchRepository`/`DatabaseBackup`/`DatabaseMigration`/`QueryUtils`,
los subpaquetes sin usar `neural.{training,core,inference,memory}`, `models/VideoData`,
`ui/Dashboard`, `utils/Logger`, `utils/JsonUtils`) estaba **desconectado** de la aplicación
empaquetada y se eliminó (~75 archivos).

- Se quitó la capa de compatibilidad de `DatabaseManager` (`getConnection`/`closeConnection`)
  y `VideoRepository.save(VideoData)`. La app usa solo las bases modulares.
- La capacidad de escaneo real de TikTok se reconstruyó como
  `darkbook.automation.TikTokFeedVideoSource`, que implementa `darkbook.scanner.VideoSource`
  y alimenta el pipeline canónico (`knowledge.KnowledgeBuilder.ingest`). Ver README › «Scanner real».
- `ScannerManager` pasó a un hilo trabajador con reintentos y *backoff*.
- El `.exe`, cuando corre fuera del árbol del proyecto, escribe `config/`, `database/` y
  `logs/` en `%LOCALAPPDATA%\DarkBookAI` (Windows) en lugar del directorio de trabajo.
