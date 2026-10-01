import React from 'react';
import { useMounted, fmt } from '../core/hooks.jsx';
export function ProgressBar({ value = 0, max = 100, label, valueLabel, target, color, size = 'md', style }) {
  const m = useMounted();
  const pct = Math.min(100, (value / max) * 100);
  const over = value > max;
  return (
    <div className={'pl-progress' + (over ? ' pl-progress--over' : '') + (size === 'lg' ? ' pl-progress--lg' : '')} style={style}>
      {(label || valueLabel !== false) && (
        <div className="pl-progress__meta"><span>{label}</span>{valueLabel !== false && <span className="pl-progress__val">{valueLabel ?? fmt(value) + ' / ' + fmt(max)}</span>}</div>
      )}
      <div className="pl-progress__track">
        <div className="pl-progress__fill" style={{ width: (m ? pct : 0) + '%', background: over ? undefined : color }} />
        {target != null && <div className="pl-progress__target" style={{ left: Math.min(100, (target / max) * 100) + '%' }} />}
      </div>
    </div>
  );
}
