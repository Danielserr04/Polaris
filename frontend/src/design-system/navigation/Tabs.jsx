import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { useIndicator } from '../core/hooks.jsx';
export function Tabs({ items = [], value, onChange, style }) {
  const [refs, pos] = useIndicator(value, [items.length]);
  return (
    <div className="pl-tabs" role="tablist" style={style}>
      {items.map(t => (
        <button key={t.value} ref={el => (refs.current[t.value] = el)} role="tab" aria-selected={t.value === value} className="pl-tabs__tab" onClick={() => onChange && onChange(t.value)}>
          {t.icon && <Icon name={t.icon} size={15} />}{t.label}
        </button>
      ))}
      {pos && <span className="pl-tabs__ind" style={{ left: pos.left, width: pos.width }} />}
    </div>
  );
}
