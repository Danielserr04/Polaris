# Fusión

Nutrición. Alimentos, comidas del día, macros y objetivos.

Código nuevo. Cubre la mitad nutricional de lo que intentaba `fitcore`, un proyecto anterior que quedó sin terminar; la otra mitad es [[atlas]]. Ver [[003-modulos-separados-fusion-atlas]]. Va en **B6** del [[roadmap]].

## Entidades

**`Alimento`** — catálogo con macros por 100 g.

**`Comida`** — una ingesta: fecha y momento del día, con sus líneas. Es un agregado ([[017-comida-agregado-con-lineas-macros-al-vuelo]]).

**`ComidaLinea`** — qué alimento y cuánto, dentro de una comida. Lleva su propio `usuario_id`. No tiene API propia: se maneja siempre a través de `Comida`.

**`Receta`** — un plato reutilizable: ingredientes (alimento y gramos de la receta entera) y raciones. Macros totales y por ración al vuelo ([[041-receta-agregado-con-ingredientes]]).

**`PlanComida`** — una semana tipo: qué comer cada día y momento, con alimentos o recetas. Un plan activo por usuario y lista de la compra calculada ([[042-plan-de-comidas-y-lista-de-la-compra]]).

**`ObjetivoNutricional`** — kcal y macros objetivo, con `vigente_desde`. Solo se crean, nunca se editan ni se borran ([[016-objetivo-nutricional-historico-inmutable]]).

Esquema completo en [[modelo-datos]].

## Las dos decisiones de diseño

**Macros por 100 g, nunca el total calculado.** Se calcula al vuelo multiplicando por `cantidad_g`. Si corriges los datos de un alimento, se corrige todo el histórico solo. Guardar totales significa que un error de hoy contamina para siempre lo que ya registraste.

**Los objetivos no se sobrescriben.** Cada cambio es una fila nueva con su `vigente_desde`. Así puedes mirar atrás y saber contra qué objetivo estabas comiendo en marzo, en vez de compararlo todo con el objetivo de hoy. El vigente en una fecha es el de mayor `vigente_desde` menor o igual a ella; dos objetivos con la misma fecha dan 409.

## Relación con Núcleo

Fusión **lee y escribe el peso corporal de [[nucleo]]**: aparece dentro de este módulo, se puede consultar y añadir desde aquí, pero no tiene tabla propia.

Lo usa para calcular necesidades calóricas, junto con el perfil (altura, edad, sexo, actividad), que lee por un puerto propio sin tabla propia ([[043-calculo-del-objetivo-desde-el-perfil]]). Qué es un "objetivo de peso" y cómo se interpreta la evolución lo decide Fusión, no Núcleo.

## Endpoints

```
GET    /api/fusion/alimento?q=            busca en nombre y marca
GET    /api/fusion/alimento/{id}
POST   /api/fusion/alimento               siempre MANUAL
PUT    /api/fusion/alimento/{id}
DELETE /api/fusion/alimento/{id}          400 si está en alguna comida

GET    /api/fusion/catalogo/buscar?q=     busca en Open Food Facts
POST   /api/fusion/catalogo/importar      {"idExterno": "<código de barras>"}; 201 si crea, 200 si ya existía

GET    /api/fusion/comida?fecha=&desde=&hasta=&momento=
GET    /api/fusion/comida/{id}            con líneas y macros
POST   /api/fusion/comida
PUT    /api/fusion/comida/{id}            reemplaza el conjunto de líneas
DELETE /api/fusion/comida/{id}            borra también sus líneas

GET    /api/fusion/objetivo?fecha=        el vigente en esa fecha (hoy si se omite); 404 si no hay
GET    /api/fusion/objetivo/historico     todos, del más reciente al más antiguo
POST   /api/fusion/objetivo               crea uno nuevo, no sustituye
GET    /api/fusion/objetivo/calculo?tipo=  propone kcal y macros desde el perfil y el último peso; no guarda

GET    /api/fusion/receta?q=
GET    /api/fusion/receta/{id}            con ingredientes, totales y por ración
POST   /api/fusion/receta
PUT    /api/fusion/receta/{id}            reemplaza los ingredientes
DELETE /api/fusion/receta/{id}            400 si está en algún plan

GET    /api/fusion/plan?activo=
GET    /api/fusion/plan/{id}              líneas por día y momento, media diaria
POST   /api/fusion/plan                   nace sin activar
PUT    /api/fusion/plan/{id}              reemplaza nombre y líneas
POST   /api/fusion/plan/{id}/activar      pasa a ser el único activo
GET    /api/fusion/plan/{id}/lista-compra gramos por alimento para la semana
DELETE /api/fusion/plan/{id}

GET    /api/fusion/resumen?fecha=         macros del día vs objetivo vigente en esa fecha (por defecto hoy)

GET    /api/fusion/peso?desde=&hasta=     peso corporal (vive en Núcleo)
POST   /api/fusion/peso                   apunta o actualiza el del día
```

