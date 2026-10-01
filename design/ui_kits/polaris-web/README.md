# Polaris web — UI kit
Recreación clicable del escritorio (1440) de Polaris. Abre `index.html`.

Flujo: **Landing/login** (estelas de estrellas en canvas) → **Inicio** (un widget por módulo) → **Odisea** (lista densa + ficha, filtros, añadir desde el catálogo) → **Kuiper**, **Fusión**, **Atlas** (maqueta, marcados) → **Perfil** (avatar → 4 estados de cuenta). ⌘K abre la búsqueda. La ruta se guarda en localStorage; "Cerrar sesión" en Perfil vuelve al login.

Archivos: `StarTrails.jsx`, `Landing.jsx`, `Shell.jsx` (AppShell, PageHeader, CommandPalette), `Dashboard.jsx`, `Odisea.jsx`, `Kuiper.jsx`, `Fusion.jsx`, `Atlas.jsx`, `Perfil.jsx`, `data.js`, `kit.css`.
Todas las primitivas vienen de `window.PolarisDesignSystem_b0ab94` (el bundle compilado).
