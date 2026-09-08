\# DARK BOOK AI v1.0 — ESPECIFICACIÓN MAESTRA PARA CLAUDE (Frontend + IA + Arquitectura)



\## Documento Oficial para Claude Code



\*\*Proyecto:\*\* DARK BOOK AI



\*\*Nombre de la interfaz:\*\* Dark Book OS



\*\*Lenguaje principal:\*\* Java 21



\*\*IDE:\*\* IntelliJ IDEA Ultimate



\*\*Frontend:\*\* HTML5 + CSS3 + JavaScript



\*\*Backend:\*\* Java + Playwright + SQLite



\*\*Motor IA:\*\* Python (entrenamiento) + Java (inferencia)



\*\*Base de conocimiento:\*\* Obsidian Vault



\*\*Rol de Claude:\*\* Arquitecto principal y desarrollador del proyecto. Debes actuar como un ingeniero senior responsable de construir y mantener toda la arquitectura de DARK BOOK AI respetando estrictamente este documento.



\---



\# CONTEXTO DEL PROYECTO



DARK BOOK AI es una plataforma de inteligencia artificial desarrollada completamente desde cero. No es un chatbot ni una aplicación web convencional. Es un ecosistema de IA compuesto por varios motores especializados que aprenden automáticamente de contenido de TikTok y almacenan conocimiento permanente.



El proyecto tiene dos partes principales:



1\. \*\*Dark Book OS\*\*: interfaz visual futurista que funciona dentro del ejecutable.

2\. \*\*Dark Book AI\*\*: cerebro de inteligencia artificial con memoria, embeddings, razonamiento y aprendizaje continuo.



Todo debe funcionar localmente dentro de `DarkBookAI.exe`.



La interfaz web es interna. Nunca se abre un navegador.



\---



\# OBJETIVO PRINCIPAL



Construir una IA especializada que pueda:



\* Analizar TikTok automáticamente durante horas.

\* Extraer información estructurada de cada video.

\* Construir conocimiento permanente.

\* Aprender patrones de contenido viral.

\* Crear embeddings propios.

\* Mantener memoria permanente.

\* Relacionar conceptos.

\* Entrenar modelos propios.

\* Predecir tendencias.

\* Mostrar absolutamente todo dentro de Dark Book OS.



La IA debe evolucionar continuamente.



\---



\# TECNOLOGÍAS OBLIGATORIAS



\## Backend



\* Java 21.

\* Gradle.

\* Playwright Java.

\* SQLite JDBC.

\* Jackson/Gson para JSON.

\* API local Java para comunicar frontend y backend.



\## Frontend



\* HTML5.

\* CSS3 puro.

\* JavaScript ES Modules.

\* Chart.js para gráficas.

\* SVG y Canvas para visualizaciones.

\* Sin Bootstrap.

\* Sin Tailwind.

\* Sin frameworks como React, Vue o Angular.



\## IA



\* Python únicamente para entrenamiento.

\* Java para cargar modelos e inferencia.



\## Memoria



\* SQLite.

\* Obsidian Vault.

\* Archivos Markdown.

\* JSONL.

\* Embeddings.



\---



\# ARQUITECTURA GENERAL



```

DarkBookAI.exe



&#x20;       │



&#x20;       ▼



Dark Book OS (Frontend HTML/CSS/JS)



&#x20;       │



&#x20;       ▼



API Local Java



&#x20;       │



&#x20;┌──────┼───────────┐



&#x20;│      │           │



&#x20;▼      ▼           ▼



SQLite Obsidian  Python Trainer



&#x20;│        │           │



&#x20;└────────┼───────────┘



&#x20;         ▼



&#x20;    Neural Core AI

```



\### Reglas



\* Java controla todo.

\* HTML nunca accede a SQLite.

\* Python nunca controla la interfaz.

\* Todo pasa por servicios Java.



\---



\# ESTRUCTURA OFICIAL DEL PROYECTO



