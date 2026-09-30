# 013 — Presupuesto: solo categorías de gasto, uno por categoría y periodo

Estado: aceptada · 2026-09-30

## Contexto

[[kuiper]] define `Presupuesto` como "límite por categoría y periodo" (`MENSUAL` o `ANUAL`), sin fijar sobre qué categorías vale, si puede haber varios para la misma, ni qué protege al borrar la categoría.

## Decisión

- **Solo categorías de `GASTO`.** Un límite de gasto sobre una categoría de ingresos no tiene sentido. Si no, **400**.
- **Uno por usuario, categoría y periodo** (`UNIQUE(usuario_id, categoria_id, periodo)`). Se puede tener uno `MENSUAL` y otro `ANUAL` para "Comida", pero no dos mensuales. Duplicado, al crear o al mover con `PUT`, **409**.
- La categoría debe ser **del usuario**. Si no existe o es de otro, **404**.
- `importe_limite` mayor que 0, con `DECIMAL(10,2)` (hasta 8 enteros y 2 decimales).
- El presupuesto **solo guarda el límite**. Cuánto se lleva gastado lo calcula el resumen a partir de los movimientos: nunca se guarda el total.
- Una categoría **con presupuestos no se borra ni cambia de tipo** (**400**), igual que con movimientos ([[012-movimiento-categoria-mismo-tipo]]).
- Listado ordenado por nombre de categoría, con filtros opcionales `?periodo=&categoriaId=`. Trae nombre, color e icono de la categoría.
- `PUT` es un reemplazo completo. Ruta `/api/kuiper/presupuesto`, CRUD completo.

## Alternativas descartadas

- **Presupuesto global** (sin categoría). Útil, pero es otra entidad: no encaja con `categoria_id` como FK obligatoria de [[modelo-datos]]. Si hace falta, se añade después.
- **Permitir presupuestos sobre ingresos** como "objetivo de ingreso". Mezcla dos conceptos con una sola tabla; si se quiere, merece su propia decisión.
- **Varios presupuestos por categoría y periodo** (p. ej. por rango de fechas). El resumen mensual no sabría cuál aplicar.
- **Borrado en cascada** de presupuestos al borrar la categoría. Pierde datos sin avisar.

## Consecuencias

- El endpoint de resumen mensual compara `importe_limite` con la suma de movimientos de esa categoría y periodo; el `MENSUAL` compara contra el mes pedido y el `ANUAL` contra el año.
- Para cambiar una categoría de gasto a ingreso hay que borrar antes sus presupuestos (y mover sus movimientos).
- Cambiar los valores del enum de periodo exige una migración nueva, como en V2.
