function Fusion() {
  const { Card, RingChart, ProgressBar, LineChart, Stat, Button, IconButton, Icon } = window.PolarisDesignSystem_b0ab94;
  const Fu = PD.fusion;
  const colors = ['var(--accent)', 'var(--mod-kuiper)', 'var(--mod-nucleo)'];
  return (
    <div>
      <PageHeader eyebrow="Fusión" coord="JUEVES 25 SEP" title="Hoy" badge={<Maqueta />} actions={<Button icon="plus">Registrar comida</Button>} />
      <div className="grid">
        <div className="span-5"><Card delay={60} eyebrow="Energía" title="Calorías del día">
          <div style={{ display: 'flex', gap: 28, alignItems: 'center' }}>
            <RingChart value={Fu.kcal} max={Fu.objetivo} size={168} thickness={14} label={Fu.kcal.toLocaleString('es-ES')} sublabel={'DE ' + Fu.objetivo.toLocaleString('es-ES') + ' KCAL'} />
            <div className="stack-16" style={{ flex: 1 }}>
              <Stat label="Te quedan" value={Fu.objetivo - Fu.kcal} unit="kcal" size={32} />
              <span className="muted" style={{ fontSize: 13 }}>Objetivo vigente desde el 1 de septiembre.</span>
            </div>
          </div>
        </Card></div>
        <div className="span-7"><Card delay={120} eyebrow="Macros" title="Contra objetivo">
          <div className="macros">
            {Fu.macros.map((m, i) => (
              <div key={m.nombre} className="macro">
                <RingChart value={m.g} max={m.obj} size={104} thickness={8} color={colors[i]} label={m.g} sublabel={'/ ' + m.obj + ' G'} />
                <b>{m.nombre}</b><span className="pl-row__num">{Math.round((m.g / m.obj) * 100)} %</span>
              </div>
            ))}
          </div>
        </Card></div>
        <div className="span-6"><Card delay={180} eyebrow="Registro" title="Comidas" padding="4px 0 8px">
          {Fu.comidas.map((c, i) => {
            const k = c.lineas.reduce((a, l) => a + l[2], 0);
            return (
              <div key={c.momento} className="meal pl-rise" style={{ animationDelay: 200 + i * 60 + 'ms' }}>
                <div className="meal__h"><b>{c.momento}</b><span className="pl-row__num">{c.hora}</span><span className="money">{k ? k + ' kcal' : ''}</span>{!c.lineas.length && <Button size="sm" variant="ghost" icon="plus">Añadir</Button>}</div>
                {c.lineas.map(l => <div key={l[0]} className="meal__l"><span>{l[0]}</span><span className="pl-row__num">{l[1]} g</span><span className="pl-row__num">{l[2]} kcal</span></div>)}
              </div>
            );
          })}
        </Card></div>
        <div className="span-6"><Card delay={240} eyebrow="Tendencia" title="Últimos 14 días">
          <LineChart height={220} showLegend labels={['12 sep', '18 sep', '25 sep']} format={v => Math.round(v / 100) / 10 + 'k'} series={[{ name: 'Kcal', points: Fu.semana }, { name: 'Objetivo', points: Fu.semana.map(() => Fu.objetivo), dashed: true, color: 'var(--text-3)' }]} />
        </Card></div>
      </div>
    </div>
  );
}
window.Fusion = Fusion;
