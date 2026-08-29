# 005 — Fijar el estilo de código sin referencia externa

Estado: aceptada · 2026-08-28

## Contexto

La estructura de carpetas de los módulos viene de GestionER y está confirmada ([[001-arquitectura-hexagonal]]). El estilo interno de las clases — cómo inyectar, cómo lanzar excepciones, cómo configurar MapStruct, qué anotaciones lleva un Controller — no se llegó a contrastar con código real.

Esperar a tenerlo bloqueaba el arranque del proyecto.

## Decisión

Se tira sin referencia externa. [[plantilla-modulo]] pasa a ser la fuente de la verdad del estilo de código de Polaris.

## Alternativas descartadas

**Esperar a tener código de GestionER delante.** Habría dado un estilo idéntico al que ya se conoce, con cero fricción mental. Descartada porque paralizaba B2 por tiempo indefinido.

**Dejar la plantilla marcada como provisional y arrancar igual.** Es lo peor de las dos opciones: se escribe código igualmente, pero cada entidad se plantea de nuevo si el estilo es el bueno, y Claude Code duda en cada sesión. Un estilo mediocre aplicado con consistencia vale más que uno bueno aplicado a medias.

## Consecuencias

- **Ningún fichero de la plantilla lleva ya la etiqueta de "pendiente".** Lo que está escrito se escribe tal cual.
- Las decisiones de estilo quedan fijadas: inyección por constructor, `@Service` como única concesión de Spring en `domain/`, MapStruct con `unmappedTargetPolicy = ERROR`, Controllers que inyectan interfaces de caso de uso y no el Service.
- Si el estilo resulta incómodo, el momento de detectarlo es **B2**, con una sola entidad escrita. Ahí se corrige la plantilla y se sigue.
- Si más adelante se quiere alinear con GestionER u otra referencia, se abre una nota nueva. Esta no se edita.