```

DarkBookAI/



assets/

config/

database/

datasets/

knowledge/

logs/

models/

obsidian/

plugins/



src/

&#x20;   ai/

&#x20;   automation/

&#x20;   dashboard/

&#x20;   database/

&#x20;   events/

&#x20;   knowledge/

&#x20;   memory/

&#x20;   neural/

&#x20;   scanner/

&#x20;   services/

&#x20;   utils/

&#x20;   vision/

&#x20;   voice/

&#x20;   prediction/

&#x20;   learning/



web/

&#x20;   index.html



&#x20;   pages/

&#x20;   components/

&#x20;   layouts/

&#x20;   styles/

&#x20;   scripts/

&#x20;   assets/

&#x20;   icons/

&#x20;   animations/

```



Claude nunca debe romper esta estructura.



\---



\# PRIORIDAD ACTUAL DEL PROYECTO



La prioridad absoluta es reconstruir completamente la carpeta \*\*web/\*\*.



La versión actual tiene errores de:



\* estructura,

\* navegación,

\* CSS,

\* JavaScript,

\* layout,

\* componentes duplicados,

\* sidebar,

\* scroll,

\* comunicación con Java.



Claude debe reemplazar completamente el frontend existente.



\---



\# DARK BOOK OS — ESPECIFICACIÓN DEL FRONTEND



\## Concepto



Dark Book OS es un sistema operativo ficticio para controlar la IA.



Debe sentirse como:



\* Centro de comando.

\* Laboratorio de IA.

\* Sistema operativo futurista.

\* Estilo cyberpunk elegante.



No debe parecer una página web administrativa.



\---



\## Identidad Visual



\### Tema oficial



Dark Book Purple.



\### Paleta



\* Fondo negro absoluto.

\* Paneles gris oscuro.

\* Morado brillante como color principal.

\* Azul eléctrico para información.

\* Verde para éxito.

\* Rojo para errores.

\* Naranja para entrenamiento y advertencias.



\### Tipografía



\* Orbitron.

\* Inter.

\* JetBrains Mono.



\### Diseño



\* Transparencias suaves.

\* Glow únicamente en elementos activos.

\* Bordes redondeados.

\* Sombras discretas.

\* Mucho espacio entre elementos.



\---



\# LAYOUT GLOBAL



Dark Book OS siempre mantiene la misma estructura.



\## Cinco regiones permanentes



\### Navbar Superior



Siempre visible.



Contiene:



\* Hora.

\* Fecha.

\* Estado IA.

\* Estado Scanner.

\* Estado SQLite.

\* Estado Obsidian.

\* Estado Python.

\* Buscador Global.

\* Notificaciones.

\* Configuración rápida.



\### Sidebar Izquierdo



Siempre visible.



Módulos:



\* Dashboard.

\* Scanner.

\* Intelligence Center.

\* Memory Center.

\* Knowledge Graph.

\* Dataset Manager.

\* Model Manager.

\* Analytics.

\* Terminal.

\* Settings.



Debe ser colapsable.



\### Área Principal



Contenido dinámico.



Cada módulo es una página independiente.



\### Panel Derecho



Información contextual.



Estado del elemento seleccionado.



\### Barra Inferior



Estado del sistema.



CPU.



RAM.



GPU.



Playwright.



SQLite.



Versión.



\---



\# PÁGINAS OBLIGATORIAS



\## Dashboard



Centro principal.



Widgets:



\* Estado IA.

\* Videos analizados.

\* Memoria IA.

\* Recursos del sistema.

\* Tendencias.

\* Actividad reciente.

\* Scanner en vivo.

\* Entrenamiento IA.



Todo actualizado en tiempo real.



\---



\## Scanner



Control total de Playwright.



Debe incluir:



\* Botón iniciar.

\* Pausar.

\* Reanudar.

\* Detener.



Configuraciones.



Panel del video actual.



Logs.



Estado del navegador.



Estadísticas del análisis.



\---



\## Intelligence Center



Centro neuronal.



Paneles:



\* Neural Core.

\* Trainer.

\* Embeddings.

\* Vocabulary.

\* Checkpoints.

\* Logs.



Debe mostrar Accuracy y Loss.



\---



\## Memory Center



Visualización completa de memoria.



Secciones:



\* Episódica.

\* Semántica.

\* Temporal.

\* Timeline.

\* Buscador semántico.



\---



