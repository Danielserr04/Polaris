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
POST   /api/odisea/catalogo/importar           del catálogo externo a tu lista
GET    /api/odisea/entrada/estadisticas
```

`importar` cuelga de `catalogo` y no de `entrada`, como se había apuntado
antes: lo que identifica la petición es una ficha de la fuente externa, no una
entrada tuya que todavía no existe.

## APIs externas (B3)

| Tipo | Fuente | Estado |
|---|---|---|
| Películas y series | TMDB | **Hecho** — `TmdbCatalogoAdapter` |
| Juegos | IGDB | **Hecho** — `IgdbCatalogoAdapter` |
| Libros | Google Books u OpenLibrary | **Sin decidir** |

**IGDB sobre RAWG**, decidido el 2026-08-30. RAWG se autentica con una clave y ya, pero su catálogo está agregado de varias fuentes y se nota: duplicados, géneros inconsistentes y fichas a medias en cuanto sales de lo conocido. IGDB está curado. Se acepta el coste de autenticación a cambio de los datos.

Cada fuente es un adaptador de salida detrás de `CatalogoExternoPort`. El
servicio recibe **la lista entera de adaptadores** y elige por tipo con
`soporta()`: pide "busca esto de tipo JUEGO" y no sabe quién responde. Añadir
una fuente es escribir un adaptador y nada más — ni tocar el servicio, ni
registrarla en ningún sitio.

Buscar un tipo sin fuente decidida devuelve **400** con el tipo en el mensaje,
no un 404 ni un 500: el tipo existe, lo que no hay todavía es de dónde sacarlo.

Al importar se comprueba `(fuente_externa, id_externo)` para no duplicar fichas.
Si otro usuario ya importó esa película, se reutiliza su `Titulo`: el catálogo
es compartido. Lo que se crea siempre es tu `Entrada`, en estado `PENDIENTE`.
Importar dos veces lo mismo da **409**, no una entrada duplicada.

### TMDB

Autentica con el *API Read Access Token* (v4) en la cabecera `Authorization`, no
con la `api_key` de v3 en la query: una clave en la URL acaba en los logs de
cualquier proxy por el que pase.

**Sin `POLARIS_TMDB_TOKEN` la aplicación arranca igual** y la búsqueda responde
400 diciendo que falta. Tumbar el contexto entero por una clave que solo hace
falta en dos endpoints sería el mismo error que ya se cometió con las de Google.

Películas y series son endpoints distintos y sus campos no se llaman igual
(`title`/`name`, `release_date`/`first_air_date`, `runtime`/`episode_run_time`).
Esa traducción vive dentro del adaptador y no sale de ahí.

### IGDB

Tiene dos rarezas que TMDB no tiene, y las dos se resuelven dentro del adaptador.

**La credencial no es una clave fija.** La aplicación se registra en **Twitch**,
no en IGDB (`dev.twitch.tv/console/apps`), y con su *Client ID* y *Client Secret*
el backend pide un token que **caduca**. `IgdbCatalogoAdapter` lo cachea y lo
renueva solo, con cinco minutos de margen para no cortar una petición en curso.
Es un *client credentials*: no hay refresh token, se vuelve a pedir entero.

**No es REST al uso.** Se consulta con `POST` y un lenguaje propio en el cuerpo:
`search "zelda"; fields name,cover.image_id; limit 20;`. Las comillas del texto
buscado se escapan, o un título con comillas rompe la consulta.

Además la respuesta no viene masticada: `first_release_date` es un epoch en
segundos y la portada es un `image_id` suelto con el que hay que componer la URL.

**`duracionMin` se queda a `null` en los juegos.** El campo son minutos en una
película y páginas en un libro; en un juego no significa nada.

## Pendiente

- **Temporadas y episodios.** No encajan en el modelo actual. De momento `progreso` guarda el número de episodio y basta. Cuando haga falta de verdad, tabla aparte.
- Decidir Google Books vs OpenLibrary y escribir ese adaptador. Es lo único que
  falta para cerrar B3.

## Estado

| Entidad | Estado |
|---|---|
| `Titulo` | **Hecho** — CRUD completo, filtros por `tipo` y `texto` con Specifications, verificado contra MySQL |
| `Entrada` | **Hecho** — CRUD completo, filtros por `tipo` y `estado`, aislada por `usuario_id`, verificada contra MySQL |
| Catálogo externo | **TMDB e IGDB hechos** — búsqueda e importación de pelis, series y juegos. Libros, sin fuente decidida |

## Decisiones de implementación (B2)

**`Entrada` referencia a `Titulo` con `@ManyToOne`, no con un id suelto.** El
filtro que pide el roadmap es *por tipo y estado*: `estado` vive en `entrada`
pero `tipo` vive en `titulo`. Sin la relación, filtrar por tipo no se puede
expresar como Specification. Además el `EntradaListDto` necesita título y
carátula para servir de algo.

**`FetchType.EAGER` explícito en esa relación.** Con `open-in-view: false`, un
`LAZY` leído fuera de la transacción del `JpaAdapter` reventaría con
`LazyInitializationException`. En un *-a-uno es un solo JOIN, no el problema
N+1 de una colección.

**Aislamiento por usuario dentro de la Specification**, no filtrando después.
`get`, `update` y `delete` comprueban propiedad y lanzan
`EntradaNotFoundException` → **404, no 403**: un 403 confirmaría que ese id
existe. El `usuarioId` nunca llega en el `RequestDto`, lo pone el servicio a
partir del JWT.

**Borrar un `Titulo` con entradas asociadas → 400.** `Titulo` es catálogo
compartido; borrarlo en cascada arrastraría entradas de otros usuarios.
`TituloService` consulta `EntradaRepositoryPort.existsByTituloId()` antes de
borrar, en vez de dejar que reviente la FK con un error crudo de MySQL.
