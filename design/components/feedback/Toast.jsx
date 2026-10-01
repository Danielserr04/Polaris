import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { IconButton } from '../core/IconButton.jsx';
const T = { accent: ['var(--accent)', 'sparkles'], success: ['var(--success)', 'circle-check'], danger: ['var(--danger)', 'circle-alert'], info: ['var(--info)', 'info'] };
export function Toast({ tone = 'accent', children, action, onClose, fixed, icon }) {
  const [c, i] = T[tone];
  return (
    <div className={'pl-toast' + (fixed ? ' pl-toast--fixed' : '')} role="status" style={{ '--tone': c }}>
      <Icon name={icon || i} size={16} className="pl-toast__icon" />
      <span>{children}</span>
      {action}
      {onClose && <IconButton icon="x" label="Cerrar" size="sm" onClick={onClose} />}
    </div>
  );
}
