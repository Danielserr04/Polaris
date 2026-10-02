import React, { useState } from 'react';
import { Icon } from '../core/Icon.jsx';
import { IconButton } from '../core/IconButton.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function Input({ label, hint, error, icon, trailing, locked, size = 'md', id, className, style, ...rest }) {
  const iid = id || (label ? 'in-' + String(label).replace(/\W+/g, '-').toLowerCase() : undefined);
  const [visible, setVisible] = useState(false);
  const esPassword = rest.type === 'password';
  return (
    <div className={cx('pl-field', className)} style={style}>
      {label && <label className="pl-field__label" htmlFor={iid}>{label}</label>}
      <div className={cx('pl-input', size === 'sm' && 'pl-input--sm', error && 'pl-input--error', locked && 'pl-input--locked')}>
        {(icon || locked) && <Icon name={locked ? 'lock' : icon} size={15} />}
        <input id={iid} readOnly={locked || rest.readOnly} {...rest} type={esPassword && visible ? 'text' : rest.type} />
        {esPassword && (
          <IconButton type="button" size="sm" icon={visible ? 'eye-off' : 'eye'} label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            pressed={visible} onClick={() => setVisible((v) => !v)} style={{ marginRight: -6 }} />
        )}
        {trailing}
      </div>
      {error ? <span className="pl-field__error"><Icon name="circle-alert" size={13} />{error}</span> : hint && <span className="pl-field__hint">{hint}</span>}
    </div>
  );
}
export function Kbd({ children }) { return <span className="pl-kbd">{children}</span>; }
