# 001 — Arquitectura hexagonal replicando GestionER

Estado: aceptada · 2026-08-28

## Contexto
Polaris son cuatro módulos, escritos de cero, que crecerán con el tiempo. Hace falta una estructura que aguante añadir módulos sin que cada uno se organice a su manera.

Existe además una referencia real y ya conocida: GestionER, con hexagonal en producción.

## Decisión
Monolito modular. Arquitectura hexagonal dentro de cada módulo, replicando la convención exacta de GestionER: `application/in`, `application/out`, `domain/model`, `domain/service`, `infrastructure/persistence`.

## Alternativas descartadas

**Capas clásicas (controller / service / repository).** Menos ficheros y más rápido de arrancar. Descartada porque el dominio queda atado a JPA y cambiar de motor de base de datos sería tocar lógica en vez de tocar un adaptador.

**Hexagonal "de manual"** (servicio en `application/`, controller en `infrastructure/in/rest`). Es lo ortodoxo, pero se descarta a propósito: replicar una convención ya conocida vale más que ser purista. El coste de traducir mentalmente entre dos variantes es real.

**Microservicios.** Absurdo para un proyecto de una persona.

## Consecuencias
- ~15 ficheros por entidad. Con 12 entidades, unos 180 ficheros. Es el precio.
- El servicio vive en `domain/`, no en `application/`. Se sale del hexagonal de libro, es deliberado, y quien venga de un tutorial lo verá raro.
- Migrar bases de datos o exponer otra interfaz de entrada no toca lógica.
- **[[roadmap]] B2 valida el molde con una sola entidad antes de replicarlo.** Si resulta insoportable, ese es el momento de simplificar.
