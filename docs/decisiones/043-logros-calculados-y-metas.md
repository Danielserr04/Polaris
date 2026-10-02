# 043 — Logros calculados al vuelo y metas de entreno en Atlas

Estado: aceptada · 2026-10-02

## Contexto

FitCore tenía metas personales (`goals`) y logros (`achievements`, con una tabla de definiciones y otra de conseguidos que se rellenaba al consultar). Polaris no tiene ninguna de las dos. Kuiper ya tiene metas de ahorro, que son otra cosa.

## Decisión

**Logros** (`GET /api/atlas/logros`):
- Catálogo fijo en código (`LogroService.CATALOGO`): sesiones (1, 10, 50, 100), ejercicios distintos (10), toneladas de volumen (10, 100, 1000), racha de semanas seguidas (4, 12) y pesajes (1, 30).
- **Sin tabla.** El progreso se calcula al pedirlo con tres consultas agregadas (`EstadisticasEntrenoPort`) y el puerto de peso que Atlas ya tiene ([[022-peso-corporal-desde-fusion-y-atlas]]).
- La racha de un logro es la **más larga** de la historia, no la actual: un logro conseguido no se pierde por dejar de entrenar.

**Metas** (`/api/atlas/meta`, entidad `MetaEntreno`, tabla `meta_entreno`, `V24`):
- Tres tipos: `PESO_CORPORAL` (kg, bajando o subiendo), `MARCA_EJERCICIO` (kg en una serie de un ejercicio) y `SESIONES_SEMANA` (1 a 7).
- Se guarda la definición y el **punto de partida** (`valor_inicial`: último peso o récord del ejercicio el día de crearla). El valor actual, el progreso (0-100) y si está conseguida se calculan al leer.
- El sentido del peso corporal lo da el punto de partida: objetivo por debajo es bajar.
- Si un `PUT` cambia lo que se mide (tipo o ejercicio), el punto de partida vuelve a ser el de hoy.
- Plazo opcional, nunca pasado. Borrar un ejercicio propio borra sus metas (`ON DELETE CASCADE`).

## Alternativas descartadas

- **Tabla de logros conseguidos con fecha, como FitCore.** Da la fecha del logro, pero obliga a "evaluar" en cada lectura (FitCore escribía en un `GET`) o a engancharse a cada alta de sesión. Sin tabla, borrar una sesión mal apuntada corrige el logro solo.
- **Logros en un módulo propio.** Necesitaría leer de Atlas, y un módulo no llama a otro. Todo lo que miden es de Atlas o llega por su puerto de peso.
- **Metas genéricas con valor actual a mano** (`current_value` de FitCore). Hay que acordarse de actualizarlas; las de Polaris se miden solas.
- **Guardar el progreso.** Se quedaría viejo con cada peso o sesión nueva.

## Consecuencias

- No hay "conseguido el día X": si se quiere, hace falta otra decisión y una tabla.
- Los logros de recetas de FitCore no están: las recetas son de [[fusion]].
