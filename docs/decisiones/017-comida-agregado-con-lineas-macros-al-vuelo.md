# 017 — Comida: un agregado con sus líneas, macros siempre al vuelo

Estado: aceptada · 2026-09-30

## Contexto

[[fusion]] define `Comida` (fecha y momento) y `ComidaLinea` (alimento y gramos), y [[modelo-datos]] las lista como dos tablas. Una comida sin líneas no significa nada y una línea sin comida tampoco. Además, [[015-alimento-catalogo-compartido-macros-por-100g]] dejó pendiente proteger el borrado de alimentos usados, y `CLAUDE.md` exige `usuario_id` en toda tabla de datos personales.

## Decisión

- **Un solo agregado.** La API es de `Comida` con sus líneas anidadas: `GET /api/fusion/comida?fecha=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`. No hay endpoints de línea sueltos. El `PUT` **reemplaza** el conjunto de líneas (como el resto de PUT de la API: reemplazo completo). El `DELETE` borra la comida y sus líneas **en cascada por JPA** (`cascade = ALL` + `orphanRemoval` en `ComidaEntity`); las FKs no llevan `ON DELETE CASCADE`, para que un borrado directo en base no se lleve el histórico sin querer.
- **`comida_linea` lleva `usuario_id`**, aunque `modelo-datos.md` no lo listaba: la regla dura 5 no admite excepciones. El servicio lo fija igual que el de la comida (del JWT, nunca del body).
- **Los macros no se guardan nunca.** Kcal, proteínas, carbohidratos y grasas de una línea = `valor_100g * cantidad_g / 100`, escala 2, `HALF_UP`, en código de dominio puro (`Macros`, `ComidaLinea.getMacros()`). El total de la comida es la **suma de las líneas ya redondeadas**, para que el total cuadre siempre con lo que se ve por línea. El detalle devuelve macros por línea y totales; el listado, solo los totales (sin líneas).
- **Límites:** de 1 a 50 líneas; `cantidad_g` > 0 y ≤ 10000 con `DECIMAL(7,2)`; `fecha` no futura (como `Movimiento`). **Sin unique** por `(usuario_id, fecha, momento)`: un día puede tener varios SNACK. Se permite repetir un alimento en varias líneas.
- **El alimento de cada línea debe existir** (404 `AlimentoNotFoundException`); es catálogo compartido, así que no se comprueba propietario.
- **Aislamiento estricto:** filtro por `usuario_id` del JWT; id ajeno = 404, no 403.
- **Listado:** filtros `?fecha=` (día exacto), `?desde=&hasta=` (rango inclusivo) y `?momento=`, combinables. Orden: fecha descendente, momento en orden natural del día (DESAYUNO, COMIDA, CENA, SNACK), id. El orden del momento lo da el `ENUM` de MySQL, que ordena por posición; por eso el orden de declaración del enum de Java y del `ENUM` de `V10` es el del día y no debe alterarse.
- **Protección del catálogo:** `AlimentoService.delete` devuelve 400 (`ValidationException`, como `TituloService` con sus entradas) si el alimento está en alguna línea de **cualquier** usuario, mediante un puerto propio `ComidaLineaRepositoryPort.existsByAlimentoId`.
- **Carga:** con `open-in-view: false`, `ComidaEntity.lineas` es `EAGER` con `SUBSELECT` (una consulta extra para todas las líneas de un listado) y `ComidaLineaEntity.alimento` es `EAGER`. Verificado con listado, detalle, PUT y DELETE contra MySQL real.
- Como es un agregado, `ComidaLinea` no replica los 16 ficheros de la plantilla: comparte controller, servicio, puerto y DTOs de `Comida`; solo tiene su Entity, su mapper, su repositorio y su adaptador de consulta de uso.

## Alternativas descartadas

- **Endpoints de línea sueltos** (`/comida/{id}/linea`). Más granular, pero permite estados intermedios inválidos (comida vacía) y complica el aislamiento y los límites.
- **Guardar los macros por línea o por comida.** Contradice [[fusion]]: corregir un alimento no corregiría el histórico.
- **Total como suma sin redondear y redondeo final.** Puede diferir un céntimo de la suma de lo mostrado por línea; se prefiere que cuadre.
- **`ON DELETE CASCADE` en la FK.** Funciona, pero Hibernate no se entera y el borrado deja de estar en el código; se prefiere JPA.
- **`comida_linea` sin `usuario_id`** (se deduce por la comida). Contra la regla dura 5, y obliga a un join para cualquier consulta por usuario.
- **Unique por día y momento.** Impide dos SNACK el mismo día.

## Consecuencias

- Editar un alimento cambia retroactivamente todos los totales que lo usan (es la intención de [[015-alimento-catalogo-compartido-macros-por-100g]]); un alimento en uso ya no se puede borrar.
- El `PUT` regenera los ids de línea en cada edición: no son estables para el cliente.
- Un mismo `usuario_id` en comida y línea es una invariante del servicio, no de la base de datos.
- Un `momento` inválido en el **body** JSON sale como 500 (el `GlobalExceptionHandler` no captura `HttpMessageNotReadableException`; afecta igual a `tipo` en Movimiento). Por query param sí sale 400. Pendiente arreglar en `shared/`.
- El resumen del día (macros contra objetivo) queda fuera de esta decisión.
