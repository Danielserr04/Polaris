# 018 — Usar Open Food Facts como API externa de alimentos

Estado: aceptada · 2026-09-30

## Contexto

[[fusion]] tenía la API de alimentos sin elegir (Open Food Facts, FatSecret o Nutritionix). [[015-alimento-catalogo-compartido-macros-por-100g]] dejó preparado el catálogo (`fuente_externa`, `id_externo`, unique) y anotó que elegir la API exigiría un puerto, una migración del enum y un caso de uso de importación. Polaris es una app personal: importa que no haga falta ninguna clave.

## Decisión

Se usa **Open Food Facts (OFF)**, detrás del puerto `CatalogoAlimentoExternoPort` y del adaptador `OpenFoodFactsAdapter`, con `GET /api/fusion/catalogo/buscar?q=` y `POST /api/fusion/catalogo/importar` (cuerpo `{"idExterno": "<código de barras>"}`). Sin clave ni secreto; solo `polaris.openfoodfacts.url-base`, `user-agent` (OFF exige identificarse) y `timeout-segundos` (15, conexión y lectura).

**Calidad de datos.** OFF es colaborativa y muchos productos están incompletos. El adaptador es el filtro y nunca deja pasar lo que Polaris no guardaría:

- **Búsqueda:** se descartan los resultados sin nombre o sin alguno de los cuatro valores (`energy-kcal_100g`, `proteins_100g`, `carbohydrates_100g`, `fat_100g`), o con alguno fuera de rango (macros 0-100, kcal 0-900, según 015). Se piden 40 a OFF y se devuelven como mucho 20 válidos.
- **Importación:** el mismo criterio, pero como error: producto sin nombre, sin los 4 valores o fuera de rango → `ValidationException` (400). Producto inexistente (404 o `status: 0`) → 400. Nunca se guarda basura ni se rellena con ceros.
- **kJ y kcal:** se usa `energy-kcal_100g`. Si falta, se convierte desde kJ (`energy-kj_100g` o `energy_100g`, que en OFF va en kJ) dividiendo entre 4,184. Se prefiere convertir a descartar: muchos productos solo traen kJ y el dato es equivalente. Tras convertir se aplica el rango.
- **Valores:** OFF mezcla números y cadenas numéricas; se aceptan ambos, lo ilegible cuenta como ausente. Se redondea a 2 decimales (la columna). Nombre y marca se recortan a 150 y 100 caracteres; de `brands` solo se guarda la primera marca.
- **Errores de OFF** (red, timeout, 5xx, 429, JSON ilegible) → `ExternalServiceException` (502), como en Odisea.
- El `idExterno` solo admite dígitos (código de barras, máx. 20): va en la ruta de la petición a OFF y no se deja pasar nada más.

**Importar es idempotente.** Si ya existe un alimento con (`OPEN_FOOD_FACTS`, `idExterno`) se devuelve ese (200) sin llamar a OFF; si no, se crea (201). Distinto de Odisea (siempre 201) porque aquí no se crea nada personal: el catálogo es compartido y no hay `usuario_id`. La búsqueda marca con `alimentoId` los resultados ya importados.

`FuenteAlimento` gana `OPEN_FOOD_FACTS` y la migración `V11` modifica el ENUM. La numeración salta V9-V10 (otras ramas): Flyway lo admite.

## Alternativas descartadas

- **FatSecret.** Exige registro y credenciales OAuth, y el plan gratuito tiene límites y condiciones de uso restrictivas (atribución, caché limitada).
- **Nutritionix.** Exige app id y clave, y el plan gratuito es limitado y solo para uso no comercial acotado.
- **USDA FoodData Central.** Gratuita pero pide clave, y su cobertura es de alimentos genéricos de EE. UU., con poca presencia de productos envasados europeos y sin código de barras fiable.
- **Descartar los productos que solo traen kJ.** Se perdería mucho catálogo válido.
- **Guardar productos incompletos con ceros.** Un macro a 0 falsea los totales del diario para siempre (015).

## Consecuencias

- Sin claves ni secretos que gestionar; cualquier persona puede levantar el proyecto.
- OFF es lenta a veces y sin garantías de servicio: el timeout convierte los cuelgues en 502. No hay caché ni reintentos.
- Depende de que OFF mantenga `search.pl` y `api/v2/product`. La búsqueda `search.pl` es la parte más frágil y es la que hay que revisar si cambia.
- Los datos son colaborativos: pueden ser erróneos aunque estén en rango. El usuario puede corregir el alimento importado (`PUT`), y `fuente_externa`/`id_externo` se conservan (015).
- Una carrera entre dos importaciones simultáneas del mismo código la frena el unique de V8 (una de las dos peticiones fallaría); no se maneja de forma especial.
- No se ha verificado contra la API real de OFF (el entorno de desarrollo bloqueó la salida a `world.openfoodfacts.org`): el adaptador se comprobó con respuestas simuladas con la forma documentada. Conviene una prueba real la primera vez que se ejecute con red.
