# Polaris — UI kit móvil

Abrir `index.html`. iPhone 402×874, interactivo; el panel lateral salta entre pantallas.

- **Navegación**: barra de módulos flotante abajo (Inicio, Odisea, Kuiper, Fusión, Atlas). Perfil desde el avatar de la cabecera.
- **Cabecera**: título grande (display 34px) que colapsa en barra compacta con blur al hacer scroll.
- **Inicio**: "Hoy" en 2×2, módulos apilados como bandas (icono + nombre + dato + mini gráfico), "Lo último".
- **Odisea**: filtros en chips horizontales, lista compacta; detalle y alta en hojas inferiores (bottom sheet).
- **Kuiper / Fusión / Atlas / Perfil**: mismas secciones que la web, en una columna.
- Reutiliza `_ds_bundle.js`, `../polaris-web/kit.css`, `data.js` y `StarTrails.jsx`; lo específico de móvil está en `mobile.css` (prefijo `m-`).
- Tap targets ≥ 44px; tab bar a 26px del borde inferior para dejar libre el indicador de inicio.
