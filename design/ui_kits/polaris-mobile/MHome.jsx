function MLogin({ onLogin }) {
  const { Logo, Button, Input, Eyebrow } = DS();
  const [user, setUser] = React.useState('dani');
  const [pass, setPass] = React.useState('');
  const [err, setErr] = React.useState(null);
  const [loading, setLoading] = React.useState(null);
  const [leaving, setLeaving] = React.useState(false);
  const go = via => {
    if (via === 'pass' && pass.length < 4) { setErr('Usuario o contraseña incorrectos'); return; }
    setErr(null); setLoading(via);
    setTimeout(() => { setLeaving(true); setTimeout(onLogin, 800); }, 600);
  };
  return (
    <div className="m-login" data-module="polaris">
      <StarTrails pole={[0.5, 0.2]} speed={leaving ? 14 : loading ? 3 : 1} density={1.2} />
      <div className="m-login__veil" />
      <div className="m-login__top pl-rise"><Logo size={26} /><span className="lp__coord">α UMi · +89° 15′</span></div>
      <section className={'m-login__hero' + (leaving ? ' m-login__hero--out' : '')}>
        <div className="pl-rise" style={{ animationDelay: '80ms' }}><Eyebrow star>Tu norte, cada día</Eyebrow></div>
        <h1 className="m-login__word pl-rise" style={{ animationDelay: '140ms' }}>Polaris</h1>
        <p className="m-login__lead pl-rise" style={{ animationDelay: '200ms' }}>Lo que ves, lo que gastas, lo que comes y lo que entrenas. En un solo sitio.</p>
      </section>
      <form className={'m-login__form pl-rise' + (leaving ? ' m-login__form--out' : '')} style={{ animationDelay: '260ms' }} onSubmit={e => { e.preventDefault(); go('pass'); }}>
        <Input label="Usuario" icon="user-round" value={user} onChange={e => setUser(e.target.value)} autoComplete="username" />
        <Input label="Contraseña" icon="key-round" type="password" placeholder="••••••••" value={pass} onChange={e => { setPass(e.target.value); setErr(null); }} error={err} autoComplete="current-password" />
        <Button type="submit" size="lg" block iconRight="arrow-right" loading={loading === 'pass'}>Entrar</Button>
        <div className="lp__or"><span>o</span></div>
        <Button type="button" variant="secondary" size="lg" block loading={loading === 'google'} onClick={() => go('google')}>Continuar con Google</Button>
        <p className="lp__fine">Cualquier contraseña de 4+ caracteres sirve en esta maqueta.</p>
      </form>
    </div>
  );
}