El listado de `comida` va ordenado por fecha descendente y momento en el orden del día (DESAYUNO, COMIDA, CENA, SNACK); devuelve solo los totales, no las líneas. Los macros de una línea y los totales de la comida se calculan al vuelo, con escala 2; el total es la suma de las líneas ya redondeadas.

## API de alimentos

**Open Food Facts**, decidido en [[018-alimentos-open-food-facts]]. Gratis, abierta y **sin claves**: solo hay que identificarse con un `user-agent` (`polaris.openfoodfacts.*` en la configuración). Adaptador de salida `OpenFoodFactsAdapter` detrás de `CatalogoAlimentoExternoPort`, igual que en [[odisea]].

- **Búsqueda y filtro de calidad:** OFF es colaborativa y muchos productos vienen incompletos. El adaptador descarta lo que no tenga nombre y los cuatro valores por 100 g, o los tenga fuera de rango. Si solo hay kJ, convierte a kcal.
- **Importar es idempotente:** si ya existe un alimento con esa fuente y ese `idExterno`, se devuelve sin llamar a OFF. La búsqueda marca con `alimentoId` los ya importados.
- **Errores de OFF** (red, timeout, 5xx) salen como 502. Un producto inexistente o incompleto, como 400.
- **Sin verificar contra la API real** todavía: el adaptador se probó con respuestas simuladas. Ver las consecuencias de la decisión.

## Pendiente

- Probar el adaptador de Open Food Facts contra la API real

## Estado

| Entidad | Estado |
|---|---|
| `Alimento` | **Hecha** — CRUD en `/api/fusion/alimento`, búsqueda `?q=`. Creación manual o importada de Open Food Facts. Borrado bloqueado (400) si está en alguna comida. Ver [[015-alimento-catalogo-compartido-macros-por-100g]] y [[018-alimentos-open-food-facts]] |
| `Comida` | **Hecha** — CRUD en `/api/fusion/comida`, con líneas anidadas y macros al vuelo. Ver [[017-comida-agregado-con-lineas-macros-al-vuelo]] |
| `ComidaLinea` | **Hecha** — dentro del agregado `Comida`, sin endpoints propios. Lleva `usuario_id` |
| `ObjetivoNutricional` | **Hecha** — `GET` (vigente), `GET /historico` y `POST`; sin `PUT` ni `DELETE`. Ver [[016-objetivo-nutricional-historico-inmutable]] |
| Peso corporal | **Hecho** — `GET`/`POST /api/fusion/peso` a través de un puerto propio hacia Núcleo; sin tabla propia. Ver [[022-peso-corporal-desde-fusion-y-atlas]] |
| `Receta` | **Hecha** — CRUD en `/api/fusion/receta`, con ingredientes y macros por ración. Ver [[041-receta-agregado-con-ingredientes]] |
| `PlanComida` | **Hecho** — CRUD en `/api/fusion/plan`, activar y lista de la compra. Ver [[042-plan-de-comidas-y-lista-de-la-compra]] |
| Cálculo del objetivo | **Hecho** — `GET /api/fusion/objetivo/calculo`, Mifflin-St Jeor desde el perfil de Núcleo. Ver [[043-calculo-del-objetivo-desde-el-perfil]] |
| Resumen del día | **Hecho** — `GET /api/fusion/resumen?fecha=`. Ver [[021-resumen-diario-fusion]] |
| Logros | **Hechos** — `GET /api/fusion/logros`: comidas, días y rachas apuntando, objetivo, recetas y planes. Ver [[044-logros-globales-con-fecha-calculada]] |
