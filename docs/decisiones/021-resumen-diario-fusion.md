# 021 — Resumen diario de Fusión: macros del día contra el objetivo, al vuelo

Estado: aceptada · 2026-09-30

## Contexto

[[fusion]] anticipa `GET /api/fusion/resumen?fecha=` ("macros del día vs objetivo"). Ya existen las comidas con macros calculados al vuelo ([[017-comida-agregado-con-lineas-macros-al-vuelo]]) y el objetivo histórico ([[016-objetivo-nutricional-historico-inmutable]]), que dejó dicho que el resumen usaría "el vigente de cualquier día pasado". Faltaba fijar la forma de la respuesta, qué pasa sin objetivo y cómo se calculan los porcentajes. Sigue el patrón del resumen mensual de Kuiper ([[014-resumen-mensual-agregado-en-servicio]]).

## Decisión

- `GET /api/fusion/resumen?fecha=2026-09-30` (formato `yyyy-MM-dd`). Sin `fecha`, **hoy** (`LocalDate.now()`, con la zona de [[019-zona-horaria-europe-madrid]]). Fecha inválida da **400**. Un día sin comidas devuelve ceros, no 404. Se admite cualquier fecha, también futura (saldrá a cero).
- Respuesta: `fecha`, `objetivoVigenteDesde` y cuatro macros: `kcal`, `proteinas`, `carbohidratos`, `grasas`. Cada macro trae `consumido`, `objetivo`, `restante` (objetivo menos consumido, **puede ser negativo** si se ha excedido) y `porcentaje` (consumido sobre objetivo, en %, puede pasar de 100). Todo `BigDecimal` con escala 2 (el objetivo entero se devuelve como `2000.00`).
- **Sin objetivo vigente en esa fecha** (no tiene ninguno, o todos empiezan después) **no es un error**: sale `consumido` y se omiten `objetivo`, `restante`, `porcentaje` y `objetivoVigenteDesde`. Los `null` se omiten del JSON por la política global (`default-property-inclusion: non_null`), igual que `limiteMensual` en Kuiper.
- El objetivo es el **vigente en `fecha`**, no el de hoy: el resumen de marzo se compara con lo que valía en marzo. Se pide con `ObjetivoNutricionalRepositoryPort.findVigente`, sin pasar por `getVigente` (que lanza 404).
- **Consumido = suma de `Comida.getTotales()`** de todas las comidas del usuario ese día, es decir, suma de líneas ya redondeadas por línea (`valor_100g * cantidad_g / 100`, escala 2, `HALF_UP`, en `Macros`). No se inventa otro criterio de redondeo: el total del día cuadra siempre con lo que se ve por comida y por línea.
- **Porcentaje:** `consumido * 100 / objetivo`, escala 2, `HALF_UP`. Si el objetivo de ese macro es 0 (es válido, [[016-objetivo-nutricional-historico-inmutable]]), `porcentaje` no se calcula (se omite) y `restante` sí.
- Se calcula en `ResumenDiarioService` **sumando en Java** las comidas del día (`ComidaRepositoryPort.findAll` con `fecha`), sin guardar nada. Sin migración ni tabla nueva.
- Solo lectura y no es una entidad, así que no sigue los 16 ficheros de la plantilla: `GetResumenDiarioInterface`, `ResumenDiarioService`, `ResumenDiarioController`, dos modelos (`ResumenDiario`, `MacroResumen`), `ResumenDiarioFilterListDto`, dos DTOs de salida y un mapper. Los nombres llevan `Diario` para no chocar con los beans `ResumenService`/`ResumenController` de Kuiper (el nombre de bean por defecto es el de la clase).
- Aislamiento: el `usuario_id` sale del JWT y filtra tanto las comidas como el objetivo.

## Alternativas descartadas

- **`SUM`/`GROUP BY` en SQL.** Mismo razonamiento que en 014: un día son pocas comidas, y los macros no están en base de datos (se calculan de alimento y gramos), así que habría que replicar el cálculo y su redondeo en SQL.
- **Sumar sin redondear por línea y redondear el total.** Puede diferir un céntimo de la suma de lo mostrado; contradice 017.
- **404 sin objetivo vigente**, como `GET /objetivo`. Aquí el objetivo es un dato secundario: lo consumido es válido igual, y obligaría al cliente a hacer dos llamadas.
- **Un bloque `objetivo` aparte** con los cinco enteros. Duplicaría lo que ya da `GET /api/fusion/objetivo`; el objetivo de cada macro va junto a su consumido, que es lo que pinta una pantalla.
- **Porcentaje sin decimales o como fracción (0.34).** Dos decimales en % da la misma precisión que el resto de la API y no obliga al cliente a multiplicar.
- **Devolver `null` explícito** con `@JsonInclude(ALWAYS)`. Rompería la convención global sin necesidad; el cliente trata "ausente" como "sin objetivo".

## Consecuencias

- Editar un alimento cambia retroactivamente el resumen de todos los días que lo usan (es la intención de [[015-alimento-catalogo-compartido-macros-por-100g]]).
- El "hoy" de `?fecha=` omitido lo decide el servidor; un frontend en otra zona debe mandar la fecha.
- El mensaje de error de una `fecha` inválida sale del manejador global de `shared/` (400, ya legible).
- No hay desglose por comida ni por momento: para eso está `GET /api/fusion/comida?fecha=`.
