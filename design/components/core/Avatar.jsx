import React from 'react';
export function Avatar({ src, name = '', size = 32, style }) {
  const initials = name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  return (
    <span className="pl-avatar" style={{ width: size, height: size, fontSize: Math.round(size * 0.4), ...style }} title={name}>
      {src ? <img src={src} alt={name} /> : initials}
    </span>
  );
}
