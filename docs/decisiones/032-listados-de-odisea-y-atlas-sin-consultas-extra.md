# 032 — Los listados de Odisea y Atlas traen lo que el frontend necesita, sin consultas extra

Estado: aceptada · 2026-10-01

## Contexto

Al conectar el frontend aparecieron tres huecos en los listados, todos anotados en el checkpoint del 2026-10-01:

- `GET /api/odisea/entrada` no traía el año, la duración ni el título original: Odisea pedía además todo el catálogo (`GET /titulo`) solo para el año y ocultaba con CSS la columna Duración.
- `GET /api/odisea/entrada` tenía un **N+1**: `EntradaEntity.titulo` es `@ManyToOne EAGER` y una consulta por Specification no hace el `JOIN`, sino una sentencia más por cada título. Medido con 14.000 entradas: 14.001 sentencias y 7,3 s.
- `GET /api/atlas/sesion` solo traía `numeroSeries`: Atlas pedía cada sesión completa (hasta 14 peticiones) para sacar ejercicios y volumen.

## Decisión

- **N+1:** `EntradaRepository.findAll(Specification)` se sobrescribe con `@EntityGraph(attributePaths = "titulo")`. El listado pasa a una sola sentencia con `JOIN`. Un test de integración (Testcontainers, estadísticas de Hibernate) fija que listar 25 entradas es 1 sentencia.
- **`EntradaListDto`** añade `tituloOriginal`, `tituloAnio` y `tituloDuracionMin`, del título que ya viene cargado con la entrada. Cambio aditivo.
- **`SesionListDto`** añade `numeroEjercicios` (ejercicios distintos) y `volumen` (suma de repeticiones por peso, `BigDecimal` con 2 decimales `HALF_UP`, el peso corporal aporta 0, como en [[026-progresion-y-records-por-volumen]]). Se calculan en el dominio (`Sesion.getNumeroEjercicios()` y `getVolumen()`, igual que `getNumeroSeries()`) con las series que el listado ya carga para contarlas: ninguna consulta más. Nunca se guardan. Cambio aditivo.
- **`GET /api/odisea/entrada/estadisticas`** (listado en `odisea.md`) **no se implementa**: nunca se definió qué estadísticas devuelve y el frontend cuenta en cliente.

## Alternativas descartadas

- **`@BatchSize` o `FetchType.LAZY` + `join fetch` a mano.** El grafo de entidad es lo mínimo que arregla el N+1 sin tocar la entidad ni escribir JPQL, y mantiene los filtros por Specification.
- **Campos nuevos solo en `EntradaFormDto` (ya los trae la ficha).** Obligaría a una petición por fila, que es el problema que se resuelve.
- **Calcular el volumen de la sesión con una consulta agregada como la progresión.** El listado ya carga las series (`EAGER`, [[025-sesion-agregado-con-series]]); sumar en memoria no cuesta nada. Si el rango crece tanto que duele, se cambia por una agregación sin tocar la API (consecuencia ya anotada en 025).
- **Implementar `estadisticas` adivinando su contenido.** Sin definición de producto, sería inventar una API.

## Consecuencias

- El frontend de Odisea ya no pide el catálogo para el listado y muestra la columna Duración; el de Atlas no pide sesiones sueltas y pinta toneladas por semana, como en el diseño.
- `SesionListDto` y `EntradaListDto` crecen: si algún día pesa, hay que decidir qué se quita antes que seguir sumando.
- El `EntityGraph` solo cubre `findAll(Specification)` de entradas. `findById` sigue cargando el título EAGER con su `JOIN` normal.