function MInicio({ go }) {
  const { Stat, ProgressBar, LineChart, RingChart, TypeTag, IconButton, Icon, Card } = DS();
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' });
  const E = PD.entradas, enCurso = E.filter(e => e.estado === 'EN_CURSO');
  const K = PD.kuiper, Fu = PD.fusion, A = PD.atlas, N = PD.nucleo;
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const Band = ({ mod, icon, name, sub, to, children }) => (
    <div className="m-band" data-module={mod} onClick={to ? () => go(to) : undefined}>
      <div className="m-band__id"><span className="m-band__ic"><Icon name={icon} size={17} /></span><div className="stack-4"><b>{name}</b><span className="pl-eyebrow">{sub}</span></div>{to && <Icon name="chevron-right" size={18} />}</div>
      {children}
    </div>
  );
  return (
    <MScreen go={go} eyebrow="Inicio" coord={fecha} short={saludo} title={<>{saludo},<br /><span style={{ color: 'var(--accent)' }}>{PD.user.nombre}</span></>}
      action={<IconButton icon="search" label="Buscar" variant="ghost" />}>
      <div className="m-today pl-rise" style={{ animationDelay: '120ms' }}>
        <div data-module="kuiper" onClick={() => go('kuiper')}><span className="today__k"><Icon name="wallet" size={13} />Hoy</span><Stat value={K.dias[K.dias.length - 1]} decimals={2} unit="€" size={26} caption={'media ' + meur(K.gastado / K.dias.length)} /></div>
        <div data-module="fusion" onClick={() => go('fusion')}><span className="today__k"><Icon name="flame" size={13} />Quedan</span><Stat value={Fu.objetivo - Fu.kcal} unit="kcal" size={26} caption="falta la cena" /></div>
        <div data-module="atlas" onClick={() => go('atlas')}><span className="today__k"><Icon name="dumbbell" size={13} />Toca</span><Stat value={A.siguiente.rutina} size={26} caption={'última: ' + A.ultima.rutina} /></div>
        <div data-module="odisea" onClick={() => go('odisea')}><span className="today__k"><Icon name="clapperboard" size={13} />En curso</span><Stat value={enCurso.length} size={26} caption={enCurso.map(e => e.titulo).join(' · ')} /></div>
      </div>

      <MSec title="Módulos" delay={180}>
        <div className="m-box">
          <Band mod="odisea" icon="clapperboard" name="Odisea" sub="Ocio · en curso" to="odisea">
            <div className="stack-12">{enCurso.map(e => (
              <div key={e.id} className="m-prow"><TypeTag tipo={e.tipo} showLabel={false} size={14} /><b>{e.titulo}</b><span className="pl-row__num">{e.tipo === 'LIBRO' ? `${e.progreso}/${e.duracionMin}` : `ep. ${e.progreso}`}</span>
                <ProgressBar value={e.progreso} max={e.tipo === 'LIBRO' ? e.duracionMin : 62} valueLabel={false} /></div>
            ))}</div>
          </Band>
          <Band mod="kuiper" icon="wallet" name="Kuiper" sub="Septiembre · maqueta" to="kuiper">
            <div className="stack-12"><Stat value={K.gastado} decimals={2} unit="€" size={30} />
              <ProgressBar value={K.gastado} max={K.presupuesto} target={K.presupuesto * 25 / 30} valueLabel={`de ${meur(K.presupuesto, 0)}`} label={`Quedan ${meur(K.presupuesto - K.gastado)}`} /></div>
          </Band>
          <Band mod="fusion" icon="flame" name="Fusión" sub="Hoy · maqueta" to="fusion">
            <div className="m-band__row">
              <div className="stack-8">{Fu.macros.map(m => <ProgressBar key={m.nombre} label={m.nombre} value={m.g} max={m.obj} valueLabel={m.g + '/' + m.obj} />)}</div>
              <RingChart value={Fu.kcal} max={Fu.objetivo} size={88} thickness={8} label={Fu.kcal.toLocaleString('es-ES')} sublabel="kcal" />
            </div>
          </Band>
          <Band mod="atlas" icon="dumbbell" name="Atlas" sub="Semana 39 · maqueta" to="atlas">
            <div className="m-band__row">
              <div className="stack-4"><span className="pl-eyebrow" style={{ color: 'var(--accent)' }}>Siguiente · hoy</span><b className="big">{A.siguiente.rutina}</b></div>
              <div className="week week--sm" style={{ width: 170, flex: 'none' }}>{dias.map((d, i) => <div key={d} className={'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')}><span>{d}</span><i /></div>)}</div>
            </div>
          </Band>
          <Band mod="nucleo" icon="orbit" name="Núcleo" sub="Peso · maqueta">
            <div className="m-band__row">
              <Stat value={N.peso} decimals={1} unit="kg" size={30} delta={'−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg'} deltaTone="up" />
              <div style={{ width: 150, flex: 'none' }}><LineChart height={48} series={[{ name: 'Peso', points: N.serie }]} showAxis={false} gridLines={2} /></div>
            </div>
          </Band>
        </div>
      </MSec>

      <MSec title="Lo último" delay={240}>
        <div className="m-box m-feed">
          {PD.actividad.map((a, i) => (
            <div key={i} className="m-feed__r" data-module={a.mod} onClick={() => a.mod !== 'nucleo' && go(a.mod)}>
              <span className="feed__ic"><Icon name={a.icon} size={14} /></span>
              <span className="stack-4" style={{ minWidth: 0 }}><span className="feed__t">{a.txt}</span><span className="pl-row__num">{a.det}</span></span>
              <span className="pl-row__num">{a.hora}</span>
            </div>
          ))}
        </div>
      </MSec>
    </MScreen>
  );
}
Object.assign(window, { MLogin, MInicio });
