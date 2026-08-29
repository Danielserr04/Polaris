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

- Definir qué métrica manda en la progresión: 1RM estimado (fórmula de Epley o Brzycki), volumen total, o peso máximo por serie
- Catálogo inicial de ejercicios: buscar uno abierto o meterlos a mano
- Descansos y cronómetro: fuera de alcance por ahora

## Estado

| Entidad | Estado |
|---|---|
| `Ejercicio` | Pendiente — B7 |
| `Rutina` | Pendiente — B7 |
| `RutinaEjercicio` | Pendiente — B7 |
| `Sesion` | Pendiente — B7 |
| `SerieRegistro` | Pendiente — B7 |
