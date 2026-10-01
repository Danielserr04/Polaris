import { useEffect } from 'react';
import { mensajeError, useRutinas, type Rutina } from '../../api/atlas';
import { Alert, Badge, Button, Card } from '../../design-system';

interface Props {
  onEditar: (r: Rutina) => void;
  /** El padre muestra el recuento en la cabecera. */
  onRecuento: (n: number | null) => void;
}

export function RutinasTab({ onEditar, onRecuento }: Props) {
  const rutinas = useRutinas();
  // Primero las activas (la lista de trabajo); las retiradas, al final.
  const filas = [...(rutinas.data ?? [])].sort((a, b) => Number(b.activa) - Number(a.activa) || a.nombre.localeCompare(b.nombre, 'es'));

  useEffect(() => {
    onRecuento(rutinas.isSuccess ? rutinas.data.length : null);
    return () => onRecuento(null);
  }, [rutinas.isSuccess, rutinas.data, onRecuento]);

  if (rutinas.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar las rutinas"
        action={
          <Button size="sm" variant="secondary" onClick={() => void rutinas.refetch()}>
            Reintentar
          </Button>
        }
      >
        {mensajeError(rutinas.error)}
      </Alert>
    );
  }
  if (rutinas.isPending) return <p className="muted">Cargando…</p>;

  return (
    <Card padding="0">
      {filas.length === 0 ? (
        <div className="atl-vacio">Aún no tienes rutinas. Crea la primera.</div>
      ) : (
        <div className="table">
          {filas.map((r) => (
            <button key={r.id} type="button" className="table__r table__r--rut atl-ses" onClick={() => onEditar(r)} aria-label={`Editar ${r.nombre}`} style={r.activa ? undefined : { opacity: 0.6 }}>
              <b>
                {r.nombre}
                {!r.activa && (
                  <Badge variant="outline" style={{ marginLeft: 8 }}>Inactiva</Badge>
                )}
              </b>
              <span className="muted">{r.numeroEjercicios} {r.numeroEjercicios === 1 ? 'ejercicio' : 'ejercicios'}</span>
            </button>
          ))}
        </div>
      )}
    </Card>
  );
}
