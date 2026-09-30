# 015 — Alimento: catálogo compartido, macros por 100 g, creación siempre manual

Estado: aceptada · 2026-09-30

## Contexto

[[fusion]] define `Alimento` como "catálogo con macros por 100 g", con `fuente_externa` e `id_externo`, y sin `usuario_id` en [[modelo-datos]]. La API de alimentos está sin elegir, y `CLAUDE.md` pide `usuario_id` en toda tabla de datos personales.

## Decisión

- **Catálogo compartido, sin `usuario_id`**, igual que `titulo` en [[odisea]]: es un dato del mundo (cuánto tiene un arroz), no personal. Lo personal serán las comidas y sus líneas, que sí llevarán `usuario_id`.
- **Los macros son por 100 g y en `DECIMAL`.** Nunca se guarda un total: se calcula con la cantidad de cada línea, así que corregir un alimento corrige el histórico.
- **La creación por la API es siempre `MANUAL`**: el cuerpo del `POST` no lleva `fuente_externa` ni `id_externo`, y aunque se manden se ignoran. El `PUT` los conserva. Un alimento hecho a mano no puede hacerse pasar por uno importado.
- **`fuente_externa` es un `ENUM('MANUAL')` de MySQL**, con `UNIQUE(fuente_externa, id_externo)` para evitar duplicados al importar (los `MANUAL` llevan `id_externo` nulo y no colisionan). Cuando se elija la API, su valor se añade con una migración nueva, como `V2` con OpenLibrary.
- **Rangos físicos** en la validación: cada macro entre 0 y 100 g por 100 g, y kcal entre 0 y 900 (grasa pura). No se exige que las kcal cuadren con los macros: las etiquetas redondean.
- `?q=` busca en `nombre` y `marca`, sin distinguir mayúsculas ni tildes ("platano" encuentra "Plátano", por la collation `utf8mb4_unicode_ci`). Listado ordenado por nombre.
- Ruta `/api/fusion/alimento`, CRUD completo.

## Alternativas descartadas

- **`usuario_id` en `alimento`** (catálogo por usuario). Cada usuario repetiría "arroz" y los datos importados de una API se duplicarían por usuario. Con un solo usuario hoy no cuesta, pero es la decisión cara de deshacer.
- **`fuente_externa` como `VARCHAR`** para no migrar al añadir la API. `modelo-datos.md` lo decía así, pero Hibernate valida un `@Enumerated(STRING)` contra un `ENUM` de MySQL, y es lo que usa el resto del esquema.
- **Dejar que el cliente mande la fuente.** Permite fichas manuales con un `id_externo` inventado que luego chocarían con el unique al importar.
- **Guardar los macros ya multiplicados por la cantidad.** Un error de hoy contamina el histórico para siempre ([[fusion]]).

## Consecuencias

- **Cualquier usuario puede editar o borrar cualquier alimento** (mismo caso que `Titulo`). Con un usuario da igual; si hay más, habrá que decidir quién es dueño de un alimento manual.
- **Pendiente con `ComidaLinea`:** un alimento usado en una comida no se podrá borrar (400, como `TituloService` con sus entradas). Hoy borra sin más porque esa tabla no existe. Está anotado en `AlimentoService`.
- Editar los macros de un alimento cambia retroactivamente los totales de todas las comidas que lo usan. Es la intención.
- Al elegir la API de alimentos hará falta: su adaptador detrás de un puerto, una migración que añada su valor al enum y un caso de uso de importación.
