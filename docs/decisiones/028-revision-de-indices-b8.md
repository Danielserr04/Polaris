# 028 — Revisión de índices (B8): solo cambia uno, el de los records de Atlas (V15)

Estado: aceptada · 2026-10-01

## Contexto

B8 incluye "revisión de índices". Hasta ahora los índices se pusieron al crear cada tabla (V1 a V14) por intuición, y [[modelo-datos]] los listaba como "previstos". Nunca se había mirado qué plan elige MySQL con volumen real. El único dato medido era el de [[026-progresion-y-records-por-volumen]] (36.000 series de un usuario, `EXPLAIN` de dos consultas).

La regla de la revisión, fijada con el usuario: **`V15` solo si algo falla**, y "falla" es un full scan o un `filesort` significativo con volumen real que se arregle con un índice razonable. Nada de índices especulativos: cada índice cuesta tiempo en cada escritura y espacio.

## Decisión

Se revisaron con `EXPLAIN` y `EXPLAIN ANALYZE` las consultas frecuentes de todos los módulos, sobre MySQL 8.4.11 con datos de prueba generados (no se commitean). **Una sola consulta fallaba** y se corrige con **`V15__indices_revision.sql`**, que sustituye un índice de `serie_registro` por otro:

```sql
ALTER TABLE `serie_registro`
    ADD INDEX `idx_serie_registro_ejercicio_usuario_peso` (`ejercicio_id`, `usuario_id`, `peso_kg`),
    DROP INDEX `idx_serie_registro_ejercicio_sesion`;
```

Todo lo demás se queda como está, con veredicto OK o "vigilar" (tabla más abajo). La aplicación arranca contra la base ya migrada con `ddl-auto: validate` y la suite completa (596 tests) sigue en verde.

### Cómo se midió

- **Datos.** 8 usuarios; el usuario 1 es el pesado y los demás tienen entre un 25 % y un 50 % de su actividad. Generados con un script de Python y cargados por SQL: `movimiento` 247.000 filas (70.000 del usuario 1), `serie_registro` 345.000 (98.000 del usuario 1, en 3.500 sesiones), `comida_linea` 154.000 (en 44.000 comidas), `entrada` 50.000 (14.000), `alimento` 38.000, `titulo` 29.000, `registro_peso` 14.000, `sesion` 12.000. Fechas de 2019 a septiembre de 2026. Distribuciones sesgadas como las reales (pocas categorías concentran los gastos, cada usuario repite unos 300 alimentos, los pesos suben despacio).
- **SQL real.** Con la aplicación arrancada y `show-sql` activo se llamó a cada endpoint con un JWT real y se copió el SQL que genera Hibernate, también el de las Specifications dinámicas con sus combinaciones típicas de filtros (mes, rango más categoría, rango más tipo, solo categoría, solo momento, sin filtros…) y el de los borrados bloqueados por uso. Los valores de los parámetros son los típicos (usuario 1, septiembre de 2026, un ejercicio con series).
- **Plan y tiempo.** `EXPLAIN` (tipo de acceso, índice, filas estimadas, `Extra`) y `EXPLAIN ANALYZE` (tiempo y filas reales), caché caliente. Las cifras son de un contenedor de pruebas: valen para comparar antes y después, no como tiempos absolutos de producción.

### Consultas, índice usado y veredicto

Tiempos de `EXPLAIN ANALYZE` del usuario 1 con la base cargada. "Filas" son las que MySQL estima leer del índice o la tabla.

