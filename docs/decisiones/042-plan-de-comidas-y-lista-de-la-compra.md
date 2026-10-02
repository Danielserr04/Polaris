# 042 — Plan de comidas semanal con lista de la compra calculada

Estado: aceptada · 2026-10-02

## Contexto

`fitcore` tenía planes de comidas por días con lista de la compra. Daniel pidió traerlos a Fusión. Faltaba decidir la forma del plan, qué puede llevar cada línea, cuántos planes activos hay y cómo se calcula la compra.

## Decisión

- **`PlanComida` con sus `PlanComidaLinea`**, un solo agregado como `Comida` (`V31`, `usuario_id` en las dos tablas). Un plan es una **semana tipo**: cada línea tiene `dia_semana` (LUNES…DOMINGO) y `momento` (los de la comida).
- **Cada línea es un alimento con gramos o una receta con raciones**, nunca las dos: 400 en el servicio y `CHECK` en base de datos. Las raciones admiten medias (`DECIMAL(5,2)`, hasta 50). Solo recetas propias (404 si no).
- De 0 a 200 líneas: se puede crear un plan vacío y rellenarlo después.
- **Como mucho un plan activo por usuario.** Nace sin activar; `POST /{id}/activar` deja ese como único activo con **un solo `UPDATE`** (sin estados intermedios). Editar conserva el estado.
- **Macros al vuelo:** línea de alimento = `Macros.de`; línea de receta = totales de la receta × raciones / raciones de la receta. La media diaria es la suma entre los **días que tienen algo** (un plan solo de lunes a viernes no se divide entre 7).
- **Lista de la compra** `GET /{id}/lista-compra`: gramos por alimento para toda la semana, sumando los alimentos sueltos y desplegando cada receta en sus ingredientes en proporción a las raciones (escala 2 por línea). Ordenada por nombre. No se guarda.
- El frontend puede **apuntar lo del plan** en un día: crea una comida por momento con los alimentos (recetas desplegadas) usando el `POST` de comida que ya existía.

## Alternativas descartadas

- **Plan con fechas concretas** (del 6 al 12 de octubre). Obliga a duplicarlo cada semana; la semana tipo es lo que tenía fitcore y lo que se repite.
- **`amountGrams` también para recetas**, como fitcore (la receta entera). Poco natural: se piensa en raciones.
- **Unique parcial para el activo.** MySQL no tiene índices parciales; una columna generada lo complica. Basta el `UPDATE` único.
- **Guardar la lista de la compra.** Se quedaría desfasada al editar el plan o una receta.

## Consecuencias

- Lo que ya está "cogido" en la lista se recuerda solo en el navegador (localStorage), no en el servidor.
- Borrar un alimento o una receta usados en un plan da 400 ([[041-receta-agregado-con-ingredientes]]).
