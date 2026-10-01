function Atlas() {
  const { Card, Stat, LineChart, BarChart, Select, Button, Badge, Icon } = window.PolarisDesignSystem_b0ab94;
  const A = PD.atlas;
  return (
    <div>
      <PageHeader eyebrow="Atlas" coord="SEMANA 39" title="Progresión" badge={<Maqueta />} actions={<Button icon="play">Empezar {A.siguiente.rutina}</Button>} />
      <div className="grid">
        <div className="span-12 stats pl-rise">
          <Stat label="Sesiones · sep" value={11} delta="+2" caption="vs agosto" />
          <Stat label="Volumen semana" value={25.3} decimals={1} unit="t" delta="+15 %" />
          <Stat label="1RM est. press banca" value={80} unit="kg" delta="+2,5 kg" caption="Epley" />
          <Stat label="Peso corporal" value={PD.nucleo.peso} decimals={1} unit="kg" caption="desde Núcleo" />
        </div>
        <div className="span-8"><Card delay={100} eyebrow="1RM estimado" title="Press banca" action={<div style={{ width: 170 }}><Select size="sm" options={['Press banca', 'Sentadilla', 'Peso muerto', 'Dominadas']} /></div>}>
          <LineChart height={220} showLegend labels={A.semanas.filter((_, i) => i % 3 === 0)} format={v => Math.round(v) + ''} series={[{ name: '1RM estimado (kg)', points: A.progresion }, { name: 'Objetivo 85 kg', points: A.progresion.map(() => 85), dashed: true, color: 'var(--text-3)' }]} />
        </Card></div>
        <div className="span-4"><Card delay={160} eyebrow="Mejores marcas" title="Récords" padding="4px 0 8px">
          {A.records.map((r, i) => (
            <div key={r.ej} className="rec pl-rise" style={{ animationDelay: 200 + i * 60 + 'ms' }}>
              <div className="stack-4"><b>{r.ej}</b><span className="pl-row__num">{r.fecha}</span></div>
              <span className="money">{r.v}</span>{r.nuevo && <Badge tone="accent" variant="solid">Nuevo</Badge>}
            </div>
          ))}
        </Card></div>
        <div className="span-7"><Card delay={220} eyebrow="Historial" title="Últimas sesiones" padding="8px 0 0">
          <div className="table">
            {A.sesiones.map((s, i) => (
              <div key={i} className="table__r table__r--5 pl-rise" style={{ animationDelay: 240 + i * 40 + 'ms' }}>
                <span className="pl-row__num">{s.fecha}</span><b>{s.rutina}{s.rutina === 'Improvisado' && <Badge variant="outline" style={{ marginLeft: 8 }}>Sin rutina</Badge>}</b><span className="muted">{s.ej} ejercicios</span><span className="muted">{s.series} series</span><span className="money">{s.vol.toLocaleString('es-ES')} kg</span>
              </div>
            ))}
          </div>
        </Card></div>
        <div className="span-5"><Card delay={280} eyebrow="Volumen" title="Toneladas por semana">
          <BarChart height={170} data={A.volumen.map((v, i) => ({ label: A.semanas[i], value: v }))} highlight={9} format={v => v.toLocaleString('es-ES') + ' t'} />
        </Card></div>
      </div>
    </div>
  );
}
window.Atlas = Atlas;
