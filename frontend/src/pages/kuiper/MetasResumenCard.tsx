import { useMetas } from '../../api/kuiperMetas';
import { Button, Card, ProgressBar } from '../../design-system';
import { eur } from '../../lib/fechas';

interface Props {
  onIrAMetas: () => void;
}

/** Las metas de ahorro sin completar, en pequeño, para el Resumen. */
export function MetasResumenCard({ onIrAMetas }: Props) {
  const metas = useMetas();
  const abiertas = (metas.data ?? []).filter((m) => !m.completada).slice(0, 3);

  return (
    <Card
      delay={240}
      eyebrow="Ahorro"
      title="Metas"
      action={
        <Button variant="ghost" size="sm" iconRight="arrow-right" onClick={onIrAMetas}>
          Ver
        </Button>
      }
    >
      {metas.isError ? (
        <span className="muted">No se han podido cargar.</span>
      ) : !metas.data ? (
        <span className="muted">Cargando…</span>
      ) : abiertas.length === 0 ? (
        <span className="muted" style={{ fontSize: 13 }}>Sin metas en marcha.</span>
      ) : (
        <div className="stack-12">
          {abiertas.map((m) => (
            <ProgressBar
              key={m.id}
              label={m.nombre}
              value={m.importeActual}
              max={m.importeObjetivo}
              color={m.color ?? undefined}
              valueLabel={`${eur(m.importeActual, 0)} / ${eur(m.importeObjetivo, 0)} · ${Math.floor(m.porcentaje)} %`}
            />
          ))}
        </div>
      )}
    </Card>
  );
}
