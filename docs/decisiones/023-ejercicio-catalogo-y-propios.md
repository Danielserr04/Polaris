# 023 — Ejercicio: catálogo compartido y ejercicios propios en la misma tabla

Estado: aceptada · 2026-09-30

## Contexto

[[atlas]] define `Ejercicio` como "catálogo, con opción de crear los tuyos", y [[modelo-datos]] la lista con `es_propio` y sin `usuario_id`. Chocan dos reglas: `CLAUDE.md` pide `usuario_id` en toda tabla de datos personales, y el catálogo (cuántos ejercicios de pecho existen) no es personal, como `alimento` ([[015-alimento-catalogo-compartido-macros-por-100g]]). Un ejercicio propio, en cambio, sí lo es. `Alimento` no lo resolvió: ahí cualquiera edita cualquier ficha, y aquí no puede ser así porque un ejercicio propio es de alguien.

## Decisión

- **Una sola tabla con `usuario_id` NULLABLE.** `NULL` es un ejercicio del catálogo: visible para todos, solo lectura. Con valor es un ejercicio propio de ese usuario. La regla dura 5 se cumple porque toda fila personal lleva `usuario_id`.
- **Sin columna `es_propio`.** Es `usuario_id IS NOT NULL`; guardarla aparte permitiría filas contradictorias. La API sí devuelve `esPropio` (derivado) y nunca `usuarioId`.
- **Visibilidad:** un usuario ve el catálogo más lo suyo. Los propios de otro usuario no existen para él: `GET`, `PUT` y `DELETE` dan **404**, no 403, para no confirmar que el id existe (igual que `Categoria`).
- **El catálogo se ve pero no se toca:** `PUT` y `DELETE` sobre un ejercicio con `usuario_id` nulo dan **403**. Aquí el 403 no filtra nada, porque el ejercicio ya es visible para todos.
- **La creación por la API es siempre propia:** el `usuarioId` sale del JWT, y un `usuarioId` o `esPropio` en el cuerpo se ignora. El catálogo no se crea por la API.
- **Nombre único entre lo que el usuario ve:** no se puede crear ni renombrar a un nombre que ya exista en el catálogo o entre los propios del mismo usuario (**409**), ignorando mayúsculas y tildes (collation `utf8mb4_unicode_ci`). Dos usuarios sí pueden tener un propio con el mismo nombre. Lo aplica `EjercicioService`; el `UNIQUE(usuario_id, nombre)` de la tabla solo cubre los propios, y además hace de red ante dos peticiones simultáneas (409 vía [[020-violacion-unicidad-409-y-errores-http-cliente]]).
- **`grupo_muscular` y `equipamiento` son texto libre** (`varchar(50)` y `varchar(100)`), como en el esquema. `grupo_muscular` es obligatorio; `equipamiento` opcional (las flexiones no llevan). `?grupoMuscular=` es una igualdad que ignora mayúsculas y tildes por la collation, y `?q=` busca por trozo de nombre, como en `Alimento`. Listado ordenado por nombre.
- **Borrado:** solo se borra un ejercicio propio. Cuando exista `SerieRegistro`, uno con series registradas se bloqueará con 400, como `AlimentoService` con sus comidas (anotado en `EjercicioService`).
- Ruta `/api/atlas/ejercicio`, CRUD completo. Sin catálogo inicial en esta entidad.

## Alternativas descartadas

- **Dos tablas** (`ejercicio_catalogo` y `ejercicio_propio`). Cada FK futura (`rutina_ejercicio`, `serie_registro`) tendría que apuntar a una de las dos o llevar un tipo, y el listado sería una unión. Con una tabla, una FK basta.
- **`usuario_id` obligatorio y catálogo copiado a cada usuario.** Repite el catálogo por usuario y las correcciones no llegan a nadie; es lo que se descartó para `alimento`.
- **Mantener `es_propio` como columna.** Es redundante con `usuario_id` y puede contradecirlo.
- **Un usuario reservado (id 0) como dueño del catálogo.** Obliga a una fila falsa en `usuario` y a que todo filtro sepa de ella; `NULL` ya significa "de nadie".
- **`grupo_muscular` como `ENUM`.** Da un filtro cerrado, pero fija una lista de grupos, que es una decisión de producto, y añadir uno exige una migración. Se puede pasar a enum después si el frontend lo pide.
- **Permitir que un propio repita el nombre de uno del catálogo.** La lista del usuario mostraría dos ejercicios indistinguibles.
- **403 también para el propio de otro usuario.** Revelaría que el id existe.

## Consecuencias

- **El `UNIQUE` de MySQL no protege el catálogo:** con `usuario_id` `NULL` los NULL no colisionan. Un catálogo que se cargue por SQL o por una importación futura tiene que evitar duplicados por su cuenta, y añadir a mano un ejercicio al catálogo con el mismo nombre que un propio existente deja a ese usuario con dos. Si se importa un catálogo, habrá que decidir qué hacer con esos choques.
- Al no tener texto cerrado, "Pecho", "pecho" y "Pectoral" son grupos distintos. Ignora mayúsculas y tildes, pero no sinónimos.
- **Pendiente con `RutinaEjercicio` y `SerieRegistro`:** deberán aceptar solo ejercicios visibles para el usuario (catálogo o suyos), y un ejercicio propio con series no se borrará.
- Un usuario no puede corregir un ejercicio del catálogo; solo crear uno propio. Un panel de administración del catálogo queda fuera de alcance.
- Si algún día hay varios usuarios y un catálogo mantenido por alguien, "quién puede editar el catálogo" será decisión nueva.
