# 007 — Esquema: `ddl-auto` primero, Flyway al cerrar B2

Estado: aceptada · 2026-08-28

## Contexto

Hay dos formas de crear y mantener el esquema en Spring Boot:

- **Hibernate con `ddl-auto: update`** — genera las tablas leyendo las `@Entity`. Cero SQL a mano.
- **Flyway** — ficheros `.sql` numerados en el repo, aplicados en orden y con registro de cuáles ya se ejecutaron.

No son alternativas al ORM: **Hibernate se usa igual en ambos casos.** Lo que cambia es quién manda sobre el esquema.

Durante B0–B2 el modelo va a cambiar constantemente. Escribir SQL a mano en esa fase es un freno sin contrapartida.

## Decisión

Enfoque en dos tiempos:

1. **B0 a B2:** `spring.jpa.hibernate.ddl-auto=update`. Sin Flyway, sin SQL a mano.
2. **Al cerrar B2**, con el modelo estable: volcar el esquema generado a `V1__esquema_inicial.sql`, añadir Flyway y pasar a `ddl-auto=validate`.
3. **De B3 en adelante:** cada cambio de esquema es un `V<n>__descripcion.sql`. Los ficheros ya aplicados no se tocan nunca.

```bash
mysqldump --no-data -u root -p polaris > src/main/resources/db/migration/V1__esquema_inicial.sql
```

## Alternativas descartadas

**Flyway desde el primer commit.** Lo correcto de manual. Descartada porque durante B2 el modelo cambia cada pocas horas y cada cambio serían un `ALTER TABLE` escrito a mano, sin ganar nada: todavía no hay datos que proteger.

**`ddl-auto: update` para siempre.** Es lo cómodo hasta que deja de serlo. Hibernate **nunca borra ni renombra columnas**: si renombras un campo, crea una columna nueva y deja la vieja con los datos dentro. A partir de B3 ya hay títulos, valoraciones y entradas propias en la BD, y no hay forma de saber qué cambió ni de reproducir el esquema en el servidor.

**Dump del esquema commiteado, sin Flyway.** Vale para levantar el proyecto de cero, pero no para modificarlo: cada cambio hay que aplicarlo a mano en cada entorno y no queda rastro de qué cambió ni cuándo.

## Consecuencias

- Durante B0–B2 la BD se recrea sin ceremonia. Los datos de esa fase son de prueba y se pueden perder.
- **El corte está al cerrar B2, no más tarde.** Llegar a B5 con datos reales de gastos y sin migraciones versionadas es el escenario que esta decisión existe para evitar.
- Regla que empieza a aplicar tras B2: un fichero `V<n>` aplicado no se edita jamás. Si estaba mal, se corrige con un `V<n+1>`.
- `ddl-auto=validate` a partir de entonces: si una entidad no cuadra con lo que Flyway creó, la aplicación no arranca. Es el comportamiento que se quiere.
- Hasta el corte, [[modelo-datos]] es la única fuente fiable del esquema. Mantenerla al día importa más de lo normal.
