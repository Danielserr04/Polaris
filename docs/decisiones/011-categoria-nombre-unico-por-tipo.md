# 011 — Categoria: nombre único por usuario y tipo

Estado: aceptada · 2026-09-30

## Contexto

[[kuiper]] define `Categoria` (nombre, color, icono, tipo) sin fijar si los nombres pueden repetirse, cómo se validan color e icono, ni qué pasa al borrar. Las categorías cuelgan de movimientos y presupuestos, que aún no existen.

## Decisión

- `UNIQUE(usuario_id, nombre, tipo)`: se puede tener "Otros" como `INGRESO` y como `GASTO`, pero no dos "Comida" de `GASTO`. El duplicado devuelve **409**, tanto al crear como al renombrar (`PUT`) a un nombre ocupado por otra categoría.
- La comparación ignora mayúsculas y tildes (collation `utf8mb4_unicode_ci`): "Comida" y "comida" son la misma.
- `tipo` usa el enum `TipoMovimiento` (`INGRESO`, `GASTO`), que `Movimiento` reutilizará: los valores son los mismos en las dos tablas.
- `color`: opcional, hex `#RRGGBB` validado con regex (`varchar(7)`). `icono`: texto libre opcional, hasta 50 caracteres; es el nombre del icono en el frontend.
- Listado ordenado por nombre, con filtro `?tipo=`.
- **Sin categorías por defecto**: cada usuario empieza vacío.
- Ruta `/api/kuiper/categoria`, CRUD completo.

## Alternativas descartadas

- **Nombre único solo por usuario.** Obliga a inventar "Otros ingresos" y "Otros gastos"; el `tipo` ya distingue.
- **Sin unique.** Deja crear dos "Comida" y reparte los movimientos entre ambas sin que se note.
- **Categorías semilla** (Comida, Transporte…). Es una decisión de producto aparte; se puede añadir después sin tocar el esquema.
- **Enum de colores o de iconos.** Rígido para algo que es puramente estético y cambia con el diseño.

## Consecuencias

- **Borrado protegido:** con `Movimiento` (ver [[012-movimiento-categoria-mismo-tipo]]) una categoría con movimientos no se borra ni cambia de tipo, y devuelve 400 y no 409 como se anticipó aquí. Falta lo mismo con `Presupuesto`.
- Cambiar un valor de `tipo` exige una migración nueva, como en V2.
- No se revisó `lumen-app` antes de empezar (no accesible desde el entorno de trabajo). Los campos salen de [[modelo-datos]].
