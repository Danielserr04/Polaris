# 040 — Notificaciones de Kuiper: avisos dentro de la app, idempotentes por clave

Estado: aceptada · 2026-10-02

## Contexto

Lumen avisaba de cargos que llegaban, de presupuestos a punto de acabarse y del cierre de mes. En [[kuiper]] los recurrentes ([[034-recurrente-genera-movimientos]]) ya generan movimientos solos, pero nadie se entera de que ha pasado si no entra a mirar.

## Decisión

- Nueva entidad **`Notificacion`** (tabla `notificacion`, V21) con `usuario_id`, `tipo`, `titulo`, `texto`, `enlace` (nullable), `leida`, `creada_en` y **`clave`**, con `UNIQUE (usuario_id, clave)`.
- **Tipos**: `CARGO_RECURRENTE`, `CARGO_PROXIMO`, `PRESUPUESTO_AVISO`, `PRESUPUESTO_EXCEDIDO`, `META_ALCANZADA` y `RESUMEN_MENSUAL`. `META_ALCANZADA` la crea `MetaAhorroService` al cruzar el objetivo con una aportación, con clave `meta-{id}` (una sola vez por meta).
- **No se crean por HTTP.** Las crean los servicios a través de **`CrearNotificacionInterface`**. Por HTTP solo se listan, se cuentan, se marcan leídas y se borran:
  - `GET /api/kuiper/notificacion?soloNoLeidas=true`
  - `GET /api/kuiper/notificacion/no-leidas/total` → `{ "total": n }`
  - `GET /api/kuiper/notificacion/{id}`
  - `PUT /api/kuiper/notificacion/{id}/leida` (sin cuerpo = leída; `{"leida": false}` la vuelve a dejar sin leer)
  - `POST /api/kuiper/notificacion/leer-todas`
  - `DELETE /api/kuiper/notificacion/{id}` y `DELETE /api/kuiper/notificacion` (borra las leídas)
- **Idempotente por `clave`.** La clave nombra el hecho avisado, así que repetir una pasada del job o volver a guardar el mismo gasto no duplica nada:

  | Tipo | Clave | Enlace |
  |---|---|---|
  | `CARGO_RECURRENTE` | `recurrente-{id}-{fecha del cargo}` | `movimientos` |
  | `CARGO_PROXIMO` | `proximo-{id}-{fecha del cargo}` | `recurrentes` |
  | `PRESUPUESTO_AVISO` | `presupuesto-aviso-{id}-{yyyy-MM}` | `presupuestos` |
  | `PRESUPUESTO_EXCEDIDO` | `presupuesto-excedido-{id}-{yyyy-MM}` | `presupuestos` |
  | `RESUMEN_MENSUAL` | `resumen-{yyyy-MM}` | `resumen` |

  El servicio comprueba la clave antes de guardar; el unique es la red para las carreras.
- **Un aviso nunca rompe la operación que lo provoca.** Se resuelve en el dominio sin Spring: `NotificacionService.crear` captura cualquier excepción, la deja en un `warn` y devuelve `Optional.empty()`; `AvisoService` envuelve cada aviso y cada bloque del job en su propio `try`. En infraestructura, `NotificacionJpaAdapter.save` va en `REQUIRES_NEW`: si el insert falla, se deshace solo el aviso y la transacción de quien llama (la pasada de `RecurrenteJob`) no queda marcada para rollback.
- **Quién avisa de qué:**
  - `RecurrenteService.generar` crea un `CARGO_RECURRENTE` por cada movimiento que genera.
  - `MovimientoService`, después de crear o editar un **gasto**, llama a **`ComprobarPresupuestoInterface`**. Solo avisa si la fecha del gasto es del mes en curso y la categoría tiene presupuesto mensual.
  - **`AvisoJob`** (08:00 Europe/Madrid, después de `RecurrenteJob`, y al arrancar; se apaga con `polaris.jobs.activos: false`) llama a `GenerarAvisosInterface`:
    - `CARGO_PROXIMO` para los recurrentes de **gasto** activos con cargo entre mañana y dentro de **3 días**. Los de hoy no: esos ya los genera `RecurrenteJob` y avisan como `CARGO_RECURRENTE`. Los ingresos no avisan.
    - `PRESUPUESTO_AVISO` desde el **porcentaje de alerta** del presupuesto (80 % por defecto, incluido; ver [[035-presupuesto-umbral-de-alerta]]) y `PRESUPUESTO_EXCEDIDO` al **pasar** del 100 %. Llegar justo al 100 % es aviso, no exceso.
    - El **día 1**, un `RESUMEN_MENSUAL` del mes anterior para cada usuario con movimientos en ese mes.
- **El umbral sale de `AvisoService.umbralAviso(Presupuesto)`**: el `porcentaje_alerta` del presupuesto, o 80 si no lo trae.
- **`enlace`** es una pista de navegación, no una URL: la pestaña de Kuiper a la que lleva (`movimientos`, `recurrentes`, `presupuestos`, `resumen`). El frontend la traduce a una pestaña real.
- **Frontend**: una campana (`IconButton` con `Badge` de no leídas) en las acciones del `PageHeader` de Kuiper abre un `Dialog` con la lista: marcar una, marcar todas, borrar una, borrar las leídas, y al pulsar una notificación se marca leída y se va a su pestaña. El contador se vuelve a pedir **cada 60 s** (`refetchInterval` de TanStack Query). Hooks en `frontend/src/api/kuiperNotificaciones.ts`.

## Alternativas descartadas

- **Calcular los avisos al vuelo al abrir la campana.** Sin estado no hay "leída", y el aviso de un cargo ya generado se perdería al pasar el día.
- **Deduplicar comparando tipo, usuario y texto.** El texto cambia con el importe; la clave nombra el hecho y no cambia.
- **Lanzar la excepción y que la capture quien llama.** Cada servicio tendría que acordarse de capturarla. Con un caso de uso que nunca lanza, no hay forma de olvidarse.
- **Eventos de Spring (`ApplicationEventPublisher`) para desacoplar.** Habría que importar Spring en el dominio. Un caso de uso de `application/in` hace lo mismo sin salirse de la regla.
- **Push o correo.** Fuera de alcance: no hay PWA ni servicio de envío. La entidad sirve igual el día que se añadan.

## Consecuencias

- Las notificaciones guardan su texto ya escrito y no tienen claves foráneas: sobreviven aunque se borre el movimiento, el recurrente o el presupuesto que las provocó.
- Una notificación de un cargo recurrente se guarda en su propia transacción; si la pasada del job se deshiciera después, el aviso quedaría. Se acepta: es un caso raro y el aviso no afecta a los datos.
- Las leídas se acumulan hasta que el usuario las borra. Si algún día pesan, se añade una limpieza por antigüedad al job.
- `PresupuestoRepositoryPort` gana `findAllByPeriodo` y `MovimientoRepositoryPort` gana `findUsuarioIdsConMovimientos`, ambas para el job, que recorre todos los usuarios.
