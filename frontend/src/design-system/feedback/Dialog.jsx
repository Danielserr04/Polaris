import React from 'react';
import { createPortal } from 'react-dom';
import { Button } from '../core/Button.jsx';
import { IconButton } from '../core/IconButton.jsx';

const FOCUSABLES = 'a[href],button:not([disabled]),input:not([disabled]):not([type="hidden"]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])';

export function Dialog({ open, title, children, footer, onClose, width = 440, confirmarDescarte }) {
  const panel = React.useRef(null);
  // confirmarDescarte: si ya se ha escrito algo, cerrar con Esc, la X o pulsando fuera pregunta
  // antes de perderlo. El botón Cancelar del pie llama a onClose directamente y no pregunta.
  const [sucio, setSucio] = React.useState(false);
  const [descartando, setDescartando] = React.useState(false);
  React.useEffect(() => { if (!open) { setSucio(false); setDescartando(false); } }, [open]);
  const pedirCierre = React.useCallback(() => {
    if (!onClose) return;
    if (confirmarDescarte && sucio) setDescartando(true);
    else onClose();
  }, [onClose, confirmarDescarte, sucio]);

  React.useEffect(() => {
    if (!open) return;
    const k = e => {
      if (e.key !== 'Escape') return;
      if (descartando) setDescartando(false);
      else pedirCierre();
    };
    window.addEventListener('keydown', k); return () => window.removeEventListener('keydown', k);
  }, [open, pedirCierre, descartando]);

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
    <div className="pl-dialog" onMouseDown={e => e.target === e.currentTarget && pedirCierre()}>
      <div ref={panel} className="pl-dialog__panel" role="dialog" aria-modal="true" tabIndex={-1} style={{ '--pl-dialog-w': width + 'px' }}>
        <div className="pl-dialog__head"><h2 className="pl-dialog__title">{title}</h2>{onClose && <IconButton icon="x" label="Cerrar" size="sm" onClick={pedirCierre} />}</div>
        <div className="pl-dialog__body" onInput={confirmarDescarte && !sucio ? () => setSucio(true) : undefined}>{children}</div>
        {descartando ? (
          <div className="pl-dialog__foot pl-dialog__foot--aviso">
            <span className="pl-dialog__pregunta">¿Descartar lo que has escrito?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setDescartando(false)}>Seguir editando</Button>
            <Button variant="danger" type="button" onClick={onClose}>Descartar</Button>
          </div>
        ) : footer && <div className="pl-dialog__foot">{footer}</div>}
      </div>
    </div>,
    destino,
  );
}
