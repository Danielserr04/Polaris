function MCover({ e, big }) {
  const { TypeTag } = DS();
  return (
    <div className="cover" data-module="odisea">
      {big && <span className="cover__star">✦</span>}
      <TypeTag tipo={e.tipo} showLabel={false} size={big ? 18 : 16} />
      {big && <span>{e.anio || '—'}</span>}
    </div>
  );
}
function MDetail({ e, update }) {
  const { TypeTag, Rating, ProgressBar, Switch, Button, Eyebrow, SegmentedControl } = DS();
  const dur = e.duracionMin == null ? null : e.tipo === 'LIBRO' ? e.duracionMin + ' pág.' : e.duracionMin + ' min';
  return (
    <div className="m-det">
      <div className="m-det__top">
        <MCover e={e} big />
        <div className="stack-8" style={{ minWidth: 0 }}>
          <Eyebrow coord={e.fuenteExterna.replace('_', ' ')}><TypeTag tipo={e.tipo} size={12} /></Eyebrow>
          <h2>{e.titulo}</h2>
          <span className="detail__meta">{[e.anio, dur].filter(Boolean).join(' · ') || 'Sin año ni duración'}</span>
        </div>
      </div>
      {e.sinopsis ? <p className="detail__syn">{e.sinopsis}</p> : <p className="detail__syn detail__syn--empty">Sin sinopsis.</p>}
      <div className="detail__block">
        <span className="pl-eyebrow">Estado</span>
        <SegmentedControl value={e.estado} onChange={v => update({ estado: v })} options={[{ value: 'PENDIENTE', label: 'Pendiente' }, { value: 'EN_CURSO', label: 'En curso' }, { value: 'TERMINADO', label: 'Terminado' }, { value: 'ABANDONADO', label: 'Abandon.' }]} />
      </div>
      {e.estado === 'EN_CURSO' && e.progreso != null && (
        <div className="detail__block">
          {e.tipo === 'LIBRO' ? <ProgressBar label="Progreso" value={e.progreso} max={e.duracionMin} valueLabel={`pág. ${e.progreso} / ${e.duracionMin}`} size="lg" />
            : <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}><div className="stack-4"><span className="pl-eyebrow">Progreso</span><b className="big">Episodio {e.progreso}</b></div><Button variant="secondary" icon="plus" onClick={() => update({ progreso: e.progreso + 1 })}>Episodio</Button></div>}
        </div>
      )}
      <div className="detail__grid">
        <div className="stack-8"><span className="pl-eyebrow">Valoración</span><Rating value={e.valoracion} size={18} onChange={v => update({ valoracion: v })} /></div>
        <div className="stack-8"><span className="pl-eyebrow">Favorito</span><Switch checked={e.favorito} onChange={v => update({ favorito: v })} label={e.favorito ? 'Sí' : 'No'} /></div>
      </div>
      <div className="detail__block"><span className="pl-eyebrow">Notas</span>{e.notas ? <p className="detail__notes">{e.notas}</p> : <p className="detail__syn--empty" style={{ margin: 0, fontSize: 13 }}>Sin notas todavía.</p>}</div>
    </div>
  );
}
function MOdisea({ go, toast }) {
  const { StatusBadge, IconButton, Input, TypeTag, Badge, Button } = DS();
  const [items, setItems] = React.useState(PD.entradas);
  const [estado, setEstado] = React.useState('ALL');
  const [q, setQ] = React.useState('');
  const [sel, setSel] = React.useState(null);
  const [add, setAdd] = React.useState(false);
  const [aq, setAq] = React.useState('');
  const count = s => items.filter(e => e.estado === s).length;
  const list = items.filter(e => (estado === 'ALL' || e.estado === estado) && e.titulo.toLowerCase().includes(q.toLowerCase()));
  const cur = items.find(e => e.id === sel);
  const update = patch => setItems(xs => xs.map(x => x.id === sel ? { ...x, ...patch } : x));
  const res = PD.catalogo.filter(c => c.titulo.toLowerCase().includes(aq.toLowerCase()));
  const onAdd = c => {
    setItems(xs => [{ id: Date.now(), ...c, tituloOriginal: null, duracionMin: null, generos: '', estado: 'PENDIENTE', valoracion: null, favorito: false, progreso: null, notas: null, sinopsis: null }, ...xs]);
    setAdd(false); toast(c.titulo + ' añadido a Pendientes');
  };
  const chips = [['ALL', 'Todo', items.length], ['EN_CURSO', 'En curso', count('EN_CURSO')], ['PENDIENTE', 'Pendiente', count('PENDIENTE')], ['TERMINADO', 'Terminado', count('TERMINADO')], ['ABANDONADO', 'Abandonado', count('ABANDONADO')]];
  return (
    <>
      <MScreen go={go} eyebrow="Odisea" coord={items.length + ' títulos'} short="Tu lista" title="Tu lista" action={<IconButton icon="plus" label="Añadir título" variant="solid" onClick={() => setAdd(true)} />}>
        <div className="stack-12 pl-rise" style={{ animationDelay: '120ms' }}>
          <Input icon="search" placeholder="Filtrar por título" value={q} onChange={e => setQ(e.target.value)} />
          <div className="m-chips">{chips.map(([v, l, n]) => <button key={v} className={'m-chip' + (estado === v ? ' on' : '')} onClick={() => setEstado(v)}>{l}<em>{n}</em></button>)}</div>
        </div>
        <MSec delay={180}>
          <div className="m-box">
            {list.map(e => (
              <div key={e.id} className="m-entry" onClick={() => setSel(e.id)}>
                <MCover e={e} />
                <div style={{ minWidth: 0 }}><b>{e.titulo}</b><div className="m-entry__meta"><TypeTag tipo={e.tipo} size={11} /><span>{e.anio}</span></div></div>
                <div className="m-entry__r"><StatusBadge estado={e.estado} variant="dot" />{e.valoracion != null && <span className="pl-row__num">★ {e.valoracion}</span>}</div>
              </div>
            ))}
            {!list.length && <div className="cmd__empty" style={{ padding: 32 }}>Nada con estos filtros.</div>}
          </div>
        </MSec>
      </MScreen>
      <MSheet open={!!cur} onClose={() => setSel(null)} title="Entrada">{cur && <MDetail e={cur} update={update} />}</MSheet>
      <MSheet open={add} onClose={() => setAdd(false)} title="Añadir a Odisea">
        <div className="stack-12">
          <Input icon="search" placeholder="TMDB, IGDB, OpenLibrary" value={aq} onChange={e => setAq(e.target.value)} />
          <div className="m-box">
            {res.map(c => (
              <div key={c.titulo} className="m-mov">
                <span className="catic" data-module="odisea"><TypeTag tipo={c.tipo} showLabel={false} size={15} /></span>
                <span style={{ minWidth: 0 }}><b>{c.titulo}</b><small>{c.anio} · {c.fuenteExterna.replace('_', ' ')}</small></span>
                <IconButton icon="plus" label="Añadir" variant="outline" onClick={() => onAdd(c)} />
              </div>
            ))}
            {!res.length && <div className="cmd__empty">Sin resultados para «{aq}».</div>}
          </div>
        </div>
      </MSheet>
    </>
  );
}
window.MOdisea = MOdisea;
