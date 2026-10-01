import React from 'react';
const STAR = 'M46,16 Q49.6,42.4 76,46 Q49.6,49.6 46,76 Q42.4,49.6 16,46 Q42.4,42.4 46,16Z';
export function Logo({ variant = 'full', size = 28, wordmarkSize, color, style }) {
  const mark = (
    <svg width={size} height={size} viewBox="0 0 100 100" aria-hidden="true" style={{ flex: 'none', overflow: 'visible' }}>
      <rect x="12" y="12" width="84" height="84" rx="14" fill="var(--accent-deep)" />
      <rect x="4" y="4" width="84" height="84" rx="14" fill={color || 'var(--accent)'} />
      <path d={STAR} fill="var(--text-on-accent)" />
      <circle cx="71" cy="21" r="3.6" fill="var(--text-on-accent)" />
    </svg>
  );
  if (variant === 'mark') return <span role="img" aria-label="Polaris" style={{ display: 'inline-flex', ...style }}>{mark}</span>;
  const ws = wordmarkSize || Math.round(size * 0.62);
  return (
    <span role="img" aria-label="Polaris" style={{ display: 'inline-flex', alignItems: 'center', gap: Math.round(size * 0.32), ...style }}>
      {variant === 'full' && mark}
      <span style={{ fontFamily: 'var(--font-display)', fontStretch: '125%', fontWeight: 800, fontSize: ws, lineHeight: 1, letterSpacing: '0.04em', color: 'var(--text-1)', textTransform: 'uppercase' }}>Polaris</span>
    </span>
  );
}
