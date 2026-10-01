import React from 'react';
import { Icon } from '../core/Icon.jsx';
export function Select({ label, hint, options = [], icon, size = 'md', id, style, ...rest }) {
  return (
    <div className="pl-field" style={style}>
      {label && <label className="pl-field__label" htmlFor={id}>{label}</label>}
      <div className={'pl-input' + (size === 'sm' ? ' pl-input--sm' : '')}>
        {icon && <Icon name={icon} size={15} />}
        <select id={id} {...rest}>
          {options.map(o => typeof o === 'string' ? <option key={o} value={o}>{o}</option> : <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <Icon name="chevrons-up-down" size={14} />
      </div>
      {hint && <span className="pl-field__hint">{hint}</span>}
    </div>
  );
}
