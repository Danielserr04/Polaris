function MKuiper({ go }) {
  const { Stat, BarChart, ProgressBar, SegmentedControl, IconButton, Icon, Badge } = DS();
  const K = PD.kuiper;
  const [tab, setTab] = React.useState('resumen');
  const media = K.gastado / K.dias.length;
  const icon = c => (K.categorias.find(x => x.nombre === c) || { icon: 'wallet' }).icon;
  const Movs = ({ rows }) => (
    <div className="m-box">{rows.map((m, i) => (
      <div key={i} className="m-mov">
        <span className="catic"><Icon name={icon(m.categoria)} size={15} /></span>
        <span style={{ minWidth: 0 }}><b>{m.concepto}</b><small>{m.fecha} · {m.categoria}</small></span>
        <span className="money" style={{ color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)' }}>{m.tipo === 'INGRESO' ? '+' : '−'}{meur(m.importe)}</span>
      </div>
    ))}</div>
  );
  return (
    <MScreen go={go} eyebrow="Kuiper" coord="Sep 2026" short="Gastos" title="Gastos" badge={<MMaqueta />} action={<IconButton icon="plus" label="Movimiento" variant="solid" />}>
      <div className="pl-rise" style={{ animationDelay: '120ms' }}><SegmentedControl value={tab} onChange={setTab} options={[{ value: 'resumen', label: 'Resumen' }, { value: 'mov', label: 'Movimientos' }]} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr' }} /></div>
      {tab === 'resumen' ? <>
        <MSec delay={160}>
          <div className="m-box">
            <div className="m-hero">
              <Stat label="Gastado en septiembre" value={K.gastado} decimals={2} unit="€" size={40} delta="−8 %" deltaTone="up" caption="vs agosto" />
              <ProgressBar value={K.gastado} max={K.presupuesto} target={K.presupuesto * 25 / 30} valueLabel={`de ${meur(K.presupuesto, 0)}`} label={`Quedan ${meur(K.presupuesto - K.gastado)} · 5 días`} />
            </div>
            <div className="m-kv" style={{ borderTop: '1px solid var(--border-1)' }}>
              <Stat label="Ingresos" value={K.ingresos} decimals={0} unit="€" />
              <Stat label="Balance" value={K.ingresos - K.gastado} decimals={0} unit="€" delta="+12 %" />
            </div>
          </div>
        </MSec>
        <MSec title="Día a día" action={<span className="pl-row__num">media {meur(media)}</span>} delay={200}>
          <div className="m-box m-pad"><BarChart height={140} gap={3} highlight={13} target={media} data={K.dias.slice(-14).map((v, i) => ({ label: i % 4 === 0 ? String(12 + i) : '', value: v }))} format={v => meur(v)} /></div>
        </MSec>
        <MSec title="Por categoría" delay={240}>
          <div className="m-box">{K.categorias.map(c => (
            <div key={c.nombre} className="m-cat"><span className="catic"><Icon name={c.icon} size={15} /></span>
              <ProgressBar label={<>{c.nombre}{c.gastado > c.limite && <Badge tone="danger" style={{ marginLeft: 8, height: 18 }}>+{meur(c.gastado - c.limite, 0)}</Badge>}</>} value={c.gastado} max={c.limite} valueLabel={meur(c.gastado, 0) + ' / ' + meur(c.limite, 0)} /></div>
          ))}</div>
        </MSec>
        <MSec title="Últimos" action={<button className="m-back" style={{ height: 24 }} onClick={() => setTab('mov')}>Ver todos</button>} delay={280}><Movs rows={K.movimientos.slice(0, 4)} /></MSec>
      </> : <MSec delay={100}><Movs rows={[...K.movimientos, ...K.movimientos.map(m => ({ ...m, fecha: m.fecha.replace('2', '1') }))]} /></MSec>}
    </MScreen>
  );
}

function MFusion({ go }) {
  const { RingChart, Stat, LineChart, Button, IconButton } = DS();
  const Fu = PD.fusion;
  const colors = ['var(--accent)', 'var(--mod-kuiper)', 'var(--mod-nucleo)'];
  return (
    <MScreen go={go} eyebrow="Fusión" coord="Jue 25 sep" short="Hoy" title="Hoy" badge={<MMaqueta />} action={<IconButton icon="plus" label="Registrar comida" variant="solid" />}>
      <MSec delay={120}>
        <div className="m-box">
          <div className="m-ring">
            <RingChart value={Fu.kcal} max={Fu.objetivo} size={132} thickness={12} label={Fu.kcal.toLocaleString('es-ES')} sublabel={'de ' + Fu.objetivo.toLocaleString('es-ES')} />
            <div className="stack-8" style={{ flex: 1, minWidth: 0 }}><Stat label="Te quedan" value={Fu.objetivo - Fu.kcal} unit="kcal" size={30} /><span className="muted" style={{ fontSize: 12.5 }}>Falta la cena.</span></div>
          </div>
          <div className="m-macros">{Fu.macros.map((m, i) => (
            <div key={m.nombre}><RingChart value={m.g} max={m.obj} size={72} thickness={6} color={colors[i]} label={m.g} sublabel={'/ ' + m.obj} /><b>{m.nombre}</b></div>
          ))}</div>
        </div>
      </MSec>
      <MSec title="Comidas" delay={180}>
        <div className="m-box">{Fu.comidas.map(c => {
          const k = c.lineas.reduce((a, l) => a + l[2], 0);
          return (
            <div key={c.momento} className="m-meal">
              <div className="meal__h"><b>{c.momento}</b><span className="pl-row__num">{c.hora}</span>{k ? <span className="money">{k} kcal</span> : <span style={{ marginLeft: 'auto' }}><Button size="sm" variant="secondary" icon="plus">Añadir</Button></span>}</div>
              {c.lineas.map(l => <div key={l[0]} className="meal__l"><span>{l[0]}</span><span className="pl-row__num">{l[1]} g</span><span className="pl-row__num">{l[2]} kcal</span></div>)}
            </div>
          );
        })}</div>
      </MSec>
      <MSec title="Últimos 14 días" delay={240}>
        <div className="m-box m-pad"><LineChart height={150} labels={['12 sep', '18 sep', '25 sep']} format={v => Math.round(v / 100) / 10 + 'k'} series={[{ name: 'Kcal', points: Fu.semana }, { name: 'Objetivo', points: Fu.semana.map(() => Fu.objetivo), dashed: true, color: 'var(--text-3)' }]} /></div>
      </MSec>
    </MScreen>
  );
}

function MAtlas({ go }) {
  const { Stat, LineChart, Button, Badge } = DS();
  const A = PD.atlas, dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  return (
    <MScreen go={go} eyebrow="Atlas" coord="Semana 39" short="Progresión" title="Progresión" badge={<MMaqueta />}>
      <MSec delay={120}>
        <div className="m-next">
          <span className="pl-eyebrow" style={{ color: 'var(--accent)' }}>Siguiente · {A.siguiente.dia}</span>
          <span className="m-next__h">{A.siguiente.rutina}</span>
          <div className="week">{dias.map((d, i) => <div key={d} className={'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')}><span>{d}</span><i /></div>)}</div>
          <Button size="lg" block icon="play">Empezar {A.siguiente.rutina}</Button>
        </div>
      </MSec>
      <MSec delay={180}>
        <div className="m-box m-kv">
          <Stat label="Sesiones · sep" value={11} delta="+2" />
          <Stat label="Volumen sem." value={25.3} decimals={1} unit="t" delta="+15 %" />
          <Stat label="1RM banca" value={80} unit="kg" caption="Epley" />
          <Stat label="Peso" value={PD.nucleo.peso} decimals={1} unit="kg" caption="Núcleo" />
        </div>
      </MSec>
      <MSec title="Press banca · 1RM" delay={220}>
        <div className="m-box m-pad"><LineChart height={150} labels={A.semanas.filter((_, i) => i % 3 === 0)} format={v => Math.round(v) + ''} series={[{ name: '1RM', points: A.progresion }, { name: 'Objetivo', points: A.progresion.map(() => 85), dashed: true, color: 'var(--text-3)' }]} /></div>
      </MSec>
      <MSec title="Récords" delay={260}>
        <div className="m-box">{A.records.map(r => (
          <div key={r.ej} className="rec"><div className="stack-4"><b>{r.ej}</b><span className="pl-row__num">{r.fecha}</span></div><span className="money">{r.v}</span>{r.nuevo && <Badge tone="accent" variant="solid">Nuevo</Badge>}</div>
        ))}</div>
      </MSec>
    </MScreen>
  );
}

