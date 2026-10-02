import React from 'react';
import { createPortal } from 'react-dom';
import { IconButton } from '../core/IconButton.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');

// Calendario propio para los campos de fecha: el selector nativo sale con los colores del sistema.
// Trabaja con fechas 'YYYY-MM-DD' en hora local, igual que <input type="date">.
const DIAS = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
const MES = new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric' });
const LARGA = new Intl.DateTimeFormat('es-ES', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
const dos = n => String(n).padStart(2, '0');
export const aIso = d => `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
const deIso = s => {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s || '');
  return m ? new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3])) : null;
};
const sumarDias = (d, n) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
const sumarMeses = (d, n) => {
  const r = new Date(d.getFullYear(), d.getMonth() + n, 1);
  return new Date(r.getFullYear(), r.getMonth(), Math.min(d.getDate(), new Date(r.getFullYear(), r.getMonth() + 1, 0).getDate()));
};

/** Popover con el calendario, anclado a `ancla` (el .pl-input del campo). */
export function Calendario({ ancla, valor, min, max, onElegir, onCerrar }) {
  const hoy = aIso(new Date());
  const inicial = deIso(valor) || deIso(max && hoy > max ? max : hoy);
  const [foco, setFoco] = React.useState(inicial);
  const [pos, setPos] = React.useState(null);
  const caja = React.useRef(null);
  const rejilla = React.useRef(null);
  const fuera = iso => (min && iso < min) || (max && iso > max);

  const colocar = () => {
    if (!ancla) return;
    const r = ancla.getBoundingClientRect();
    const alto = 340, ancho = 280;
    const abajo = window.innerHeight - r.bottom;
    const arriba = abajo < alto + 12 && r.top > abajo;
    const left = Math.max(8, Math.min(r.left, window.innerWidth - ancho - 8));
    setPos({ left, top: arriba ? undefined : r.bottom + 6, bottom: arriba ? window.innerHeight - r.top + 6 : undefined });
  };
  React.useLayoutEffect(colocar, [ancla]);

  React.useEffect(() => {
    const clic = e => { if (!caja.current?.contains(e.target) && !ancla?.contains(e.target)) onCerrar(false); };
    const mover = e => { if (!caja.current?.contains(e.target)) colocar(); };
    document.addEventListener('mousedown', clic);
    window.addEventListener('scroll', mover, true);
    window.addEventListener('resize', colocar);
    return () => {
      document.removeEventListener('mousedown', clic);
      window.removeEventListener('scroll', mover, true);
      window.removeEventListener('resize', colocar);
    };
  }, [ancla, onCerrar]);

  // El dia con foco siempre es el que tiene tabIndex 0: al moverse con el teclado se le pasa el foco.
  React.useEffect(() => {
    rejilla.current?.querySelector('[tabindex="0"]')?.focus();
  }, [foco, pos !== null]);

  const primero = new Date(foco.getFullYear(), foco.getMonth(), 1);
  const desde = sumarDias(primero, -((primero.getDay() + 6) % 7));
  const dias = Array.from({ length: 42 }, (_, i) => sumarDias(desde, i));
  const isoFoco = aIso(foco);

  const tecla = e => {
    const mov = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 }[e.key];
    if (mov) { e.preventDefault(); setFoco(f => sumarDias(f, mov)); return; }
    if (e.key === 'PageUp' || e.key === 'PageDown') { e.preventDefault(); setFoco(f => sumarMeses(f, e.key === 'PageUp' ? -1 : 1)); return; }
    if (e.key === 'Home') { e.preventDefault(); setFoco(f => sumarDias(f, -((f.getDay() + 6) % 7))); return; }
    if (e.key === 'End') { e.preventDefault(); setFoco(f => sumarDias(f, 6 - ((f.getDay() + 6) % 7))); return; }
    if (e.key === 'Escape') { e.preventDefault(); e.stopPropagation(); onCerrar(true); }
    if (e.key === 'Tab') onCerrar(false);
  };

  const destino = document.querySelector('.app') || document.body;
  if (!pos) return null;
  return createPortal(
    <div ref={caja} className="pl-cal" role="dialog" aria-label="Elegir fecha" style={{ left: pos.left, top: pos.top, bottom: pos.bottom }} onKeyDown={tecla}>
      <div className="pl-cal__head">
        <span className="pl-cal__mes" aria-live="polite">{MES.format(foco).replace(/^./, c => c.toUpperCase())}</span>
        <IconButton type="button" size="sm" icon="chevron-left" label="Mes anterior" onClick={() => setFoco(f => sumarMeses(f, -1))} />
        <IconButton type="button" size="sm" icon="chevron-right" label="Mes siguiente" onClick={() => setFoco(f => sumarMeses(f, 1))} />
      </div>
      <div className="pl-cal__grid" role="grid" ref={rejilla}>
        {DIAS.map(d => <span key={d} className="pl-cal__dow" aria-hidden="true">{d}</span>)}
        {dias.map(d => {
          const iso = aIso(d);
          const deshabilitado = fuera(iso);
          return (
            <button key={iso} type="button" role="gridcell" tabIndex={iso === isoFoco ? 0 : -1} disabled={deshabilitado}
              aria-selected={iso === valor} aria-label={LARGA.format(d)} aria-current={iso === hoy ? 'date' : undefined}
              className={cx('pl-cal__dia', d.getMonth() !== foco.getMonth() && 'pl-cal__dia--fuera', iso === valor && 'pl-cal__dia--sel', iso === hoy && 'pl-cal__dia--hoy')}
              onClick={() => onElegir(iso)}>
              {d.getDate()}
            </button>
          );
        })}
      </div>
      <div className="pl-cal__foot">
        <button type="button" className="pl-cal__link" onClick={() => onElegir('')}>Borrar</button>
        <button type="button" className="pl-cal__link" disabled={fuera(hoy)} onClick={() => onElegir(hoy)}>Hoy</button>
      </div>
    </div>,
    destino,
  );
}
