# 004 — Nombres espaciales para la app y los módulos

Estado: aceptada · 2026-08-28

## Contexto
La app necesitaba nombre propio, y los módulos también. Ya existían Lumen y FitCore, puestos sin criterio común.

## Decisión
**Polaris** para la app. Módulos con nombres del mismo campo semántico:

| Módulo | Nombre | Por qué |
|---|---|---|
| Ocio | **Odisea** | El viaje largo lleno de historias |
| Gastos | **Kuiper** | Cinturón de miles de cuerpos pequeños acumulados |
| Nutrición | **Fusión** | Convertir materia en energía, lo que hacen las estrellas |
| Gym | **Atlas** | Luna de Saturno, y el titán que carga peso |
| Perfil | **Núcleo** | Donde ocurre la fusión; el centro compartido |

Polaris es la estrella que marca el norte: la app que te orienta.

## Alternativas descartadas

**Nombres directos** (Mediateca, Balance, Nutrición, Entreno). Más claros para una app de uso personal, pero sin carácter.

**Latín** (Otium, Nummus, Cibus, Robur). Cada palabra significaba literalmente su módulo, pero se descartó por preferencia de tema.

**Estrellas de la Osa Menor** (Kochab, Pherkad, Yildun), la constelación de Polaris. Coherentísimo, pero ninguno dice qué hace el módulo y a los seis meses no recuerdas cuál era Pherkad.

## Consecuencias
- Los nombres `lumen-app` y `fitcore` se abandonan: los módulos equivalentes de Polaris son Kuiper y Fusión, y son código nuevo. Los repos viejos se quedan como están.
- Queda sitio en el tema para módulos futuros: el Calendario aparcado podría ser Sideral, Efeméride o Terminador.
- En código, los paquetes van sin tilde: `fusion`, `nucleo`.
