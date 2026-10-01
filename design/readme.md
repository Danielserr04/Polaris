# Polaris Design System

Polaris es una **app web personal** que junta en un solo sitio lo que antes vivía en apps sueltas: ocio, gastos, nutrición y gimnasio. Es para uso propio, no un producto que se venda: gana lo más útil, no lo más vendible. Se abre varias veces al día, así que tiene que apetecer abrirla.

**Fuente:** [github.com/Danielserr04/Polaris](https://github.com/Danielserr04/Polaris) — backend Spring Boot + MySQL (arquitectura hexagonal, un paquete por módulo) y una bóveda de Obsidian en `docs/`. **No hay ni una línea de frontend ni ninguna decisión visual en el repo**, así que este sistema es una dirección visual nueva, basada en `docs/briefing-diseno.md`, `docs/briefing-figma.md`, `docs/vision.md`, `docs/decisiones/004-nombres-espaciales.md` y `docs/modulos/*.md`, además del brief del usuario (minimalista, un toque de estilo bloque, corporativo, toques astronómicos sin ser 100 % espacial, muy animado). Explora el repo para ver el modelo de datos real y los endpoints antes de diseñar pantallas nuevas.

## Productos / módulos
| Módulo | Dominio | Estado en el backend | Acento |
|---|---|---|---|
| **Polaris** (shell, inicio) | La estrella que marca el norte | — | teal `#5bb3a0` |
| **Odisea** | Pelis, series, juegos y libros | **Real** (TMDB, IGDB, OpenLibrary) | rosa `#d0799f` |
| **Kuiper** | Ingresos, gastos, presupuestos | Pendiente (B5) → los datos de la maqueta son inventados | dorado `#e0b04a` |
| **Fusión** | Alimentos, comidas, macros | Pendiente (B6) → maqueta | naranja `#e8845a` |
| **Atlas** | Ejercicios, rutinas, sesiones | Pendiente (B7) → maqueta | azul `#7d95e0` |
| **Núcleo** | Perfil y peso corporal (compartido) | Pendiente (B4), sin interfaz propia | arena `#cdbf98` |

Auth/perfil (usuario + contraseña, Google OAuth, verificación de email) **es real**: la pantalla de Perfil sigue al pie de la letra los 4 estados que salen de `tieneGoogle / tienePassword / emailVerificado`.

---

## CONTENT FUNDAMENTALS
- **Idioma:** español de España, **tuteo**. La app te habla a ti ("Lo que tienes entre manos", "Te quedan 660 kcal"). Nunca "nosotros" salvo en acciones del sistema ("Te hemos enviado un enlace").
- **Tono:** cercano, calmado y exacto. Sin marketing, sin signos de exclamación ni frases grandilocuentes. Explica el *porqué* cuando algo está bloqueado ("Pon antes una contraseña: sin ella no te quedaría ninguna forma de entrar").
- **Mayúsculas:** frases en *sentence case* ("Añadir título", "Cambiar contraseña"). Las MAYÚSCULAS solo en las etiquetas mono (eyebrows, cabeceras de columna, badges), con tracking +0.1em.
- **Nombres de módulo:** siempre con su nombre propio (Odisea, Kuiper, Fusión, Atlas, Núcleo) y la tilde en la interfaz; sin tilde solo en el código (`fusion`, `nucleo`).
- **Números:** formato español: `1.284,50 €`, `78,2 kg`, `169 min`, `607 pág`. Siempre con su unidad, en mono y con cifras tabulares. Un campo vacío se muestra como **—** en gris apagado, nunca como "N/A".
- **Estados de error:** frases cortas y literales, como las que devuelve la API: "La contraseña actual no es correcta.", "Ese email ya está registrado por otra cuenta.", "Ese ya es tu email."
- **Estados vacíos:** una línea, sin ilustración: "Sin sinopsis.", "Nada con estos filtros."
- **Voz astronómica:** solo como guiño, en las etiquetas mono tipo coordenada: `✦ KUIPER · SEPTIEMBRE 2026`, `α UMi · RA 02h 31m 49s · Dec +89° 15′ 51″` en el login. Nunca en frases corridas ("¡Despega!" está prohibido).
- **Emoji:** nunca. El único glifo unicode es **✦** (estrella, marcador de acento / favorito).
- **Datos inventados:** cualquier widget sin backend lleva el badge `Maqueta · datos inventados`.

## VISUAL FOUNDATIONS
- **Carácter:** una herramienta minimalista y corporativa con alma. **Listas densas, fichas que respiran.** Tema oscuro cálido primero (`--ink-50 #12110e`, nunca gris azulado); hay un tema claro derivado vía `[data-theme="light"]`.
- **Color:** neutros cálidos (rampa `--ink-*`) + **un acento por módulo**. El acento base es el teal de Polaris. Envolver cualquier subárbol en `data-module="odisea|kuiper|fusion|atlas|nucleo|polaris"` re-tiñe toda la familia `--accent*` (botones, foco, gráficos, sombras de bloque). Los **estados de Odisea** tienen colores propios (gris / cielo / salvia / rojo) **y además forma** (anillo / medio / lleno / tachado), así que nunca compiten con los tipos, que son neutros (icono + etiqueta mono).
- **Tipografía:** contraste fuerte. **Archivo** expandida (wdth 118–125 %, peso 800, tracking −0.02em) para títulos y números grandes; **Instrument Sans** 400–600 para el texto; **JetBrains Mono** para etiquetas, datos y coordenadas. El cuerpo va a 15 px; las listas a 13,5 px.
- **Estilo bloque (medio):** radios pequeños (2/4/6 px; 10 px en la isla del nav), bordes cálidos de 1 px, **sin sombra en reposo**. Al pasar el ratón, lo interactivo se desplaza −2/−3 px y proyecta una **sombra dura desplazada** (`4–5px 4–5px 0`) en `--accent-deep`. Los inputs con foco hacen lo mismo. Al pulsar: vuelve a 0 y escala 0,98.
- **Tarjetas:** `--surface-1`, borde `--border-1`, radio 6 px, cabecera con eyebrow mono (✦ + módulo) y título en display de 18 px. Nada de tarjetas grandes y huecas: cada una responde a una pregunta.
- **Fondos:** superficies planas. El shell tiene un **brillo radial muy tenue** del color del módulo en la parte de arriba (9 % de acento). La landing es la única pantalla con imagen: **estelas de estrellas en canvas** girando alrededor de Polaris, con un velo que protege el texto. No hay texturas, ruido ni ilustraciones.
- **Movimiento (muy expresivo, siempre con propósito):** entradas escalonadas (`polaris-rise`, 6 px + fade, 35–60 ms de paso), indicadores de nav, segmentos y pestañas que se deslizan con muelle (`--ease-spring`), barras que crecen, líneas que se dibujan (1,4 s), anillos que barren y números que cuentan. Del login al dashboard, las estrellas aceleran en una transición tipo *warp* y la pantalla se desenfoca. Se respeta `prefers-reduced-motion` en el canvas.
- **Hover:** filas → `--surface-hover`; botones y tarjetas → elevación de bloque; iconos del nav → giran −6°; estrellas de valoración → escalan y giran.
- **Transparencia y desenfoque:** solo en la isla del nav flotante (`--surface-glass` + `blur(16px)`), en la tarjeta del login y en el velo de los diálogos. En ningún otro sitio.
- **Layout:** sin barra lateral: una **isla de navegación flotante centrada arriba** (fija, top 16 px). Contenido con `max-width: 1560px`, retícula de 12 columnas con 16 px de gap y cabecera de página con eyebrow + H1 display de 48 px.
- **Gráficos:** las barras van en un tinte apagado del acento y la barra importante en acento puro; la línea de referencia es discontinua (`--text-2`); retícula discontinua en `--border-1`; ejes en mono de 10,5 px. Los objetivos siempre discontinuos.
- **Imagen:** las carátulas son reales cuando las hay (TMDB/IGDB/OpenLibrary). Si no, se usa un **placeholder tipográfico** (degradado del acento → ink, título en display, estrella ✦).

## ICONOGRAPHY
- **Lucide** (líneas de 2 px, esquinas redondeadas), cargado desde CDN como `lucide-static@0.460.0` y pintado con máscara CSS para que herede `currentColor` → componente `<Icon name="…"/>`. El repo no trae ningún set de iconos: **es una sustitución** elegida por su trazo neutro y corporativo.
- Tamaños: 14–16 px en la interfaz densa y 16–20 px en el nav y las cabeceras.
- Iconos de módulo: Inicio `compass`, Odisea `clapperboard`, Kuiper `wallet`, Fusión `flame`, Atlas `dumbbell`, Núcleo `orbit`. Tipos: `clapperboard` (película), `tv` (serie), `gamepad-2` (juego), `book-open` (libro).
- Sin emoji ni fuentes de iconos. El único carácter unicode es ✦.
- **Logo:** no había logo en el repo; lo pidió el usuario y se generó con código: `assets/logo-mark.svg` (estrella de 4 puntas recortada en un cuadrado de bloque con sombra desplazada), `assets/logo-mark-mono.svg`, `assets/app-icon.svg` y los PNG de 512/192 (icono de la PWA: estrella teal con órbitas finas sobre ink). En React: `<Logo/>`.

---

## Estado y decisión
**Este sistema sustituye a la dirección visual anterior** definida en `docs/briefing-diseno.md` y `docs/briefing-figma.md` del repo. Es la referencia para construir el frontend de Polaris. El Inicio canónico es el layout de **bandas** (`Dashboard.jsx`); `DashboardGrid.jsx` se conserva solo como alternativa.

### Pendientes
- [ ] Fuentes en local: meter los archivos de Archivo, Instrument Sans y JetBrains Mono en `assets/fonts/` y reemplazar el @import de Google Fonts.
- [ ] Logo oficial de Google para el botón "Continuar con Google" del login.
- [ ] Llevar Kuiper, Fusión y Atlas al sistema de bandas del Inicio (ahora siguen en retícula de tarjetas).
- [ ] Revisar las pantallas con el tema claro (`[data-theme="light"]`); de momento solo existen los tokens.
- [ ] Sustituir los datos de maqueta de Kuiper, Fusión, Atlas y Núcleo cuando existan sus backends (B4–B7).

## Index
- `styles.css` → solo `@import`: `tokens/fonts.css` (Google Fonts), `tokens/colors.css`, `tokens/typography.css`, `tokens/spacing.css`, `tokens/base.css` y `components/components.css` (clases `pl-*`)
- `guidelines/` → tarjetas de fundamentos (colores, tipografía, espaciado, marca)
- `assets/` → logo, icono de la app
- `components/` → primitivas de React (ver abajo)
- `ui_kits/polaris-web/` → la app completa clicable (login → Inicio, Odisea, Kuiper, Fusión, Atlas, Perfil)
- `SKILL.md` → punto de entrada como Agent Skill

## Components
- **core/**: Button, IconButton, Icon, Logo, Avatar, Eyebrow, Badge, StatusBadge, StateGlyph, TypeTag, Card
- **forms/**: Input, Kbd, Select, Checkbox, Switch, SegmentedControl
- **data/**: ListRow, ListHeader, Rating, ProgressBar, Stat, BarChart, LineChart, RingChart
- **navigation/**: NavBar, Tabs
- **feedback/**: Alert, Dialog, Toast, Tooltip

El repo no define ningún inventario de componentes: se partió del set que pide `docs/briefing-diseno.md` (fila de lista, badge de estado, botón, búsqueda, tarjeta de detalle, valoración) y se amplió con lo que necesitan las gráficas y el shell.
