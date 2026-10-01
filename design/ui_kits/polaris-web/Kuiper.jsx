function Kuiper() {
  const { Stat, Card, BarChart, ProgressBar, Tabs, Select, Button, Icon, Badge } = window.PolarisDesignSystem_b0ab94;
  const K = PD.kuiper;
  const [tab, setTab] = React.useState('resumen');
  const media = K.gastado / K.dias.length;
  const Movs = ({ rows }) => (
    <div className="table">
      {rows.map((m, i) => (
        <div key={i} className="table__r pl-rise" style={{ animationDelay: i * 40 + 'ms' }}>
          <span className="pl-row__num">{m.fecha}</span><b>{m.concepto}</b><span className="muted">{m.categoria}</span>
          <span className="money" style={{ color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)' }}>{m.tipo === 'INGRESO' ? '+' : '−'}{eur(m.importe)}</span>
        </div>
      ))}
    </div>
  );
  return (
    <div>
      <PageHeader eyebrow="Kuiper" coord="SEPTIEMBRE 2026" title="Gastos" badge={<Maqueta />} actions={<><div style={{ width: 190 }}><Select size="sm" icon="calendar" options={['Septiembre 2026', 'Agosto 2026', 'Julio 2026']} /></div><Button icon="plus">Movimiento</Button></>} />
      <Tabs value={tab} onChange={setTab} items={[{ value: 'resumen', label: 'Resumen' }, { value: 'mov', label: 'Movimientos' }]} style={{ marginBottom: 24 }} />
      {tab === 'resumen' ? (
        <div className="grid">
          <div className="span-12 stats pl-rise">
            <Stat label="Gastado" value={K.gastado} decimals={2} unit="€" delta="−8 %" deltaTone="up" caption="vs agosto" />
            <Stat label="Ingresos" value={K.ingresos} decimals={2} unit="€" />
            <Stat label="Balance" value={K.ingresos - K.gastado} decimals={2} unit="€" delta="+12 %" caption="vs agosto" />
            <Stat label="Queda de presupuesto" value={K.presupuesto - K.gastado} decimals={2} unit="€" caption={'para 5 días'} />
          </div>
          <div className="span-8"><Card delay={100} eyebrow="Día a día" title="Gasto diario" action={<span className="pl-row__num">media {eur(media)}</span>}>
            <BarChart height={200} gap={5} highlight={24} target={media} targetLabel="Media" data={K.dias.map((v, i) => ({ label: (i + 1) % 5 === 0 || i === 0 ? String(i + 1) : '', value: v }))} format={v => eur(v)} />
          </Card></div>
          <div className="span-4"><Card delay={160} eyebrow="Presupuestos" title="Por categoría">
            <div className="stack-16">
              {K.categorias.map(c => (
                <div key={c.nombre} style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                  <span className="catic"><Icon name={c.icon} size={15} /></span>
                  <ProgressBar style={{ flex: 1 }} label={<>{c.nombre}{c.gastado > c.limite && <Badge tone="danger" style={{ marginLeft: 8, height: 18 }}>+{eur(c.gastado - c.limite, 0)}</Badge>}</>} value={c.gastado} max={c.limite} valueLabel={eur(c.gastado, 0) + ' / ' + eur(c.limite, 0)} />
                </div>
              ))}
            </div>
          </Card></div>
          <div className="span-12"><Card delay={220} eyebrow="Últimos" title="Movimientos" action={<Button variant="ghost" size="sm" iconRight="arrow-right" onClick={() => setTab('mov')}>Ver todos</Button>} padding="8px 0 0"><Movs rows={K.movimientos.slice(0, 4)} /></Card></div>
        </div>
      ) : <Card padding="8px 0 0"><Movs rows={[...K.movimientos, ...K.movimientos.map(m => ({ ...m, fecha: m.fecha.replace('2', '1') }))]} /></Card>}
    </div>
  );
}
window.Kuiper = Kuiper;
