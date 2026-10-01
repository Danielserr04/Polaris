import { useEffect, useState } from 'react';
import { mensajeError, useAlimentos, type Alimento } from '../../api/fusion';
import { Alert, Button, Card, Input } from '../../design-system';
import { num } from '../../lib/fechas';

interface Props {
  onEditar: (a: Alimento) => void;
  /** El padre muestra el recuento en la cabecera. */
  onRecuento: (n: number | null) => void;
}

export function AlimentosTab({ onEditar, onRecuento }: Props) {
  const [q, setQ] = useState('');
  const [debounced, setDebounced] = useState('');
  const alimentos = useAlimentos(debounced);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(q), 250);
    return () => clearTimeout(t);
  }, [q]);

  const filas = [...(alimentos.data ?? [])].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  useEffect(() => {
    onRecuento(alimentos.isSuccess && debounced === '' ? alimentos.data.length : null);
    return () => onRecuento(null);
  }, [alimentos.isSuccess, alimentos.data, debounced, onRecuento]);

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
        <div style={{ width: 320 }}>
          <Input size="sm" icon="search" aria-label="Buscar alimento" placeholder="Buscar por nombre o marca…" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
      </div>

      {alimentos.isError ? (
        <Alert
          tone="danger"
          title="No se ha podido cargar el catálogo"
          action={
            <Button size="sm" variant="secondary" onClick={() => void alimentos.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(alimentos.error)}
        </Alert>
      ) : alimentos.isPending ? (
        <p className="muted">Cargando…</p>
      ) : (
        <Card padding="0">
          {filas.length === 0 ? (
            <div className="fus-vacio" style={{ padding: '22px 18px' }}>
              {debounced ? 'Nada con esa búsqueda.' : 'Aún no tienes alimentos. Crea uno o impórtalo de Open Food Facts.'}
            </div>
          ) : (
            <div className="table">
              <div className="table__r table__r--ali fus-ali-cab" aria-hidden="true">
                <span className="pl-eyebrow">Alimento · por 100 g</span>
                <span className="pl-eyebrow">Kcal</span>
                <span className="pl-eyebrow">Prot.</span>
                <span className="pl-eyebrow">Carb.</span>
                <span className="pl-eyebrow">Grasas</span>
              </div>
              {filas.map((a, i) => (
                <button
                  key={a.id}
                  type="button"
                  className="table__r table__r--ali fus-ali pl-rise"
                  style={{ animationDelay: Math.min(i, 12) * 25 + 'ms' }}
                  onClick={() => onEditar(a)}
                  aria-label={`Editar ${a.nombre}`}
                >
                  <b>
                    {a.nombre}
                    {a.marca && <span className="muted"> · {a.marca}</span>}
                  </b>
                  <span className="money">{num(a.kcal100g)}</span>
                  <span className="money">{num(a.proteinas100g, 1)}</span>
                  <span className="money">{num(a.carbohidratos100g, 1)}</span>
                  <span className="money">{num(a.grasas100g, 1)}</span>
                </button>
              ))}
            </div>
          )}
        </Card>
      )}
    </>
  );
}
