# Atlas

Gym. Ejercicios, rutinas, sesiones de entrenamiento y progresión.

Código nuevo. Cubre la mitad de gimnasio de lo que intentaba `fitcore`; la otra mitad es [[fusion]]. Ver [[003-modulos-separados-fusion-atlas]]. Va en **B7** del [[roadmap]].

## Entidades

**`Ejercicio`** — catálogo, con opción de crear los tuyos.

**`Rutina`** — una plantilla de entrenamiento.

**`RutinaEjercicio`** — qué ejercicios lleva, en qué orden, con qué objetivo de series y reps.

**`Sesion`** — un entreno real, con o sin rutina asociada.

**`SerieRegistro`** — una serie concreta: reps, peso, RPE.

Esquema completo en [[modelo-datos]].

## La decisión de diseño

**`SerieRegistro` es el corazón del módulo.** Todo lo demás existe para llegar a ella, y toda la progresión sale de ahí.

Es también la tabla que más crecerá: si entrenas cuatro días por semana con seis ejercicios y cuatro series, son casi 100 filas semanales. Índice por `(ejercicio_id, sesion_id)` desde el principio.

**`Sesion.rutina_id` es nullable**, a propósito: un entreno improvisado sigue siendo un entreno y debe poder registrarse igual.

**Separar rutina de sesión** permite cambiar la rutina sin reescribir el histórico. La rutina es el plan; la sesión es lo que de verdad pasó, que casi nunca coincide.

## Relación con Núcleo

Atlas **lee y escribe el peso corporal de [[nucleo]]**: se ve y se apunta desde aquí, igual que desde Fusión, pero el dato vive una sola vez.

Lo usa para relativizar cargas (fuerza por kilo de peso corporal) y para ver la progresión de peso junto a la de fuerza en la misma gráfica.

Qué es un "récord" lo decide Atlas. Núcleo solo guarda kilos y fecha.

## Endpoints

```
GET    /api/atlas/ejercicio?grupoMuscular=
POST   /api/atlas/ejercicio
GET    /api/atlas/rutina
POST   /api/atlas/rutina
PUT    /api/atlas/rutina/{id}
GET    /api/atlas/sesion?desde=&hasta=
POST   /api/atlas/sesion
PUT    /api/atlas/sesion/{id}
GET    /api/atlas/progresion?ejercicioId=      evolución de carga y volumen
GET    /api/atlas/records                       mejores marcas por ejercicio
```

`/progresion` y `/records` son las consultas con miga: agregaciones sobre `serie_registro`. El resto es CRUD.

## Pendiente

- Catálogo inicial de ejercicios: decidido vacío, cada usuario crea los suyos (o inserta catálogo con `usuario_id` NULL)
- Descansos y cronómetro: fuera de alcance por ahora

## Progresión

Métrica que manda: **volumen total** (reps × peso). Ver [[026-progresion-y-records-por-volumen]]. `/progresion` da el volumen por sesión de un ejercicio; `/records` da, por ejercicio, el peso máximo en una serie y el mayor volumen en una sesión. Se calcula en la base de datos.

## Estado

| Entidad | Estado |
|---|---|
| `Ejercicio` | **Hecha** — CRUD en `/api/atlas/ejercicio`, filtros `?grupoMuscular=` y `?q=`. Catálogo compartido (solo lectura) más ejercicios propios. Ver [[023-ejercicio-catalogo-y-propios]] |
| `Rutina` | **Hecha** — CRUD en `/api/atlas/rutina`, filtro `?activa=`; agregado con sus ejercicios. Ver [[024-rutina-agregado-con-lineas]] |
| Peso corporal | **Hecho** — `GET`/`POST /api/atlas/peso` a través de un puerto propio hacia Núcleo; sin tabla propia. Ver [[022-peso-corporal-desde-fusion-y-atlas]] |
| `RutinaEjercicio` | **Hecha** — dentro del agregado `Rutina`, sin endpoints propios. Lleva `usuario_id` |
| `Sesion` | **Hecha** — CRUD en `/api/atlas/sesion`, filtros `?desde=&hasta=&rutinaId=`; agregado con sus series. El listado trae `numeroSeries`, `numeroEjercicios` y `volumen`. Ver [[025-sesion-agregado-con-series]] y [[032-listados-de-odisea-y-atlas-sin-consultas-extra]] |
| `SerieRegistro` | **Hecha** — dentro del agregado `Sesion`, sin endpoints propios. Lleva `usuario_id` |
