# 003 — Separar fitcore en Fusión y Atlas

Estado: aceptada · 2026-08-28

## Contexto
El intento anterior, `fitcore`, metía nutrición y gym en una sola app. Comparten conceptos evidentes: peso corporal, energía, objetivos. Al reescribirlo todo de cero hay que decidir si se repite ese planteamiento.

## Decisión
Dos módulos independientes y escritos de cero: [[fusion]] (nutrición) y [[atlas]] (gym). Lo que comparten no vive en ninguno de los dos, sino en [[nucleo]].

## Alternativas descartadas

**Un solo módulo `fitcore`.** Menos duplicación aparente. Descartada porque son dos dominios distintos: se usan en momentos distintos, evolucionan a ritmos distintos, y meterlos juntos crea un módulo que lo toca todo.

**Dos módulos, cada uno con su peso corporal.** Sería duplicar el dato. Apuntas 78 kg en el gym y nutrición sigue calculando con 80. Descartada sin discusión.

## Consecuencias
- **El registro de peso aparece en ambos módulos**, se consulta y se añade desde los dos, pero el dato vive una sola vez en Núcleo.
- Cada módulo interpreta ese dato a su manera: Fusión define qué es un objetivo de peso, Atlas define qué es un récord. Núcleo solo guarda número y fecha.
- Núcleo hay que construirlo **antes** que Fusión y Atlas. Va en B4 del [[roadmap]].
- Riesgo vigilado: que se duplique lógica entre ambos. Todo lo común va a Núcleo desde el principio, no "ya lo sacaré luego".
