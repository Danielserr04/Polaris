const DS = () => window.PolarisDesignSystem_b0ab94;
const MNAV = [
  { id: 'inicio', label: 'Inicio', icon: 'compass', mod: 'polaris' },
  { id: 'odisea', label: 'Odisea', icon: 'clapperboard', mod: 'odisea' },
  { id: 'kuiper', label: 'Kuiper', icon: 'wallet', mod: 'kuiper' },
  { id: 'fusion', label: 'Fusión', icon: 'flame', mod: 'fusion' },
  { id: 'atlas', label: 'Atlas', icon: 'dumbbell', mod: 'atlas' },
];
const meur = (n, d = 2) => n.toLocaleString('es-ES', { minimumFractionDigits: d, maximumFractionDigits: d }) + ' €';

function MTabBar({ route, go }) {
  const { Icon } = DS();
  const i = MNAV.findIndex(n => n.id === route);
  return (
    <nav className="m-tabs"><div className="m-tabs__in">
      {i >= 0 && <span className="m-tabs__ind" data-module={MNAV[i].mod} style={{ left: `calc(${i} * (100% - 8px) / 5 + ${i * 2}px)` }} />}
      {MNAV.map(n => (
        <button key={n.id} data-module={n.mod} className={'m-tab' + (route === n.id ? ' on' : '')} aria-current={route === n.id ? 'page' : undefined} onClick={() => go(n.id)}>
          <Icon name={n.icon} size={19} /><span>{n.label}</span>
        </button>
      ))}
    </div></nav>
  );
}

// Pantalla con cabecera grande que colapsa a barra compacta al hacer scroll
function MScreen({ eyebrow, coord, title, short, action, badge, back, go, children }) {
  const { Eyebrow, Avatar, Logo, Icon } = DS();
  const [sc, setSc] = React.useState(false);
  return (
    <>
      <div className={'m-top' + (sc ? ' on' : '')}>{short || eyebrow}</div>
      <div className="m-scroll" onScroll={e => setSc(e.currentTarget.scrollTop > 70)}>
        <header className="m-hd">
          <div className="m-hd__bar pl-rise">
            {back ? <button className="m-back" onClick={back}><Icon name="chevron-left" size={20} />Inicio</button> : <Logo variant="mark" size={24} />}
            <div className="m-hd__act">{action}{!back && <button className="m-av" onClick={() => go('perfil')} aria-label="Perfil"><Avatar name={PD.user.nombre} size={34} /></button>}</div>
          </div>
          <div className="pl-rise" style={{ display: 'flex', gap: 10, alignItems: 'center', animationDelay: '40ms' }}><Eyebrow star coord={coord}>{eyebrow}</Eyebrow>{badge}</div>
          <h1 className="m-hd__h pl-rise" style={{ animationDelay: '80ms' }}>{title}</h1>
        </header>
        {children}
      </div>
    </>
  );
}

function MSec({ title, action, children, delay = 0 }) {
  return (
    <section className="m-sec pl-rise" style={{ animationDelay: delay + 'ms' }}>
      {title && <div className="m-sec__h"><b>{title}</b>{action}</div>}
      {children}
    </section>
  );
}

function MSheet({ open, onClose, title, children }) {
  const { IconButton } = DS();
  return (
    <div className={'m-sheet' + (open ? ' on' : '')} aria-hidden={!open}>
      <div className="m-sheet__bg" onClick={onClose} />
      <div className="m-sheet__p" role="dialog">
        <div className="m-sheet__grab"><i /></div>
        <div className="m-sheet__hd"><b>{title}</b><IconButton icon="x" label="Cerrar" variant="ghost" onClick={onClose} /></div>
        <div className="m-sheet__b">{children}</div>
      </div>
    </div>
  );
}

function MMaqueta() { const { Badge } = DS(); return <Badge variant="outline" color="var(--text-3)">Maqueta</Badge>; }

Object.assign(window, { DS, MNAV, meur, MTabBar, MScreen, MSec, MSheet, MMaqueta });
