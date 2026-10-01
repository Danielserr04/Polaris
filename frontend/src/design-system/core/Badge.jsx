import React from 'react';
const TONES = { neutral: 'var(--text-2)', accent: 'var(--accent)', success: 'var(--success)', warning: 'var(--warning)', danger: 'var(--danger)', info: 'var(--info)' };
export function Badge({ tone = 'neutral', variant = 'soft', color, children, style }) {
  return <span className={'pl-badge' + (variant !== 'soft' ? ' pl-badge--' + variant : '')} style={{ '--tone': color || TONES[tone], ...style }}>{children}</span>;
}
