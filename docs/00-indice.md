# Polaris — Índice

Vault de documentación del proyecto. Vive dentro del repo, en `docs/`.

## Empieza por aquí

- [[vision]] — qué es Polaris y para qué sirve
- [[arquitectura]] — cómo está montado el backend
- [[plantilla-modulo]] — el molde exacto de ficheros por entidad
- [[convenciones]] — naming, commits, errores, tests
- [[modelo-datos]] — todas las tablas
- [[roadmap]] — fases y estado actual
- [[briefing-diseno]] — el texto que se le pasa a una herramienta de diseño (D0)
- [[briefing-figma]] — la versión corta, bajo 2000 caracteres

## Módulos

| Nota | Módulo | Estado |
|---|---|---|
| [[auth]] | Identidad: OAuth2 Google y JWT | **Hecho** |
| [[nucleo]] | Perfil y peso corporal | **Hecho** (B4) |
| [[odisea]] | Ocio: pelis, series, juegos, libros | **Hecho** (B2) |
| [[kuiper]] | Gastos | **Hecho** (B5) |
| [[fusion]] | Nutrición | En curso (B6, casi cerrada) |
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
- [[009-perfil-unico-por-usuario]]
- [[010-registro-peso-un-peso-por-dia]]
- [[011-categoria-nombre-unico-por-tipo]]
- [[012-movimiento-categoria-mismo-tipo]]
- [[013-presupuesto-solo-gastos-uno-por-periodo]]
- [[014-resumen-mensual-agregado-en-servicio]]
- [[015-alimento-catalogo-compartido-macros-por-100g]]
- [[016-objetivo-nutricional-historico-inmutable]]
- [[017-comida-agregado-con-lineas-macros-al-vuelo]]
- [[018-alimentos-open-food-facts]]
- [[019-zona-horaria-europe-madrid]]
- [[020-violacion-unicidad-409-y-errores-http-cliente]]
- [[021-resumen-diario-fusion]]
- [[022-peso-corporal-desde-fusion-y-atlas]]
- [[023-ejercicio-catalogo-y-propios]]
- [[024-rutina-agregado-con-lineas]]
- [[025-sesion-agregado-con-series]]
- [[026-progresion-y-records-por-volumen]]

## Cómo se mantiene esto

- Una nota por concepto. Si necesita dos títulos de nivel 1, son dos notas.
- Las decisiones no se editan: se sustituyen por una nueva que marca la vieja como reemplazada.
- `CLAUDE.md` (en la raíz del repo) no crece. Solo reglas duras; el detalle vive aquí.
