# Polaris — frontend

Vite + React 18 + TypeScript. Ver `docs/decisiones/030-frontend-react-vite-y-design-system.md`.

```powershell
npm install
npm run dev        # http://localhost:5173, /api se redirige al backend (:8080 o POLARIS_API_URL)
npm run typecheck
npm run build
```

- `src/design-system/`: primitivas del design system (JSX original + `.d.ts`). La fuente de verdad visual es `../design/`.
- `src/styles/`: tokens y CSS del sistema. `public/icons/`: Lucide en local.
- `src/app/`: router, shell y navegación. `src/pages/`: una pantalla por ruta.
