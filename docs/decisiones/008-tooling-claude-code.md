# 008 — Tooling de Claude Code: mínimo al arrancar

Estado: aceptada · 2026-08-28

## Contexto

Existe un ecosistema grande de repos que amplían Claude Code: colecciones de skills, agentes, comandos y hooks. Algunos son enormes — [Everything Claude Code](https://github.com/affaan-m/everything-claude-code) trae 68 agentes, 286 skills, 94 comandos, hooks y su propio sistema de memoria.

La tentación es instalarlo todo antes de escribir la primera línea.

## Decisión

Al arrancar se instalan solo dos, y ambos hacen una cosa concreta:

- **[`kepano/obsidian-skills`](https://github.com/kepano/obsidian-skills)** — markdown de Obsidian (wikilinks, embeds, callouts), Bases, JSON Canvas y CLI del vault. Directamente aplicable a `docs/`.
- **[`anthropics/skills`](https://github.com/anthropics/skills)** — las oficiales de Anthropic para documentos, hojas de cálculo y PDF.

El resto se revisa **después de B2**.

## Alternativas descartadas

**Everything Claude Code desde el día 1.** Descartada por una razón concreta: **trae su propia metodología de trabajo** (plan → test → implement → review → verify → remember), que se superpone a las convenciones de este repo. Con dos fuentes de verdad sobre cómo se trabaja aquí, cuando algo salga raro no hay forma de saber si viene de `CLAUDE.md` o de sus reglas. Se valorará cuando haya código propio con el que comparar y criterio para juzgar qué aporta.

**Repomix.** Empaqueta un codebase grande en un fichero para dárselo a un modelo. Con el repo vacío no aporta nada. Tiene sentido más adelante, cuando haya varios cientos de ficheros.

**Dify, Flowise, Onyx, NotebookLM, skills de marketing.** Otros casos de uso — agentes no-code, RAG corporativo, marketing. No aplican.

## Consecuencias

- Las skills de terceros se instalan en `.claude/skills/` y van a `.gitignore`: son código ajeno y se reinstalan clonando.
- Merece la pena aprovechar `obsidian-skills` para que las notas de `docs/` usen bien los wikilinks y los callouts, no solo markdown plano.
- **Punto de revisión: al cerrar B2.** Ahí se decide si ECC aporta o estorba, con un módulo real ya escrito como referencia.
- Regla general que sale de aquí: una herramienta entra cuando resuelve un problema que ya se ha tenido, no cuando promete resolver uno que podría aparecer.
