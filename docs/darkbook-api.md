# API local de Dark Book OS

Toda respuesta utiliza `{ status, message, data, timestamp }`.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/status` | salud y versiones de motores |
| GET | `/api/dashboard` | snapshot agregado |
| GET | `/api/events` | eventos recientes |
| GET | `/api/videos` | videos recientes |
| GET/POST | `/api/scanner`, `/api/scanner/{start,pause,resume,stop}` | control del scanner |
| GET | `/api/memory/search?q=` | recuperación vectorial |
| GET | `/api/graph` | nodos y relaciones |
| GET | `/api/datasets`, `/api/models`, `/api/plugins` | inventarios |
| GET/POST | `/api/training`, `/api/training/{start,cancel}` | ciclo Python |
| POST | `/api/reasoning` | razonamiento con evidencia |
| POST | `/api/inference` | clasificación con el modelo local entrenado |
| GET/PUT | `/api/settings`, `/api/settings/{nombre}` | configuración JSON |
| POST | `/api/ingest` | importación de un video canónico |

Los límites de lista y cuerpo se validan en Java. No existe un endpoint SQL genérico.
