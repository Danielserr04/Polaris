# 041 — Receta: agregado con ingredientes, macros por ración al vuelo

Estado: aceptada · 2026-10-02

## Contexto

[[fusion]] dejaba pendiente "recetas: agrupar alimentos en un plato reutilizable". `fitcore` ya lo tenía (receta con ingredientes y macros por ración) y Daniel pidió traerlo. Hay que decidir de quién es una receta, cómo se calculan sus macros y qué pasa al borrar lo que usa.

## Decisión

- **Mismo patrón que `Comida`** ([[017-comida-agregado-con-lineas-macros-al-vuelo]]): `Receta` con sus `RecetaIngrediente` anidados, un solo agregado. `PUT` reemplaza los ingredientes; `DELETE` los borra en cascada por JPA. Las dos tablas llevan `usuario_id` (`V30`).
- **Las recetas son de cada usuario**, no catálogo compartido como `Alimento`: id ajeno = 404.
- **`cantidad_g` es para la receta entera**; `raciones` (1–50) dice para cuántos sale. Los macros no se guardan: total = suma de ingredientes (cada uno escala 2, `HALF_UP`, en `Macros`) y por ración = total / raciones, escala 2. Editar un alimento corrige todas las recetas, como en las comidas.
- De 1 a 50 ingredientes; nombre hasta 120, descripción hasta 500, preparación libre (`TEXT`).
- **Protección de borrado:** un alimento usado en alguna receta (de cualquier usuario) da 400, igual que si está en una comida. Una receta usada en algún plan ([[042-plan-de-comidas-y-lista-de-la-compra]]) también da 400.
- Una **comida no guarda la receta**: el frontend añade sus ingredientes escalados a las raciones. Así el registro del día sigue siendo alimentos y gramos y cambiar la receta mañana no altera lo que ya comiste.
- Plantilla completa de la entidad; `RecetaIngrediente` solo tiene Entity, mapper, repositorio y adaptador de uso, como `ComidaLinea`.

## Alternativas descartadas

- **Receta como alimento compuesto** (una fila en `alimento` con macros por 100 g calculados). Mezcla catálogo compartido con datos personales y obliga a recalcular al editar un ingrediente.
- **Guardar los macros por ración.** Contradice [[015-alimento-catalogo-compartido-macros-por-100g]].
- **Comida con líneas de receta.** El histórico cambiaría al editar la receta; se prefiere copiar los ingredientes.

## Consecuencias

- Editar una receta cambia los macros de los planes que la usan, no los de las comidas ya registradas.
- Una receta en un plan no se borra: hay que quitarla antes del plan.
