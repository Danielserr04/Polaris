# Fusión

Nutrición. Alimentos, comidas del día, macros y objetivos.

Código nuevo. Cubre la mitad nutricional de lo que intentaba `fitcore`, un proyecto anterior que quedó sin terminar; la otra mitad es [[atlas]]. Ver [[003-modulos-separados-fusion-atlas]]. Va en **B6** del [[roadmap]].

## Entidades

**`Alimento`** — catálogo con macros por 100 g.

**`Comida`** — una ingesta: fecha y momento del día.

**`ComidaLinea`** — qué alimento y cuánto, dentro de una comida.

**`ObjetivoNutricional`** — kcal y macros objetivo, con `vigente_desde`.

Esquema completo en [[modelo-datos]].

## Las dos decisiones de diseño

**Macros por 100 g, nunca el total calculado.** Se calcula al vuelo multiplicando por `cantidad_g`. Si corriges los datos de un alimento, se corrige todo el histórico solo. Guardar totales significa que un error de hoy contamina para siempre lo que ya registraste.

**Los objetivos no se sobrescriben.** Cada cambio es una fila nueva con su `vigente_desde`. Así puedes mirar atrás y saber contra qué objetivo estabas comiendo en marzo, en vez de compararlo todo con el objetivo de hoy.

## Relación con Núcleo

Fusión **lee y escribe el peso corporal de [[nucleo]]**: aparece dentro de este módulo, se puede consultar y añadir desde aquí, pero no tiene tabla propia.

Lo usa para calcular necesidades calóricas. Qué es un "objetivo de peso" y cómo se interpreta la evolución lo decide Fusión, no Núcleo.

## Endpoints

```
GET    /api/fusion/alimento?q=
POST   /api/fusion/alimento
GET    /api/fusion/comida?fecha=
POST   /api/fusion/comida
PUT    /api/fusion/comida/{id}
DELETE /api/fusion/comida/{id}
GET    /api/fusion/objetivo               el vigente
POST   /api/fusion/objetivo               crea uno nuevo, no sustituye
GET    /api/fusion/resumen?fecha=         macros del día vs objetivo
```

## API de alimentos

**Pendiente de decidir:** Open Food Facts (gratis y abierta, calidad irregular), FatSecret o Nutritionix (mejores datos, con límites de uso).

Adaptador de salida detrás de un puerto, igual que en [[odisea]].

## Pendiente

- Elegir la API de alimentos
- Recetas: agrupar alimentos en un plato reutilizable. Se valorará cuando el módulo básico funcione

## Estado

| Entidad | Estado |
|---|---|
| `Alimento` | Pendiente — B6 |
| `Comida` | Pendiente — B6 |
| `ComidaLinea` | Pendiente — B6 |
| `ObjetivoNutricional` | Pendiente — B6 |
