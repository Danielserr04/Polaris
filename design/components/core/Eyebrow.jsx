import React from 'react';
export function Eyebrow({ children, star, coord, style }) {
  return (
    <span className="pl-eyebrow" style={style}>
      {star && <span className="pl-eyebrow__star">✦</span>}
      <span>{children}</span>
      {coord && <><span className="pl-eyebrow__sep">·</span><span>{coord}</span></>}
    </span>
  );
}
