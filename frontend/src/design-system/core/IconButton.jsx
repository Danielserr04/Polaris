import React from 'react';
import { Icon } from './Icon.jsx';
const cx = (...a) => a.filter(Boolean).join(' ');
export function IconButton({ icon, label, variant = 'ghost', size = 'md', pressed, className, ...rest }) {
  return (
    <button aria-label={label} title={label} aria-pressed={pressed}
      className={cx('pl-iconbtn', variant !== 'ghost' && 'pl-iconbtn--' + variant, size === 'sm' && 'pl-iconbtn--sm', className)} {...rest}>
      <Icon name={icon} size={size === 'sm' ? 14 : 16} />
    </button>
  );
}
