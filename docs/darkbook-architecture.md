# Arquitectura implementada de DARK BOOK AI

## Flujo principal

```text
DarkBookApplication (JavaFX WebView)
  -> LocalApiServer
  -> ScannerManager / services
  -> KnowledgeBuilder
  -> SQLite + Markdown/Obsidian + JSONL
  -> EmbeddingEngine + MemoryRepository + KnowledgeGraphEngine
  -> TrendPredictionEngine + ReasoningEngine
  -> Dark Book OS
```

`DarkBookRuntime` es la raíz de composición y mantiene el orden de arranque. Los módulos no conocen la interfaz. El frontend no contiene JDBC ni abre archivos del sistema.

## Persistencia

| Recurso | Responsabilidad |
|---|---|
| `database/videos.db` | videos canónicos |
| `database/memory.db` | memoria vectorial, nodos y relaciones |
| `database/analytics.db` | predicciones y métricas |
| `database/settings.db` | settings estructurados y registro futuro de modelos |
| `datasets/raw/videos.jsonl` | ejemplos append-only |
| `knowledge/` | conocimiento Markdown por dominio |
| `obsidian/` | vault legible por Obsidian |
| `models/` | modelos y checkpoints, nunca BLOBs SQLite |
| `logs/` | eventos diarios separados por módulo |

## Extensibilidad

`VideoSource` separa el scanner de cada plataforma. Un nuevo origen implementa esa interfaz. Los motores se coordinan con `DarkBookEventBus`. Los plugins v1 son manifests declarativos y no ejecutan código hasta que exista un modelo de permisos y firma.

## Seguridad

La API escucha por defecto solo en loopback. Los paths de Vision/Voice se normalizan y deben permanecer dentro del directorio de la aplicación. Playwright acepta únicamente HTTPS bajo `tiktok.com`. El terminal web ejecuta comandos internos predefinidos, nunca comandos del sistema operativo.

