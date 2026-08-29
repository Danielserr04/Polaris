# Odisea

Ocio: películas, series, juegos y libros. Qué tienes pendiente, qué estás consumiendo, qué terminaste y qué te pareció.

**Es el primer módulo que se construye** ([[roadmap]] B2). Es código nuevo, sin migración de datos y sin decisiones heredadas, así que sirve para validar [[plantilla-modulo]] antes de replicarla diez veces.

## Entidades

**`Titulo`** — la ficha del contenido. Es catálogo: no lleva `usuario_id`.

**`Entrada`** — tu relación con esa ficha: estado, valoración, progreso, notas.

Esquema completo en [[modelo-datos]].

## La decisión de diseño

**Una sola tabla para los cuatro tipos de contenido**, discriminados por un enum `tipo`.

Añadir "Anime", "Podcast" o "Cómic" mañana es un valor más en el enum. Cero migraciones, cero tablas nuevas, cero código duplicado.

El precio: campos que no aplican a todos los tipos. `duracion_min` son minutos en una peli y páginas en un libro. Es un compromiso aceptado a cambio de la flexibilidad.

## Endpoints

```
GET    /api/odisea/titulo               listado del catálogo
GET    /api/odisea/titulo/{id}
POST   /api/odisea/titulo
PUT    /api/odisea/titulo/{id}
DELETE /api/odisea/titulo/{id}

GET    /api/odisea/entrada?tipo=&estado=    mi lista, filtrada
GET    /api/odisea/entrada/{id}
POST   /api/odisea/entrada
PUT    /api/odisea/entrada/{id}
DELETE /api/odisea/entrada/{id}

GET    /api/odisea/catalogo/buscar?q=&tipo=    proxy a la API externa
POST   /api/odisea/entrada/importar            del catálogo externo a tu lista
GET    /api/odisea/entrada/estadisticas
```

## APIs externas (B3)

| Tipo | Fuente | Estado |
|---|---|---|
| Películas y series | TMDB | Decidido |
| Juegos | IGDB o RAWG | **Pendiente** |
| Libros | Google Books u OpenLibrary | **Pendiente** |

IGDB tiene mejores datos; RAWG es mucho más fácil de autenticar.

Cada fuente es un adaptador de salida detrás de un puerto común. El servicio pide "busca esto de tipo JUEGO" y no sabe quién responde.

Al importar se comprueba `(fuente_externa, id_externo)` para no duplicar fichas.

## Pendiente

- **Temporadas y episodios.** No encajan en el modelo actual. De momento `progreso` guarda el número de episodio y basta. Cuando haga falta de verdad, tabla aparte.
- Decidir IGDB vs RAWG y Google Books vs OpenLibrary.

## Estado

| Entidad | Estado |
|---|---|
| `Titulo` | Pendiente — B2 |
| `Entrada` | Pendiente — B2 |
