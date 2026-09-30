# 024 — Rutina: un agregado con sus líneas, ejercicios visibles y nombre único

Estado: aceptada · 2026-09-30

## Contexto

[[atlas]] define `Rutina` (una plantilla de entrenamiento) y `RutinaEjercicio` (qué ejercicios lleva, en qué orden y con qué objetivo), y [[modelo-datos]] las lista como dos tablas. Una rutina sin ejercicios no sirve de plantilla y una línea sin rutina no significa nada, igual que `Comida` y `ComidaLinea` ([[017-comida-agregado-con-lineas-macros-al-vuelo]]). Además:

- `CLAUDE.md` exige `usuario_id` en toda tabla de datos personales, y `modelo-datos.md` no lo lista en `rutina_ejercicio`.
- [[023-ejercicio-catalogo-y-propios]] dejó pendiente que `RutinaEjercicio` acepte solo ejercicios visibles para el usuario y que un ejercicio usado no se pueda borrar.
- `Sesion` (con su `rutina_id` nullable) aún no existe.

## Decisión

- **Un solo agregado.** La API es de `Rutina` con sus líneas anidadas en `/api/atlas/rutina`: `GET ?activa=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`. No hay endpoints de línea sueltos. El `PUT` **reemplaza** el conjunto de líneas. El `DELETE` borra la rutina y sus líneas **en cascada por JPA** (`cascade = ALL` + `orphanRemoval` en `RutinaEntity`); las FKs no llevan `ON DELETE CASCADE`, como en `V10`. `RutinaEjercicio` comparte controller, servicio, puerto y DTOs de `Rutina`; solo tiene su Entity, su mapper, su repositorio y su adaptador de consulta de uso.
- **`rutina_ejercicio` lleva `usuario_id`**, aunque `modelo-datos.md` no lo listaba: regla dura 5. Lo fija el servicio igual que el de la rutina (del JWT, nunca del body); que coincidan es una invariante del servicio, no de la base.
- **Campos.** `rutina`: `nombre` (`varchar(100)`, obligatorio), `descripcion` (`text`, opcional, hasta 2000 caracteres por la API), `activa` (obligatorio en cada `POST` y `PUT`, sin valor por defecto). Línea: `ejercicio_id`, `orden` (≥ 1), `series_objetivo` (1 a 20, `int`), `reps_objetivo` (`varchar(20)`, texto libre: `"8-12"`, `"5"`, `"AMRAP"`; no se valida el formato). Serie y reps objetivo son obligatorias en cada línea.
- **Varias rutinas pueden estar activas a la vez.** `activa` solo marca cuáles se usan hoy (una persona alterna Push, Pull, Pierna); `?activa=true|false` filtra, sin él salen todas. No hay "la rutina activa" única.
- **Al menos una línea, como mucho 50.** Una rutina vacía no es una plantilla; se rechaza con 400. Es el mismo criterio y el mismo tope que `Comida`.
- **El ejercicio de cada línea debe ser visible para el usuario:** del catálogo o propio suyo (regla de [[023-ejercicio-catalogo-y-propios]]). Si no, **400** con mensaje legible (`El ejercicio N no existe o no esta disponible para ti`). Un ejercicio inexistente y uno propio de otro usuario dan **el mismo mensaje y el mismo código**: es una referencia dentro del cuerpo, no el recurso de la URL, y distinguirlos confirmaría que el id ajeno existe. (Sobre el recurso de la URL sigue valiendo el 404 de siempre.) Se puede repetir un ejercicio en varias líneas.
- **`orden` sin duplicados dentro de la rutina** (400). No hace falta que sea consecutivo (1, 2, 5 vale). El servicio guarda las líneas ordenadas por `orden` y las lecturas las devuelven así (`@OrderBy`).
- **Sin `UNIQUE (rutina_id, orden)` en la base**, a propósito: Hibernate ejecuta los `INSERT` antes que los `DELETE`, así que un `PUT` que reemplaza las líneas chocaría con el unique contra las líneas viejas, aún sin borrar. La regla la aplica solo el servicio.
- **Nombre único por usuario, ignorando mayúsculas y tildes** (409): `RutinaService` lo comprueba (recortando espacios de los extremos) y `UNIQUE (usuario_id, nombre)` con la collation `utf8mb4_unicode_ci` es la red ante dos peticiones simultáneas (409 vía [[020-violacion-unicidad-409-y-errores-http-cliente]]). Aquí el unique sí protege del todo, porque `usuario_id` nunca es `NULL`. Dos usuarios pueden tener una rutina con el mismo nombre. Renombrar a su propio nombre (por ejemplo cambiando solo las mayúsculas) es válido.
- **Aislamiento estricto:** filtro por `usuario_id` del JWT; el id de otro usuario da **404**, no 403 (`GET`, `PUT` y `DELETE`).
- **Protección del ejercicio:** `EjercicioService.delete` devuelve 400 (`ValidationException`) si el ejercicio está en alguna línea de rutina, mediante un puerto propio `RutinaEjercicioRepositoryPort.existsByEjercicioId`, con el mismo patrón que `AlimentoService` y `ComidaLineaRepositoryPort`. La comprobación va después de las de propiedad (404 / 403). Índice `idx_rutina_ejercicio_ejercicio` para esa consulta, y la FK a `ejercicio` es la red de seguridad. Quien hoy lo pide es un ejercicio propio: el del catálogo no se puede borrar de ningún modo.
- **Carga:** con `open-in-view: false`, `RutinaEntity.lineas` es `EAGER` con `SUBSELECT` y `RutinaEjercicioEntity.ejercicio` es `EAGER`, como en Comida. El listado es ligero: id, nombre, descripción, `activa` y `numeroEjercicios`, sin líneas; ordenado por nombre.
- **Borrado con sesiones.** Qué pasa al borrar una `Rutina` que ya tiene `Sesion` asociadas (`sesion.rutina_id`) **se decidirá al crear `Sesion`**, que aún no existe. Hoy el `DELETE` borra sin más; `RutinaService` lo deja anotado.