| Módulo | Consulta (SQL real resumido) | Índice usado | Plan | Veredicto |
|---|---|---|---|---|
| Odisea | `entrada` listado `where usuario_id=?` (con `and estado=?`) | `idx_entrada_usuario` | `ref`, 24.900 filas (14.000 reales), 15–19 ms | OK (ver "vigilar" 1) |
| Odisea | `entrada` por tipo: `join titulo … where usuario_id=? and tipo=?` (con `estado`) | `idx_entrada_usuario` + PK de `titulo` (el optimizador alterna con recorrer `titulo`) | 56 ms (4.075 filas), 21 ms con `estado` | OK |
| Odisea | `entrada` existe (usuario, título) al importar; existe por título al borrar un `titulo` | `idx_entrada_titulo` | `const`/`ref`, `Using index`, 0,02 ms | OK |
| Odisea | `titulo` por `tipo` | ninguno | `ALL`, 29.000 filas, 18 ms | Vigilar (2) |
| Odisea | `titulo` por texto: `lower(titulo) like '%x%' or lower(titulo_original) like …` | ninguno | `ALL`, 29 ms | Vigilar (2) |
| Odisea | `titulo` por `(fuente_externa, id_externo)` al importar | `uk_titulo_fuente_externa` | `const` | OK |
| Kuiper | `movimiento` de un mes (listado y resumen mensual): `usuario_id=? and fecha between … order by fecha desc, id desc` | `idx_movimiento_usuario_fecha` | `range`, 766 filas, 2 ms, **sin filesort** (`Backward index scan`) | OK |
| Kuiper | `movimiento` rango más tipo | `idx_movimiento_usuario_fecha` | `range`, 12.500 filas, 15 ms | OK |
| Kuiper | `movimiento` rango más categoría, o solo categoría | `idx_movimiento_categoria` | `ref`, 4.900 filas, `filesort`, 8–10 ms | OK (ver "vigilar" 3) |
| Kuiper | `movimiento` sin filtros | `idx_movimiento_usuario_fecha` | `ref`, 70.000 filas, 138 ms | OK (vigilar 4: no pagina) |
| Kuiper | `movimiento`/`presupuesto` por `categoria_id` al borrar una categoría | `idx_movimiento_categoria`, `idx_presupuesto_categoria` | `const`, `Using index`, 0,01–0,04 ms | OK |
| Kuiper | `categoria` listado, y por `(usuario_id, nombre, tipo)` al crear | `uk_categoria_usuario_nombre_tipo` | `ref`/`const`, 0,04 ms | OK |
| Kuiper | `presupuesto` listado por usuario y periodo; por `(usuario, categoría, periodo)` | `uk_presupuesto_usuario_categoria_periodo` | `ref`/`const`, 0,3 ms (el `filesort` es de 12 filas) | OK |
| Fusión | `comida` un día, un rango, rango más momento | `idx_comida_usuario_fecha` | `ref`/`range`, 4–100 filas, `filesort` de ≤ 100 filas, 0,03–0,2 ms | OK |
| Fusión | `comida` solo por momento (sin fechas) | `idx_comida_usuario_fecha` | `ref`, 17.000 filas, 10 ms | Vigilar (3) |
| Fusión | `comida_linea` de las comidas de un día/mes (`SUBSELECT`) | `idx_comida_usuario_fecha` + `idx_comida_linea_comida` + PK de `alimento` | `ref`, 2–9 ms | OK |
| Fusión | `comida_linea` por `alimento_id` al borrar un alimento | `idx_comida_linea_alimento` | `const`, `Using index`, 0,006 ms (en uso y sin uso) | OK |
| Fusión | `alimento` listado (con o sin texto, `lower(nombre/marca) like '%x%'`, `order by nombre`) | ninguno | `ALL`, 38.000 filas, `filesort`, 47 ms | Vigilar (2, 4) |
| Fusión | `alimento` por `(fuente_externa, id_externo)` | `uk_alimento_fuente_externa` | `const` | OK |
| Fusión | `objetivo_nutricional` vigente (`vigente_desde <= ? order by … limit 1`), histórico, existe | `uk_objetivo_nutricional_usuario_vigente` | `range`/`ref` hacia atrás, 0,06–0,3 ms | OK |
| Fusión | resumen diario = `comida` de un día + `comida_linea` + objetivo vigente | los tres de arriba | 3 sentencias, 21 ms HTTP | OK |
| Atlas | `ejercicio` visibles (`usuario_id is null or usuario_id=?`), con grupo, por nombre al crear | `uk_ejercicio_usuario_nombre` (`ref_or_null`), `idx_ejercicio_grupo_muscular` | 175 filas, 0,03–0,35 ms | OK |
| Atlas | `rutina` listado, sus líneas, por `(usuario_id, nombre)` | `uk_rutina_usuario_nombre`, `idx_rutina_ejercicio_rutina` | 0,2–0,9 ms | OK |
| Atlas | `rutina_ejercicio` por `ejercicio_id` al borrar un ejercicio | `idx_rutina_ejercicio_ejercicio` | `const`, `Using index`, 0,007 ms | OK |
| Atlas | `sesion` rango de fechas | `idx_sesion_usuario_fecha` | `range`, 40 filas, 0,07 ms, sin filesort | OK |
| Atlas | `sesion` por rutina; por `rutina_id` al borrar una rutina | `idx_sesion_rutina` | `ref`, 569 filas, `filesort`, 0,8 ms; borrado 0,02 ms | OK |
| Atlas | `sesion` sin filtros | `idx_sesion_usuario_fecha` | `ref`, 3.500 filas, 6,5 ms | OK |
| Atlas | `serie_registro` de las sesiones de un mes (`SUBSELECT`) | `idx_sesion_usuario_fecha` + `idx_serie_registro_sesion` | 1.114 filas, 3 ms | OK |
| Atlas | `serie_registro` por `ejercicio_id` al borrar un ejercicio | `idx_serie_registro_ejercicio_usuario_peso` (antes `…_ejercicio_sesion`) | `const`, `Using index`, 0,04 ms | OK |
| Atlas | **progresión** de un ejercicio (agrupa por sesión) | antes `…_ejercicio_sesion`, ahora `…_ejercicio_usuario_peso` | 4.069 filas leídas → 2.514 (solo las del usuario), 10,5 → 7,8 ms | OK |
| Atlas | **records, consulta 1**: peso máximo por ejercicio (tabla derivada `max(peso_kg) … group by ejercicio_id`) | antes **ninguno** (`ALL` + recorrido del índice con acceso a fila), ahora `…_ejercicio_usuario_peso` | **345.000 filas, 846 ms → 179 filas, 1,6–7,8 ms** | **Arreglado en V15** |
| Atlas | **records, consulta 2**: volumen por ejercicio y sesión (`sum(reps*peso_kg) … group by ejercicio_id, sesion_id`) | ninguno | `ALL`, 345.000 filas (98.000 del usuario), tabla temporal, 202 ms | Vigilar (5) |
| Núcleo | `registro_peso` rango, sin filtros, por `(usuario_id, fecha)` | `uk_registro_peso_usuario_fecha` | `range`/`ref`/`const`, 0,3–9 ms, sin filesort | OK |
| Núcleo | `perfil` por `usuario_id` | `uk_perfil_usuario` | `const` | OK |
| Auth | `usuario` por username, email, `google_id`, id | `uk_usuario_username`, `uk_usuario_email`, `uk_usuario_google_id`, PK | `const` | OK |

