import React from 'react';
// Iconos Lucide servidos en local desde public/icons (nada de CDN en produccion).
const ICONS = '/icons/';
export function Icon({ name, size = 16, color, style, className = '', title }) {
  return <span role={title ? 'img' : undefined} aria-label={title} aria-hidden={title ? undefined : true}
    className={'pl-icon ' + className}
    style={{ '--pl-icon': `url(${ICONS + name}.svg)`, width: size, height: size, color, ...style }} />;
}
