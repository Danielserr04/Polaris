import { useEffect, useState } from 'react';
import { mensajeErrorReceta, useRecetas, type Receta } from '../../api/fusionRecetas';
import { Alert, Button, Card, Input } from '../../design-system';
import { num } from '../../lib/fechas';

interface Props {
  onAbrir: (r: Receta) => void;
  onNueva: () => void;
}

export function RecetasTab({ onAbrir, onNueva }: Props) {
  const [q, setQ] = useState('');
  const [debounced, setDebounced] = useState('');
  const recetas = useRecetas(debounced);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(q), 250);
    return () => clearTimeout(t);
  }, [q]);

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
        <div style={{ width: 320, maxWidth: '100%' }}>
          <Input size="sm" icon="search" aria-label="Buscar receta" placeholder="Buscar por nombre…" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
      </div>

      {recetas.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar las recetas"
          action={
            <Button size="sm" variant="secondary" onClick={() => void recetas.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeErrorReceta(recetas.error)}
        </Alert>
      ) : recetas.isPending ? (
        <p className="muted">Cargando…</p>
      ) : recetas.data.length === 0 ? (
        <Card>
          <div className="fus-vacio">
            {debounced ? (
              'Ninguna receta con esa búsqueda.'
            ) : (
              <>
                Aún no tienes recetas. Junta varios alimentos en un plato y verás sus calorías por ración.{' '}
                <Button size="sm" variant="ghost" icon="plus" onClick={onNueva}>
                  Crear la primera
                </Button>
              </>
            )}
          </div>
        </Card>
      ) : (
        <Card padding="0">
          <div className="table">
            <div className="table__r table__r--rec fus-ali-cab" aria-hidden="true">
              <span className="pl-eyebrow">Receta · por ración</span>
              <span className="pl-eyebrow">Rac.</span>
              <span className="pl-eyebrow">Kcal</span>
              <span className="pl-eyebrow">Prot.</span>
              <span className="pl-eyebrow">Carb.</span>
              <span className="pl-eyebrow">Grasas</span>
            </div>
            {recetas.data.map((r, i) => (
              <button
                key={r.id}
                type="button"
                className="table__r table__r--rec fus-ali pl-rise"
                style={{ animationDelay: Math.min(i, 12) * 25 + 'ms' }}
                onClick={() => onAbrir(r)}
                aria-label={`Abrir ${r.nombre}`}
              >
                <b>
                  {r.nombre}
                  <span className="muted">{"\u00a0·\u00a0"}{r.numIngredientes} {r.numIngredientes === 1 ? 'ingrediente' : 'ingredientes'}</span>
                </b>
                <span className="money">{r.raciones}</span>
                <span className="money">{num(r.kcalRacion)}</span>
                <span className="money">{num(r.proteinasRacion, 1)}</span>
                <span className="money">{num(r.carbohidratosRacion, 1)}</span>
                <span className="money">{num(r.grasasRacion, 1)}</span>
              </button>
            ))}
          </div>
        </Card>
      )}
    </>
  );
}
