import React from 'react';
export function Avatar({ src, name = '', size = 32, style }) {
  const initials = name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  // Si la imagen no carga (URL caducada, sin red) se vuelve a las iniciales en vez de dejar el icono roto.
  const [fallo, setFallo] = React.useState(null);
  const mostrar = src && fallo !== src;
  return (
    <span className="pl-avatar" style={{ width: size, height: size, fontSize: Math.round(size * 0.4), ...style }} title={name}>
      {mostrar ? <img src={src} alt={name} onError={() => setFallo(src)} /> : initials}
    </span>
  );
}
