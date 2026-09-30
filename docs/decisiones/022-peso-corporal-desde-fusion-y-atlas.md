# 022 — Peso corporal desde Fusión y Atlas: puerto propio en cada módulo y un adaptador hacia Núcleo

Estado: aceptada · 2026-09-30

## Contexto

[[nucleo]] guarda el peso corporal (`RegistroPeso`, un peso por día, [[010-registro-peso-un-peso-por-dia]]) y [[arquitectura]] dice que "Fusión y Atlas necesitan el mismo peso corporal" y que ambos lo consumen de Núcleo, que "expone su puerto y su modelo". Pero la regla dura 2 de `CLAUDE.md` prohíbe que un módulo importe clases de otro, y la regla dura 1 prohíbe cualquier dependencia rara en `domain/`. Falta fijar **cómo** Fusión (y luego Atlas) ve y apunta el peso sin romper ninguna de las dos.

Lo que Núcleo expone hoy son sus casos de uso de entrada (`application/in`: `CreateRegistroPesoInterface`, `ListRegistroPesoInterface`...). Esos son los únicos contratos de Núcleo pensados para que los llame alguien.

## Decisión

Cada módulo consumidor define **su propio puerto de salida con su propio modelo mínimo**, y un **adaptador en su `infrastructure/`** lo implementa llamando a los casos de uso de entrada de Núcleo. Solo el adaptador conoce clases de Núcleo.

En Fusión:

- `fusion/application/out/PesoCorporalPort`: `findAll(usuarioId, filter)` y `registrar(usuarioId, peso)`, en lenguaje de Fusión.
- `fusion/domain/model/PesoCorporal` (`fecha`, `pesoKg`, `grasaPct` opcional, `notas`) y `PesoCorporalFilter` (`desde`, `hasta`). Sin `id` ni `usuarioId`: el día identifica el peso y el usuario sale del JWT.
- `fusion/application/in/ListPesoCorporalInterface` y `CreatePesoCorporalInterface`, implementadas por `PesoCorporalService`, que solo delega en el puerto: las reglas son de Núcleo y no se duplican.
- `fusion/infrastructure/nucleo/NucleoPesoCorporalAdapter` (con su `PesoCorporalNucleoMapper`, MapStruct con `unmappedTargetPolicy = ERROR`): es el **único fichero de Fusión** que importa `com.polaris.nucleo`. Llama a `ListRegistroPesoInterface` y `CreateRegistroPesoInterface`, nunca al repositorio ni a la entidad de Núcleo. Va en `infrastructure/nucleo/` y no en `persistence/` porque no toca base de datos: su sitio es junto a lo que habla con "otro sistema", como `externo/` para Open Food Facts.
- API: `GET /api/fusion/peso?desde=&hasta=` (rango inclusivo, ambos opcionales, del más reciente al más antiguo, como en Núcleo) y `POST /api/fusion/peso`. El POST **apunta el peso del día o lo actualiza si ya existía**, con la regla del ADR 010 (reemplazo completo, respuesta 201 en ambos casos). DTOs propios de Fusión, con las mismas validaciones que Núcleo (`pesoKg` > 0 con `DECIMAL(5,2)`, `grasaPct` 0-100 con `DECIMAL(4,1)`, fecha no futura). Todo `BigDecimal`.
- **Sin migración y sin tabla nueva**: el dato vive una sola vez, en `registro_peso`. Lo que se apunta desde Fusión aparece en `GET /api/nucleo/registro-peso` y al revés.
- Sin `PUT`, `DELETE` ni `GET /{id}` en Fusión: el POST del día ya reemplaza, y corregir o borrar por `id` se hace en Núcleo. Fusión solo necesita ver y apuntar.
- Un DTO de salida único (`PesoCorporalDto`) para lista y POST, en vez de `FormDto` y `ListDto`: son cuatro campos, un registro por día, y sin detalle que aligerar. Se incluyen las notas también en el listado para que quien reescribe el día no las pierda sin haberlas visto.

**Atlas hará exactamente lo mismo, con su propio puerto** (`PesoCorporalPort` en `atlas/application/out`, su propio modelo y su propio adaptador en `atlas/infrastructure/nucleo`). No comparte el puerto ni el modelo de Fusión: cada módulo declara lo que necesita y puede ser distinto (Atlas querrá el peso en una fecha para relativizar una carga; Fusión, una serie para calcular necesidades).

