import React from 'react';
import { Icon } from '../core/Icon.jsx';
export function Checkbox({ label, checked, defaultChecked, onChange, disabled }) {
  return (
    <label className={'pl-check' + (disabled ? ' pl-check--disabled' : '')}>
      <input type="checkbox" checked={checked} defaultChecked={defaultChecked} disabled={disabled} onChange={e => onChange && onChange(e.target.checked)} />
      <span className="pl-check__box"><Icon name="check" size={12} /></span>
      {label}
    </label>
  );
}
