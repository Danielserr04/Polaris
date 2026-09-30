# 020 — Violación de unicidad a 409 y errores HTTP de cliente legibles

Estado: aceptada · 2026-09-30

## Contexto

Los servicios comprueban duplicados antes de insertar (`DuplicateResourceException` → 409), pero dos peticiones simultáneas pasan ambas la comprobación y una choca con el `UNIQUE` de MySQL. Llegaba como `DataIntegrityViolationException` y salía como 500. Medido contra MySQL 8.4 con dos `POST /api/auth/registro` a la vez: 500 en una de las dos en prácticamente todas las rondas. Además, método HTTP no soportado, `Content-Type` no soportado, ruta inexistente y query param obligatorio ausente también acababan en la red de seguridad (500).

## Decisión

`GlobalExceptionHandler` traduce a 409 ("El recurso ya existe") solo las `DataIntegrityViolationException` cuya cadena de causas contiene una excepción SQL con código de error MySQL 1062 (`ER_DUP_ENTRY`); y traduce a 405 (con cabecera `Allow`), 415, 404 y 400 los errores de petición correspondientes, con mensajes en español y el formato `ErrorResponse` de siempre.

## Alternativas descartadas

**Traducir toda `DataIntegrityViolationException` a 409.** Una FK rota o un NOT NULL son fallos nuestros, no del cliente: esconderlos tras un 409 falsearía el diagnóstico.

**Bloquear o serializar las escrituras en el servicio.** Más complejo y no protege frente a varias instancias; la restricción `UNIQUE` ya es la fuente de verdad.

**Incluir el nombre del índice o el valor duplicado en el mensaje.** Filtra detalles internos del esquema y datos de la fila.

## Consecuencias

- Los 409 de la carrera son indistinguibles para el cliente de los 409 por comprobación previa, salvo por el mensaje (genérico).
- La detección depende del código de MySQL 1062; si se cambia de motor habrá que revisarla.
- La respuesta 405 incluye `Allow`. Las rutas inexistentes autenticadas dan 404 en vez de 500.
