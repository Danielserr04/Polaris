# Núcleo

Módulo compartido. No tiene interfaz propia: existe para que [[fusion]] y [[atlas]] no dupliquen datos ni se llamen entre sí.

Ver [[003-modulos-separados-fusion-atlas]].

## Qué guarda

**`Perfil`** — altura, fecha de nacimiento, sexo, nivel de actividad. Los datos que casi no cambian.

**`RegistroPeso`** — un peso por día, con grasa corporal opcional y notas.

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
| `Perfil` | Pendiente |
| `RegistroPeso` | Pendiente |

## Notas

- Va en **B4** del [[roadmap]], antes que Fusión y Atlas porque los bloquea.
- `registro_peso` tiene índice único por `(usuario_id, fecha)`: un peso por día. Si te pesas dos veces, se actualiza.
