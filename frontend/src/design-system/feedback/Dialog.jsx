import React from 'react';
import { createPortal } from 'react-dom';
import { IconButton } from '../core/IconButton.jsx';

const FOCUSABLES = 'a[href],button:not([disabled]),input:not([disabled]):not([type="hidden"]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])';

export function Dialog({ open, title, children, footer, onClose, width = 440 }) {
  const panel = React.useRef(null);

  React.useEffect(() => {
    if (!open) return;
    const k = e => e.key === 'Escape' && onClose && onClose();
    window.addEventListener('keydown', k); return () => window.removeEventListener('keydown', k);
  }, [open, onClose]);

  // Foco: al abrir entra en el dialogo (salvo que un campo ya tenga autoFocus), Tab no se sale de el
  // y al cerrar vuelve a quien lo abrio.
  React.useEffect(() => {
    if (!open) return;
    const previo = document.activeElement;
    const el = panel.current;
    if (el && !el.contains(document.activeElement)) {
      (el.querySelector('.pl-dialog__body')?.querySelector(FOCUSABLES) || el).focus();
    }
    const trampa = e => {
      if (e.key !== 'Tab' || !el) return;
      const f = [...el.querySelectorAll(FOCUSABLES)].filter(x => x.offsetParent !== null);
      if (!f.length) { e.preventDefault(); return; }
      const primero = f[0], ultimo = f[f.length - 1];
      if (e.shiftKey && (document.activeElement === primero || !el.contains(document.activeElement))) { e.preventDefault(); ultimo.focus(); }
      else if (!e.shiftKey && (document.activeElement === ultimo || !el.contains(document.activeElement))) { e.preventDefault(); primero.focus(); }
    };
    window.addEventListener('keydown', trampa);
    return () => {
      window.removeEventListener('keydown', trampa);
      if (previo instanceof HTMLElement && previo.isConnected) previo.focus();
    };
  }, [open]);

  if (!open) return null;
  // Se monta en .app (o en body, p. ej. en el login) y no donde se declara: asi un ancestro con
  // transform o animacion no encierra el position:fixed. En .app hereda el acento del modulo.
  const destino = document.querySelector('.app') || document.body;
  return createPortal(
    <div className="pl-dialog" onMouseDown={e => e.target === e.currentTarget && onClose && onClose()}>
      <div ref={panel} className="pl-dialog__panel" role="dialog" aria-modal="true" tabIndex={-1} style={{ '--pl-dialog-w': width + 'px' }}>
        <div className="pl-dialog__head"><h2 className="pl-dialog__title">{title}</h2>{onClose && <IconButton icon="x" label="Cerrar" size="sm" onClick={onClose} />}</div>
        <div className="pl-dialog__body">{children}</div>
        {footer && <div className="pl-dialog__foot">{footer}</div>}
      </div>
    </div>,
    destino,
  );
}
