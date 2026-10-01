import { useEffect, useState } from 'react';
import { mensajeError, useBuscarAlimentos, useCrearAlimento, type Alimento } from '../../api/fusion';
import { Alert, Button, Input } from '../../design-system';
import { num } from '../../lib/fechas';

interface Props {
  onElegir: (a: Alimento) => void;
}

const MACRO = /^\d{1,3}([.,]\d{1,2})?$/;

/** Busca en el catalogo y, si no esta, permite crearlo ahi mismo (valores por 100 g). */
export function BuscadorAlimento({ onElegir }: Props) {
  const [q, setQ] = useState('');
  const [debounced, setDebounced] = useState('');
  const [creando, setCreando] = useState(false);
  const busqueda = useBuscarAlimentos(debounced);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(q), 300);
    return () => clearTimeout(t);
  }, [q]);

  const elegir = (a: Alimento) => {
    onElegir(a);
    setQ('');
    setDebounced('');
    setCreando(false);
  };

  const hayTexto = q.trim().length >= 2;
  const sinResultados = hayTexto && debounced === q && busqueda.isSuccess && busqueda.data.length === 0;

  return (
    // Intro aqui no debe enviar el formulario de la comida que lo contiene.
    <div onKeyDown={(e) => e.key === 'Enter' && e.preventDefault()}>
      <Input
        label="Añadir alimento"
        icon="search"
        placeholder="Busca por nombre o marca…"
        value={q}
        onChange={(e) => {
          setQ(e.target.value);
          setCreando(false);
        }}
        autoComplete="off"
      />
      {busqueda.isError && <p className="muted" style={{ margin: '6px 0 0', fontSize: 12.5 }}>No se ha podido buscar.</p>}
      {busqueda.isSuccess && busqueda.data.length > 0 && hayTexto && (
        <div className="fus-res" role="listbox" aria-label="Resultados">
          {busqueda.data.slice(0, 20).map((a) => (
            <button key={a.id} type="button" role="option" aria-selected={false} onClick={() => elegir(a)}>
              <span>{a.nombre}</span>
              {a.marca && <i>{a.marca}</i>}
              <em>{num(a.kcal100g)} kcal / 100 g</em>
            </button>
          ))}
        </div>
      )}
      {sinResultados && !creando && (
        <div style={{ marginTop: 8, display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="muted" style={{ fontSize: 13 }}>Sin resultados.</span>
          <Button size="sm" variant="secondary" type="button" icon="plus" onClick={() => setCreando(true)}>
            Crear «{q.trim()}»
          </Button>
        </div>
      )}
      {creando && <NuevoAlimento nombreInicial={q.trim()} onCreado={elegir} onCancelar={() => setCreando(false)} />}
    </div>
  );
}

function NuevoAlimento({ nombreInicial, onCreado, onCancelar }: { nombreInicial: string; onCreado: (a: Alimento) => void; onCancelar: () => void }) {
  const crear = useCrearAlimento();
  const [nombre, setNombre] = useState(nombreInicial);
  const [marca, setMarca] = useState('');
  const [kcal, setKcal] = useState('');
  const [prot, setProt] = useState('');
  const [carb, setCarb] = useState('');
  const [gras, setGras] = useState('');
  const [intentado, setIntentado] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const val = (s: string, max: number) => (MACRO.test(s.trim()) && Number(s.trim().replace(',', '.')) <= max ? Number(s.trim().replace(',', '.')) : NaN);
  const k = val(kcal, 900);
  const p = val(prot, 100);
  const c = val(carb, 100);
  const g = val(gras, 100);
  const err = (v: number, max: number) => (Number.isNaN(v) ? `0 a ${max}` : null);
  const ver = (e: string | null) => (intentado ? e : null);

  const guardar = () => {
    setIntentado(true);
    setError(null);
    if (!nombre.trim() || [k, p, c, g].some(Number.isNaN)) return;
    crear.mutate(
      { nombre: nombre.trim(), marca: marca.trim() || null, kcal100g: k, proteinas100g: p, carbohidratos100g: c, grasas100g: g },
      { onSuccess: onCreado, onError: (e) => setError(mensajeError(e)) },
    );
  };

  return (
    <div className="fus-form" style={{ marginTop: 10, padding: 12, border: '1px dashed var(--border-2)', borderRadius: 4 }}>
      <span className="pl-eyebrow">Alimento nuevo · por 100 g</span>
      {error && <Alert tone="danger">{error}</Alert>}
      <div className="fus-form__row">
        <Input label="Nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} error={ver(nombre.trim() ? null : 'Escribe un nombre.')} />
        <Input label="Marca" placeholder="Opcional" value={marca} onChange={(e) => setMarca(e.target.value)} />
      </div>
      <div className="fus-form__row4">
        <Input label="Kcal" inputMode="decimal" value={kcal} onChange={(e) => setKcal(e.target.value)} error={ver(err(k, 900))} />
        <Input label="Prot. (g)" inputMode="decimal" value={prot} onChange={(e) => setProt(e.target.value)} error={ver(err(p, 100))} />
        <Input label="Carb. (g)" inputMode="decimal" value={carb} onChange={(e) => setCarb(e.target.value)} error={ver(err(c, 100))} />
        <Input label="Grasas (g)" inputMode="decimal" value={gras} onChange={(e) => setGras(e.target.value)} error={ver(err(g, 100))} />
      </div>
      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
        <Button size="sm" variant="ghost" type="button" onClick={onCancelar}>
          Cancelar
        </Button>
        <Button size="sm" type="button" loading={crear.isPending} onClick={guardar}>
          Crear y añadir
        </Button>
      </div>
    </div>
  );
}
