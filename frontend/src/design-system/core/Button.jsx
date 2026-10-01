import React from 'react';
import { Icon } from './Icon.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function Button({ variant = 'primary', size = 'md', icon, iconRight, loading, block, disabled, children, className, ...rest }) {
  const is = size === 'sm' ? 14 : size === 'lg' ? 18 : 16;
  return (
    <button className={cx('pl-btn', 'pl-btn--' + variant, size !== 'md' && 'pl-btn--' + size, block && 'pl-btn--block', className)}
      disabled={disabled || loading} {...rest}>
      {loading ? <span className="pl-btn__spin" /> : icon && <Icon name={icon} size={is} />}
      {children}
      {iconRight && !loading && <Icon name={iconRight} size={is} />}
    </button>
  );
}