Un test (`FusionNoImportaNucleoTest`) escanea los ficheros fuente y falla si algo de `fusion/domain` importa `com.polaris.nucleo`, o si cualquier fichero de Fusión fuera de `infrastructure/nucleo/` lo hace. Sin ArchUnit: no hay dependencia y el `pom.xml` no se toca sin preguntar.

## Alternativas descartadas

- **Importar directamente `CreateRegistroPesoInterface`/`RegistroPeso` desde el `domain/` de Fusión.** Es lo más corto, pero es exactamente lo que prohíben las reglas duras 1 y 2: Fusión quedaría acoplado a la forma interna de Núcleo (si cambia `RegistroPeso`, se rompe `domain/` de Fusión) y no habría un único punto donde mirar qué sabe Fusión de Núcleo. El coste del adaptador (tres ficheros pequeños) es bajo comparado con eso.
- **Tabla propia de pesos en Fusión (y otra en Atlas).** Duplica el dato y obliga a sincronizarlo, que es justo lo que [[nucleo]] existe para evitar: apuntas 78 kg después de entrenar y nutrición ya calcula con ese número. Además rompería el "un peso por día" del ADR 010.
- **Eventos de dominio** (Núcleo publica, Fusión y Atlas escuchan y guardan copia). [[arquitectura]] los reserva para "algo más elaborado" y "no antes de necesitarlo". Aquí hace falta leer y escribir en el momento, con respuesta síncrona; un evento no da el valor a devolver y llevaría otra vez a una copia local del dato.
- **Un puerto común en `shared/` que usen Fusión y Atlas.** `shared/` no conoce a ningún módulo y sería un modelo compartido que ata a los dos consumidores a la misma forma. Dos puertos casi iguales son un precio aceptable por que cada módulo evolucione el suyo.
- **Fusión llamando al `RegistroPesoController` de Núcleo por HTTP.** Un monolito no se llama a sí mismo por la red.
- **`PUT`/`DELETE` en Fusión.** Nadie los ha pedido; los añade quien los necesite.

## Consecuencias

- Fusión ve y apunta el peso sin que su `domain/` sepa que existe Núcleo. Si Núcleo cambia `RegistroPeso`, solo se toca el adaptador y su mapper; el `unmappedTargetPolicy = ERROR` lo hace fallar al compilar, no en producción.
- Las reglas de Núcleo (un peso por día, `usuarioId` filtrando siempre, 409/404) se aplican sin copiarlas: `PesoCorporalService` no valida nada por su cuenta. Las validaciones del DTO de entrada sí se repiten a propósito (mismos límites que la columna) para que un valor que no cabe sea un 400 en Fusión y no un error en Núcleo; si cambian allí, hay que cambiarlas aquí.
- Hay un modelo `PesoCorporal` casi idéntico a `RegistroPeso`. Es el precio de la regla 2, y a cambio Fusión puede tener el suyo distinto cuando lo necesite.
- El POST reemplaza el día completo, igual que en Núcleo: quien apunta el peso desde Fusión sin reenviar grasa o notas las borra. Es la consecuencia ya aceptada en el ADR 010, ahora visible también desde este módulo.
- La estructura de módulo de la plantilla no cubre un puerto hacia otro módulo, así que aquí no se replica entera: es un caso de lectura/escritura sin entidad ni tabla propia (un modelo, un filtro, dos interfaces de entrada, un puerto, un servicio, un adaptador, un controller). Igual que el resumen diario ([[021-resumen-diario-fusion]]), no sigue los 23 ficheros de una entidad.
- Cuando Atlas necesite el peso, repite este patrón sin tocar Fusión ni Núcleo. El test de imports se copia (o generaliza) para `atlas/`.
- Nota (Atlas, hecho): Atlas repite el patrón con las mismas clases y los mismos límites. Única diferencia: los beans llevan nombre propio (`@Service("atlasPesoCorporalService")`, idem controller y adaptador, y `implementationName = "Atlas<CLASS_NAME>Impl"` en los mappers) porque Spring identifica los beans por el nombre simple de la clase y los de Fusión se llaman igual; sin eso el arranque falla con `ConflictingBeanDefinitionException`. Las clases no beans (puerto, modelo, filtro, interfaces) conservan el mismo nombre en su propio paquete. Su test de imports (`AtlasNoImportaNucleoTest`) es una copia del de Fusión.
- El resumen diario o cualquier cálculo de necesidades calóricas ([[fusion]]) podrá pedir el peso a este mismo puerto; queda para cuando se necesite.