**Índices redundantes o sin uso:** ninguno. `sys.schema_redundant_indexes` sale vacía y `sys.schema_unused_indexes` solo lista el de `flyway_schema_history`. Ningún índice es prefijo de otro. Los de claves ajenas (`idx_entrada_titulo`, `idx_movimiento_categoria`, `idx_presupuesto_categoria`, `idx_comida_linea_comida`, `idx_comida_linea_alimento`, `idx_rutina_ejercicio_*`, `idx_sesion_rutina`, `idx_serie_registro_sesion`) hacen falta para la FK y además sirven a las comprobaciones de uso. `idx_ejercicio_grupo_muscular` lo usa el optimizador solo en una tabla de 350 filas: su utilidad es marginal, pero cuesta casi nada y no se toca.

### Antes y después (el único cambio)

Records de Atlas, usuario 1 (98.000 series de 345.000), `EXPLAIN ANALYZE` mejor de 5 ejecuciones:

| | Antes (V14) | Después (V15) |
|---|---|---|
| Consulta 1 (peso máximo por ejercicio) | 727–846 ms. `ALL` sobre `serie_registro` + tabla temporal. La derivada recorría los 345.000 índices `(ejercicio_id, sesion_id)` y leía cada fila para ver `usuario_id` y `peso_kg` (505 ms solo ahí) | **1,6 ms** (7,8 ms en frío). La derivada sale de `Using index for group-by`: 179 entradas del índice, ninguna fila |
| Consulta 2 (volumen por sesión) | 202 ms | 199 ms (sin cambios) |
| `GET /api/atlas/records` de punta a punta | 1.224–1.357 ms | 702–726 ms |
| Un solo usuario con las mismas 98.000 series (base aparte, solo para medir) | 305 ms (consulta 1) | **1,0 ms** |
| Progresión de un ejercicio (consulta 14) | 10,5 ms | 7,8 ms |
| Borrado bloqueado por uso (`existsByEjercicio_Id`) | 0,04 ms | 0,04 ms |

