import React from 'react';
import { useMounted, fmt } from '../core/hooks.jsx';
export function BarChart({ data = [], height = 160, max, target, targetLabel, highlight, format = v => fmt(v), gap = 6, showAxis = true, gridLines = 3 }) {
  const m = useMounted();
  const top = max || Math.max(...data.map(d => d.value), target || 0) * 1.1 || 1;
  return (
    <div className="pl-chart">
      <div style={{ position: 'relative', height }}>
        {Array.from({ length: gridLines }, (_, i) => <div key={i} className="pl-chart__grid" style={{ top: (i / gridLines) * 100 + '%' }} />)}
        {target != null && <div className="pl-chart__target" style={{ bottom: (target / top) * 100 + '%' }}>{targetLabel && <span>{targetLabel}</span>}</div>}
        <div className="pl-bars" style={{ '--pl-bar-gap': gap + 'px' }}>
          {data.map((d, i) => {
            const h = (d.value / top) * 100;
            const hl = highlight === i || highlight === d.label || d.highlight;
            return (
              <div key={i} className={'pl-bar' + (hl ? ' pl-bar--hl' : '')} style={{ '--h': h + '%' }}>
                <div className="pl-bar__fill" style={{ height: (m ? h : 0) + '%', transitionDelay: i * 30 + 'ms', background: d.color }} />
                <span className="pl-bar__tip">{format(d.value)}</span>
              </div>
            );
          })}
        </div>
      </div>
      {showAxis && <div style={{ display: 'flex', gap, marginTop: 8 }}>{data.map((d, i) => <span key={i} className="pl-chart__axis" style={{ flex: 1, textAlign: 'center', minWidth: 0 }}>{d.label}</span>)}</div>}
    </div>
  );
}
