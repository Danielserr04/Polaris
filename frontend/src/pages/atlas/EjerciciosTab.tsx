import { useEffect, useMemo, useState } from 'react';
import { mensajeError, useEjercicios, type Ejercicio } from '../../api/atlas';
import { Alert, Badge, Button, Card, Input } from '../../design-system';

interface Props {
  onEditar: (e: Ejercicio) => void;
  /** El padre muestra el recuento en la cabecera. */
  onRecuento: (n: number | null) => void;
}

const normalizar = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

export function EjerciciosTab({ onEditar, onRecuento }: Props) {
  const [q, setQ] = useState('');
  const ejercicios = useEjercicios();

  const filas = useMemo(() => {
    const t = normalizar(q.trim());
    return [...(ejercicios.data ?? [])]
      .filter((e) => !t || normalizar(e.nombre).includes(t) || normalizar(e.grupoMuscular).includes(t))
      .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  }, [ejercicios.data, q]);

  useEffect(() => {
    onRecuento(ejercicios.isSuccess ? ejercicios.data.length : null);
    return () => onRecuento(null);
  }, [ejercicios.isSuccess, ejercicios.data, onRecuento]);

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
        <div style={{ width: 320 }}>
          <Input size="sm" icon="search" aria-label="Buscar ejercicio" placeholder="Buscar por nombre o grupo…" value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
      </div>

      {ejercicios.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar los ejercicios"
          action={
            <Button size="sm" variant="secondary" onClick={() => void ejercicios.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(ejercicios.error)}
        </Alert>
      ) : ejercicios.isPending ? (
        <p className="muted">Cargando…</p>
      ) : (
        <Card padding="0">
          {filas.length === 0 ? (
            <div className="atl-vacio">{q ? 'Nada con esa búsqueda.' : 'Aún no tienes ejercicios. Crea el primero.'}</div>
          ) : (
            <div className="table">
              {filas.map((e) =>
                e.esPropio ? (
                  <button key={e.id} type="button" className="table__r table__r--ej atl-ses" onClick={() => onEditar(e)} aria-label={`Editar ${e.nombre}`}>
                    <b>{e.nombre}</b>
                    <span className="muted">{e.grupoMuscular}</span>
                    <span className="muted">{e.equipamiento ?? ''}</span>
                  </button>
                ) : (
                  <div key={e.id} className="table__r table__r--ej">
                    <b>
                      {e.nombre}
                      <Badge variant="outline" style={{ marginLeft: 8 }}>Catálogo</Badge>
                    </b>
                    <span className="muted">{e.grupoMuscular}</span>
                    <span className="muted">{e.equipamiento ?? ''}</span>
                  </div>
                ),
              )}
            </div>
          )}
        </Card>
      )}
    </>
  );
}
