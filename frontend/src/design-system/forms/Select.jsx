import React from 'react';
import { createPortal } from 'react-dom';
import { Icon } from '../core/Icon.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
const ALTO_OPCION = 36;

// Desplegable propio: el <select> nativo pinta la lista con los colores del sistema (fondo blanco,
// resaltado azul) y no se puede estilar. Mantiene la API: `onChange` recibe { target: { value } }.
export function Select({ label, hint, options = [], icon, size = 'md', id, style, className, value, onChange, disabled, ...rest }) {
  const ops = options.map(o => (typeof o === 'string' ? { value: o, label: o } : o));
  const autoId = React.useId().replace(/:/g, '');
  const bid = id || 'sel-' + autoId;
  const lid = bid + '-lista';
  const boton = React.useRef(null);
  const lista = React.useRef(null);
  const busqueda = React.useRef({ texto: '', t: 0 });
  const [abierto, setAbierto] = React.useState(false);
  const [activo, setActivo] = React.useState(-1);
  const [pos, setPos] = React.useState(null);
  const sel = ops.findIndex(o => String(o.value) === String(value ?? ''));

  const colocar = () => {
    const el = boton.current?.closest('.pl-input');
    if (!el) return;
    const r = el.getBoundingClientRect();
    const alto = Math.min(280, ops.length * ALTO_OPCION + 10);
    const abajo = window.innerHeight - r.bottom;
    const arriba = abajo < alto + 12 && r.top > abajo;
    setPos({ left: r.left, width: r.width, top: arriba ? undefined : r.bottom + 6, bottom: arriba ? window.innerHeight - r.top + 6 : undefined });
  };
  const abrir = () => {
    if (disabled || !ops.length) return;
    colocar();
    setActivo(sel >= 0 ? sel : 0);
    setAbierto(true);
  };
  const cerrar = () => setAbierto(false);
  const elegir = i => {
    const o = ops[i];
    cerrar();
    boton.current?.focus();
    if (o && String(o.value) !== String(value ?? '')) onChange?.({ target: { value: o.value }, currentTarget: { value: o.value } });
  };

  React.useEffect(() => {
    if (!abierto) return;
    const fuera = e => { if (!boton.current?.closest('.pl-input')?.contains(e.target) && !lista.current?.contains(e.target)) cerrar(); };
    const mover = e => { if (!lista.current?.contains(e.target)) colocar(); };
    document.addEventListener('mousedown', fuera);
    window.addEventListener('scroll', mover, true);
    window.addEventListener('resize', colocar);
    return () => {
      document.removeEventListener('mousedown', fuera);
      window.removeEventListener('scroll', mover, true);
      window.removeEventListener('resize', colocar);
    };
  }, [abierto]);

  React.useLayoutEffect(() => {
    if (abierto && activo >= 0) lista.current?.children[activo]?.scrollIntoView({ block: 'nearest' });
  }, [abierto, activo]);

  const tecla = e => {
    const ultimo = ops.length - 1;
    switch (e.key) {
      case 'ArrowDown':
      case 'ArrowUp': {
        e.preventDefault();
        if (!abierto) return abrir();
        const paso = e.key === 'ArrowDown' ? 1 : -1;
        setActivo(a => Math.min(ultimo, Math.max(0, a + paso)));
        return;
      }
      case 'Home': case 'End':
        if (abierto) { e.preventDefault(); setActivo(e.key === 'Home' ? 0 : ultimo); }
        return;
      case 'Enter': case ' ':
        e.preventDefault();
        if (abierto) elegir(activo); else abrir();
        return;
      case 'Escape':
        // Que no cierre tambien el Dialog en el que vive.
        if (abierto) { e.preventDefault(); e.stopPropagation(); cerrar(); }
        return;
      case 'Tab':
        cerrar();
        return;
      default:
        if (e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
          const b = busqueda.current;
          b.texto = (Date.now() - b.t > 600 ? '' : b.texto) + e.key.toLowerCase();
          b.t = Date.now();
          const desde = abierto ? activo : sel;
          const orden = ops.map((_, k) => (desde + 1 + k) % ops.length);
          const i = orden.find(k => String(ops[k].label).toLowerCase().startsWith(b.texto)) ?? orden.find(k => String(ops[k].label).toLowerCase().startsWith(e.key.toLowerCase()));
          if (i === undefined) return;
          if (abierto) setActivo(i); else elegir(i);
        }
    }
  };

  // Se monta en .app (como el Dialog) para heredar tema y acento sin quedar recortada por un overflow.
  const destino = typeof document !== 'undefined' ? document.querySelector('.app') || document.body : null;

  return (
    <div className={cx('pl-field', className)} style={style}>
      {label && <label className="pl-field__label" htmlFor={bid}>{label}</label>}
      <div className={cx('pl-input', 'pl-select', size === 'sm' && 'pl-input--sm', abierto && 'pl-select--abierto')}>
        {icon && <Icon name={icon} size={15} />}
        <button
          ref={boton}
          id={bid}
          type="button"
          className="pl-select__btn"
          role="combobox"
          aria-haspopup="listbox"
          aria-expanded={abierto}
          aria-controls={abierto ? lid : undefined}
          aria-activedescendant={abierto && activo >= 0 ? `${lid}-${activo}` : undefined}
          disabled={disabled}
          onClick={() => (abierto ? cerrar() : abrir())}
          onKeyDown={tecla}
          {...rest}
        >
          {sel >= 0 ? ops[sel].label : ''}
        </button>
        <Icon name="chevrons-up-down" size={14} className="pl-select__flecha" />
      </div>
      {hint && <span className="pl-field__hint">{hint}</span>}
      {abierto && pos && destino && createPortal(
        <ul ref={lista} id={lid} role="listbox" aria-labelledby={label ? undefined : bid} className="pl-select__lista"
          style={{ left: pos.left, width: pos.width, top: pos.top, bottom: pos.bottom }} onMouseDown={e => e.preventDefault()}>
          {ops.map((o, i) => (
            <li key={String(o.value)} id={`${lid}-${i}`} role="option" aria-selected={i === sel}
              className={cx('pl-select__op', i === activo && 'pl-select__op--activa')}
              onMouseEnter={() => setActivo(i)} onClick={() => elegir(i)}>
              <span className="pl-select__texto">{o.label}</span>
              {i === sel && <Icon name="check" size={14} />}
            </li>
          ))}
        </ul>,
        destino,
      )}
    </div>
  );
}