El índice nuevo ocupa 12,5 MB con 345.000 series. Como sustituye a otro, `serie_registro` sigue teniendo los mismos índices secundarios (dos) y no paga más en cada inserción.

## Alternativas descartadas

- **`(usuario_id, ejercicio_id, peso_kg)`**, con `usuario_id` el primero. Es lo natural en un esquema donde todo filtra por usuario, y arreglaba la consulta 1 (846 → 25 ms), pero **empeoraba la consulta 2**: con ese índice disponible el optimizador deja de recorrer la tabla y la hace con lecturas por clave (202 → 282 ms con un solo usuario, 202 → 530 ms con ocho, donde llegó a elegir un plan por `sesion` más lento todavía). Con `ejercicio_id` primero y `usuario_id` segundo el optimizador no lo considera para la consulta 2 y el plan no cambia. La consulta externa de la 1 sigue entrando por los tres valores.
- **`(usuario_id, ejercicio_id, sesion_id)`**, el que [[026-progresion-y-records-por-volumen]] ya discutía. Mejora la progresión (13 → 7 ms) pero la consulta 1 solo baja de 727 a 487 ms y la 2 empeora (260 ms). No cubre `peso_kg`, que es lo que obliga a leer filas.
- **Índice cubriente `(usuario_id, ejercicio_id, sesion_id, reps, peso_kg)`.** Mejora algo las tres (progresión 4 ms, consulta 1 238 ms, consulta 2 162 ms) pero es bastante más grande, no resuelve la consulta 1 y para la 2 el ahorro (40 ms) no compensa.
- **Añadir el índice nuevo y conservar `idx_serie_registro_ejercicio_sesion`.** Seguiría sirviendo a la FK y a la comprobación de uso, pero el nuevo empieza por `ejercicio_id` y ya hace lo mismo; la progresión no necesita `sesion_id` en el índice porque une con `sesion` por clave primaria. Medido: sin el antiguo, el borrado bloqueado (0,04 ms) y la progresión (8 ms) no cambian. Mantenerlo era pagar un índice de varios MB en cada inserción de la tabla que más crece por nada.
- **`entrada (usuario_id, estado)`**, el que [[modelo-datos]] daba por previsto y que V1 nunca creó (solo existe `idx_entrada_usuario`). Probado: `estado=EN_CURSO` baja de 16 a 1,6 ms, `estado=TERMINADO` (la mitad de las entradas) de 17 a 8 ms. Son milisegundos sobre 14.000 entradas de un usuario; el listado tarda segundos por otra causa (ver consecuencias) y un usuario real tendrá cientos. No hay full scan ni `filesort` y se descarta.
- **`movimiento (usuario_id, categoria_id, fecha)`.** Probado: rango más categoría baja de 8,7 a 1,3 ms; solo categoría no cambia (10 ms). `idx_movimiento_categoria` ya localiza las filas y el `filesort` es de 5.000 filas. Descartado por lo mismo.
- **`comida (usuario_id, momento, fecha)`.** El filtro "solo momento, sin fechas" baja de 10,5 a 0,9 ms. No es una consulta que se vaya a hacer sin acotar fechas: la pantalla trabaja por día. Descartado.
- **Índice en `titulo (tipo)` o en `alimento (nombre)`.** `tipo` tiene cuatro valores y una consulta que devuelve el 20 % de la tabla no usaría el índice. `lower(nombre) like '%x%'` no puede usar ningún índice (comodín inicial y función sobre la columna). El `order by nombre` sin filtro sí lo usaría, pero esa consulta devuelve las 38.000 filas, y el problema es la respuesta, no el orden.
- **`FULLTEXT` para la búsqueda de texto.** Es la salida si la búsqueda de `titulo`/`alimento` se vuelve lenta, pero obliga a cambiar las consultas (`MATCH … AGAINST`) y a decidir sobre la collation. Con 30.000–40.000 filas son 30–50 ms: no hace falta.
- **Reescribir la consulta 2 de records** o paginar listados. Es la solución a los puntos "vigilar", pero toca `src/main/java`, que la revisión de índices no puede tocar. Queda como trabajo aparte.

## Consecuencias

**Vigilar** (nada que arreglar con un índice razonable hoy, pero conviene saberlo):

