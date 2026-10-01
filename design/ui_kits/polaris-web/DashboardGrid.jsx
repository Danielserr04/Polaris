function DashboardGrid({ go }) {
  const { Card, Stat, ProgressBar, BarChart, LineChart, RingChart, StatusBadge, TypeTag, Rating, Button, Icon, Badge } = window.PolarisDesignSystem_b0ab94;
  const h = new Date().getHours();
  const saludo = h < 13 ? 'Buenos días' : h < 21 ? 'Buenas tardes' : 'Buenas noches';
  const fecha = new Date().toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  const enCurso = PD.entradas.filter(e => e.estado === 'EN_CURSO');
  const pendientes = PD.entradas.filter(e => e.estado === 'PENDIENTE');
  const hecho = PD.entradas.filter(e => e.estado === 'TERMINADO');
  const K = PD.kuiper, Fu = PD.fusion, A = PD.atlas, N = PD.nucleo;
  const hoyGasto = K.dias[K.dias.length - 1];
  const top = [...K.categorias].sort((a, b) => b.gastado / b.limite - a.gastado / a.limite).slice(0, 3);
  const dias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const colorMod = m => `var(--mod-${m})`;
  return (
    <div>
      <PageHeader eyebrow="Inicio" coord={fecha.toUpperCase()} title={<>{saludo}, <span style={{ color: 'var(--accent)' }}>{PD.user.nombre}</span></>}
        actions={<Button variant="secondary" icon="plus" onClick={() => go('odisea')}>Añadir</Button>} />
      <div className="grid">
        <div className="span-12 stats stats--today pl-rise" style={{ animationDelay: '60ms' }}>
          <div data-module="odisea" className="today" onClick={() => go('odisea')}><span className="today__k"><Icon name="clapperboard" size={14} />En curso</span><Stat value={enCurso.length} size={34} caption={pendientes.length + ' pendiente · ' + hecho.length + ' terminado'} /></div>
          <div data-module="kuiper" className="today" onClick={() => go('kuiper')}><span className="today__k"><Icon name="wallet" size={14} />Gastado hoy</span><Stat value={hoyGasto} decimals={2} unit="€" size={34} caption={'media ' + eur(K.gastado / K.dias.length)} /></div>
          <div data-module="fusion" className="today" onClick={() => go('fusion')}><span className="today__k"><Icon name="flame" size={14} />Te quedan</span><Stat value={Fu.objetivo - Fu.kcal} unit="kcal" size={34} caption="falta la cena" /></div>
          <div data-module="atlas" className="today" onClick={() => go('atlas')}><span className="today__k"><Icon name="dumbbell" size={14} />Hoy toca</span><Stat value={A.siguiente.rutina} size={34} caption={'última: ' + A.ultima.rutina + ', ' + A.ultima.dia.toLowerCase()} /></div>
        </div>

        <div data-module="odisea" className="span-5"><Card delay={100} interactive onClick={() => go('odisea')} eyebrow="Odisea · en curso" title="Lo que tienes entre manos" action={<Icon name="arrow-up-right" size={16} color="var(--text-3)" />}>
          <div className="wfill">
            {enCurso.map(e => (
              <div key={e.id} className="wrow">
                <span className="wrow__ic"><TypeTag tipo={e.tipo} showLabel={false} size={16} /></span>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="wrow__t"><b>{e.titulo}</b><span>{e.tipo === 'LIBRO' ? `pág. ${e.progreso} / ${e.duracionMin}` : `episodio ${e.progreso}`}</span></div>
                  {e.tipo === 'LIBRO' ? <ProgressBar value={e.progreso} max={e.duracionMin} valueLabel={false} /> : <ProgressBar value={e.progreso} max={62} valueLabel={false} />}
                </div>
              </div>
            ))}
            <div className="wsub"><span className="pl-eyebrow">Recientes</span></div>
            {[...hecho, ...pendientes].map(e => <div key={e.id} className="wmini"><StatusBadge estado={e.estado} variant="dot" /><b>{e.titulo}</b><span className="pl-row__num">{e.anio}</span>{e.valoracion != null ? <Rating value={e.valoracion} size={11} /> : <span className="pl-row__num">—</span>}</div>)}
          </div>
        </Card></div>

        <div data-module="kuiper" className="span-4"><Card delay={140} interactive onClick={() => go('kuiper')} eyebrow="Kuiper · septiembre" title="Gastado este mes" action={<Maqueta />}>
          <div className="wfill">
            <Stat value={K.gastado} decimals={2} unit="€" size={44} caption={`de ${eur(K.presupuesto, 0)} · quedan ${eur(K.presupuesto - K.gastado)}`} />
            <ProgressBar value={K.gastado} max={K.presupuesto} target={K.presupuesto * 25 / 30} valueLabel={false} size="lg" />
            <div className="stack-8">{top.map(c => <ProgressBar key={c.nombre} label={c.nombre} value={c.gastado} max={c.limite} valueLabel={eur(c.gastado, 0) + ' / ' + eur(c.limite, 0)} />)}</div>
            <div className="wbottom"><BarChart height={64} gap={3} data={K.dias.slice(-14).map((v, i) => ({ label: '', value: v }))} highlight={13} format={v => eur(v)} showAxis={false} gridLines={2} /></div>
          </div>
        </Card></div>

        <div data-module="fusion" className="span-3"><Card delay={180} interactive onClick={() => go('fusion')} eyebrow="Fusión · hoy" title="Macros" action={<Maqueta />}>
          <div className="wfill" style={{ alignItems: 'stretch' }}>
            <div style={{ display: 'flex', justifyContent: 'center', padding: '4px 0' }}><RingChart value={Fu.kcal} max={Fu.objetivo} size={148} thickness={12} label={Fu.kcal.toLocaleString('es-ES')} sublabel={'/ ' + Fu.objetivo.toLocaleString('es-ES') + ' KCAL'} /></div>
            <div className="stack-12 wbottom">{Fu.macros.map(m => <ProgressBar key={m.nombre} label={m.nombre} value={m.g} max={m.obj} valueLabel={m.g + ' / ' + m.obj + ' g'} />)}</div>
          </div>
        </Card></div>

        <div data-module="atlas" className="span-5"><Card delay={220} interactive onClick={() => go('atlas')} eyebrow="Atlas · semana 39" title="Entrenamiento" action={<Maqueta />}>
          <div className="wfill">
            <div className="split">
              <div className="stack-8"><span className="pl-eyebrow">Última · {A.ultima.dia}</span><b className="big">{A.ultima.rutina}</b><span className="muted" style={{ fontSize: 13 }}>{A.ultima.ejercicios} ejercicios · {A.ultima.series} series · {A.ultima.volumen.toLocaleString('es-ES')} kg</span></div>
              <div className="stack-8 next"><span className="pl-eyebrow" style={{ color: 'var(--accent)' }}>Siguiente · {A.siguiente.dia}</span><b className="big">{A.siguiente.rutina}</b><Button size="sm" icon="play" onClick={e => { e.stopPropagation(); go('atlas'); }}>Empezar</Button></div>
            </div>
            <div className="week">{dias.map((d, i) => <div key={d} className={'week__d' + (PD.semanaAtlas[i] ? ' on' : '') + (i === 4 ? ' today' : '')} style={{ animationDelay: 260 + i * 40 + 'ms' }}><span>{d}</span><i /></div>)}</div>
            <div className="wbottom"><BarChart height={72} data={A.volumen.map((v, i) => ({ label: A.semanas[i], value: v }))} highlight={9} gap={4} format={v => v.toLocaleString('es-ES') + ' t'} gridLines={2} /></div>
          </div>
        </Card></div>

        <div data-module="nucleo" className="span-3"><Card delay={260} eyebrow="Núcleo" title="Peso" action={<Maqueta />}>
          <div className="wfill">
            <Stat value={N.peso} decimals={1} unit="kg" size={44} delta={'−' + Math.abs(N.delta).toLocaleString('es-ES') + ' kg'} deltaTone="up" caption="en 10 días" />
            <div className="wbottom"><LineChart height={120} series={[{ name: 'Peso', points: N.serie }]} labels={['15 sep', '20 sep', '25 sep']} format={v => v.toFixed(1).replace('.', ',')} gridLines={2} /></div>
          </div>
        </Card></div>

        <div className="span-4"><Card delay={300} eyebrow="Actividad" title="Lo último" padding="4px 0 6px">
          <div className="feed">
            {PD.actividad.map((a, i) => (
              <div key={i} className="feed__r pl-rise" data-module={a.mod} style={{ animationDelay: 340 + i * 50 + 'ms' }} onClick={() => go(a.mod === 'nucleo' ? 'inicio' : a.mod)}>
                <span className="feed__ic"><Icon name={a.icon} size={14} /></span>
                <span className="feed__t">{a.txt}</span>
                <span className="pl-row__num">{a.det}</span>
                <span className="pl-row__num feed__h">{a.hora}</span>
              </div>
            ))}
          </div>
        </Card></div>
      </div>
    </div>
  );
}
window.DashboardGrid = DashboardGrid;
