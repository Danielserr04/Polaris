# 030 — Frontend: Vite + React + TypeScript y el design system exportado

Estado: aceptada · 2026-10-01

## Contexto

El backend está cerrado (B0 a B8) y no hay ni una línea de frontend. El [[002-react-sobre-angular]] dejó pendiente elegir router, estado y formularios. Además, el diseño se ha cerrado con Claude Design y se ha exportado como un **design system** (tokens, componentes React, y dos kits clicables, web y móvil, con datos de maqueta). Ese export dice de sí mismo que **sustituye** a [[briefing-diseno]] y [[briefing-figma]].

## Decisión

- App en `frontend/`, dentro de este repo: **Vite + React 18 + TypeScript**, `react-router-dom` para rutas y `@tanstack/react-query` para los datos de la API. CSS plano (el del design system, clases `pl-*` y tokens), sin Tailwind. Sin librería de formularios por ahora.
- El export del design system se guarda **tal cual** en `design/` como referencia y fuente de verdad visual (sin las capturas pegadas de `uploads/`). No se edita: lo que se adapta vive en `frontend/`.
- `frontend/src/design-system/` contiene las primitivas del export (`.jsx` con su `.d.ts`): el JSX original se conserva para no perder fidelidad y TypeScript toma los tipos de los `.d.ts`. Cambios respecto al export: los iconos Lucide se sirven en local desde `public/icons` (no desde CDN) y `StarTrails` se exporta como módulo en vez de colgarse de `window`.
- En desarrollo, Vite hace de proxy de `/api` hacia el backend (`:8080`, o `POLARIS_API_URL`): un solo origen, sin CORS.
- Las pantallas se construyen **una a una conectadas a la API real**, sustituyendo los datos de maqueta del kit. Orden: shell, login, Perfil, Odisea, Kuiper, Fusión, Atlas.

## Alternativas descartadas

**Reutilizar el kit tal cual** (Babel en el navegador, `window.PolarisDesignSystem_*`): es un prototipo, no un proyecto mantenible.

**Convertir las 29 primitivas a `.tsx` a mano ahora:** más tipado, pero mucho trabajo sin ganancia visible. Se puede hacer componente a componente cuando se toque cada uno.

**Tailwind / CSS-in-JS:** el sistema ya trae sus tokens y su CSS; añadir otra capa de estilos duplicaría decisiones.

**react-hook-form + zod:** útiles, pero se añaden cuando los formularios de Kuiper, Fusión y Atlas lo pidan; las dependencias no se meten por adelantado.

## Consecuencias

- Hay Node y `npm` en el flujo de trabajo y un `package.json` nuevo: las dependencias del frontend también se discuten antes de añadirlas.
- `docs/briefing-diseno.md` y `docs/briefing-figma.md` quedan como histórico; la referencia visual es `design/readme.md`.
- Pendientes que arrastra el export: fuentes en local (hoy se piden a Google Fonts), logo oficial de Google para el login, tema claro (solo existen los tokens) y llevar Kuiper, Fusión y Atlas al sistema de bandas del Inicio.
- Producción (dónde se sirve el `dist/`, y quién habla con quién) no está decidido; se decide cuando haya que desplegar.
