# 014 — Resumen mensual: se calcula al vuelo, en el servicio

Estado: aceptada · 2026-09-30

## Contexto

[[kuiper]] anticipa `GET /api/kuiper/resumen?periodo=` con "balance del mes, gasto por categoría". Falta fijar la forma de la respuesta, cómo se calcula y qué pasa con los presupuestos, que pueden ser mensuales o anuales ([[013-presupuesto-solo-gastos-uno-por-periodo]]).

## Decisión

- `GET /api/kuiper/resumen?periodo=2026-09` (formato `yyyy-MM`). Sin `periodo`, el **mes actual**. Un valor inválido da **400**. Un mes sin datos devuelve ceros, no 404.
- Respuesta: `periodo`, `ingresos`, `gastos`, `balance` (ingresos menos gastos, puede ser negativo) y `gastoPorCategoria`.
- Cada fila de `gastoPorCategoria` trae la categoría (id, nombre, color, icono), `gastado`, `limiteMensual` y `restante`. `restante` es negativo si se ha excedido. Sin presupuesto, `limiteMensual` y `restante` van a `null`.
- Aparecen las categorías **con gasto en el mes o con presupuesto mensual** (aunque no hayan gastado nada), de mayor a menor gasto y por nombre en los empates. Los ingresos cuentan en `ingresos` pero no salen por categoría.
- **Solo se compara con presupuestos `MENSUAL`.** Los `ANUAL` no aparecen en este endpoint.
- Se calcula en `ResumenService` **sumando en Java** los movimientos del mes (rango inclusivo del día 1 al último), con `BigDecimal` y escala 2. No hay migración ni tabla nueva: los totales nunca se guardan.
- Es solo lectura y no es una entidad, así que no sigue los 16 ficheros de la plantilla: `GetResumenMensualInterface`, `ResumenService`, `ResumenController`, dos modelos, tres DTOs y un mapper.

## Alternativas descartadas

- **`SUM`/`GROUP BY` en JPQL o SQL.** Es lo eficiente, pero añade consultas a medida y un puerto nuevo que probar contra MySQL. Un mes de un usuario son decenas o pocos cientos de filas y el índice `(usuario_id, fecha)` ya las acota. Si algún día pesa, cambia solo `ResumenService`.
- **Incluir el presupuesto anual** con lo gastado en el año. Añade una segunda agregación y decidir a qué mes se refiere; queda para cuando haya una pantalla que lo pida.
- **404 para un mes sin movimientos.** Obliga al cliente a tratar como error algo normal.
- **Tomar el mes actual solo en el cliente.** Cada cliente lo calcularía distinto (zona horaria); mejor un único criterio en el servidor.

## Consecuencias

- El presupuesto anual no tiene todavía ningún endpoint que lo compare con el gasto: si se quiere verlo, es un resumen anual aparte.
- El mes actual lo decide el servidor (`YearMonth.now()` en el controlador, con la zona horaria del servidor).
- El mensaje de error de un `periodo` inválido es el técnico de Spring: correcto como 400, feo para mostrar. Arreglarlo es cosa del manejador global de `shared/`.
