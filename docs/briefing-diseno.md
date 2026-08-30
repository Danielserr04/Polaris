# Briefing de diseño — D0

Texto para pegar en Claude Design (u otra herramienta de diseño). **No tiene
acceso a este repo ni a las sesiones de Claude Code**, así que esta nota es
autosuficiente a propósito: repite datos que ya están en [[vision]] y
[[modelo-datos]] porque el que la lee no puede consultarlos.

Decisiones tomadas el 2026-08-30: carácter **mixto** (lista densa, ficha
visual), **tema oscuro primero**, **escritorio primero**, y esta primera tanda
es **solo dirección visual**, sin flujos.

---

## A partir de aquí, lo que se pega

Estoy construyendo **Polaris**, una app web personal que reúne en un sitio lo
que hoy tengo repartido en apps sueltas. Es de uso propio, no un producto: lo
más útil gana a lo más vendible. Se abre varias veces al día.

El backend ya existe y funciona (Spring Boot + MySQL). El frontend será React y
más adelante una PWA instalable. **Todavía no hay ni una línea de front escrita,
ni ninguna decisión visual tomada.** Empezamos por ahí.

### Qué te pido en esta primera tanda

**Solo la dirección visual.** Nada de flujos ni de todas las pantallas todavía:

1. **Paleta**, en modo oscuro. Fondo, superficies elevadas, texto primario y
   secundario, borde, color de acento y los colores de estado (ver más abajo).
   Defínela como *tokens* con nombre, no como valores sueltos: más adelante
   quiero un modo claro y quiero derivarlo sin rehacerlo.
2. **Tipografía.** Familia y escala. Va a haber mucha lista con datos, así que
   importa más la legibilidad a tamaño pequeño que el carácter.
3. **Componentes base**: fila de lista, badge de estado, botón (primario y
   secundario), campo de búsqueda, tarjeta de detalle, valoración en estrellas.
4. **Dos pantallas de muestra** que los usen todos:
   - **El dashboard**, que hace de recibidor: da entrada a los cinco módulos y
     enseña un *widget* de cada uno con lo importante de un vistazo.
   - **El listado de Odisea**, que es la pantalla de trabajo típica.

Escritorio primero, ancho 1440. El móvil viene después.

### Sobre el dashboard

Es lo primero que veo al abrir la app. Tiene que responder a "¿qué tengo hoy?"
sin hacer clic, y a la vez ser la puerta a los cinco módulos.

Un widget por módulo. Lo que enseñaría cada uno:

| Widget | Qué muestra |
|---|---|
| **Odisea** | Lo que tengo en curso, con su progreso. 3 o 4 elementos |
| **Kuiper** | Gastado este mes contra el presupuesto |
| **Fusión** | Macros de hoy contra el objetivo |
| **Atlas** | Última sesión y la siguiente que toca |
| **Núcleo** | Peso actual y la tendencia |

**Aviso importante y no negociable:** de esos cinco módulos **solo Odisea existe
de verdad**. Kuiper, Fusión, Atlas y Núcleo no están construidos: no hay API ni
tablas. Sus widgets son **maqueta especulativa** — los números que pongas ahí te
los estarás inventando, y hay que tratarlos como tales.

Diséñalos igualmente, porque quiero ver cómo queda el conjunto y porque el
dashboard es lo que decide la retícula del shell. Pero:

- **Marca en tu entrega qué widgets son reales y cuáles inventados.** No quiero
  descubrirlo yo dentro de tres semanas.
- Prioriza que la **retícula aguante** widgets de tamaños y formas distintos (una
  lista, una barra de progreso, un número grande, una gráfica pequeña) por encima
  de que los datos concretos sean verosímiles.
- El widget de Odisea sí con datos reales, los de más abajo.

### El carácter que busco

**Lista densa, ficha visual.** Son dos registros distintos a propósito:

- **Las listas son una herramienta.** Filas finas, mucha información por
  pantalla, tipografía pequeña, poco color. Quiero escanear cuarenta títulos sin
  scroll. Sin carátulas en la lista, o como mucho una miniatura pequeña.
- **La ficha de detalle sí respira.** Carátula grande, aire, jerarquía clara.

Que ambos se sientan de la misma app es justo lo difícil, y es lo que te pido
que resuelvas.

