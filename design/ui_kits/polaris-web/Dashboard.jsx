// Inicio — layout "bandas": un panel de módulos (una fila por módulo) + columna de actividad.
function Band({ mod, icon, name, sub, mock, go, to, children, aside }) {
  const { Icon, Badge } = window.PolarisDesignSystem_b0ab94;
  return (
    <div className="band" data-module={mod} onClick={to ? () => go(to) : undefined} style={{ cursor: to ? 'pointer' : 'default' }}>
      <div className="band__id">
        <span className="band__ic"><Icon name={icon} size={18} /></span>
        <div className="stack-4" style={{ minWidth: 0 }}>
          <b className="band__name">{name}{to && <Icon name="arrow-up-right" size={14} className="band__go" />}</b>
          <span className="pl-eyebrow">{sub}</span>
          {mock && <span title="Datos inventados: el módulo aún no tiene backend" style={{ marginTop: 6 }}><Badge variant="outline" color="var(--text-3)">Maqueta</Badge></span>}
        </div>
      </div>
      <div className="band__main">{children}</div>
      <div className="band__aside">{aside}</div>
    </div>
  );
}
function Dashboard({ go }) {
  const { Card, Stat, ProgressBar, BarChart, LineChart, RingChart, StateGlyph, TypeTag, Button, Icon } = window.PolarisDesignSystem_b0ab94;
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  const E = PD.entradas, enCurso = E.filter(e => e.estado === 'EN_CURSO');
  const K = PD.kuiper, Fu = PD.fusion, A = PD.atlas, N = PD.nucleo;
  const cnt = s => E.filter(e => e.estado === s).length;
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const peor = [...K.categorias].sort((a, b) => b.gastado / b.limite - a.gastado / a.limite)[0];
  return (
    <div className="dash">
      <PageHeader eyebrow="Inicio" coord={fecha.toUpperCase()} title={<>{saludo}, <span style={{ color: 'var(--accent)' }}>{PD.user.nombre}</span></>}
        actions={<Button variant="secondary" icon="plus" onClick={() => go('odisea')}>Añadir</Button>} />

      <div className="stats stats--today pl-rise" style={{ animationDelay: '60ms', marginBottom: 24 }}>
        <div data-module="kuiper" className="today" onClick={() => go('kuiper')}><span className="today__k"><Icon name="wallet" size={14} />Gastado hoy</span><Stat value={K.dias[K.dias.length - 1]} decimals={2} unit="€" size={34} caption={'media ' + eur(K.gastado / K.dias.length)} /></div>
        <div data-module="fusion" className="today" onClick={() => go('fusion')}><span className="today__k"><Icon name="flame" size={14} />Te quedan</span><Stat value={Fu.objetivo - Fu.kcal} unit="kcal" size={34} caption="falta la cena" /></div>
        <div data-module="atlas" className="today" onClick={() => go('atlas')}><span className="today__k"><Icon name="dumbbell" size={14} />Hoy toca</span><Stat value={A.siguiente.rutina} size={34} caption={'última: ' + A.ultima.rutina + ', ' + A.ultima.dia.toLowerCase()} /></div>
        <div data-module="odisea" className="today" onClick={() => go('odisea')}><span className="today__k"><Icon name="clapperboard" size={14} />En curso</span><Stat value={enCurso.length} size={34} caption={enCurso.map(e => e.titulo).join(' · ')} /></div>
      </div>

      <div className="dash__cols">
        <section className="bands pl-rise" style={{ animationDelay: '120ms' }}>
          <Band mod="odisea" icon="clapperboard" name="Odisea" sub="Ocio" go={go} to="odisea"
            aside={<div className="states">{[['EN_CURSO', 'En curso'], ['PENDIENTE', 'Pendiente'], ['TERMINADO', 'Terminado'], ['ABANDONADO', 'Abandonado']].map(([s, l]) => <span key={s}><StateGlyph estado={s} /><b>{cnt(s)}</b>{l}</span>)}</div>}>
            <div className="stack-12">
              {enCurso.map(e => (
                <div key={e.id} className="prow">
                  <TypeTag tipo={e.tipo} showLabel={false} size={15} />
                  <b>{e.titulo}</b>
                  <ProgressBar value={e.progreso} max={e.tipo === 'LIBRO' ? e.duracionMin : 62} valueLabel={false} />
                  <span className="pl-row__num">{e.tipo === 'LIBRO' ? `pág. ${e.progreso}/${e.duracionMin}` : `ep. ${e.progreso}`}</span>
                </div>
              ))}
            </div>
          </Band>
          <Band mod="kuiper" icon="wallet" name="Kuiper" sub="Septiembre" mock go={go} to="kuiper"
            aside={<BarChart height={64} gap={3} data={K.dias.slice(-14).map(v => ({ label: '', value: v }))} highlight={13} format={v => eur(v)} showAxis={false} gridLines={2} />}>
            <div className="bmain">
              <Stat value={K.gastado} decimals={2} unit="€" size={36} />
              <div className="stack-8" style={{ flex: 1 }}>
                <ProgressBar value={K.gastado} max={K.presupuesto} target={K.presupuesto * 25 / 30} valueLabel={`de ${eur(K.presupuesto, 0)}`} label={`Quedan ${eur(K.presupuesto - K.gastado)}`} />
                <span className="muted" style={{ fontSize: 12.5 }}>{peor.nombre} va por encima: {eur(peor.gastado, 0)} de {eur(peor.limite, 0)}</span>
              </div>
            </div>
          </Band>
          <Band mod="fusion" icon="flame" name="Fusión" sub="Hoy" mock go={go} to="fusion"
            aside={<div style={{ display: 'flex', justifyContent: 'flex-end' }}><RingChart value={Fu.kcal} max={Fu.objetivo} size={84} thickness={8} label={Fu.kcal.toLocaleString('es-ES')} sublabel="KCAL" /></div>}>
            <div className="macros3">{Fu.macros.map(m => <ProgressBar key={m.nombre} label={m.nombre} value={m.g} max={m.obj} valueLabel={m.g + '/' + m.obj + ' g'} />)}</div>
          </Band>
          <Band mod="atlas" icon="dumbbell" name="Atlas" sub="Semana 39" mock go={go} to="atlas"
            aside={<div className="week week--sm">{dias.map((d, i) => <div key={d} className={'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')}><span>{d}</span><i /></div>)}</div>}>
            <div className="bmain">
              <div className="stack-4"><span className="pl-eyebrow">Última · {A.ultima.dia}</span><b className="big">{A.ultima.rutina}</b><span className="muted" style={{ fontSize: 12.5 }}>{A.ultima.series} series · {A.ultima.volumen.toLocaleString('es-ES')} kg</span></div>
              <div className="stack-4 next"><span className="pl-eyebrow" style={{ color: 'var(--accent)' }}>Siguiente · {A.siguiente.dia}</span><b className="big">{A.siguiente.rutina}</b></div>
              <Button size="sm" icon="play" onClick={e => { e.stopPropagation(); go('atlas'); }}>Empezar</Button>
            </div>
          </Band>
          <Band mod="nucleo" icon="orbit" name="Núcleo" sub="Peso" mock go={go}
            aside={<LineChart height={56} series={[{ name: 'Peso', points: N.serie }]} showAxis={false} gridLines={2} />}>
            <div className="bmain"><Stat value={N.peso} decimals={1} unit="kg" size={36} delta={'−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg'} deltaTone="up" caption="en 10 días" /></div>
          </Band>
        </section>

        <aside className="dash__side">
          <Card delay={180} eyebrow="Actividad" title="Lo último" padding="4px 0 6px">
            <div className="feed">
              {PD.actividad.map((a, i) => (
                <div key={i} className="feed__r feed__r--stack pl-rise" data-module={a.mod} style={{ animationDelay: 220 + i * 50 + 'ms' }} onClick={() => go(a.mod === 'nucleo' ? 'inicio' : a.mod)}>
                  <span className="feed__ic"><Icon name={a.icon} size={14} /></span>
                  <span className="stack-4" style={{ minWidth: 0 }}><span className="feed__t">{a.txt}</span><span className="pl-row__num">{a.det}</span></span>
                  <span className="pl-row__num feed__h">{a.hora}</span>
                </div>
              ))}
            </div>
          </Card>
        </aside>
      </div>
    </div>
  );
}
window.Dashboard = Dashboard;
