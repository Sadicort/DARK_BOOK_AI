# Dark Book OS Web Interface

La interfaz se compone bajo demanda desde `layouts/`, `components/` y `pages/`. `index.html` solo contiene el arranque, los estilos compartidos y el punto de montaje.

## Ciclo de una página

1. `router.js` cancela el controlador de la página anterior.
2. Carga el fragmento HTML y su módulo JavaScript homónimo.
3. El módulo registra interacciones usando el `AbortSignal` de la ruta.
4. Al cambiar de ruta, todos esos listeners se eliminan automáticamente.
5. `websocket.js` mantiene un único ciclo de sincronización y distribuye snapshots mediante `events.js`.

Este diseño evita intervalos duplicados, componentes montados dos veces y fugas de listeners.

## Design System

`variables.css` es la única fuente para colores, sombras, espaciado, radios, tamaños, tipografías, velocidades y layout. Los archivos de módulo no contienen colores directos. El tema base es Dark Book Purple y el control de tema permite cambiar localmente a Blue Core Midnight.

## Estados y errores

Todas las rutas tienen loading, empty y error state. `error-boundary.js` captura errores globales y promesas rechazadas y los envía a `/api/frontend/log`; Java los convierte en eventos persistentes del módulo `frontend`.

## Tiempo real

La API Java actual no publica un servidor WebSocket. `websocket.js` implementa un puente central mediante polling no solapado cada tres segundos. El contrato permite reemplazar el transporte por WebSocket sin modificar las páginas.

## Operaciones protegidas

Las acciones sin endpoint Java —como borrar datasets o duplicar modelos— nunca simulan éxito. La interfaz explica que necesitan un servicio backend dedicado. El terminal acepta exclusivamente comandos internos incluidos en una lista segura.

