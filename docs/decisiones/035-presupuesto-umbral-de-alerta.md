# 035 — Presupuesto: umbral de alerta y estado frente al gasto

Estado: aceptada · 2026-10-02

## Contexto

Lumen avisaba cuando una categoría se acercaba a su límite. En [[kuiper]] el resumen mensual solo daba `limiteMensual` y `restante`: el frontend tenía que decidir por su cuenta cuándo algo "va mal", y el presupuesto `ANUAL` se guardaba sin que ningún endpoint lo comparase con el gasto ([[013-presupuesto-solo-gastos-uno-por-periodo]]).

## Decisión

- **Nueva columna `porcentaje_alerta`** en `presupuesto` (V17): `int NOT NULL DEFAULT 80`, con `CHECK` de 1 a 100. Los presupuestos que ya existían quedan en 80.
- En la API es **opcional**: `porcentajeAlerta` ausente o `null` se guarda como 80, también en un `PUT` (es un reemplazo completo). Fuera de 1..100 es **400** (lo filtra el DTO y, por si acaso, el servicio). Viaja en el detalle y en el listado.
- **Estado** (`EstadoPresupuesto`), calculado al vuelo, nunca guardado:
  - `SIN_PRESUPUESTO`: la categoría no tiene límite en ese periodo.
  - `EXCEDIDO`: lo gastado es **mayor** que el límite.
  - `AVISO`: lo gastado llega al umbral (`gastado * 100 >= límite * porcentajeAlerta`) sin pasarse. Gastar justo el límite es aviso, no exceso.
  - `OK`: el resto.
- La comparación con el umbral se hace **sin redondear**. El `porcentaje` que se devuelve (`gastado / límite * 100`, un decimal, `HALF_UP`) es solo para enseñar: un 79,96 % sale como `80.0` pero sigue `OK`.
- **Resumen mensual** (`GET /api/kuiper/resumen`): cada fila suma `porcentaje`, `porcentajeAlerta` y `estado`; el resumen suma `presupuestoTotal` (los límites mensuales) y cuántas categorías hay en aviso y excedidas.
- **Resumen anual** (`GET /api/kuiper/resumen/anual?anio=2026`, por defecto el año actual): una fila por presupuesto `ANUAL` con lo gastado en esa categoría del 1 de enero al 31 de diciembre, `restante`, `porcentaje`, `porcentajeAlerta` y `estado`, de la más consumida a la menos. Se agrega en Java, como el mensual ([[014-resumen-mensual-agregado-en-servicio]]); sin presupuestos anuales no se piden los movimientos.

## Alternativas descartadas

- **Umbral global por usuario.** Una categoría fija (alquiler) y otra variable (ocio) no merecen el mismo aviso.
- **Calcular el estado en el frontend.** Inicio y Kuiper lo repetirían, y la regla del borde (umbral exacto, límite exacto) acabaría distinta en cada sitio.
- **Varios umbrales** (p. ej. 50 % y 80 %). Más configuración para poco; un aviso y el exceso bastan.
- **`tinyint` en la columna.** Con `ddl-auto: validate` no encaja con el `Integer` de la Entity sin forzar el tipo; un `int` ocupa más, pero hay una fila por categoría y periodo.

## Consecuencias

- El resumen mensual cambia de forma (campos nuevos); el frontend los usa para colorear las barras y avisar.
- El presupuesto anual por fin se compara con el gasto, pero solo en su propio endpoint: el mensual sigue ignorándolo.
- No hay notificaciones: el aviso se ve al abrir Kuiper. Si algún día se notifica, el estado ya está calculado en el dominio.
