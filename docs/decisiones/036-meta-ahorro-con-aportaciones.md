# 036 — Meta de ahorro: lo ahorrado es la suma de sus aportaciones

Estado: aceptada · 2026-10-02

## Contexto

Lumen tenía metas de ahorro (objetivo, fecha límite, color, icono) con un `current_amount` que cada aportación sumaba. No había historial ni forma de retirar dinero, y un total guardado aparte se puede desincronizar de lo que de verdad se aportó. En [[kuiper]] no había nada parecido.

## Decisión

- Nueva entidad **`MetaAhorro`** (tabla `meta_ahorro`, V18): nombre, `importe_objetivo` `DECIMAL(10,2)` > 0, `fecha_limite` opcional, color, icono y `creada_en`. **Nombre único por usuario** (409, como [[011-categoria-nombre-unico-por-tipo]]; con la collation ignora mayúsculas y tildes).
- **Lo ahorrado no es una columna.** Tabla hija **`aportacion_meta`** (id, `usuario_id`, `meta_id` con FK `ON DELETE CASCADE`, fecha, `importe` `DECIMAL(10,2)` con signo, nota). Positivo aporta, negativo retira. `importeActual` es `SUM(importe)` y lo calcula el adaptador al leer: una consulta para una meta, una agrupada (`GROUP BY meta_id`) para el listado.
- **El total nunca baja de 0.** `MetaAhorroService` rechaza con 400 una retirada mayor que lo ahorrado y también **borrar una aportación** si sin ella el total quedaría negativo (borrar un ingreso que después se retiró). Importe 0 y fecha futura también son 400. Sin fecha, hoy.
- Las aportaciones **no son movimientos**: apartar dinero no es gastarlo, así que no tocan el resumen mensual ni los presupuestos.
- Endpoints: CRUD en `/api/kuiper/meta` (filtro `?completada=`), `POST /meta/{id}/aportacion` (devuelve la meta ya actualizada, 201), `GET /meta/{id}/aportacion` (historial, de la más reciente a la más antigua) y `DELETE /meta/{id}/aportacion/{aportacionId}`. Ids de otro usuario o de otra meta: 404.
- **Derivados en los DTO**, no en la base: `importeActual`, `porcentaje` (un decimal, truncado: 99,99 % no se ve como 100; puede pasar de 100), `restante` (nunca negativo), `completada` (`actual >= objetivo`), `diasRestantes` y `ahorroMensualNecesario`.
- **Ahorro mensual necesario** = lo que falta / meses que quedan, redondeado al céntimo hacia arriba. Los meses cuentan el mes en curso aunque esté empezado (del 2 de octubre al 31 de diciembre son 3). Con la fecha ya vencida o de hoy, es todo lo que falta. Nulo sin fecha o con la meta completada.
- Listado: primero las que vencen antes, las que no tienen fecha al final, y a igual fecha por nombre.

## Alternativas descartadas

- **Una columna `importe_actual` que cada aportación incrementa** (lo que hacía Lumen). Más simple de leer, pero sin historial, sin poder deshacer una aportación concreta y con riesgo de que el total no cuadre con lo aportado.
- **Columna y tabla a la vez** (total cacheado más historial). Dos fuentes de verdad para un volumen que la suma resuelve sin esfuerzo con el índice `(meta_id, fecha)`.
- **Registrar cada aportación como un `Movimiento` de gasto.** Inflaría los gastos del mes con dinero que no se ha gastado.
- **Rechazar retiradas o permitir saldo negativo.** Retirar es real (imprevistos) y un saldo negativo no significa nada.

## Consecuencias

- La comprobación del saldo no bloquea filas: dos retiradas simultáneas podrían dejar el total por debajo de 0. Es una app de un usuario y se acepta; si hiciera falta, bastaría un bloqueo de la fila de la meta.
- Bajar el objetivo por debajo de lo ahorrado solo marca la meta como completada; no se toca el historial.
- Borrar una meta borra su historial sin preguntar en el backend; el frontend pide confirmación.
