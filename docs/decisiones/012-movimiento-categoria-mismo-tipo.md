# 012 — Movimiento: la categoría es del usuario y de su mismo tipo

Estado: aceptada · 2026-09-30

## Contexto

[[kuiper]] define `Movimiento` (fecha, importe, tipo, categoría, concepto) con `importe` siempre positivo y el signo en `tipo`. Cada movimiento apunta a una `Categoria`, que también tiene `tipo`. Si no se relacionan, un gasto podría colgar de una categoría de ingresos y los resúmenes por categoría no cuadrarían.

## Decisión

- El `tipo` del movimiento **debe coincidir** con el de su categoría. Si no, **400**.
- La categoría debe ser **del usuario**. Si no existe o es de otro, **404** (no se distingue, como con los ids ajenos en el resto de la API).
- Ambas comprobaciones viven en `MovimientoService`, no en la base de datos: MySQL no expresa una regla entre dos tablas sin triggers.
- Una categoría **con movimientos no se borra ni cambia de tipo**: **400**, como `TituloService` con sus entradas en Odisea. Sin esto quedarían movimientos huérfanos o de tipo distinto al de su categoría.
- `importe` mayor que 0, con `DECIMAL(10,2)` (hasta 8 enteros y 2 decimales). `fecha` no puede ser futura.
- `concepto` (255) y `metodo_pago` (50, texto libre) opcionales; `recurrente` es `false` por defecto.
- Listado con filtros opcionales `?desde=&hasta=&categoriaId=&tipo=` (rango inclusivo), del más reciente al más antiguo. La lista trae nombre, color e icono de la categoría.
- `PUT` es un reemplazo completo: lo que no se manda (concepto, método de pago) se borra.
- Ruta `/api/kuiper/movimiento`, CRUD completo.

## Alternativas descartadas

- **Sin relación entre tipos.** Más simple, pero permite datos incoherentes que el resumen mensual tendría que arreglar después.
- **Quitar `tipo` de `Movimiento` y derivarlo de la categoría.** Un JOIN en cada consulta y el `importe` sin signo explícito; va contra la decisión de [[kuiper]] de que el enum sea explícito.
- **Borrado de categoría en cascada.** Borrar una categoría se llevaría por delante el histórico de gastos.
- **409 para "categoría en uso".** Semánticamente encaja, pero `DuplicateResourceException` significa "ya existe" y no hay una excepción de conflicto; se sigue el precedente de Odisea (400 con `ValidationException`). Corrige lo apuntado en [[011-categoria-nombre-unico-por-tipo]], que anticipaba 409.

## Consecuencias

- **Pendiente con `Presupuesto`:** la misma protección de borrado y cambio de tipo para categorías con presupuestos.
- Para cambiar el tipo de una categoría en uso hay que crear otra y mover los movimientos a mano.
- El resumen mensual podrá agrupar por categoría sin comprobar tipos.
