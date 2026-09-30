# 010 — RegistroPeso: un peso por día, el POST del día existente actualiza

Estado: aceptada · 2026-09-30

## Contexto

[[nucleo]] dice que `registro_peso` tiene índice único por `(usuario_id, fecha)` y que "si te pesas dos veces, se actualiza". Falta fijar cómo se traduce eso a la API, y qué filtros tiene el listado (los docs no los definen).

## Decisión

- `POST /api/nucleo/registro-peso` sobre una fecha que ya tiene registro **actualiza ese registro** (conserva su `id`) y responde 201 igual. Es un reemplazo completo: lo que no se manda (grasa, notas) se borra, igual que un `PUT`.
- `PUT /{id}` que mueve el registro a una fecha ocupada por **otro** registro devuelve **409** (`DuplicateResourceException`). Aquí el cliente pidió editar uno concreto, no fusionar dos.
- El unique `(usuario_id, fecha)` está también en la tabla (`V4__nucleo_registro_peso.sql`) como red de seguridad.
- Listado con filtro por rango de fechas inclusivo, `?desde=&hasta=`, ambos opcionales, ordenado **de más reciente a más antiguo**.
- `pesoKg` > 0 con `DECIMAL(5,2)`; `grasaPct` entre 0 y 100 con `DECIMAL(4,1)`; `fecha` no puede ser futura.
- Ruta `/api/nucleo/registro-peso` (singular, minúscula, con guion para el nombre compuesto).

## Alternativas descartadas

- **POST que devuelve 409 si el día existe.** Cumpliría el unique, pero obliga al cliente a comprobar antes o a reintentar con `PUT`; contradice el "se actualiza" de [[nucleo]].
- **`PUT` por fecha** (`/registro-peso/2026-09-30`) como único punto de escritura. Más limpio para este caso, pero rompe la plantilla de cinco verbos con `{id}`.
- **Filtros por peso mínimo/máximo.** Nadie los ha pedido; los añade quien los necesite.

## Consecuencias

- Fusión y Atlas escriben el peso con el mismo `POST` y no sincronizan nada: el último gana.
- Un `POST` que solo quiere corregir el peso pierde las notas del día si no las reenvía. Es el precio del reemplazo completo, coherente con el resto de la API.
- `Perfil` no sigue este patrón: ver [[009-perfil-unico-por-usuario]].
