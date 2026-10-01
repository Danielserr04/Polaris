import React from 'react';
import { useMounted, fmt } from '../core/hooks.jsx';
export function LineChart({ series = [], labels = [], height = 180, min, max, area = true, format = v => fmt(v), gridLines = 4, showAxis = true, showLegend }) {
  const m = useMounted(60);
  const id = React.useId ? React.useId().replace(/:/g, '') : 'lc';
  const all = series.flatMap(s => s.points).filter(v => v != null);
  const lo = min ?? Math.min(...all), hi = max ?? Math.max(...all);
  const pad = (hi - lo) * 0.12 || 1, y0 = lo - pad, y1 = hi + pad;
  const n = Math.max(...series.map(s => s.points.length));
  const X = i => (n <= 1 ? 50 : (i / (n - 1)) * 100), Y = v => 100 - ((v - y0) / (y1 - y0)) * 100;
  return (
    <div className="pl-chart pl-line">
      {showLegend && <div className="pl-legend" style={{ marginBottom: 12 }}>{series.map(s => <span key={s.name}><i style={{ background: s.color || 'var(--accent)' }} />{s.name}</span>)}</div>}
      <div style={{ position: 'relative', height, marginLeft: showAxis ? 34 : 0 }}>
        {Array.from({ length: gridLines + 1 }, (_, i) => (
          <div key={i} className="pl-chart__grid" style={{ top: (i / gridLines) * 100 + '%', borderTopStyle: i === gridLines ? 'solid' : 'dashed' }}>
            {showAxis && <span className="pl-chart__axis" style={{ position: 'absolute', left: -34, top: -7, width: 28, textAlign: 'right' }}>{format(y1 - (i / gridLines) * (y1 - y0))}</span>}
          </div>
        ))}
        <svg viewBox="0 0 100 100" preserveAspectRatio="none" width="100%" height="100%" style={{ position: 'absolute', inset: 0, overflow: 'visible' }}>
          <defs>{series.map((s, k) => <linearGradient key={k} id={id + k} x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor={s.color || 'var(--accent)'} stopOpacity=".22" /><stop offset="1" stopColor={s.color || 'var(--accent)'} stopOpacity="0" /></linearGradient>)}</defs>
          {series.map((s, k) => {
            const pts = s.points.map((v, i) => [X(i), Y(v)]);
            const d = pts.map((p, i) => (i ? 'L' : 'M') + p[0] + ',' + p[1]).join(' ');
            return (
              <g key={k}>
                {area && !s.dashed && k === 0 && <path d={d + ` L${pts[pts.length - 1][0]},100 L${pts[0][0]},100 Z`} fill={`url(#${id + k})`} style={{ opacity: m ? 1 : 0, transition: 'opacity 1200ms' }} />}
                <path className="pl-line__stroke" d={d} fill="none" stroke={s.color || 'var(--accent)'} strokeWidth={s.dashed ? 1.5 : 2} vectorEffect="non-scaling-stroke"
                  strokeDasharray={s.dashed ? '4 4' : 1} pathLength={s.dashed ? undefined : 1} strokeDashoffset={s.dashed ? 0 : m ? 0 : 1} strokeLinejoin="round" strokeLinecap="round" />
              </g>
            );
          })}
        </svg>
        {series.map((s, k) => !s.dashed && <span key={k} className="pl-line__dot" style={{ left: X(s.points.length - 1) + '%', top: Y(s.points[s.points.length - 1]) + '%', borderColor: s.color || 'var(--accent)', opacity: m ? 1 : 0, transition: 'opacity 300ms 1200ms' }} />)}
      </div>
      {showAxis && labels.length > 0 && <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, marginLeft: 34 }}>{labels.map((l, i) => <span key={i} className="pl-chart__axis">{l}</span>)}</div>}
    </div>
  );
}
