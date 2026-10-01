const NAV = [
  { id: 'inicio', label: 'Inicio', icon: 'compass', color: 'var(--mod-polaris)', mod: 'polaris' },
  { id: 'odisea', label: 'Odisea', icon: 'clapperboard', color: 'var(--mod-odisea)', mod: 'odisea' },
  { id: 'kuiper', label: 'Kuiper', icon: 'wallet', color: 'var(--mod-kuiper)', mod: 'kuiper' },
  { id: 'fusion', label: 'Fusión', icon: 'flame', color: 'var(--mod-fusion)', mod: 'fusion' },
  { id: 'atlas', label: 'Atlas', icon: 'dumbbell', color: 'var(--mod-atlas)', mod: 'atlas' },
];
function AppShell({ route, go, children }) {
  const { NavBar } = window.PolarisDesignSystem_b0ab94;
  const [cmd, setCmd] = React.useState(false);
  React.useEffect(() => {
    const k = e => { if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') { e.preventDefault(); setCmd(c => !c); } };
    window.addEventListener('keydown', k); return () => window.removeEventListener('keydown', k);
  }, []);
  const mod = route === 'perfil' ? 'nucleo' : (NAV.find(n => n.id === route) || NAV[0]).mod;
  return (
    <div className="app" data-module={mod}>
      <div className="app__sky"><StarTrails pole={[0.5, -0.08]} speed={0.35} density={0.7} /></div>
      <div className="app__veil" />
      <div className="app__glow" />
      <div className="app__nav"><NavBar items={NAV} value={route === 'perfil' ? null : route} onChange={go} onBrand={() => go('inicio')} onSearch={() => setCmd(true)} user={{ name: PD.user.nombre, onClick: () => go('perfil') }} /></div>
      <main className="app__main" key={route}>{children}</main>
      <CommandPalette open={cmd} onClose={() => setCmd(false)} go={r => { setCmd(false); go(r); }} />
    </div>
  );
}
function PageHeader({ eyebrow, coord, title, actions, badge }) {
  const { Eyebrow } = window.PolarisDesignSystem_b0ab94;
  return (
    <header className="ph">
      <div className="ph__t">
        <div className="pl-rise" style={{ display: 'flex', gap: 12, alignItems: 'center' }}><Eyebrow star coord={coord}>{eyebrow}</Eyebrow>{badge}</div>
        <h1 className="ph__h pl-rise" style={{ animationDelay: '60ms' }}>{title}</h1>
      </div>
      {actions && <div className="ph__a pl-rise" style={{ animationDelay: '120ms' }}>{actions}</div>}
    </header>
  );
}
function CommandPalette({ open, onClose, go }) {
  const { Dialog, Input, Icon, TypeTag } = window.PolarisDesignSystem_b0ab94;
  const [q, setQ] = React.useState('');
  const items = [...NAV.map(n => ({ k: n.id, label: n.label, icon: n.icon, hint: 'Módulo', go: n.id })), { k: 'perfil', label: 'Perfil de cuenta', icon: 'user-round', hint: 'Cuenta', go: 'perfil' },
    ...PD.entradas.map(e => ({ k: 'e' + e.id, label: e.titulo, tipo: e.tipo, hint: 'Odisea', go: 'odisea' }))].filter(i => i.label.toLowerCase().includes(q.toLowerCase()));
  return (
    <Dialog open={open} onClose={onClose} title="Buscar" width={520}>
      <Input icon="search" autoFocus placeholder="Módulos, títulos…" value={q} onChange={e => setQ(e.target.value)} />
      <div className="cmd">
        {items.map(i => (
          <button key={i.k} className="cmd__i" onClick={() => go(i.go)}>
            {i.tipo ? <TypeTag tipo={i.tipo} showLabel={false} /> : <Icon name={i.icon} size={15} />}<span>{i.label}</span><em>{i.hint}</em>
          </button>
        ))}
        {!items.length && <div className="cmd__empty">Nada con «{q}».</div>}
      </div>
    </Dialog>
  );
}
function Maqueta() { const { Badge } = window.PolarisDesignSystem_b0ab94; return <span title="Datos inventados: el módulo aún no tiene backend"><Badge variant="outline" color="var(--text-3)">Maqueta</Badge></span>; }
const eur = (n, d = 2) => n.toLocaleString('es-ES', { minimumFractionDigits: d, maximumFractionDigits: d }) + ' €';
Object.assign(window, { AppShell, PageHeader, CommandPalette, Maqueta, NAV, eur });