1. **El listado de `entrada` es un N+1 y es lo más lento de la aplicación**, y no es cosa de índices. `EntradaEntity` carga `titulo` con `FetchType.EAGER` y `findAll(spec)` lanza una sentencia por título distinto: **14.001 sentencias y 7,3 s para 14.000 entradas** (`GET /api/odisea/entrada`), 3,9 s con `estado=TERMINADO`, 2,2 s con `tipo=LIBRO`. La consulta de entradas en sí tarda 15 ms. Se arregla con un `join fetch` o `@EntityGraph` en el repositorio y no cuesta nada en la base. Lo mismo, en pequeño, en `movimiento` y `presupuesto` (una sentencia por categoría distinta, 13–17 en total): irrelevante hoy. Para un usuario con unos cientos de entradas ni se nota, pero es la primera cosa que habría que tocar si el listado se queda lento.
2. **Búsquedas de texto sobre `titulo` y `alimento`:** `lower(col) like '%x%'` recorre la tabla entera (29 y 47 ms con 29.000 y 38.000 filas). Crece linealmente. Si el catálogo de alimentos (compartido entre usuarios, alimentado por Open Food Facts) llega a cientos de miles de filas, hay que pasar a `FULLTEXT` o a búsqueda externa; antes que eso, paginar.
3. **Filtros sin su prefijo habitual de fechas** (`comida` solo por momento, `movimiento` solo por categoría): usan el índice por usuario o por categoría y filtran el resto. 10 ms con 17.000 y 5.000 filas. Los índices compuestos candidatos están medidos arriba por si algún día duelen.
4. **Ningún listado pagina.** `movimiento` sin filtros devuelve 70.000 filas (13 MB, 1 s), `alimento` 38.000 (5,7 MB, 1 s), `sesion` 3.500 con sus series (350 KB, 1,2 s). El `EXPLAIN` de cada una es correcto; lo caro es traer y serializar. Está fuera del alcance de los índices.
5. **Records, consulta 2 (202 ms):** lee todas las series del usuario y agrega 24.500 grupos. Ningún índice razonable la mejora de forma estable (ver alternativas) y el endpoint completo queda en unos 700 ms con 98.000 series. Crece linealmente con las series del usuario: con un orden de magnitud más, segundos. La salida, como ya decía [[026-progresion-y-records-por-volumen]], es acotar los records (por rango o por ejercicio) o guardarlos, con una nota nueva si hace falta.

**Otras consecuencias:**

- **Las notas [[025-sesion-agregado-con-series]] y [[026-progresion-y-records-por-volumen]] citan `idx_serie_registro_ejercicio_sesion` y dicen que no hace falta `V15`.** Eran correctas cuando se escribieron: la 026 midió el `ALL` de los records (0,4 s con 36.000 series) y lo dio por aceptable, y una consulta con `usuario_id` primero (la única que consideró) no lo arreglaba. Esta nota las matiza y no se editan. La referencia vigente del índice de `serie_registro` es [[modelo-datos]] y esta nota.
- **El plan de MySQL depende de las estadísticas.** Tras añadir un índice y `ANALYZE TABLE` el optimizador cambió de plan en la consulta 2 de records y en el listado de `entrada` por tipo (la tabla de arriba da el de la base de pruebas). `ANALYZE` es lo primero que probar si una consulta se vuelve lenta de repente. No se fuerzan índices con hints.
- **`V15` se aplica sobre `serie_registro` entera con una reconstrucción de índice** (un `ALTER TABLE … ADD INDEX, DROP INDEX`, en línea en InnoDB). Con 345.000 filas tardó unos segundos en el contenedor de pruebas; con un usuario real, menos. Sin cambios de entidades: `ddl-auto: validate` no mira índices y la aplicación arranca igual.
- **Cualquier índice nuevo para estas tablas debe pasar el mismo criterio** (full scan o `filesort` significativo, medido con volumen) y probarse también en las demás consultas de la tabla, como demuestra el primer descarte. El script de datos y los `EXPLAIN` no se guardan; el procedimiento está en "Cómo se midió".
- **Al revisar queda confirmado el esquema del resto de módulos**: Kuiper, Fusión y Núcleo, de los índices de [[modelo-datos]], usan todos los que se crearon y ninguno sobra. El único "previsto" que no existe (`entrada (usuario_id, estado)`) se descartó con medida.