function MPerfil({ go, logout }) {
  const { Avatar, Icon, Badge, Button, Switch } = DS();
  const U = PD.user;
  const [notif, setNotif] = React.useState(true);
  const Row = ({ icon, t, s, right }) => <div className="m-set"><span className="catic"><Icon name={icon} size={15} /></span><span><span>{t}</span>{s && <small>{s}</small>}</span>{right || <Icon name="chevron-right" size={16} />}</div>;
  return (
    <MScreen go={go} back={() => go('inicio')} eyebrow="Cuenta" coord={'@' + U.username} short="Perfil" title="Perfil">
      <div className="m-pf pl-rise" style={{ animationDelay: '100ms' }}><Avatar name={U.nombre} size={84} /><b>{U.nombre}</b><span className="muted" style={{ fontSize: 13.5 }}>{U.email}</span></div>
      <MSec title="Datos" delay={160}>
        <div className="m-box">
          <Row icon="user-round" t="Nombre y usuario" s={U.nombre + ' · @' + U.username} />
          <Row icon="mail" t="Correo" s={U.email} right={<Badge tone="success">Verificado</Badge>} />
          <Row icon="image-up" t="Foto de perfil" s="Sin foto" />
        </div>
      </MSec>
      <MSec title="Acceso" delay={200}>
        <div className="m-box">
          <Row icon="lock" t="Contraseña" s="Cambiada hace 28 días" />
          <Row icon="link-2" t="Google" s="Vinculada" right={<Badge tone="success">Activa</Badge>} />
          <Row icon="info" t="Avisos" s="Resumen diario a las 21:00" right={<Switch checked={notif} onChange={setNotif} />} />
        </div>
      </MSec>
      <div className="pl-rise" style={{ marginTop: 26, animationDelay: '240ms' }}><Button variant="danger" size="lg" block icon="log-out" onClick={logout}>Cerrar sesión</Button></div>
    </MScreen>
  );
}
Object.assign(window, { MKuiper, MFusion, MAtlas, MPerfil });
