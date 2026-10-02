import { useResumenAnual } from '../../api/kuiperAlertas';
import { Button, Card } from '../../design-system';
import { eur } from '../../lib/fechas';
import { BarraPresupuesto } from './BarraPresupuesto';

interface Props {
  anio: number;
  onIrACategorias: () => void;
}

/** Los presupuestos anuales frente a lo gastado en el año. */
export function PresupuestosAnualesCard({ anio, onIrACategorias }: Props) {
  const resumen = useResumenAnual(anio);
  const filas = resumen.data?.presupuestos ?? [];
  const total = filas.reduce((s, f) => s + f.limite, 0);
  const gastado = filas.reduce((s, f) => s + f.gastado, 0);

  return (
    <Card
      delay={200}
      eyebrow={`Año ${anio}`}
      title="Presupuestos anuales"
      action={filas.length > 0 ? <span className="pl-row__num">{eur(gastado, 0)} / {eur(total, 0)}</span> : undefined}
    >
      {resumen.isError ? (
        <span className="muted">No se han podido cargar.</span>
      ) : !resumen.data ? (
        <span className="muted">Cargando…</span>
      ) : filas.length === 0 ? (
        <span className="muted" style={{ fontSize: 13 }}>
          Sin presupuestos anuales.{' '}
          <Button variant="ghost" size="sm" onClick={onIrACategorias}>
            Ponlos en Categorías
          </Button>
        </span>
      ) : (
        <div className="stack-16">
          {filas.map((f) => (
            <BarraPresupuesto
              key={f.categoriaId}
              nombre={f.categoriaNombre}
              icono={f.categoriaIcono}
              gastado={f.gastado}
              limite={f.limite}
              porcentaje={f.porcentaje}
              porcentajeAlerta={f.porcentajeAlerta}
              estado={f.estado}
            />
          ))}
        </div>
      )}
    </Card>
  );
}
