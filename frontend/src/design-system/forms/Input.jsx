import React, { useState } from 'react';
import { Icon } from '../core/Icon.jsx';
import { IconButton } from '../core/IconButton.jsx';
import { Calendario } from './Calendario.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
const NUMERICO = /^-?\d*(?:[.,]\d*)?$/;

// Suma `paso` a un texto numérico respetando la coma o el punto y los decimales que ya tenga.
function sumar(texto, paso, min) {
  const t = String(texto ?? '').trim();
  if (!NUMERICO.test(t)) return null;
  const coma = t.includes(',');
  const decimales = Math.max((t.split(/[.,]/)[1] || '').length, (String(paso).split('.')[1] || '').length);
  const n = Math.max(min, (t === '' || t === '-' ? 0 : Number(t.replace(',', '.'))) + paso);
  const r = n.toFixed(decimales);
  return coma ? r.replace('.', ',') : r;
}

export function Input({ label, hint, error, icon, trailing, locked, size = 'md', validarAlSalir, id, className, style, ...rest }) {
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
  // validarAlSalir: el error llega siempre del padre, pero solo se enseña cuando la persona ha
  // escrito y sale del campo, o al intentar enviar el formulario. Si el valor cambia desde fuera
  // (un formulario que se vacía tras guardar) se vuelve a esconder.
  const [mostrar, setMostrar] = useState(false);
  const escrito = React.useRef(null);
  const editado = React.useRef(false);
  const cambiar = valor => {
    escrito.current = String(valor);
    editado.current = true;
    rest.onChange?.({ target: { value: valor }, currentTarget: { value: valor } });
  };
  React.useEffect(() => {
    if (escrito.current !== null && String(rest.value ?? '') !== escrito.current) {
      escrito.current = null;
      editado.current = false;
      setMostrar(false);
    }
  }, [rest.value]);
  // Al enviar: se enseñan los errores y el foco salta al primer campo que falla.
  React.useEffect(() => {
    const form = campo.current?.form;
    if (!form) return;
    const enviar = () => {
      setMostrar(true);
      requestAnimationFrame(() => {
        const primero = form.querySelector('[aria-invalid="true"]');
        if (primero && primero !== document.activeElement && primero.isConnected) primero.focus();
      });
    };
    form.addEventListener('submit', enviar);
    return () => form.removeEventListener('submit', enviar);
  }, []);
  const errorVisible = validarAlSalir && !mostrar ? null : error;
  const numerico = (rest.inputMode === 'numeric' || rest.inputMode === 'decimal') && !locked && !rest.readOnly;
  const tecla = e => {
    rest.onKeyDown?.(e);
    if (e.defaultPrevented || !numerico || (e.key !== 'ArrowUp' && e.key !== 'ArrowDown')) return;
    const base = Number(rest.step) || 1;
    const nuevo = sumar(rest.value, (e.key === 'ArrowUp' ? 1 : -1) * (e.shiftKey ? base * 10 : base), rest.min != null ? Number(rest.min) : 0);
    if (nuevo === null) return;
    e.preventDefault();
    cambiar(nuevo);
  };
  const elegirFecha = iso => {
    cerrarCalendario(true);
    cambiar(iso);
  };
  return (
    <div className={cx('pl-field', className)} style={style}>
      {label && <label className="pl-field__label" htmlFor={iid}>{label}</label>}
      <div ref={caja} className={cx('pl-input', esFecha && 'pl-input--fecha', size === 'sm' && 'pl-input--sm', errorVisible && 'pl-input--error', locked && 'pl-input--locked')}>
        {(icon || locked) && <Icon name={locked ? 'lock' : icon} size={15} />}
        <input ref={campo} id={iid} readOnly={locked || rest.readOnly} aria-invalid={errorVisible ? true : undefined} {...rest}
          type={esPassword && visible ? 'text' : rest.type}
          onChange={e => { escrito.current = e.target.value; editado.current = true; rest.onChange?.(e); }}
          onBlur={e => { if (editado.current) setMostrar(true); rest.onBlur?.(e); }}
          onKeyDown={tecla} />
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
      {errorVisible ? <span className="pl-field__error"><Icon name="circle-alert" size={13} />{errorVisible}</span> : hint && <span className="pl-field__hint">{hint}</span>}
    </div>
  );
}
export function Kbd({ children }) { return <span className="pl-kbd">{children}</span>; }
