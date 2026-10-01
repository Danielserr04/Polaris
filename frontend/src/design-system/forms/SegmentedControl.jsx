import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { useIndicator } from '../core/hooks.jsx';
export function SegmentedControl({ options = [], value, onChange, style }) {
  const [refs, pos] = useIndicator(value, [options.length]);
  return (
    <div className="pl-seg" role="group" style={style}>
      {pos && <span className="pl-seg__ind" style={{ left: pos.left, width: pos.width }} />}
      {options.map(o => (
        <button key={o.value} ref={el => (refs.current[o.value] = el)} className="pl-seg__opt" aria-pressed={o.value === value} onClick={() => onChange && onChange(o.value)}>
          {o.icon && <Icon name={o.icon} size={14} />}{o.label}{o.count != null && <span className="pl-seg__count">{o.count}</span>}
        </button>
      ))}
    </div>
  );
}
