# 042 — Calendario y mapa muscular: con lo que ya hay, más una agregación por grupo

Estado: aceptada · 2026-10-02

## Contexto

FitCore tenía un calendario de entrenos, un mapa muscular y calculadoras (1RM, discos, IMC). Atlas tiene las sesiones y las series, pero no sabe cuánto se ha trabajado cada grupo muscular en un periodo.

## Decisión

- **Calendario**: solo frontend, pestaña Calendario de Atlas, con `GET /api/atlas/sesion?desde=&hasta=` del mes. Añade la racha de semanas seguidas con al menos una sesión.
- **Mapa muscular**: un endpoint de solo lectura, `GET /api/atlas/trabajo-muscular?desde=&hasta=`, que agrega en la base series, sesiones distintas y volumen por `grupo_muscular` del ejercicio (mismo patrón que [[026-progresion-y-records-por-volumen]]). El grupo sale tal cual está escrito; el frontend lo reparte entre los músculos del dibujo por palabras clave (pecho, espalda, cuádriceps…). Los polígonos del dibujo son los de FitCore (react-body-highlighter, MIT).
- **Calculadoras** de 1RM (media de Epley y Brzycki, hasta 12 reps) y discos por lado: solo frontend, pestaña Calculadoras.
- **IMC** y cintura/altura: frontend, pestaña Cuerpo, con la altura de `GET /api/nucleo/perfil` y el último peso. Núcleo sigue sin interpretar.

## Alternativas descartadas

- **Pedir el detalle de cada sesión del mes para contar series por grupo.** Una petición por sesión; la agregación en la base es una consulta.
- **Convertir `grupo_muscular` en un enum.** Rompe los ejercicios ya escritos a mano y es un cambio de esquema que nadie ha pedido.
- **Calculadoras en el backend.** Son fórmulas sin datos guardados: no aportan nada en el servidor.

## Consecuencias

- Un grupo escrito de forma rara ("tren superior") no se pinta en el mapa, pero sí sale en la lista de grupos debajo.
- La pestaña Cuerpo de Atlas llama a `/api/nucleo/...` directamente, como ya hace Inicio con el peso.
