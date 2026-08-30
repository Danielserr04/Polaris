# Polaris — Índice

Vault de documentación del proyecto. Vive dentro del repo, en `docs/`.

## Empieza por aquí

- [[vision]] — qué es Polaris y para qué sirve
- [[arquitectura]] — cómo está montado el backend
- [[plantilla-modulo]] — el molde exacto de ficheros por entidad
- [[convenciones]] — naming, commits, errores, tests
- [[modelo-datos]] — todas las tablas
- [[roadmap]] — fases y estado actual

## Módulos

| Nota | Módulo | Estado |
|---|---|---|
| [[auth]] | Identidad: OAuth2 Google y JWT | **Hecho** |
| [[nucleo]] | Perfil y peso corporal | Pendiente |
| [[odisea]] | Ocio: pelis, series, juegos, libros | **Hecho** (B2) |
| [[kuiper]] | Gastos | Nuevo |
| [[fusion]] | Nutrición | Nuevo |
| [[atlas]] | Gym | Nuevo |

## Decisiones

- [[000-plantilla]] — formato de las notas de decisión
- [[001-arquitectura-hexagonal]]
- [[002-react-sobre-angular]]
- [[003-modulos-separados-fusion-atlas]]
- [[004-nombres-espaciales]]
- [[005-estilo-propio-sin-referencia]]
- [[006-mysql]]
- [[007-esquema-ddl-auto-luego-flyway]]
- [[008-tooling-claude-code]]

## Cómo se mantiene esto

- Una nota por concepto. Si necesita dos títulos de nivel 1, son dos notas.
- Las decisiones no se editan: se sustituyen por una nueva que marca la vieja como reemplazada.
- `CLAUDE.md` (en la raíz del repo) no crece. Solo reglas duras; el detalle vive aquí.
