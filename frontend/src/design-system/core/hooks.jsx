import React from 'react';
export function useIndicator(value, deps) {
  const refs = React.useRef({});
  const [pos, setPos] = React.useState(null);
  const measure = () => { const el = refs.current[value]; if (el) setPos({ left: el.offsetLeft, width: el.offsetWidth }); };
  React.useLayoutEffect(measure, [value, ...(deps || [])]);
  React.useEffect(() => {
    document.fonts && document.fonts.ready.then(measure);
    window.addEventListener('resize', measure); return () => window.removeEventListener('resize', measure);
  }, [value]);
  return [refs, pos];
}
export function useMounted(delay = 30) {
  const [m, setM] = React.useState(false);
  React.useEffect(() => { const t = setTimeout(() => setM(true), delay); return () => clearTimeout(t); }, []);
  return m;
}
export function useCountUp(target, duration = 900) {
  const [v, setV] = React.useState(typeof target === 'number' ? 0 : target);
  React.useEffect(() => {
    if (typeof target !== 'number') { setV(target); return; }
    let raf, start; const from = 0;
    const step = t => { if (!start) start = t; const p = Math.min(1, (t - start) / duration); const e = 1 - Math.pow(1 - p, 3); setV(from + (target - from) * e); if (p < 1) raf = requestAnimationFrame(step); };
    raf = requestAnimationFrame(step); return () => cancelAnimationFrame(raf);
  }, [target]);
  return v;
}
export const fmt = (n, d = 0) => typeof n === 'number' ? n.toLocaleString('es-ES', { minimumFractionDigits: d, maximumFractionDigits: d }) : n;