## Alternativas descartadas

- **Endpoints de línea sueltos** (`/rutina/{id}/ejercicio`). Permiten estados intermedios inválidos (rutina vacía, orden duplicado) y complican el aislamiento y los límites.
- **Permitir rutinas sin líneas** (crear primero, rellenar después). Tolera un borrador, pero abre el estado "rutina vacía" en cada lectura. Si el frontend necesita borradores, se cambia con una nota nueva; `activa = false` ya sirve de aparcadero mientras tanto.
- **404 en vez de 400 para un ejercicio ajeno.** Es coherente con el 404 de las URLs, pero un 404 en un `POST` de rutina confunde: parece que la rutina no existe. El 400 dice qué campo del cuerpo falla.
- **Una única rutina activa** (al activar una, se desactivan las demás). Es una regla de producto que nadie ha pedido y que rompe la rotación semanal.
- **`UNIQUE (rutina_id, orden)`.** Ver arriba: choca con el orden de flush de Hibernate en el `PUT`. Solucionarlo exigiría un `flush` entre borrar e insertar o `ON DELETE CASCADE`, y ninguna de las dos merece la pena.
- **Renumerar `orden` al guardar (1..n).** Oculta la elección del cliente y hace que el `PUT` de la misma lista pueda cambiar valores.
- **`series_objetivo` y `reps_objetivo` opcionales.** Se pueden relajar más adelante sin migración de datos; al revés, no.
- **`reps_objetivo` con un formato validado** (`n` o `n-m`). Impide `AMRAP` o `al fallo`, que son objetivos reales.
- **`rutina_ejercicio` sin `usuario_id`** (se deduce por la rutina). Contra la regla dura 5, y obliga a un join para cualquier consulta por usuario.
- **`ON DELETE CASCADE` en la FK.** Hibernate no se entera y el borrado deja de estar en el código; se prefiere JPA, como en [[017-comida-agregado-con-lineas-macros-al-vuelo]].
- **Solo la comprobación en la FK para el borrado del ejercicio.** Da un 409 genérico de integridad, no un 400 legible.

## Consecuencias

- El `PUT` regenera los ids de línea en cada edición: no son estables para el cliente. Cuando `Sesion` y `SerieRegistro` existan, no deberán apuntar a `rutina_ejercicio.id`; si necesitan el ejercicio, que sea por `ejercicio_id`.
- La regla "el ejercicio es visible para el usuario" se comprueba al guardar la rutina, no después. Un ejercicio propio se puede borrar mientras no esté en una rutina, así que no puede quedar una línea apuntando a un ejercicio que el dueño ya no ve; el catálogo no se borra.
- `docs/modelo-datos.md` sigue sin listar `usuario_id` en `rutina_ejercicio` ni el `UNIQUE (usuario_id, nombre)` de `rutina`; `V13__atlas_rutina.sql` es la fuente.
- Pendiente: el borrado de una rutina con sesiones (al crear `Sesion`), y que un ejercicio con series registradas tampoco se pueda borrar (al crear `SerieRegistro`, ver [[023-ejercicio-catalogo-y-propios]]).
