import { useMemo } from 'react';
import { mensajeError, useMovimientos, useResumen, type MovimientoList } from '../../api/kuiper';
import { Alert, BarChart, Badge, Button, Card, Icon, ProgressBar, Stat } from '../../design-system';
import { eur, iso, mesAnterior, periodo as periodoDe, rangoMes } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { ListaMovimientos } from './ListaMovimientos';

interface Props {
  periodo: string;
  onVerTodos: () => void;
  onEditar: (m: MovimientoList) => void;
  onIrACategorias: () => void;
}

function pct(actual: number, previo: number): { texto: string; sube: boolean } | null {
  if (!previo) return null;
  const p = ((actual - previo) / Math.abs(previo)) * 100;
  const r = Math.round(p);
  return { texto: `${r > 0 ? '+' : r < 0 ? '−' : ''}${Math.abs(r)} %`, sube: p > 0 };
}

export function ResumenTab({ periodo, onVerTodos, onEditar, onIrACategorias }: Props) {
  const hoy = useMemo(() => new Date(), []);
  const esActual = periodo === periodoDe(hoy);
  const { desde, hasta, dias } = rangoMes(periodo);
  const previo = mesAnterior(periodo);

  const resumen = useResumen(periodo);
  const resumenPrevio = useResumen(previo);
  const movimientos = useMovimientos({ desde, hasta });

  const gastosPorDia = useMemo(() => {
    const porDia = new Map<string, number>();
    for (const m of movimientos.data ?? []) {
      if (m.tipo === 'GASTO') porDia.set(m.fecha, (porDia.get(m.fecha) ?? 0) + m.importe);
    }
    return Array.from({ length: dias }, (_, i) => {
      const f = `${periodo}-${String(i + 1).padStart(2, '0')}`;
      return { label: (i + 1) % 5 === 0 || i === 0 ? String(i + 1) : '', value: porDia.get(f) ?? 0 };
    });
  }, [movimientos.data, dias, periodo]);

  if (resumen.isError || movimientos.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar los datos"
        action={
          <Button size="sm" variant="secondary" onClick={() => { void resumen.refetch(); void movimientos.refetch(); }}>
            Reintentar
          </Button>
        }
      >
        {mensajeError(resumen.error ?? movimientos.error)}
      </Alert>
    );
  }
  if (!resumen.data || !movimientos.data) return <p className="muted">Cargando…</p>;

  const r = resumen.data;
  const conLimite = r.gastoPorCategoria.filter((c) => (c.limiteMensual ?? 0) > 0);
  const sinLimite = r.gastoPorCategoria.filter((c) => !(c.limiteMensual ?? 0) && c.gastado > 0);
  const totalLimite = conLimite.reduce((s, c) => s + (c.limiteMensual ?? 0), 0);
  const gastadoConLimite = conLimite.reduce((s, c) => s + c.gastado, 0);
  const hoyIso = iso(hoy);
  const diaHoy = esActual ? Number(hoyIso.slice(8)) : 0;
  const diasQueda = esActual ? dias - diaHoy : 0;
  const transcurridos = esActual ? Math.max(diaHoy, 1) : dias;
  const media = r.gastos / transcurridos;
  // Un mes a medias contra uno completo no es comparable: la variacion solo sale en meses cerrados.
  const comparable = !esActual && resumenPrevio.data;
  const dGasto = comparable ? pct(r.gastos, resumenPrevio.data.gastos) : null;
  const dBalance = comparable ? pct(r.balance, resumenPrevio.data.balance) : null;
  const ultimos = [...movimientos.data]
    .sort((a, b) => (a.fecha < b.fecha ? 1 : a.fecha > b.fecha ? -1 : b.id - a.id))
    .slice(0, 4);

  return (
    <div className="grid">
      <div className="span-12 stats pl-rise">
        <Stat
          label="Gastado"
          value={r.gastos}
          decimals={2}
          unit="€"
          delta={dGasto?.texto}
          deltaTone={dGasto ? (dGasto.sube ? 'down' : 'up') : undefined}
          caption={dGasto ? 'vs mes anterior' : undefined}
        />
        <Stat label="Ingresos" value={r.ingresos} decimals={2} unit="€" />
        <Stat
          label="Balance"
          value={r.balance}
          decimals={2}
          unit="€"
          delta={dBalance?.texto}
          deltaTone={dBalance ? (dBalance.sube ? 'up' : 'down') : undefined}
          caption={dBalance ? 'vs mes anterior' : undefined}
        />
        {totalLimite > 0 ? (
          <Stat
            label="Queda de presupuesto"
            value={totalLimite - gastadoConLimite}
            decimals={2}
            unit="€"
            caption={esActual ? `para ${diasQueda} ${diasQueda === 1 ? 'día' : 'días'}` : `de ${eur(totalLimite, 0)}`}
          />
        ) : (
          <Stat label="Queda de presupuesto" value="—" caption="sin presupuestos este mes" />
        )}
      </div>

      <div className="span-12 kui-cols">
        <Card delay={100} eyebrow="Día a día" title="Gasto diario" action={<span className="pl-row__num">media {eur(media)}</span>}>
          <BarChart
            height={200}
            gap={5}
            highlight={esActual ? diaHoy - 1 : undefined}
            target={media}
            targetLabel="Media"
            data={gastosPorDia}
            format={(v) => eur(v)}
          />
        </Card>

        <Card delay={160} eyebrow="Presupuestos" title="Por categoría">
          {conLimite.length === 0 && sinLimite.length === 0 ? (
            <span className="muted">Sin gastos este mes.</span>
          ) : (
            <div className="stack-16">
              {conLimite.map((c) => {
                const exceso = c.gastado - (c.limiteMensual ?? 0);
                return (
                  <div key={c.categoriaId} style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <span className="catic">
                      <Icon name={iconoOr(c.categoriaIcono)} size={15} />
                    </span>
                    <ProgressBar
                      style={{ flex: 1 }}
                      label={
                        <>
                          {c.categoriaNombre}
                          {exceso > 0 && (
                            <Badge tone="danger" style={{ marginLeft: 8, height: 18 }}>
                              +{eur(exceso, 0)}
                            </Badge>
                          )}
                        </>
                      }
                      value={c.gastado}
                      max={c.limiteMensual ?? 1}
                      valueLabel={`${eur(c.gastado, 0)} / ${eur(c.limiteMensual ?? 0, 0)}`}
                    />
                  </div>
                );
              })}
              {conLimite.length === 0 && (
                <span className="muted" style={{ fontSize: 13 }}>
                  Aún no tienes presupuestos.{' '}
                  <Button variant="ghost" size="sm" onClick={onIrACategorias}>
                    Ponlos en Categorías
                  </Button>
                </span>
              )}
              {sinLimite.length > 0 && (
                <div className="stack-8">
                  <span className="pl-eyebrow">Sin presupuesto</span>
                  {sinLimite.map((c) => (
                    <div key={c.categoriaId} className="kui-sinpres">
                      <b>{c.categoriaNombre}</b>
                      <span className="money">{eur(c.gastado)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </Card>
      </div>

      <div className="span-12">
        <Card
          delay={220}
          eyebrow="Últimos"
          title="Movimientos"
          action={
            <Button variant="ghost" size="sm" iconRight="arrow-right" onClick={onVerTodos}>
              Ver todos
            </Button>
          }
          padding="8px 0 0"
        >
          <ListaMovimientos movimientos={ultimos} vacio="Sin movimientos este mes." onEditar={onEditar} />
        </Card>
      </div>
    </div>
  );
}
