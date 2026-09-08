# Estado de implementación

## Funcional

- arranque de escritorio híbrido y API local;
- configuración, migraciones, logs y eventos;
- scanner demo y recolección de metadatos con Playwright;
- persistencia completa del pipeline de texto;
- embeddings deterministas, búsqueda coseno, grafo y razonamiento con evidencia;
- predicción base de viralidad y entrenador de centroides en Python;
- interfaz modular con actualización periódica.

## Preparado, pero dependiente de componentes externos

- Vision Engine: requiere seleccionar e instalar OCR/modelo visual;
- Voice Engine: requiere seleccionar e instalar transcriptor/modelo emocional;
- métricas reales GPU/VRAM/temperatura: necesitan un adaptador específico del fabricante;
- scraping continuo de un feed personalizado: necesita una sesión autorizada y mantenimiento de selectores conforme a las reglas de la plataforma;
- modelos neuronales de producción: necesitan datos suficientes, evaluación, versionado y criterios de promoción.

Estos estados se muestran como `READY FOR PROVIDER`, no como `ONLINE`, para no presentar stubs como capacidades terminadas.

