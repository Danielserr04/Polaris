# 038 — Movimiento: papelera de 30 días y duplicar

Estado: aceptada · 2026-10-02

## Contexto

En [[kuiper]] borrar un movimiento lo eliminaba de la base sin vuelta atrás, y un toque de más en el formulario costaba volver a escribirlo. Lumen tenía además "duplicar" para los gastos que se repiten a mano (el café de cada día) y el borrado de varios a la vez desde el listado.

## Decisión

- **Borrado lógico**: columna `borrado_en DATETIME NULL` en `movimiento` (V19). `DELETE /api/kuiper/movimiento/{id}` ya no borra: pone `borrado_en = ahora`. Nulo = movimiento normal.
- **Las lecturas normales no ven la papelera.** `MovimientoSpecifications` añade siempre `borrado_en IS NULL`, igual que el filtro por usuario, así que el listado y el resumen mensual (que usa `findAll`) la excluyen sin tocarlos. `MovimientoRepositoryPort.findById` solo devuelve movimientos fuera de la papelera: `get`, `update`, `delete` y `duplicar` de uno borrado dan 404. Lo borrado solo se alcanza con métodos que lo dicen en el nombre (`findEnPapeleraById`, `findPapelera`).
- **Papelera** (`MovimientoPapeleraController` + `MovimientoPapeleraService`, en la misma ruta):
  - `GET /papelera`: la del usuario, el borrado más reciente primero, con `borradoEn`.
  - `POST /{id}/restaurar`: pone `borrado_en` a nulo y devuelve el movimiento. 404 si no está en tu papelera.
  - `DELETE /{id}/definitivo`: borrado físico, **solo desde la papelera** (404 si el movimiento está activo: primero se manda a la papelera).
  - `DELETE /papelera`: la vacía entera.
  - `POST /borrar {ids}`: manda varios a la papelera. **Todos o ninguno**: se comprueba cada id y, si alguno no existe, es de otro usuario o ya está en la papelera, 404 y no se mueve nada. Luego una sola sentencia `UPDATE`. Entre 1 y 500 ids.
- **Purga a los 30 días**: `PapeleraJob` (00:15 Europe/Madrid y al arrancar, mismo `@ConditionalOnProperty` que `RecurrenteJob`) llama al caso de uso `PurgarPapeleraMovimientoInterface`, que borra de verdad lo que lleva más de 30 días en la papelera, de todos los usuarios. Un solo `DELETE`; `idx_movimiento_borrado` lo sirve.
- **Categorías**: lo de la papelera **no cuenta como uso**. `existsByCategoriaId` solo mira movimientos activos, así que una categoría cuyos movimientos están todos en la papelera se puede borrar y cambiar de tipo. Al borrarla, `CategoriaService` borra antes de verdad sus movimientos de la papelera (`deleteEnPapeleraByCategoriaId`); si no, la FK lo impediría.
- **Restaurar con la categoría cambiada de tipo** es un 400: mientras estaba en la papelera la categoría pudo pasar de gasto a ingreso, y restaurarlo rompería [[012-movimiento-categoria-mismo-tipo]]. Se queda en la papelera hasta que se borre o caduque.
- **Duplicar**: `POST /{id}/duplicar` con cuerpo opcional `{fecha}` (hoy si no viene; futura = 400). Copia importe, tipo, categoría, concepto y método de pago y devuelve 201 con la ficha. La copia **no** lleva `recurrente = true`: esa marca dice que la generó un Recurrente ([[034-recurrente-genera-movimientos]]), y esta la crea el usuario.
- Frontend: tras borrar sale un aviso con "Deshacer" (restaura); "Duplicar" en el formulario y en cada fila del listado; selección múltiple en Movimientos para borrar varios; y un diálogo "Papelera" con restaurar, borrar definitivamente y vaciar.

## Alternativas descartadas

- **Mover lo borrado a una tabla aparte.** Restaurar sería copiar la fila de vuelta (con su id) y la tabla tendría que seguir el esquema de `movimiento` en cada migración.
- **Filtro global de Hibernate (`@SQLRestriction`)** en `MovimientoEntity`. Escondería la papelera también a las consultas que sí la necesitan (verla, restaurar, purgar) y la regla quedaría implícita, lejos del puerto.
- **Que los movimientos de la papelera bloqueen borrar la categoría.** El usuario vería "tiene movimientos" sin ver ninguno en el listado.
- **Purgar al leer la papelera en vez de con un job.** Dependería de que alguien la abra.

## Consecuencias

- Todo lo que lea `movimiento` sin pasar por `MovimientoSpecifications` o por los métodos del puerto tiene que filtrar `borrado_en IS NULL` a mano.
- Un movimiento borrado sigue ocupando sitio hasta 30 días; con los volúmenes de un usuario es irrelevante.
- El borrado de una categoría no es una transacción: si fallase después de purgar su papelera, se habrían perdido antes de tiempo movimientos que ya estaban borrados.
