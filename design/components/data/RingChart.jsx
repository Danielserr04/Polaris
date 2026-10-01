import React from 'react';
import { useMounted, fmt } from '../core/hooks.jsx';
export function RingChart({ value = 0, max = 100, size = 96, thickness = 8, color, label, sublabel, children }) {
  const m = useMounted(60);
  const r = (size - thickness) / 2, c = 2 * Math.PI * r;
  const p = Math.min(1, value / max);
  return (
    <div className="pl-ring" style={{ width: size, height: size }}>
      <svg width={size} height={size}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--surface-3)" strokeWidth={thickness} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={value > max ? 'var(--danger)' : color || 'var(--accent)'} strokeWidth={thickness}
          strokeDasharray={c} strokeDashoffset={m ? c * (1 - p) : c} strokeLinecap="butt" />
      </svg>
      <div className="pl-ring__label">
        {children || <>
          <span style={{ fontFamily: 'var(--font-display)', fontStretch: '118%', fontWeight: 800, fontSize: size * 0.22, lineHeight: 1 }}>{label ?? Math.round(p * 100) + '%'}</span>
          {sublabel && <span className="pl-chart__axis">{sublabel}</span>}
        </>}
      </div>
    </div>
  );
}
