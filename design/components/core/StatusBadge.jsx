import React from 'react';
const S = {
  PENDIENTE: ['Pendiente', 'pendiente'], EN_CURSO: ['En curso', 'en-curso'],
  TERMINADO: ['Terminado', 'terminado'], ABANDONADO: ['Abandonado', 'abandonado'],
};
export function StateGlyph({ estado = 'PENDIENTE', size = 10 }) {
  const k = S[estado][1];
  return <span className={'pl-glyph pl-glyph--' + k} style={{ '--tone': `var(--state-${k})`, width: size, height: size }} />;
}
export function StatusBadge({ estado = 'PENDIENTE', variant = 'badge' }) {
  const [label, k] = S[estado];
  const glyph = <StateGlyph estado={estado} size={8} />;
  if (variant === 'dot') return <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, fontSize: 13, color: 'var(--text-2)', whiteSpace: 'nowrap' }}>{glyph}{label}</span>;
  return <span className="pl-badge" style={{ '--tone': `var(--state-${k})` }}>{glyph}{label}</span>;
}
