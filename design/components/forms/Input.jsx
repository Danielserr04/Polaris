import React from 'react';
import { Icon } from '../core/Icon.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function Input({ label, hint, error, icon, trailing, locked, size = 'md', id, className, style, ...rest }) {
  const iid = id || (label ? 'in-' + String(label).replace(/\W+/g, '-').toLowerCase() : undefined);
  return (
    <div className={cx('pl-field', className)} style={style}>
      {label && <label className="pl-field__label" htmlFor={iid}>{label}</label>}
      <div className={cx('pl-input', size === 'sm' && 'pl-input--sm', error && 'pl-input--error', locked && 'pl-input--locked')}>
        {(icon || locked) && <Icon name={locked ? 'lock' : icon} size={15} />}
        <input id={iid} readOnly={locked || rest.readOnly} {...rest} />
        {trailing}
      </div>
      {error ? <span className="pl-field__error"><Icon name="circle-alert" size={13} />{error}</span> : hint && <span className="pl-field__hint">{hint}</span>}
    </div>
  );
}
export function Kbd({ children }) { return <span className="pl-kbd">{children}</span>; }
