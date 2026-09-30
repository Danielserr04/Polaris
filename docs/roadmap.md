# Roadmap

Sin fechas. Se avanza por entregables. **Fase actual: B6.** B0 a B5 cerrados.

## Backend

### B0 — Esqueleto
Proyecto Spring Boot, `docker-compose.yml` con MySQL 8, `ddl-auto: update`, `shared/` con manejo global de errores y config de MapStruct, OpenAPI, `.env` en `.gitignore`.
**Entregable:** arranca y responde `/health`.

Sin Flyway todavía, a propósito: ver [[007-esquema-ddl-auto-luego-flyway]].

### B1 — Auth
Entidad `Usuario`, OAuth2 con Google, emisión de JWT propio, filtro de seguridad, `usuarioId` accesible desde cualquier servicio.
**Entregable:** endpoint protegido que devuelve tu usuario.

### B2 — Odisea, núcleo
`Titulo` y `Entrada` completos siguiendo [[plantilla-modulo]]. Filtros por tipo y estado con Specifications.
**Entregable:** CRUD completo probado con Postman.

> Esta fase es la que más importa. Aquí se valida que la plantilla de 16 ficheros funciona y es soportable, **antes** de replicarla en las otras 10 entidades. Si el molde chirría, se corrige aquí y no después.

**Hecho el 2026-08-30:** esquema volcado a `V1__esquema_inicial.sql`, Flyway dentro y `ddl-auto` en `validate`.

```bash
docker exec polaris-mysql mysqldump --no-data --compact --skip-comments -u root -p polaris
```

Al volcado solo se le cambiaron los nombres de indices y claves ajenas, que Hibernate genera como hashes. Hibernate no valida nombres de constraint, solo tablas, columnas y tipos.

### B3 — Odisea, APIs externas
Clientes de TMDB, juegos y libros como adaptadores de salida. Endpoint de búsqueda y de importación. Claves en variables de entorno.
**Entregable:** buscas un título y se guarda con carátula.

**Hecho el 2026-08-30.** `CatalogoExternoPort`, `CatalogoService`, los dos
endpoints y los tres adaptadores: **TMDB** (pelis y series), **IGDB** (juegos) y
**OpenLibrary** (libros). Los tres verificados contra sus APIs reales, no solo
con respuestas simuladas.

`V2__fuente_externa_open_library.sql` es la primera migración escrita a mano:
cambiar el enum de Java no basta, `fuente_externa` es un `ENUM` de MySQL.

### B4 — Núcleo
`Perfil` y `RegistroPeso`. Módulo pequeño, pero bloquea a Fusión y Atlas, así que va antes que ellos.

**Hecho el 2026-09-30.** `Perfil` (uno por usuario, `GET`/`PUT`, ver [[009-perfil-unico-por-usuario]]) y `RegistroPeso` (CRUD, un peso por día, ver [[010-registro-peso-un-peso-por-dia]]). Migraciones `V3` y `V4`, verificadas contra MySQL 8.4 real.

### B5 — Kuiper
`Categoria`, `Movimiento`, `Presupuesto`, y el endpoint de resumen mensual.
**Entregable:** control de gastos funcionando.

**Hecho el 2026-09-30.** `Categoria` ([[011-categoria-nombre-unico-por-tipo]], `V5`), `Movimiento` ([[012-movimiento-categoria-mismo-tipo]], `V6`), `Presupuesto` ([[013-presupuesto-solo-gastos-uno-por-periodo]], `V7`) y el resumen mensual ([[014-resumen-mensual-agregado-en-servicio]]), verificados contra MySQL 8.4 real.

Sin migración de datos: `lumen-app` nunca llegó a terminarse. Lo que sí conviene es revisar el proyecto viejo antes de empezar y quedarse con lo aprendido del dominio.

### B6 — Fusión
`Alimento`, `Comida`, `ComidaLinea`, `ObjetivoNutricional`. API de alimentos. Cálculo de macros del día contra objetivo.

**Casi cerrada (2026-09-30).** Hechas:

