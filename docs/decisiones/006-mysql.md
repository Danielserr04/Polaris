# 006 — MySQL como base de datos

Estado: aceptada · 2026-08-28

## Contexto

El plan inicial era PostgreSQL. MySQL es el motor que ya se domina.

> **Corrección posterior:** esta nota se escribió creyendo que había que migrar datos de `fitcore` y `lumen-app`, lo que reforzaba la elección. Resultó que ninguno de los dos llegó a completarse y todo se escribe de cero, así que ese argumento desaparece. **La decisión se mantiene por el motivo que sigue siendo válido: se conoce MySQL y no se conoce Postgres.**

## Decisión

MySQL 8. Una sola base de datos para toda la aplicación, con todos los módulos dentro.

## Alternativas descartadas

**PostgreSQL.** Mejores tipos (JSONB, arrays nativos, enums reales), más estricto por defecto, planes gratuitos de hosting más generosos, y es lo que más se usa en backend nuevo. Descartada porque no se conoce.

El proyecto ya tiene bastantes cosas nuevas a la vez — hexagonal aplicado de cero, React, OAuth2, despliegue. Meter también un motor desconocido era añadir un frente más sin necesidad.

## Consecuencias

- **Una sola BD centralizada**, no una por módulo. Es lo que permite que el peso corporal de [[nucleo]] lo lean [[fusion]] y [[atlas]] sin sincronizar nada.
- Tipos en [[modelo-datos]]: `BIGINT AUTO_INCREMENT` para claves, `DECIMAL(p,e)` para importes y pesos. **Nunca `FLOAT` ni `DOUBLE` para dinero.**
- Charset `utf8mb4` y collation `utf8mb4_unicode_ci` en todas las tablas. Con `utf8` a secas MySQL no guarda emojis ni parte del Unicode.
- El campo `generos` de [[odisea]] se queda como varchar separado por comas. En Postgres habría sido un array nativo. Si molesta, tabla de unión.
- **Esto no toca el dominio.** El motor solo lo conoce `infrastructure/persistence`. Cambiarlo algún día sería tocar el `JpaAdapter` y las migraciones, nada más — que es justo lo que compra [[001-arquitectura-hexagonal]].

Ver también [[007-esquema-ddl-auto-luego-flyway]] para cómo se gestiona el esquema.
