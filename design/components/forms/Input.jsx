import React, { useState } from 'react';
import { Icon } from '../core/Icon.jsx';
import { IconButton } from '../core/IconButton.jsx';
import { Calendario } from './Calendario.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function Input({ label, hint, error, icon, trailing, locked, size = 'md', id, className, style, ...rest }) {
  const iid = id || (label ? 'in-' + String(label).replace(/\W+/g, '-').toLowerCase() : undefined);
  const [visible, setVisible] = useState(false);
  const esPassword = rest.type === 'password';
  const esFecha = rest.type === 'date' && !locked && !rest.readOnly && !rest.disabled;
  const [calendario, setCalendario] = useState(false);
  const caja = React.useRef(null);
  const campo = React.useRef(null);
  const cerrarCalendario = React.useCallback(devolverFoco => {
    setCalendario(false);
    if (devolverFoco) campo.current?.focus();
  }, []);
  const elegirFecha = iso => {
    cerrarCalendario(true);
    rest.onChange?.({ target: { value: iso }, currentTarget: { value: iso } });
  };
  return (
    <div className={cx('pl-field', className)} style={style}>
      {label && <label className="pl-field__label" htmlFor={iid}>{label}</label>}
      <div ref={caja} className={cx('pl-input', esFecha && 'pl-input--fecha', size === 'sm' && 'pl-input--sm', error && 'pl-input--error', locked && 'pl-input--locked')}>
        {(icon || locked) && <Icon name={locked ? 'lock' : icon} size={15} />}
        <input ref={campo} id={iid} readOnly={locked || rest.readOnly} {...rest} type={esPassword && visible ? 'text' : rest.type} />
        {esPassword && (
          <IconButton type="button" size="sm" icon={visible ? 'eye-off' : 'eye'} label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            pressed={visible} onClick={() => setVisible((v) => !v)} style={{ marginRight: -6 }} />
        )}
        {esFecha && (
          <IconButton type="button" size="sm" icon="calendar" label="Abrir calendario" pressed={calendario}
            onClick={() => setCalendario((v) => !v)} style={{ marginRight: -6 }} />
        )}
        {trailing}
      </div>
      {calendario && <Calendario ancla={caja.current} valor={rest.value} min={rest.min} max={rest.max} onElegir={elegirFecha} onCerrar={cerrarCalendario} />}
      {error ? <span className="pl-field__error"><Icon name="circle-alert" size={13} />{error}</span> : hint && <span className="pl-field__hint">{hint}</span>}
    </div>
  );
}
export function Kbd({ children }) { return <span className="pl-kbd">{children}</span>; }