- `Alimento` ([[015-alimento-catalogo-compartido-macros-por-100g]], `V8`), con borrado bloqueado si está en alguna comida.
- `ObjetivoNutricional` ([[016-objetivo-nutricional-historico-inmutable]], `V9`): histórico inmutable, sin `PUT` ni `DELETE`.
- `Comida` y `ComidaLinea` ([[017-comida-agregado-con-lineas-macros-al-vuelo]], `V10`): un agregado, macros siempre al vuelo.
- API de alimentos: **Open Food Facts** ([[018-alimentos-open-food-facts]], `V11`), búsqueda e importación, sin claves. No se ha probado contra la API real (ver la nota de la decisión).

Resumen del día (`GET /api/fusion/resumen?fecha=`, [[021-resumen-diario-fusion]]) hecho. Peso corporal visible y apuntable desde Fusión ([[022-peso-corporal-desde-fusion-y-atlas]]). **Falta** probar Open Food Facts contra la API real.

### B7 — Atlas
`Ejercicio`, `Rutina`, `RutinaEjercicio`, `Sesion`, `SerieRegistro`. La parte con miga son las consultas de progresión.

`Ejercicio` hecho ([[023-ejercicio-catalogo-y-propios]], `V12`). `Rutina` y `RutinaEjercicio` hechos como agregado ([[024-rutina-agregado-con-lineas]], `V13`). Peso corporal desde Atlas hecho (mismo patrón que Fusión, [[022-peso-corporal-desde-fusion-y-atlas]]). `Sesion` y `SerieRegistro` hechos como agregado ([[025-sesion-agregado-con-series]], `V14`). Progresión y récords por volumen hechos ([[026-progresion-y-records-por-volumen]]). B7 cerrado.

### B8 — Cierre
Tests de los servicios de dominio, OpenAPI completo, logs, revisión de índices.

## Diseño

Va en dos tiempos, a propósito.

### D0 — Pase ligero *(pendiente, hacer antes o durante B2)*

Cuatro artboards: shell escritorio, shell móvil, listado de Odisea, ficha de detalle. Más la dirección visual: paleta, tipografía y modo claro/oscuro.

**No se hace por estética, se hace para validar el modelo de datos.** Al dibujar la ficha de una película salen los campos que faltan — dónde la viste, en qué plataforma, si `progreso` como entero aguanta una serie. Detectarlo aquí cuesta editar una tabla en [[modelo-datos]]; detectarlo en B5 cuesta reescribir 15 ficheros y rehacer el esquema.

La paleta, la tipografía y el shell no dependen de la API, así que es trabajo que no se tira.

### D1 — Diseño fino *(tras B3)*

Componentes, estados de carga, vacíos y de error, animaciones. Con datos reales de la API, no inventados.

**Prematuro hacerlo antes:** todo esto cambia en cuanto ves lo que la API devuelve de verdad.

## Frontend

Se planifica cuando **B3** esté cerrado y haya una API real contra la que trabajar. React, ver [[002-react-sobre-angular]].

Orden previsto: shell y navegación → Odisea → el resto de módulos → PWA.

## Después

- **Hosting.** Se decide al terminar B3, que es cuando hay algo que enseñar.
- **Calendario.** El módulo aparcado. Se retoma cuando los cuatro estén en marcha, porque los consume a todos.
- **Temporadas y episodios** en Odisea.
- **Multiusuario real**, si alguna vez hace falta.

## Estado

| Fase | Estado |
|---|---|
| B0 | **Hecho** |
| B1 | **Hecho** — login nativo y Google, los dos verificados de punta a punta |
| B2 | **Hecho** — cerrado con el corte a Flyway |
| B3 | **Hecho** — TMDB, IGDB y OpenLibrary |
| B4 | **Hecho** — `Perfil` y `RegistroPeso` |
| B5 | **Hecho** — `Categoria`, `Movimiento`, `Presupuesto` y resumen mensual |
| B6 | **Casi cerrada** — `Alimento`, `ObjetivoNutricional`, `Comida` (con sus líneas) y Open Food Facts y resumen del día hechos; falta probar Open Food Facts con la API real |
| B7 | **Hecho** — `Ejercicio`, `Rutina`, `Sesion`, series, peso y progresión |
| B8 | Pendiente |
| D0 | Pendiente — se saltó su ventana (era antes o durante B2) |
| D1 | Pendiente — ya toca, B3 está cerrado |

Actualizar esta tabla al cerrar cada fase.
