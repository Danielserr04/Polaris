# Convenciones

## Idioma

- **Dominio en español:** `Titulo`, `Entrada`, `Movimiento`, `Sesion`. Es tu app, tu vocabulario.
- **Técnica en inglés:** `Service`, `Repository`, `Mapper`, `Controller`, `Port`, `Interface`.
- **Sin tildes ni eñes en identificadores:** `Sesion`, no `Sesión`. En textos de UI y comentarios, con tildes normales.

## Nombres de clase

| Patrón | Ejemplo |
|---|---|
| `<Verbo><Entidad>Interface` | `CreateTituloInterface` |
| `<Entidad>RepositoryPort` | `TituloRepositoryPort` |
| `<Entidad>Service` | `TituloService` |
| `<Entidad>Controller` | `TituloController` |
| `<Entidad>Entity` | `TituloEntity` |
| `<Entidad>JpaAdapter` | `TituloJpaAdapter` |
| `<Entidad>Repository` | `TituloRepository` |
| `<Entidad>RequestDto` | `TituloRequestDto` |
| `<Entidad>FilterListDto` | `TituloFilterListDto` |
| `<Entidad>FormDto` | `TituloFormDto` |
| `<Entidad>ListDto` | `TituloListDto` |
| `<Entidad><Destino>Mapper` | `TituloEntityMapper` |

Verbos de caso de uso: `Create`, `Get`, `List`, `Update`, `Delete`, `Exists`, `Import`.

## Base de datos

MySQL 8, charset `utf8mb4`, collation `utf8mb4_unicode_ci`.

- Tablas y columnas en `snake_case` y **singular**: `titulo`, `entrada`, `registro_peso`
- Claves: `BIGINT AUTO_INCREMENT`
- Claves foráneas: `<tabla>_id` → `titulo_id`
- Toda tabla de datos personales lleva `usuario_id`
- Enums como `VARCHAR` con `@Enumerated(EnumType.STRING)`, nunca ordinal
- **Dinero y pesos en `DECIMAL(p,e)`.** Nunca `FLOAT` ni `DOUBLE`: 0.1 + 0.2 no da 0.3 y en un saldo eso se nota
- Migraciones Flyway (a partir de B3): `V<n>__descripcion_en_snake_case.sql`. Un fichero aplicado **no se edita nunca**; si estaba mal, se corrige con el siguiente

## API REST

```
GET    /api/<modulo>/<entidad>          listado con filtros → ListDto
GET    /api/<modulo>/<entidad>/{id}     detalle → FormDto
POST   /api/<modulo>/<entidad>          crear ← RequestDto
PUT    /api/<modulo>/<entidad>/{id}     actualizar ← RequestDto
DELETE /api/<modulo>/<entidad>/{id}
```

Rutas en **singular y minúscula**: `/api/odisea/titulo`.
Filtros por query params, nunca en el body de un GET.

## Errores

Excepciones de dominio propias, traducidas a HTTP en un `@RestControllerAdvice` de `shared/`.

| Excepción | HTTP |
|---|---|
| `<Entidad>NotFoundException` | 404 |
| `ValidationException` | 400 |
| `DuplicateResourceException` | 409 |
| `UnauthorizedException` | 401 |
| `ForbiddenException` | 403 |
| `ExternalServiceException` | 502 + log |
| Cualquier otra | 500 + log |

El 502 existe para separar "ha fallado TMDB" de "hemos fallado nosotros". Con
un 500 para todo no hay forma de saber si mirar nuestros logs o el estado de la
API externa.

**El Controller no construye respuestas de error a mano.** Lanza y deja que el advice traduzca.

Formato uniforme:

```json
{ "timestamp": "...", "status": 404, "error": "Titulo no encontrado", "path": "/api/odisea/titulo/42" }
```

## Tests

Prioridad, en este orden:

1. **Servicios de dominio** con el puerto mockeado. Es donde está la lógica y donde los tests salen baratos.
2. **Mappers**, sobre todo tras añadir campos.
3. **Controllers** con `@WebMvcTest` solo si tienen algo que probar más allá de delegar.

No perseguir cobertura. Un test que solo comprueba que un getter devuelve lo que el setter puso es ruido.

## Git

Ramas: `main` estable, `feat/<modulo>-<cosa>` para el trabajo.

Commits en imperativo y en español, con prefijo:

```
feat(odisea): crear entidad Titulo con CRUD completo
fix(kuiper): corregir cálculo de saldo mensual
docs: añadir ADR sobre React
refactor(shared): extraer manejo global de errores
chore: subir Spring Boot a 3.3.2
```

Un commit por unidad con sentido. No mezclar refactor y funcionalidad nueva.

## Configuración

- Nada de secretos en el repo. `.env` en `.gitignore` desde el primer commit
- Claves de APIs y credenciales por variables de entorno
- `application.yml` con perfiles: `dev` y `prod`
- El esquema es de Flyway y `ddl-auto` está en `validate` desde el cierre de B2. Cada cambio es un `V<n>__descripcion.sql` nuevo; los ya aplicados no se tocan. Ver [[007-esquema-ddl-auto-luego-flyway]]

## Documentación

- Toda decisión con alternativas descartadas → nota en `docs/decisiones/`
- Cada módulo mantiene su nota en `docs/modulos/`
- Un campo nuevo en una entidad se refleja en [[modelo-datos]] en el mismo commit
