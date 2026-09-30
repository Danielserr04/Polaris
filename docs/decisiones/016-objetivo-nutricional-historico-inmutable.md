# 016 — ObjetivoNutricional: histórico inmutable, uno por fecha de vigencia

Estado: aceptada · 2026-09-30

## Contexto

[[fusion]] define `ObjetivoNutricional` (kcal y macros objetivo con `vigente_desde`) y fija que **los objetivos no se sobrescriben**: cada cambio es una fila nueva, para saber contra qué objetivo se comía en marzo. Faltaba decidir qué operaciones existen, qué es "el vigente", qué pasa con fechas repetidas o futuras y qué se valida.

## Decisión

- **Solo se crean objetivos, nunca se editan ni se borran.** Hay `POST`, `GET` (vigente) y un listado histórico; no hay `PUT` ni `DELETE`. Un error de tecleo se corrige creando otro objetivo con otra fecha. Mismo razonamiento que [[009-perfil-unico-por-usuario]]: aquí la regla del modelo es que el pasado no cambia.
- **Vigente en una fecha = el de mayor `vigente_desde` menor o igual a esa fecha**, del usuario. Sin `?fecha=` se usa hoy. Si no hay ninguno (no tiene, o todos empiezan después), **404**.
- **`UNIQUE(usuario_id, vigente_desde)`**: dos objetivos con la misma fecha no tendrían orden. Al crear con una fecha ya usada, **409** (no se sustituye, para no reintroducir la sobrescritura por la puerta de atrás).
- **`vigente_desde` es obligatoria y puede ser futura** (un objetivo "desde el lunes"). El vigente de hoy sigue siendo el anterior hasta que llegue la fecha.
- **Validaciones (400):** `kcal_diarias` entre 500 y 10000; `proteinas_obj`, `carbos_obj` y `grasas_obj` entre 0 y 1000 gramos. Todos enteros (`int`). **No se exige** que las kcal cuadren con los macros (4/4/9): es una decisión del usuario y hay quien fija solo unos macros orientativos.
- **Aislamiento:** `usuario_id` sale del JWT, nunca del body. No hay acceso por id, así que no existe el caso "id ajeno": cada consulta va filtrada por el usuario y el 404 solo significa "sin objetivo en esa fecha".
- **Rutas:** `GET /api/fusion/objetivo?fecha=` (el vigente), `POST /api/fusion/objetivo` (201) y `GET /api/fusion/objetivo/historico` (todas las filas, la más reciente primero). El histórico va en subruta para que `GET /api/fusion/objetivo` siga siendo "el vigente", como prevé [[fusion]].
- **Ficheros de la plantilla que se omiten, con razón:** `UpdateObjetivoNutricionalInterface` y `DeleteObjetivoNutricionalInterface` (no hay update ni delete); `ObjetivoNutricionalFilter`, `FilterMapper` y `Specifications` (no hay filtros dinámicos, bastan consultas derivadas de Spring Data); `ObjetivoNutricionalListDto` y su mapper (la ficha son cinco enteros y una fecha, no hay nada pesado que aligerar: el histórico devuelve `FormDto`). `ObjetivoNutricionalFilterListDto` se conserva solo para recibir `?fecha=` con binding a 400 si el formato es malo, como `ResumenFilterListDto` en [[kuiper]].

## Alternativas descartadas

- **Un único objetivo por usuario, actualizable** (como `Perfil`). Pierde el histórico, que es el motivo de existir de la tabla.
- **Permitir `PUT`/`DELETE` "por si me equivoco".** Rompe la promesa de que el pasado es fiable: un resumen de marzo cambiaría al editar hoy. Se corrige con una fila nueva.
- **Tabla con `vigente_desde` y `vigente_hasta`.** Dos fechas que mantener coherentes y sin solapes; el fin de un objetivo es siempre el inicio del siguiente, se deduce.
- **Sustituir la fila si la fecha se repite** (como el peso por día, [[010-registro-peso-un-peso-por-dia]]). Aquí sería una edición encubierta; se prefiere el 409 explícito.
- **`vigente_desde` opcional con valor por defecto hoy.** Menos explícito y ambiguo entre zonas horarias; se exige.
- **Validar coherencia kcal contra macros.** Rechazaría objetivos legítimos y obliga a decidir tolerancias.

## Consecuencias

- El resumen diario ([[fusion]], `GET /api/fusion/resumen?fecha=`) podrá calcular el vigente de cualquier día pasado con `GET`/`getVigente(usuarioId, fecha)`, sin más datos.
- Corregir un objetivo equivocado deja la fila errónea en el histórico; solo se puede "tapar" con una fila de fecha posterior, o con una de la misma fecha si primero se borrase a mano en base de datos.
- El "hoy" de `?fecha=` omitido es el del servidor (`LocalDate.now()`); si el frontend vive en otra zona horaria, debe enviar la fecha.
- Cambiar los rangos de validación no exige migración (viven en el DTO, no en la tabla).
