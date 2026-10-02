# 045 — Recordatorios y avisos al móvil (Web Push)

Estado: aceptada · 2026-10-02

## Contexto

Daniel quiere que Polaris le recuerde apuntar comidas, gastos y demás, y que **le llegue al móvil**, también con la app cerrada. Hasta ahora solo había la campana de [[kuiper]] ([[040-notificaciones-de-kuiper]]), que se ve al abrir Kuiper. Condiciones: nada de correos ni servicios de pago, y todo se gestiona desde el Perfil.

## Decisión

- **Dos entidades nuevas en [[nucleo]]**, porque los recordatorios son de todos los módulos:
  - **`Recordatorio`** (`recordatorio`, V35): uno por usuario y tipo (`UNIQUE (usuario_id, tipo)`), con `activo`, `hora` (hora de Madrid, [[019-zona-horaria-europe-madrid]]), `dias` (días ISO separados por comas, `"1,3,5"`) y dos marcas de día: `avisado_en` (último push) y `descartado_en` (último "hecho por hoy").
  - **`SuscripcionPush`** (`suscripcion_push`, V36): un dispositivo que ha pedido avisos. `endpoint` es único en toda la tabla y lo identifica.
- **Tipos y valores de serie** (`TipoRecordatorio`): `COMIDAS` 21:00 todos los días, `GASTOS` 22:00 todos los días, `ENTRENO` 18:00 lunes, miércoles y viernes **apagado**, `PRESUPUESTO` 10:00 todos los días. Un tipo que el usuario no ha tocado no tiene fila: sale por defecto. Mismo patrón que [[009-perfil-unico-por-usuario]]: solo `GET` y `PUT` (que crea), sin `{id}`, con el tipo en la ruta.
- **Si un recordatorio sigue pendiente lo dice el módulo dueño del dato**, no Núcleo. Núcleo define el puerto de salida **`ComprobarRecordatorioPort`** y cada módulo lo implementa en su `infrastructure/nucleo/` con sus propios casos de uso de entrada:
  - Fusión (`ComidasRecordatorioAdapter`): ninguna comida ese día.
  - Kuiper (`GastosRecordatorioAdapter`): ningún gasto ese día; (`PresupuestoRecordatorioAdapter`): alguna categoría del mes en `AVISO` o `EXCEDIDO` ([[035-presupuesto-umbral-de-alerta]]).
  - Atlas (`EntrenoRecordatorioAdapter`): ninguna sesión ese día.
  La dependencia va de los módulos hacia Núcleo, como ya pasaba con el peso ([[022-peso-corporal-desde-fusion-y-atlas]]). Núcleo no importa nada de ellos.
- **"Toca"** = encendido, hoy es uno de sus días, ya es su hora o más tarde y no se ha descartado hoy. Un comprobador que falla cuenta como "nada pendiente" (mejor callar que avisar mal).
- **API:**
  - `GET /api/nucleo/recordatorio`, `GET`/`PUT /api/nucleo/recordatorio/{tipo}` (`{"activo", "hora": "HH:mm", "dias": [1..7]}`).
  - `GET /api/nucleo/recordatorio/pendientes`: lo que la campana enseña.
  - `POST /api/nucleo/recordatorio/{tipo}/descartar`: hecho por hoy.
  - `GET /api/nucleo/push/clave`, `POST`/`DELETE /api/nucleo/push/suscripcion`, `POST /api/nucleo/push/prueba`.
- **Web Push estándar, sin librerías**: cifrado `aes128gcm` (RFC 8291) y firma VAPID (RFC 8292) con lo que trae el JDK (`WebPushCifrado`). Los servicios de push de Google, Apple y Mozilla son gratis y no piden cuenta.
- **Claves VAPID**: de `POLARIS_VAPID_PUBLICA` / `POLARIS_VAPID_PRIVADA` si están; si no, el backend genera un par la primera vez y lo guarda en `push_vapid` (una fila). Tienen que ser estables: si cambian, los dispositivos dejan de recibir hasta volver a activarse. `POLARIS_PUSH_CONTACTO` (`mailto:` o `https://`) va en la firma; Apple lo exige.
- **`RecordatorioJob`** cada minuto (Europe/Madrid; se apaga con `polaris.jobs.activos: false`): para cada usuario con algún dispositivo, manda los pendientes que aún no se mandaron hoy y marca `avisado_en`. Uno por tipo y día como mucho. Un 404/410 del servicio de push borra la suscripción.
- **Frontend**:
  - **Campana** en la cabecera (al lado de la isla en escritorio, junto al avatar en móvil) con los pendientes, "hecho por hoy" y enlace a su módulo. Se vuelve a pedir cada 60 s.
  - **Perfil → Notificaciones**: activar o desactivar los avisos en este dispositivo, probar, y cada recordatorio con interruptor, hora y días; "Apagar todos".
  - **PWA mínima**: `manifest.webmanifest` y `public/sw.js`, que solo recibe los push y abre la pantalla al pulsarlos. Sin caché offline.
- La campana de Kuiper sigue igual: son avisos de hechos (un cargo, un presupuesto superado), no recordatorios.

## Alternativas descartadas

- **Calcular los pendientes en el frontend** con las APIs de cada módulo. Con la app cerrada no hay frontend que calcule: el push tiene que salir del servidor.
- **Que Núcleo llame a los repositorios de Fusión, Kuiper y Atlas.** Rompe la regla 2 de CLAUDE.md. El puerto invertido deja a Núcleo sin conocerlos.
- **Librería `web-push` de Java** (y Bouncy Castle). Son dependencias nuevas para unas 200 líneas que el JDK ya resuelve.
- **Firebase Cloud Messaging directo, correo o Telegram.** Cuentas y servicios externos; Web Push llega igual a Android, iPhone (16.4+, instalada) y escritorio.
- **Avisos solo con la app abierta** (`new Notification`). No es lo que se pidió.

## Consecuencias

- **Hace falta HTTPS** para el service worker y el push (salvo `localhost`). En el móvil solo funcionará cuando Polaris esté desplegado. En iPhone, además, hay que añadirlo a la pantalla de inicio.
- Un recordatorio guardado tarde (cambias la hora a una ya pasada) puede llegar en el minuto siguiente si sigue pendiente.
- Si el servidor está caído a la hora del aviso, llega al volver, el mismo día.
- La entrega real solo se prueba con un dispositivo de verdad: los tests comprueban que el cifrado se descifra y la firma se verifica.

> Antes era la 044, que chocaba con [[044-logros-globales-con-fecha-calculada]]. Los comentarios de `V35` y `V36` siguen citando `044-recordatorios.md` porque una migración aplicada no se edita.