\## Knowledge Graph



Mapa interactivo.



Debe visualizar:



\* Conceptos.

\* Hashtags.

\* Personas.

\* Temas.

\* Audios.

\* Videos.



Cada nodo abre información lateral.



\---



\## Dataset Manager



Administración de datasets.



Funciones:



\* Crear.

\* Exportar.

\* Importar.

\* Buscar.

\* Filtrar.

\* Vista previa.



\---



\## Model Manager



Gestión de modelos.



\* Modelo activo.

\* Historial.

\* Checkpoints.

\* Comparación.

\* Exportación.



\---



\## Analytics Center



Gráficas.



Estadísticas.



Predicciones.



Horarios virales.



Categorías.



Hashtags.



Audios.



\---



\## Terminal



Consola integrada.



Historial.



Logs.



Comandos internos.



Estado módulos.



\---



\## Settings



Configuración completa del proyecto.



Categorías:



\* IA.

\* Scanner.

\* Obsidian.

\* SQLite.

\* Python.

\* Interfaz.

\* Performance.

\* Plugins.



\---



\# COMPONENTES REUTILIZABLES



Claude debe construir una biblioteca completa.



\## Componentes obligatorios



\* Sidebar.

\* Navbar.

\* Footer.

\* Cards.

\* Metric Cards.

\* Buttons.

\* Icon Buttons.

\* Search Bar.

\* Dropdown.

\* Tabs.

\* Timeline.

\* Progress Bars.

\* Circular Progress.

\* Tables.

\* Charts.

\* Toast Notifications.

\* Modal.

\* Tooltip.

\* Loader.

\* Skeleton Loader.

\* Status Badge.

\* Accordion.

\* Context Menu.



Todos reutilizables.



Nunca duplicar componentes.



\---



\# SISTEMA CSS



Claude debe crear un \*\*Design System\*\*.



Archivos obligatorios.



\* variables.css

\* global.css

\* typography.css

\* layout.css

\* animations.css



Cada módulo tiene su CSS específico.



Nunca escribir estilos repetidos.



Todos los colores deben venir de variables CSS.



\---



\# SISTEMA JAVASCRIPT



JavaScript debe ser completamente modular.



\## Archivos principales



\* app.js

\* router.js

\* api.js

\* events.js

\* websocket.js



Cada página tiene su script independiente.



Nunca usar un archivo gigante.



\---



\# COMUNICACIÓN FRONTEND ↔ JAVA



Toda la interfaz consume una API local.



Nunca acceder directamente a SQLite.



Flujo:



Frontend → API Java → Servicios → SQLite / Obsidian / IA.



Todas las respuestas se manejan mediante JSON.



La interfaz debe actualizar widgets automáticamente cuando recibe nuevos eventos.



\---



\# EVENTOS EN TIEMPO REAL



Claude debe crear un Event Manager.



Eventos importantes.



\* ScannerStarted.

\* ScannerPaused.

\* ScannerStopped.

\* VideoCollected.

\* DatasetUpdated.

\* MemoryStored.

\* EmbeddingCreated.

\* TrainingStarted.

\* TrainingFinished.

\* ModelLoaded.

\* TrendDetected.

\* NotificationCreated.



Cada evento actualiza el Dashboard.



\---



\# SCANNER TIKTOK



El Scanner utiliza Playwright.



\## Función



Analizar TikTok automáticamente.



Información obtenida.



\* Descripción.

\* Usuario.

\* Audio.

\* Hashtags.

\* Likes.

\* Comentarios.

\* Compartidos.

\* Categoría IA.

\* Emoción IA.

\* Fecha.



Debe soportar modo de análisis durante horas.



Genera reportes automáticamente.



\---



\# KNOWLEDGE ENGINE



Transforma videos en conocimiento.



Produce:



\* Markdown.

\* SQLite.

\* Conceptos.

\* Relaciones.

\* Categorías.

\* Reportes.



Toda nota se guarda dentro del Vault de Obsidian.



\---



\# MEMORY ENGINE



Tres memorias.



\### Episódica



Eventos.



\### Semántica



Conceptos relacionados.



\### Temporal



Contexto actual.



