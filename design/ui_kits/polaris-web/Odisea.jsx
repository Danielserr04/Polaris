function Cover({ e, big }) {
  const { TypeTag } = window.PolarisDesignSystem_b0ab94;
  return (
    <div className={'cover' + (big ? ' cover--big' : '')}>
      <span className="cover__star">✦</span>
      <TypeTag tipo={e.tipo} showLabel={false} size={big ? 18 : 12} />
      <b>{e.titulo}</b>
      <span>{e.anio || '—'}</span>
    </div>
  );
}
function Detail({ e, update }) {
  const { StatusBadge, TypeTag, Rating, ProgressBar, Switch, Badge, Button, Eyebrow, SegmentedControl } = window.PolarisDesignSystem_b0ab94;
  const dur = e.duracionMin == null ? null : e.tipo === 'LIBRO' ? e.duracionMin + ' páginas' : e.duracionMin + ' min';
  const fmtD = d => d ? new Date(d).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' }) : '—';
  return (
    <aside className="detail" key={e.id}>
      <div className="detail__top">
        <Cover e={e} big />
        <div className="stack-8" style={{ minWidth: 0 }}>
          <Eyebrow star coord={e.fuenteExterna.replace('_', ' ')}><TypeTag tipo={e.tipo} size={12} /></Eyebrow>
          <h2 className="detail__h">{e.titulo}</h2>
          {e.tituloOriginal && e.tituloOriginal !== e.titulo && <span className="muted">{e.tituloOriginal}</span>}
          <span className="detail__meta">{[e.anio, dur].filter(Boolean).join(' · ') || 'Sin año ni duración'}</span>
          <span className="muted" style={{ fontSize: 13 }}>{e.generos}</span>
        </div>
      </div>
      {e.sinopsis ? <p className="detail__syn">{e.sinopsis}</p> : <p className="detail__syn detail__syn--empty">Sin sinopsis.</p>}
      <div className="detail__block">
        <span className="pl-eyebrow">Estado</span>
        <SegmentedControl value={e.estado} onChange={v => update({ estado: v })} options={[{ value: 'PENDIENTE', label: 'Pendiente' }, { value: 'EN_CURSO', label: 'En curso' }, { value: 'TERMINADO', label: 'Terminado' }, { value: 'ABANDONADO', label: 'Abandonado' }]} />
      </div>
      {e.estado === 'EN_CURSO' && e.progreso != null && (
        <div className="detail__block">
          {e.tipo === 'LIBRO' ? <ProgressBar label="Progreso" value={e.progreso} max={e.duracionMin} valueLabel={`pág. ${e.progreso} / ${e.duracionMin}`} size="lg" />
            : <div className="stack-8"><span className="pl-eyebrow">Progreso</span><span style={{ display: 'flex', alignItems: 'center', gap: 12 }}><b className="big">Episodio {e.progreso}</b><Button size="sm" variant="secondary" icon="plus" onClick={() => update({ progreso: e.progreso + 1 })}>1</Button></span></div>}
        </div>
      )}
      <div className="detail__grid">
        <div className="stack-8"><span className="pl-eyebrow">Valoración</span><Rating value={e.valoracion} size={20} onChange={v => update({ valoracion: v })} /></div>
        <div className="stack-8"><span className="pl-eyebrow">Favorito</span><Switch checked={e.favorito} onChange={v => update({ favorito: v })} label={e.favorito ? 'Sí' : 'No'} /></div>
        <div className="stack-8"><span className="pl-eyebrow">Inicio</span><span className="detail__meta">{fmtD(e.fechaInicio)}</span></div>
        <div className="stack-8"><span className="pl-eyebrow">Fin</span><span className="detail__meta">{fmtD(e.fechaFin)}</span></div>
      </div>
      <div className="detail__block"><span className="pl-eyebrow">Notas</span>{e.notas ? <p className="detail__notes">{e.notas}</p> : <p className="detail__syn--empty" style={{ margin: 0, fontSize: 13 }}>Sin notas todavía.</p>}</div>
    </aside>
  );
}
function AddDialog({ open, onClose, onAdd }) {
  const { Dialog, Input, SegmentedControl, TypeTag, Badge, Button } = window.PolarisDesignSystem_b0ab94;
  const [tipo, setTipo] = React.useState('ALL');
  const [q, setQ] = React.useState('');
  const res = PD.catalogo.filter(c => (tipo === 'ALL' || c.tipo === tipo) && c.titulo.toLowerCase().includes(q.toLowerCase()));
  return (
    <Dialog open={open} onClose={onClose} title="Añadir a Odisea" width={560}>
      <div className="stack-12">
        <Input icon="search" autoFocus placeholder="Busca en TMDB, IGDB y OpenLibrary" value={q} onChange={e => setQ(e.target.value)} />
        <SegmentedControl value={tipo} onChange={setTipo} options={[{ value: 'ALL', label: 'Todo' }, { value: 'PELICULA', label: 'Pelis', icon: 'clapperboard' }, { value: 'SERIE', label: 'Series', icon: 'tv' }, { value: 'JUEGO', label: 'Juegos', icon: 'gamepad-2' }, { value: 'LIBRO', label: 'Libros', icon: 'book-open' }]} />
        <div className="results">
          {res.map(c => (
            <div key={c.titulo} className="result">
              <TypeTag tipo={c.tipo} showLabel={false} size={15} /><b>{c.titulo}</b><span className="pl-row__num">{c.anio}</span><Badge variant="outline">{c.fuenteExterna.replace('_', ' ')}</Badge>
              <Button size="sm" variant="secondary" icon="plus" onClick={() => onAdd(c)}>Añadir</Button>
            </div>
          ))}
          {!res.length && <div className="cmd__empty">Sin resultados para «{q}».</div>}
        </div>
      </div>
    </Dialog>
  );
}
function Odisea() {
  const { ListRow, ListHeader, SegmentedControl, Input, Button, IconButton, Toast } = window.PolarisDesignSystem_b0ab94;
  const [items, setItems] = React.useState(PD.entradas);
  const [estado, setEstado] = React.useState('ALL');
  const [tipo, setTipo] = React.useState(null);
  const [q, setQ] = React.useState('');
  const [sel, setSel] = React.useState(2);
  const [add, setAdd] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const count = s => items.filter(e => e.estado === s).length;
  const list = items.filter(e => (estado === 'ALL' || e.estado === estado) && (!tipo || e.tipo === tipo) && e.titulo.toLowerCase().includes(q.toLowerCase()));
  const cur = items.find(e => e.id === sel);
  const update = patch => setItems(xs => xs.map(x => x.id === sel ? { ...x, ...patch } : x));
  const onAdd = c => {
    const id = Date.now();
    setItems(xs => [{ id, ...c, tituloOriginal: null, duracionMin: null, generos: '', estado: 'PENDIENTE', valoracion: null, favorito: false, progreso: null, notas: null, sinopsis: null, fechaInicio: null, fechaFin: null }, ...xs]);
    setAdd(false); setSel(id); setToast(c.titulo); setTimeout(() => setToast(null), 3200);
  };
  return (
    <div>
      <PageHeader eyebrow="Odisea" coord={items.length + ' TÍTULOS'} title="Tu lista" actions={<Button icon="plus" onClick={() => setAdd(true)}>Añadir título</Button>} />
      <div className="toolbar pl-rise" style={{ animationDelay: '140ms' }}>
        <SegmentedControl value={estado} onChange={setEstado} options={[{ value: 'ALL', label: 'Todo', count: items.length }, { value: 'EN_CURSO', label: 'En curso', count: count('EN_CURSO') }, { value: 'PENDIENTE', label: 'Pendiente', count: count('PENDIENTE') }, { value: 'TERMINADO', label: 'Terminado', count: count('TERMINADO') }, { value: 'ABANDONADO', label: 'Abandonado', count: count('ABANDONADO') }]} />
        <div className="toolbar__types">
          {[['PELICULA', 'clapperboard', 'Películas'], ['SERIE', 'tv', 'Series'], ['JUEGO', 'gamepad-2', 'Juegos'], ['LIBRO', 'book-open', 'Libros']].map(([t, i, l]) => <IconButton key={t} icon={i} label={l} pressed={tipo === t} variant={tipo === t ? 'outline' : 'ghost'} onClick={() => setTipo(tipo === t ? null : t)} />)}
        </div>
        <Input size="sm" icon="search" placeholder="Filtrar por título" value={q} onChange={e => setQ(e.target.value)} style={{ width: 240 }} />
      </div>
      <div className="odisea">
        <div className="listbox pl-rise" style={{ animationDelay: '180ms' }}>
          <ListHeader />
          {list.map((e, i) => <ListRow key={e.id} index={i} {...e} selected={e.id === sel} onClick={() => setSel(e.id)} />)}
          {!list.length && <div className="cmd__empty" style={{ padding: 32 }}>Nada con estos filtros.</div>}
        </div>
        {cur && <Detail e={cur} update={update} />}
      </div>
      <AddDialog open={add} onClose={() => setAdd(false)} onAdd={onAdd} />
      {toast && <Toast fixed tone="success" onClose={() => setToast(null)}>{toast} añadido a Pendientes</Toast>}
    </div>
  );
}
window.Odisea = Odisea;
