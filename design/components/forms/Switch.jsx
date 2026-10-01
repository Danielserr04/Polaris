import React from 'react';
export function Switch({ label, checked, defaultChecked, onChange, disabled }) {
  return (
    <label className={'pl-switch' + (disabled ? ' pl-check--disabled' : '')}>
      <input type="checkbox" role="switch" checked={checked} defaultChecked={defaultChecked} disabled={disabled} onChange={e => onChange && onChange(e.target.checked)} />
      <span className="pl-switch__track" />
      {label}
    </label>
  );
}
