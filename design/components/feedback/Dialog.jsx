import React from 'react';
import { IconButton } from '../core/IconButton.jsx';
export function Dialog({ open, title, children, footer, onClose, width = 440 }) {
  React.useEffect(() => {
    if (!open) return;
    const k = e => e.key === 'Escape' && onClose && onClose();
    window.addEventListener('keydown', k); return () => window.removeEventListener('keydown', k);
  }, [open, onClose]);
  if (!open) return null;
  return (
    <div className="pl-dialog" onMouseDown={e => e.target === e.currentTarget && onClose && onClose()}>
      <div className="pl-dialog__panel" role="dialog" aria-modal="true" style={{ '--pl-dialog-w': width + 'px' }}>
        <div className="pl-dialog__head"><h2 className="pl-dialog__title">{title}</h2>{onClose && <IconButton icon="x" label="Cerrar" size="sm" onClick={onClose} />}</div>
        <div className="pl-dialog__body">{children}</div>
        {footer && <div className="pl-dialog__foot">{footer}</div>}
      </div>
    </div>
  );
}
