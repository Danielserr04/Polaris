# 044 — Logros de todos los módulos, con la fecha calculada y su propio apartado

Estado: aceptada · 2026-10-02 · amplía [[043-logros-calculados-y-metas]]

## Contexto

Los logros solo existían en Atlas (12, en la pestaña Metas) y sin fecha de obtención ([[043-logros-calculados-y-metas]]). Daniel los quiere de toda la app, en un apartado propio junto al perfil, más completos y con mejor pinta. Para eso hacen falta dos cosas que la 043 dejó fuera: logros de cada módulo y saber **cuándo** se consiguió cada uno.

## Decisión

**Cada módulo calcula sus logros; la pantalla los junta.**
- `GET /api/{nucleo,odisea,kuiper,fusion,atlas}/logros`, todos con la misma respuesta (`LogroDto`). Cada módulo tiene su catálogo fijo en su `LogroService`, su `MetricaLogro` y un puerto `EstadisticasLogrosPort` (en Atlas sigue siendo `EstadisticasEntrenoPort`) con su adaptador JPA que trae solo fechas y recuentos, nunca entidades enteras.
- La pantalla `/logros` del frontend pide los cinco en paralelo. Si uno falla, el resto se ve y avisa de cuál falta.
- 55 logros en cuatro niveles (`BRONCE`, `PLATA`, `ORO`, `PLATINO`): Odisea 10, Kuiper 12, Fusión 11, Atlas 13, Núcleo 9. Los pesajes pasan de Atlas a Núcleo, que es donde vive el peso; mantienen sus códigos.

**La fecha se calcula, no se guarda.**
- Cada métrica se convierte en una lista de *hitos* (`Hito`: fecha y cantidad). El progreso es la suma; la fecha del logro es la del hito con el que el acumulado llega al objetivo. 10 sesiones: la fecha de la décima. 10 t: la sesión con la que el volumen acumulado pasa de 10.000 kg. Racha de 4 semanas: el primer día entrenado de la cuarta semana seguida. Meta de ahorro: la aportación que llega al objetivo. Mes en verde: el último día del mes, y solo cuenta cuando ha terminado.
- Lo que no tiene fecha en la base (recetas, planes, presupuestos, perfil, títulos terminados sin fecha de fin) cuenta igual, pero el logro sale conseguido sin día. Los hitos sin fecha van al final: si el que llega al objetivo es uno de ellos, no hay día.
- Lo común (`Logro`, `NivelLogro`, `Hito`, `CalculoLogros`, `LogroDto`, `LogroDtoMapper`) vive en `shared/logro/`: Java plano salvo el DTO y su mapper, sin conocer a ningún módulo. Los módulos lo usan como usan `shared/error`.

**Apartado propio.** Botón de trofeo junto al avatar (isla de navegación en escritorio, barra de arriba en móvil). La pestaña Metas de Atlas deja la rejilla de logros y enlaza al apartado (`/logros?modulo=atlas`).

## Alternativas descartadas

- **Tabla `logro_conseguido` con la fecha.** Se apuntaría la fecha de cuando se *mira* el logro, no la de cuando se consiguió: al estrenar la tabla, todo lo ya conseguido saldría con fecha de hoy. Además obliga a escribir en un `GET` o a engancharse a cada alta de cada módulo, y un dato borrado no "desconsigue" nada. Calcular desde los datos da la fecha real, también para lo apuntado antes, sin migración.
- **Un módulo `logros` que lea de todos.** Rompe la regla 2: un módulo no llama a otro. Juntar es trabajo de la pantalla, como ya hace Inicio.
- **Un endpoint único en `shared/`.** `shared/` no conoce a los módulos.
- **Guardar `creado_en` en recetas y planes para fecharlos.** Daría fecha solo a lo creado desde ahora y necesita migración. Para un logro de bronce no compensa; se puede añadir si se echa en falta.

## Consecuencias

- Ninguna tabla ni migración nueva. Cada `GET /logros` hace entre dos y cuatro consultas agregadas del usuario.
- Borrar datos puede quitar un logro o cambiar su fecha. Es coherente con la 043.
- No hay aviso de "logro nuevo": haría falta saber qué logros ya se vieron, y eso sí sería estado guardado. Queda para cuando se pida (encaja con las notificaciones).
- Añadir un logro es una línea en el catálogo de su módulo; una métrica nueva, un método en su puerto.
- `NavBar` del design system gana una ranura `actions` junto al avatar ([[030-frontend-react-vite-y-design-system]]).
