import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { useCountUp, fmt } from '../core/hooks.jsx';
export function Stat({ label, value, unit, decimals = 0, delta, deltaTone, caption, size = 40, style }) {
  const v = useCountUp(value);
  const dir = deltaTone || (delta == null ? null : String(delta).trim().startsWith('-') ? 'down' : String(delta).trim().startsWith('+') ? 'up' : 'flat');
  return (
    <div className="pl-stat" style={style}>
      {label && <span className="pl-eyebrow">{label}</span>}
      <span className="pl-stat__value" style={{ fontSize: size }}>{fmt(v, decimals)}{unit && <span className="pl-stat__unit">{unit}</span>}</span>
      {(delta != null || caption) && (
        <span className="pl-stat__foot">
          {delta != null && <span className={'pl-delta pl-delta--' + dir}><Icon name={dir === 'up' ? 'arrow-up-right' : dir === 'down' ? 'arrow-down-right' : 'minus'} size={12} /> {delta}</span>}
          {caption}
        </span>
      )}
    </div>
  );
}
