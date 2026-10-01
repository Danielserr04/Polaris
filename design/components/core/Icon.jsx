import React from 'react';
const LUCIDE = 'https://unpkg.com/lucide-static@0.460.0/icons/';
export function Icon({ name, size = 16, color, style, className = '', title }) {
  return <span role={title ? 'img' : undefined} aria-label={title} aria-hidden={title ? undefined : true}
    className={'pl-icon ' + className}
    style={{ '--pl-icon': `url(${(window.__resources && window.__resources['ic-' + name]) || LUCIDE + name + '.svg'})`, width: size, height: size, color, ...style }} />;
}
