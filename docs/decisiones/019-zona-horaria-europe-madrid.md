# 019 — Zona horaria de la aplicación: Europe/Madrid

Estado: aceptada · 2026-09-30

## Contexto

La JVM arranca con la zona del sistema. En el contenedor y en servidores suele ser UTC, en el PC de desarrollo es Madrid. Con la app en UTC, `usuario.creado_en` se guardaba en UTC (diferencia 0 con `utc_timestamp()` en MySQL), y el mismo código daría horas distintas según dónde corra.

## Decisión

Fijar `Europe/Madrid` como zona por defecto de la JVM. Clase `shared/config/ZonaHoraria` con `aplicar(String)` (`TimeZone.setDefault`), llamada desde `PolarisApplication.main` antes de `SpringApplication.run`.

## Alternativas descartadas

**Variable de entorno para la zona.** El `.env` se lee después de `main`, no llegaría a tiempo. Sin ventaja real en una app personal de un solo usuario.

**`hibernate.jdbc.time_zone=UTC`.** Guardaría todo en UTC, que es lo más limpio, pero reinterpretaría las filas ya guardadas bajo otra zona.

**Configurar la zona en el sistema o en Docker.** Depende de cada entorno; el código no garantizaría nada.

## Consecuencias

- Mismo comportamiento en local, contenedor y servidor.
- **Medido contra MySQL 8.4 (2026-09-30):** con la zona en Madrid, `usuario.creado_en` sigue guardándose en UTC (diferencia 0 con `utc_timestamp()`). Hibernate 6.6 escribe los `Instant` en UTC con independencia de la zona de la JVM. Por tanto **no hay filas desplazadas** ni migración de datos; la previsión inicial de +2 h en `creado_en` era errónea.
- Lo que sí cambia es todo lo que use la zona local: `LocalDate.now()` / `LocalDateTime.now()` (hoy, `ErrorResponse.timestamp`, y las fechas por defecto de Kuiper, Fusión y Atlas). Entre las 00:00 y las 02:00 de Madrid, "hoy" ya no es el día anterior en UTC.
- `TimeZone.setDefault` es estado global: los tests que lo cambian deben restaurarlo.
