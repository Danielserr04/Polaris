# Núcleo

Módulo compartido. No tiene interfaz propia: existe para que [[fusion]] y [[atlas]] no dupliquen datos ni se llamen entre sí.

Ver [[003-modulos-separados-fusion-atlas]].

## Qué guarda

**`Perfil`** — altura, fecha de nacimiento, sexo, nivel de actividad. Los datos que casi no cambian.

**`RegistroPeso`** — un peso por día, con grasa corporal opcional y notas.

**`MedidaCorporal`** — perímetros en cm (cuello, pecho, cintura, cadera, brazos y muslos), una medición por día.

**`Recordatorio`** y **`SuscripcionPush`** — los recordatorios del usuario (comidas, gastos, entreno, presupuesto) y los dispositivos que reciben sus avisos. Si algo sigue pendiente lo dice cada módulo a través de `ComprobarRecordatorioPort`. Ver [[045-recordatorios]].

Esquema completo en [[modelo-datos]].

## La regla

**El registro de peso se ve y se añade desde Fusión y desde Atlas.** Apuntas 78 kg después de entrenar y nutrición ya calcula con ese número, sin sincronizar nada.

Pero el dato vive una sola vez, aquí. Ningún módulo tiene su propia tabla de pesos.

## Qué NO hace

Núcleo guarda el número y la fecha. **No interpreta.**

- Qué es un "objetivo de peso" lo decide [[fusion]]
- Qué es un "récord" o cómo relativizar una carga lo decide [[atlas]]
- Ni TMB, ni IMC, ni tendencias: cada módulo calcula lo que necesita a partir del dato crudo

Si aparece la tentación de meter un cálculo aquí, la pregunta es: ¿lo necesitan los dos módulos exactamente igual? Si la respuesta no es un sí rotundo, no va en Núcleo.

## Entidades

| Entidad | Estado |
|---|---|
| `Perfil` | **Hecha** — `GET`/`PUT /api/nucleo/perfil`, un perfil por usuario. Ver [[009-perfil-unico-por-usuario]] |
| `RegistroPeso` | **Hecha** — CRUD en `/api/nucleo/registro-peso`, filtro `desde`/`hasta`. Ver [[010-registro-peso-un-peso-por-dia]] |
| `MedidaCorporal` | **Hecha** — CRUD en `/api/nucleo/medida-corporal`, filtro `desde`/`hasta`, al menos una medida. Ver [[041-medida-corporal-una-por-dia]] |
| `Recordatorio` | **Hecha** — `GET`/`PUT /api/nucleo/recordatorio/{tipo}`, pendientes y descartar; job cada minuto. Ver [[045-recordatorios]] |
| `SuscripcionPush` | **Hecha** — `/api/nucleo/push`: clave, alta, baja y prueba (Web Push sin librerías). Ver [[045-recordatorios]] |
| Logros | **Hechos** — `GET /api/nucleo/logros`: pesajes, rachas de pesaje, medidas y perfil completo. Ver [[044-logros-globales-con-fecha-calculada]] |

## Notas

- Va en **B4** del [[roadmap]], antes que Fusión y Atlas porque los bloquea.
- `registro_peso` tiene índice único por `(usuario_id, fecha)`: un peso por día. Si te pesas dos veces, se actualiza.
