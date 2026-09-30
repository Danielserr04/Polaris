# 009 — Perfil: un registro por usuario, sin listado ni borrado

Estado: aceptada · 2026-09-30

## Contexto

[[plantilla-modulo]] prescribe 16 ficheros por entidad con cinco casos de uso (Create, Get, List, Update, Delete). `Perfil` (altura, fecha de nacimiento, sexo, nivel de actividad) es un registro **único por usuario**: no hay colección que listar, y borrarlo no tiene sentido de negocio.

## Decisión

- `perfil.usuario_id` es `UNIQUE`: un perfil por usuario.
- Se mantienen los 16 ficheros de la plantilla, pero solo hay dos casos de uso reales: `GetPerfilInterface` y `UpdatePerfilInterface`.
- API sin `{id}`, siempre sobre el usuario del JWT: `GET /api/nucleo/perfil` y `PUT /api/nucleo/perfil`. El `PUT` crea si no existe.
- `PerfilFilter`, `PerfilFilterListDto`, `PerfilFilterMapper`, `PerfilListDto` y `PerfilListDtoMapper` existen vacíos o sin uso, solo por cumplir la plantilla.
- Todos los campos salvo `usuario_id` son opcionales: se admite un perfil a medias.
- `sexo`: `HOMBRE`, `MUJER`. `nivel_actividad`: `SEDENTARIO`, `LIGERO`, `MODERADO`, `ALTO`, `MUY_ALTO`. Enums de MySQL, como el resto del esquema.

## Alternativas descartadas

- **Los cinco verbos tal cual.** `List` devolvería siempre 0 o 1 elemento y `Delete` dejaría a Fusión y Atlas sin datos base. Ruido sin valor.
- **Recortar la plantilla** (no crear los ficheros vacíos). Rompe la regla de que la estructura no se improvisa; se prefiere el fichero mínimo antes que la excepción.
- **`POST` para crear y `PUT` para actualizar.** Obliga al cliente a saber si ya existe perfil; el `PUT` idempotente lo evita.

## Consecuencias

- Añadir un valor a `sexo` o `nivel_actividad` exige una migración nueva (`ALTER ... MODIFY`), como en V2.
- Si algún día hay que borrar un perfil (p. ej. al dar de baja un usuario), se añade entonces `DeleteInterface`.
- `RegistroPeso` sí es una colección y seguirá la plantilla completa.