Referencias de densidad: Linear, Notion. Referencia de ficha: Letterboxd.
**No copies ninguna**, son solo para situar el nivel de densidad.

### Los datos son reales, úsalos

Esto sale de mi base de datos ahora mismo. **No inventes campos ni uses texto de
relleno**: si un dato no está aquí, no existe en la app.

Un **Titulo** (la ficha del contenido, compartida) tiene:

| Campo | Ejemplo |
|---|---|
| tipo | `PELICULA`, `SERIE`, `JUEGO`, `LIBRO` |
| titulo | "Interstellar" |
| tituloOriginal | "Interstellar" (puede faltar) |
| anio | 2014 |
| sinopsis | Párrafo largo. Puede faltar |
| imagenUrl | Carátula. Puede faltar |
| generos | "Aventura, Drama, Ciencia ficción" |
| duracionMin | 169 — **minutos** en pelis y series, **páginas** en libros, **vacío** en juegos |
| fuenteExterna | `TMDB`, `IGDB`, `OPEN_LIBRARY`, `MANUAL` |

Una **Entrada** (mi relación con ese título) tiene:

| Campo | Ejemplo |
|---|---|
| estado | `PENDIENTE`, `EN_CURSO`, `TERMINADO`, `ABANDONADO` |
| valoracion | 0 a 10, o vacío |
| notas | Texto libre, puede faltar |
| fechaInicio / fechaFin | Fechas, pueden faltar |
| favorito | sí / no |
| progreso | Un entero: el episodio por el que voy |

**Cuatro estados y cuatro tipos**: necesito que se distingan de un vistazo en una
lista, y que no compitan entre sí. Ese es el problema de color más concreto que
tienes que resolver.

Contenido real para las maquetas, con sus datos de verdad:

```
Interstellar          PELICULA  2014  169 min   Aventura, Drama, Ciencia ficción
Breaking Bad          SERIE     2008   45 min   Drama, Crimen
The Legend of Zelda   JUEGO     1986        —   Aventura
Dune                  LIBRO     1965  607 pág   Ciencia ficción, Ficción
Blade Runner 2049     PELICULA  2017  164 min   Ciencia ficción, Drama
```

Ojo con dos cosas que pasan de verdad: **muchos campos vienen vacíos** (un juego
sin fecha, un libro sin carátula), y **los títulos son largos** ("The Legend of
Zelda: Breath of the Wild"). El diseño tiene que aguantar ambas sin romperse.

### La app tendrá cinco módulos

El shell tiene que dar sitio a cinco secciones, aunque ahora solo diseñemos una:

| Módulo | Qué es |
|---|---|
| **Odisea** | Ocio: pelis, series, juegos, libros ← *la que diseñamos ahora* |
| **Kuiper** | Gastos, ingresos, presupuestos |
| **Fusión** | Nutrición: alimentos, comidas, macros |
| **Atlas** | Gym: ejercicios, rutinas, sesiones |
| **Núcleo** | Perfil y peso corporal |

Los nombres son espaciales por decisión explícita. Si eso te sugiere algo para
la dirección visual, adelante — pero **no quiero una app "de temática espacial"**
con estrellas y planetas. El nombre es un guiño interno, no el tema.

Kuiper y Fusión van a ser pantallas con **tablas y gráficas**, muy distintas de
Odisea. Ténlo en cuenta al elegir tipografía y densidad: lo que decidas aquí
tiene que servir también ahí.

### Restricciones

- **Modo oscuro primero**, pero la paleta en tokens para poder derivar el claro.
- **Se implementará en React.** No propongas nada que dependa de una librería de
  componentes concreta; prefiero decidir eso después.
- **PWA instalable** más adelante, y móvil. No lo diseñamos ahora, pero no me
  pintes algo que solo funcione a 1440.
- Es de **un solo usuario**. No hay onboarding, ni pantallas de equipo, ni
  planes de precios, ni landing.

### Qué no quiero

- Texto de relleno ni datos inventados en Odisea. Usa los de arriba. La
  excepción son los widgets de los módulos que aún no existen, y ahí quiero que
  esté marcado.
- Pantallas de marketing o de producto: esto no se vende a nadie.
- Densidad de dashboard corporativo con tarjetas grandes y huecas.
- Que la lista y la ficha parezcan de dos apps distintas.
