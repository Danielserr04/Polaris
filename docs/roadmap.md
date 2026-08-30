# Roadmap

Sin fechas. Se avanza por entregables. **Fase actual: B3.** B0, B1 y B2 cerrados.

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

### B4 — Núcleo
`Perfil` y `RegistroPeso`. Módulo pequeño, pero bloquea a Fusión y Atlas, así que va antes que ellos.

### B5 — Kuiper
`Categoria`, `Movimiento`, `Presupuesto`, y el endpoint de resumen mensual.
**Entregable:** control de gastos funcionando.

Sin migración de datos: `lumen-app` nunca llegó a terminarse. Lo que sí conviene es revisar el proyecto viejo antes de empezar y quedarse con lo aprendido del dominio.

### B6 — Fusión
`Alimento`, `Comida`, `ComidaLinea`, `ObjetivoNutricional`. API de alimentos. Cálculo de macros del día contra objetivo.

### B7 — Atlas
`Ejercicio`, `Rutina`, `RutinaEjercicio`, `Sesion`, `SerieRegistro`. La parte con miga son las consultas de progresión.

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
| B0 | En curso |
| D0 | Pendiente — antes o durante B2 |
| B1 – B8 | Pendiente |
| D1 | Pendiente — tras B3 |

Actualizar esta tabla al cerrar cada fase.
