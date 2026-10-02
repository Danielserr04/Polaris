import React from 'react';
import { Icon } from '../core/Icon.jsx';
import { Logo } from '../core/Logo.jsx';
import { Avatar } from '../core/Avatar.jsx';
import { useIndicator } from '../core/hooks.jsx';
export function NavBar({ items = [], value, onChange, onBrand, onSearch, actions, user, style }) {
  const [refs, pos] = useIndicator(value, [items.length]);
  const cur = items.find(i => i.id === value);
  return (
    <nav className="pl-nav" style={style}>
      <button className="pl-nav__brand" onClick={onBrand} aria-label="Polaris — inicio"><Logo variant="mark" size={24} /></button>
      <div className="pl-nav__items">
        {pos && cur && <span className="pl-nav__ind" style={{ left: pos.left, width: pos.width, '--ind': cur.color || 'var(--accent)' }} />}
        {items.map(it => (
          <button key={it.id} ref={el => (refs.current[it.id] = el)} className="pl-nav__item" aria-current={it.id === value ? 'page' : undefined}
            style={{ '--c': it.color || 'var(--accent)' }} onClick={() => onChange && onChange(it.id)}>
            {it.icon && <Icon name={it.icon} size={16} />}{it.label}
          </button>
        ))}
      </div>
      {onSearch !== null && <button className="pl-nav__search" onClick={onSearch}><Icon name="search" size={14} /><span>Buscar</span></button>}
      {actions && <div className="pl-nav__acts">{actions}</div>}
      {user && <button className="pl-nav__user" onClick={user.onClick} aria-label="Perfil"><Avatar name={user.name} src={user.src} size={34} /></button>}
    </nav>
  );
}
