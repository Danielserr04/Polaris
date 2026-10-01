import React from 'react';
import { Icon } from '../core/Icon.jsx';
const T = { info: ['var(--info)', 'info'], success: ['var(--success)', 'circle-check'], warning: ['var(--warning)', 'mail-warning'], danger: ['var(--danger)', 'circle-alert'], accent: ['var(--accent)', 'sparkles'] };
export function Alert({ tone = 'info', title, children, action, icon, style }) {
  const [c, i] = T[tone];
  return (
    <div className="pl-alert" role="status" style={{ '--tone': c, ...style }}>
      <Icon name={icon || i} size={17} className="pl-alert__icon" />
      <div className="pl-alert__main">{title && <span className="pl-alert__title">{title}</span>}{children && <span className="pl-alert__body">{children}</span>}</div>
      {action && <div className="pl-alert__action">{action}</div>}
    </div>
  );
}
