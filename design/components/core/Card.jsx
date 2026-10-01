import React from 'react';
import { Eyebrow } from './Eyebrow.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function Card({ eyebrow, title, action, footer, interactive, variant = 'default', padding, children, className, style, onClick, delay }) {
  return (
    <div className={cx('pl-card', interactive && 'pl-card--interactive', variant !== 'default' && 'pl-card--' + variant, delay != null && 'pl-rise', className)}
      style={{ animationDelay: delay != null ? delay + 'ms' : undefined, ...style }} onClick={onClick}>
      {(eyebrow || title || action) && (
        <div className="pl-card__head">
          <div className="pl-card__titles">
            {eyebrow && (typeof eyebrow === 'string' ? <Eyebrow star>{eyebrow}</Eyebrow> : eyebrow)}
            {title && <h3 className="pl-card__title">{title}</h3>}
          </div>
          {action}
        </div>
      )}
      <div className="pl-card__body" style={padding != null ? { padding } : undefined}>{children}</div>
      {footer && <div className="pl-card__foot">{footer}</div>}
    </div>
  );
}
