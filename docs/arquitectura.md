# Arquitectura

Monolito modular. Una sola aplicación Spring Boot, una sola base de datos, pero dividida en módulos que no se conocen entre sí.

Dentro de cada módulo, arquitectura hexagonal. Ver [[001-arquitectura-hexagonal]] para el porqué.

## Árbol raíz

```
com.polaris
├── shared/     config, seguridad, manejo global de errores, utilidades JPA
├── auth/       Usuario, OAuth2 Google, JWT
├── nucleo/     perfil y peso corporal
├── odisea/     ocio
├── kuiper/     gastos
├── fusion/     nutrición
└── atlas/      gym
```

**`shared/` no conoce a ningún módulo.** Los módulos sí conocen a `shared/`. Esa flecha va en un solo sentido y no se negocia.

## Las tres capas

Cada módulo se organiza en `application`, `domain` e `infrastructure`. El molde completo con todos los ficheros está en [[plantilla-modulo]].

### `application/`

Los contratos. No hay implementaciones aquí.

- **`in/`** — una interfaz por caso de uso: `CreateTituloInterface`, `ListTituloInterface`… Cada una tiene un solo método. Parece excesivo, pero hace que un servicio declare exactamente lo que sabe hacer, y que un test pueda mockear un caso de uso sin arrastrar los otros ocho.
- **`out/`** — `<Entidad>RepositoryPort`. Lo que el dominio necesita del mundo exterior, expresado en sus propios términos.

### `domain/`

El corazón. **Aquí no entra Spring ni JPA.**

- **`model/`** — clases planas: `Titulo`, `TituloFilter`. Sin anotaciones.
- **`service/`** — `TituloService` implementa todas las interfaces de `application/in` y depende solo de puertos de `application/out`.

Que el servicio viva en `domain/` y no en `application/` es una particularidad de esta convención, heredada de GestionER. Es deliberado: ver [[001-arquitectura-hexagonal]].

### `infrastructure/persistence/`

Todo lo que toca tecnología concreta, junto en la misma carpeta: el Controller (entrada HTTP), la Entity y el Repository (salida a BD), el JpaAdapter (el puente), los DTOs y los mappers.

Es poco ortodoxo tener el Controller ahí dentro — en el hexagonal de manual iría en `infrastructure/in/rest`. Se mantiene así por coherencia con GestionER.

## Flujo de una petición

```
HTTP → TituloController
         ↓ RequestDto → modelo (TituloRequestDtoMapper)
       TituloService              [domain, no sabe que existe HTTP]
         ↓ usa TituloRepositoryPort
       TituloJpaAdapter
         ↓ modelo → Entity (TituloEntityMapper)
       TituloRepository → MySQL
```

Y de vuelta, el mismo camino al revés: Entity → modelo → `ListDto` o `FormDto`.

La gracia es que `TituloService` no sabe si detrás hay MySQL, un fichero o una API. Y `TituloController` no sabe cómo se guarda nada.

## Comunicación entre módulos

**Un módulo nunca importa una clase de otro módulo.**

Fusión y Atlas necesitan el mismo peso corporal. La solución no es que uno llame al otro, sino que **ambos consumen [[nucleo]]**, que expone su puerto y su modelo. El dato vive una vez; los dos módulos lo leen y lo escriben.

Lo que cada módulo no comparte es la interpretación: Fusión decide qué es un objetivo de peso, Atlas decide qué es un récord. Núcleo solo guarda el número y la fecha.

Si algún día hace falta algo más elaborado (que Atlas reaccione a algo que pasa en Fusión), eventos de dominio con `ApplicationEventPublisher`. No antes de necesitarlo.

## Filtros

Los listados usan JPA Specifications, construidas en el `JpaAdapter` a partir de un `<Entidad>Filter` del dominio.

El dominio expresa *qué* quiere filtrar; la infraestructura decide *cómo* se traduce a SQL. Por eso `TituloFilter` es una clase plana en `domain/model/` y no un `Specification`.

## Base de datos

MySQL 8. Una sola base de datos para todos los módulos; las tablas se prefijan por módulo si hay riesgo de colisión.

El esquema se gestiona en dos tiempos (ver [[007-esquema-ddl-auto-luego-flyway]]):

- **Hasta cerrar B2:** `ddl-auto: update`. Hibernate crea las tablas leyendo las entidades. El modelo aún se mueve y no hay datos que proteger.
- **A partir de B3:** Flyway con migraciones versionadas y `ddl-auto: validate`.

`DECIMAL` para importes y pesos, nunca `FLOAT` ni `DOUBLE`. Tablas en `utf8mb4`.