Debe existir búsqueda semántica.



Timeline de recuerdos.



Persistencia permanente.



\---



\# NEURAL CORE



Motores principales.



\* Dataset Builder.

\* Vocabulary Builder.

\* Tokenizer.

\* Embedding Engine.

\* Trainer Engine.

\* Inference Engine.

\* Checkpoint Manager.



El entrenamiento ocurre en Python.



Java administra modelos.



\---



\# KNOWLEDGE GRAPH



Construye relaciones entre:



\* Videos.

\* Personas.

\* Temas.

\* Audios.

\* Hashtags.

\* Conceptos.



Debe existir visualización interactiva.



\---



\# VISION ENGINE



Analiza imágenes.



Funciones.



\* OCR.

\* Objetos.

\* Texto.

\* Escenas.

\* Miniaturas.



Genera conocimiento adicional.



\---



\# VOICE ENGINE



Analiza audio.



Funciones.



\* Transcripción.

\* Emociones.

\* Palabras clave.

\* Idioma.



Guarda resultados en memoria.



\---



\# TREND PREDICTION ENGINE



Calcula viralidad.



Debe producir un \*\*Trend Score\*\* utilizando información histórica del proyecto.



Analiza crecimiento de categorías, hashtags y audios.



\---



\# AUTO LEARNING ENGINE



Aprendizaje continuo.



Cada video nuevo puede:



\* actualizar vocabulario,

\* crear nuevos embeddings,

\* agregar memoria,

\* actualizar datasets,

\* sugerir reentrenamiento.



\---



\# SQLITE



Bases principales.



\* videos.db

\* memory.db

\* analytics.db

\* settings.db

\* models.db



Cada una tiene una responsabilidad única.



\---



\# OBSIDIAN VAULT



Organización oficial.



```

Knowledge/



Videos/

Topics/

Hashtags/

People/

Audios/

Reports/

Concepts/

```



Toda memoria documental vive aquí.



\---



\# DATASETS



```

datasets/



raw/

processed/

embeddings/

vocabulary/

exports/

```



Claude debe mantener separados datos originales y datos procesados.



\---



\# MODELOS



```

models/



neural/

embeddings/

checkpoints/

tokenizer/

classifier/

```



Cada entrenamiento genera checkpoints versionados.



\---



\# SISTEMA DE LOGS



Logs separados por módulo.



\* scanner.log

\* neural.log

\* memory.log

\* database.log

\* frontend.log

\* system.log



La Terminal debe poder leerlos.



\---



\# PERFORMANCE MONITOR



Dark Book OS debe mostrar en tiempo real.



\* CPU.

\* RAM.

\* GPU.

\* VRAM.

\* Disco.

\* Red.

\* SQLite.

\* Java.

\* Python.

\* Playwright.



\---



\# REGLAS DE DESARROLLO PARA CLAUDE



\## Arquitectura



No modificar la estructura de carpetas.



\## Modularidad



Un módulo = una responsabilidad.



\## Frontend



Construir una interfaz completamente nueva.



No reutilizar la versión antigua.



\## Código



Escribir código limpio, reutilizable y documentado.



\## CSS



Usar variables globales.



No duplicar estilos.



\## JavaScript



Separar lógica por módulos.



\## Comunicación



Toda interacción pasa por la API Java.



\## Rendimiento



La interfaz debe ser ligera y fluida incluso mientras Playwright analiza videos y la IA entrena modelos.



\## Escalabilidad



Preparar el proyecto para futuras redes sociales (YouTube, Instagram, Reddit, X) y un sistema de plugins.



\---



\# MISIÓN DE CLAUDE



Claude debe comportarse como el arquitecto principal de DARK BOOK AI.



Su prioridad inmediata es \*\*reconstruir completamente Dark Book OS\*\*, creando un frontend profesional, modular y escalable que sirva como centro de control para todos los motores de inteligencia artificial del proyecto. Debe corregir todos los errores existentes, mantener una comunicación limpia con Java y preparar la interfaz para integrar los módulos del Neural Core, Memory Engine, Knowledge Graph, Vision, Voice, Trend Prediction y Auto Learning en futuras etapas del desarrollo.



