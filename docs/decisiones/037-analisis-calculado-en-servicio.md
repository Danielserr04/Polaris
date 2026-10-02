# 037 — Análisis de Kuiper: se calcula al vuelo, en el servicio

Estado: aceptada · 2026-10-02

## Contexto

Lumen tenía una pantalla de analítica (evolución, gasto por categoría, comercios frecuentes, insights). En [[kuiper]] solo existía el resumen mensual ([[014-resumen-mensual-agregado-en-servicio]]). Falta decidir qué se calcula, dónde y con qué reglas, sobre los movimientos, presupuestos y recurrentes ([[034-recurrente-genera-movimientos]]) que ya existen.

## Decisión

- **Sin tabla ni migración.** `AnalisisService` implementa cinco casos de uso de solo lectura y reutiliza `MovimientoRepositoryPort`, `PresupuestoRepositoryPort` y `RecurrenteRepositoryPort`. Agrega en Java, igual que el resumen: cada endpoint hace **una sola consulta de movimientos** por rango de fechas, acotada por el índice `(usuario_id, fecha)`.
- Endpoints bajo `/api/kuiper/analisis`:
  - `GET /evolucion?meses=6&hasta=yyyy-MM`: ingresos, gastos, balance y **tasa de ahorro** (`balance / ingresos × 100`, `null` sin ingresos) por mes. `meses` de 1 a 24 (400 fuera). Los meses vacíos salen a cero.
  - `GET /categorias?desde&hasta`: gasto por categoría, su **peso** sobre el total y la comparación con el **periodo anterior equivalente**: si el rango son meses enteros, los mismos meses justo antes (septiembre contra agosto entero); si no, los mismos días justo antes. Salen también las categorías que solo gastaron en el anterior. `variacion` es `null` si antes no hubo gasto.
  - `GET /comercios?desde&hasta&limite=10`: gastos agrupados por **concepto normalizado** (sin espacios sobrantes, mayúsculas ni tildes), con total, veces y ticket medio. Se muestra la forma escrita más repetida. Sin concepto no cuenta. `limite` de 1 a 50.
  - `GET /insights?periodo=yyyy-MM`: lista de `{tipo, severidad, titulo, texto}`, con `severidad` `AVISO`, `BIEN` o `INFO`, ordenada en ese orden. Los textos van redactados en español en el servidor.
  - `GET /proyeccion?periodo=yyyy-MM`: gasto previsto a fin de mes.
- **Insights**, cada uno solo si hay datos para decir algo:
  - Gasto del mes frente a la **media de los 3 meses anteriores que tengan movimientos** (un usuario nuevo no se compara con meses vacíos). En el mes en curso la media se prorratea a los días transcurridos. ±10 % es el umbral entre `INFO` y `AVISO`/`BIEN`.
  - Categoría que **más sube** respecto al mes anterior (solo si el anterior tiene movimientos).
  - **Presupuestos** `MENSUAL` superados o, si no hay ninguno, el más ajustado desde el 80 %.
  - **Tasa de ahorro**: `BIEN` desde el 20 %, `AVISO` si es negativa.
  - **Mayor gasto** individual del mes.
  - **Cargos recurrentes de los próximos 7 días** con su total (solo en el mes en curso).
  - **Día de la semana** con más gasto, con al menos 5 gastos en el mes.
- **Proyección** = gasto actual + ritmo diario del gasto **no recurrente** × días que faltan + **cargos recurrentes pendientes** del mes (desde `proxima_fecha`, respetando las cuotas que queden). Los recurrentes ya cobrados no se extrapolan: son fijos. Un mes pasado (`CERRADO`) devuelve el gasto real; uno futuro (`FUTURO`) solo los recurrentes. Incluye la suma de presupuestos `MENSUAL` para compararla.
- **Hoy lo decide el controlador** (`LocalDate.now()`, zona del servidor, [[019-zona-horaria-europe-madrid]]) y se pasa al servicio, que así se prueba con fechas fijas.
- No es una entidad: no sigue la plantilla de 23 ficheros. Son cinco interfaces de caso de uso, un servicio, un controlador, modelos de dominio, tres DTOs de entrada, seis de salida y cinco mappers MapStruct.

## Alternativas descartadas

- **`SUM`/`GROUP BY` en SQL.** Mismo argumento que en 014: el volumen de un usuario es pequeño y añadiría puertos y consultas a medida que probar contra MySQL.
- **Guardar los insights o los agregados.** Se quedarían viejos al editar un movimiento; recalcular es barato.
- **Generar los textos en el frontend.** Duplica las reglas y los umbrales; el servidor ya tiene los datos.
- **Extrapolar todo el gasto al ritmo diario.** Un alquiler cobrado el día 1 dispararía la proyección; por eso se separa lo recurrente.
- **Agrupar comercios por texto exacto.** "Mercadona" y "MERCADONA " serían dos comercios.

## Consecuencias

- Los textos de los insights están en español y con formato español (`1.234,50 €`). Si algún día hay otro idioma, el cliente tendrá que redactarlos a partir de `tipo` y datos estructurados.
- La proyección depende de que los movimientos generados por recurrentes lleven la marca `recurrente`; uno marcado a mano también se trata como fijo.
- Si el análisis llega a pesar (muchos años de datos en `/categorias` o `/comercios`), el único sitio que cambia es `AnalisisService`.
