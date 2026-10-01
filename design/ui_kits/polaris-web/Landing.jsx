function Landing({ onLogin }) {
  const { Logo, Button, Input, Eyebrow } = window.PolarisDesignSystem_b0ab94;
  const [user, setUser] = React.useState('dani');
  const [pass, setPass] = React.useState('');
  const [err, setErr] = React.useState(null);
  const [loading, setLoading] = React.useState(null);
  const [leaving, setLeaving] = React.useState(false);
  const [now, setNow] = React.useState(new Date());
  React.useEffect(() => { const i = setInterval(() => setNow(new Date()), 1000); return () => clearInterval(i); }, []);
  const go = (via) => {
    if (via === 'pass' && pass.length < 4) { setErr('Usuario o contraseña incorrectos'); return; }
    setErr(null); setLoading(via);
    setTimeout(() => { setLeaving(true); setTimeout(onLogin, 900); }, 650);
  };
  const mods = [['Odisea', 'Ocio', 'var(--mod-odisea)'], ['Kuiper', 'Gastos', 'var(--mod-kuiper)'], ['Fusión', 'Nutrición', 'var(--mod-fusion)'], ['Atlas', 'Gym', 'var(--mod-atlas)']];
  const time = now.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  return (
    <div className="lp" data-module="polaris">
      <StarTrails anchor={() => { const c = document.querySelector('.lp__card'), h = document.querySelector('.lp__word'); if (!c || !h) return null; const cr = c.getBoundingClientRect(), hr = h.getBoundingClientRect(); return [(hr.right + cr.left) / 2, Math.max(70, cr.top - 70)]; }} speed={leaving ? 14 : loading ? 3 : 1} />
      <div className="lp__veil" />
      <header className="lp__top pl-rise">
        <Logo size={30} />
        <span className="lp__coord">α UMi <b>·</b> RA 02h 31m 49s <b>·</b> Dec +89° 15′ 51″ <b>·</b> {time}</span>
      </header>
      <main className={'lp__main' + (leaving ? ' lp__main--out' : '')}>
        <section className="lp__hero">
          <div className="pl-rise" style={{ animationDelay: '80ms' }}><Eyebrow star>Tu norte, cada día</Eyebrow></div>
          <h1 className="lp__word pl-rise" style={{ animationDelay: '140ms' }}>Polaris</h1>
          <p className="lp__lead pl-rise" style={{ animationDelay: '220ms' }}>Lo que ves, lo que gastas, lo que comes y lo que entrenas. En un solo sitio, sin ruido.</p>
          <ul className="lp__mods">
            {mods.map(([n, d, c], i) => (
              <li key={n} className="pl-rise" style={{ animationDelay: 300 + i * 70 + 'ms', '--c': c }}><i /><b>{n}</b><span>{d}</span></li>
            ))}
          </ul>
        </section>
        <section className="lp__card pl-rise" style={{ animationDelay: '260ms' }}>
          <div className="lp__cardhead">
            <h2>Entrar</h2>
            <span className="pl-eyebrow">Sesión personal</span>
          </div>
          <form className="lp__form" onSubmit={e => { e.preventDefault(); go('pass'); }}>
            <Input label="Usuario" icon="user-round" value={user} onChange={e => setUser(e.target.value)} autoComplete="username" />
            <Input label="Contraseña" icon="key-round" type="password" placeholder="••••••••" value={pass} onChange={e => { setPass(e.target.value); setErr(null); }} error={err} autoComplete="current-password" />
            <Button type="submit" size="lg" block iconRight="arrow-right" loading={loading === 'pass'}>Entrar</Button>
          </form>
          <div className="lp__or"><span>o</span></div>
          <Button variant="secondary" size="lg" block loading={loading === 'google'} onClick={() => go('google')}>Continuar con Google</Button>
          <p className="lp__fine">Cualquier contraseña de 4+ caracteres sirve en esta maqueta.</p>
        </section>
      </main>
      <footer className="lp__foot"><span>Uso personal · un solo usuario</span><span>Odisea · Kuiper · Fusión · Atlas · Núcleo</span></footer>
    </div>
  );
}
window.Landing = Landing;
