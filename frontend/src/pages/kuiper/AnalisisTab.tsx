import { useState, type CSSProperties } from 'react';
import { mensajeError } from '../../api/kuiper';
import {
  useComercios,
  useComparativaCategorias,
  useEvolucion,
  useInsights,
  useProyeccion,
  type CategoriaComparada,
  type Insight,
  type ProyeccionMensual,
} from '../../api/kuiperAnalisis';
import { Alert, BarChart, Button, Card, Icon, LineChart, RingChart, SegmentedControl, Stat } from '../../design-system';
import { etiquetaMes, eur, nombreMes, num, rangoMes } from '../../lib/fechas';
import './analisis.css';

interface Props {
  periodo: string;
}

const ICONO: Record<Insight['severidad'], string> = { AVISO: 'circle-alert', BIEN: 'circle-check', INFO: 'info' };

/** "Sep" a partir de "2026-09". */
function mesCorto(p: string): string {
  const [a, m] = p.split('-').map(Number);
  return nombreMes(new Date(a, m - 1, 1)).slice(0, 3);
}

function ErrorCarga({ error, reintentar }: { error: unknown; reintentar: () => void }) {
  return (
    <Alert
      tone="danger"
      title="No se han podido cargar los datos"
      action={
        <Button size="sm" variant="secondary" onClick={reintentar}>
          Reintentar
        </Button>
      }
    >
      {mensajeError(error)}
    </Alert>
  );
}

export function AnalisisTab({ periodo }: Props) {
  return (
    <div className="grid">
      <div className="span-12">
        <Proyeccion periodo={periodo} />
      </div>
      <div className="span-12 kui-cols">
        <Evolucion periodo={periodo} />
        <Insights periodo={periodo} />
      </div>
      <div className="span-12 kui-cols">
        <Categorias periodo={periodo} />
        <Comercios periodo={periodo} />
      </div>
    </div>
  );
}

// ---------------------------------------------------------------- proyeccion

function Proyeccion({ periodo }: Props) {
  const q = useProyeccion(periodo);
  if (q.isError) return <ErrorCarga error={q.error} reintentar={() => void q.refetch()} />;
  const p = q.data;

  return (
    <Card delay={40} eyebrow="Proyección" title={titulo(p)}>
      {!p ? (
        <p className="muted">Cargando…</p>
      ) : (
        <>
          <div className="kan-proy">
            <AnilloProyeccion p={p} />
            <div className="kan-proy__stats">
              <Stat label="Gastado" value={p.gastoActual} decimals={2} unit="€" caption={`${p.diasTranscurridos} de ${p.diasMes} días`} />
              <Stat
                label={p.estado === 'CERRADO' ? 'Gasto final' : 'Previsto a fin de mes'}
                value={p.gastoProyectado}
                decimals={2}
                unit="€"
                delta={difPresupuesto(p)?.texto}
                deltaTone={difPresupuesto(p)?.tono}
                caption={p.presupuestoMensual ? `presupuesto ${eur(p.presupuestoMensual, 0)}` : undefined}
              />
              <Stat label="Ritmo diario" value={p.ritmoDiario} decimals={2} unit="€" caption="sin contar recurrentes" />
              <Stat
                label="Recurrentes pendientes"
                value={p.recurrentesPendientes}
                decimals={2}
                unit="€"
                caption={`${p.cargosPendientes} ${p.cargosPendientes === 1 ? 'cargo' : 'cargos'}`}
              />
            </div>
          </div>
          {p.estado === 'EN_CURSO' && (
            <p className="kan-proy__nota">
              Lo gastado, más {eur(p.ritmoDiario)} al día durante los {p.diasMes - p.diasTranscurridos} días que quedan, más los
              cargos recurrentes que faltan por cobrar.
            </p>
          )}
          {p.estado === 'FUTURO' && <p className="kan-proy__nota">Mes futuro: solo cuentan los recurrentes ya programados.</p>}
        </>
      )}
    </Card>
  );
}

function titulo(p: ProyeccionMensual | undefined): string {
  if (!p) return 'Fin de mes';
  if (p.estado === 'CERRADO') return `${etiquetaMes(p.periodo)}, cerrado`;
  return `Así acabará ${etiquetaMes(p.periodo).toLowerCase()}`;
}

function difPresupuesto(p: ProyeccionMensual): { texto: string; tono: 'up' | 'down' } | null {
  if (!p.presupuestoMensual) return null;
  const dif = p.gastoProyectado - p.presupuestoMensual;
  return dif > 0
    ? { texto: `+${eur(dif, 0)} sobre presupuesto`, tono: 'down' }
    : { texto: `${eur(-dif, 0)} de margen`, tono: 'up' };
}

/** Contra el presupuesto si lo hay; si no, lo gastado frente a lo previsto. */
function AnilloProyeccion({ p }: { p: ProyeccionMensual }) {
  const max = p.presupuestoMensual || p.gastoProyectado || 1;
  const valor = p.presupuestoMensual ? p.gastoProyectado : p.gastoActual;
  // Por encima de max el propio anillo se pinta en rojo.
  return (
    <RingChart
      value={valor}
      max={max}
      size={132}
      thickness={12}
      label={`${num((valor / max) * 100)} %`}
      sublabel={p.presupuestoMensual ? 'del presupuesto' : 'de lo previsto'}
    />
  );
}

// ----------------------------------------------------------------- evolucion

function Evolucion({ periodo }: Props) {
  const [meses, setMeses] = useState('6');
  const q = useEvolucion(periodo, Number(meses));
  if (q.isError) return <ErrorCarga error={q.error} reintentar={() => void q.refetch()} />;
  const serie = q.data ?? [];
  const conAhorro = serie.filter((m) => m.tasaAhorro != null);

  return (
    <Card
      delay={100}
      eyebrow="Evolución"
      title="Ingresos y gastos"
      action={
        <SegmentedControl
          value={meses}
          onChange={setMeses}
          options={[
            { value: '6', label: '6 m' },
            { value: '12', label: '12 m' },
          ]}
        />
      }
    >
      {!q.data ? (
        <p className="muted">Cargando…</p>
      ) : (
        <>
          <LineChart
            height={220}
            showLegend
            labels={serie.map((m) => mesCorto(m.periodo))}
            series={[
              { name: 'Gastos', points: serie.map((m) => m.gastos) },
              { name: 'Ingresos', points: serie.map((m) => m.ingresos), color: 'var(--success)' },
            ]}
            format={(v) => eur(v, 0)}
          />
          {conAhorro.length > 0 && (
            <div className="kan-ahorro" aria-label="Tasa de ahorro por mes">
              {serie.map((m) => (
                <span key={m.periodo} className="pl-row__num" title={etiquetaMes(m.periodo)}>
                  {mesCorto(m.periodo)} {m.tasaAhorro == null ? '—' : `${num(m.tasaAhorro)} %`}
                </span>
              ))}
            </div>
          )}
        </>
      )}
    </Card>
  );
}

// ------------------------------------------------------------------ insights

function Insights({ periodo }: Props) {
  const q = useInsights(periodo);
  if (q.isError) return <ErrorCarga error={q.error} reintentar={() => void q.refetch()} />;

  return (
    <Card delay={160} eyebrow="Insights" title="Lo que dicen tus números">
      {!q.data ? (
        <p className="muted">Cargando…</p>
      ) : q.data.length === 0 ? (
        <p className="muted">Aún no hay datos suficientes para sacar conclusiones este mes.</p>
      ) : (
        <div className="kan-insights">
          {q.data.map((i) => (
            <div key={i.tipo} className={`kan-insight kan-insight--${i.severidad}`}>
              <span className="kan-insight__ico">
                <Icon name={ICONO[i.severidad]} size={16} />
              </span>
              <div>
                <b>{i.titulo}</b>
                <p>{i.texto}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

// ---------------------------------------------------------------- categorias

function Delta({ c }: { c: CategoriaComparada }) {
  if (c.diferencia === 0) return <span className="kan-delta kan-delta--igual">=</span>;
  const sube = c.diferencia > 0;
  const texto = c.variacion == null ? 'nuevo' : `${sube ? '+' : '−'}${num(Math.abs(c.variacion))} %`;
  return (
    <span className={`kan-delta kan-delta--${sube ? 'sube' : 'baja'}`} title={`${sube ? '+' : '−'}${eur(Math.abs(c.diferencia))}`}>
      {texto}
    </span>
  );
}

function Categorias({ periodo }: Props) {
  const { desde, hasta } = rangoMes(periodo);
  const q = useComparativaCategorias(desde, hasta);
  if (q.isError) return <ErrorCarga error={q.error} reintentar={() => void q.refetch()} />;
  const c = q.data;
  const conGasto = c?.categorias.filter((x) => x.gastado > 0) ?? [];

  return (
    <Card
      delay={220}
      eyebrow="Categorías"
      title="Dónde se va el dinero"
      action={c && c.totalAnterior > 0 ? <span className="pl-row__num">vs {eur(c.totalAnterior, 0)} el mes anterior</span> : undefined}
    >
      {!c ? (
        <p className="muted">Cargando…</p>
      ) : c.categorias.length === 0 ? (
        <p className="muted">Sin gastos este mes ni el anterior.</p>
      ) : (
        <div className="stack-16">
          {conGasto.length > 0 && (
            <BarChart
              height={180}
              gap={8}
              data={conGasto.slice(0, 8).map((x) => ({
                label: x.categoriaNombre.length > 9 ? `${x.categoriaNombre.slice(0, 8)}…` : x.categoriaNombre,
                value: x.gastado,
                color: x.categoriaColor ?? undefined,
              }))}
              format={(v) => eur(v, 0)}
            />
          )}
          <div className="kan-filas">
            <div className="kan-fila kan-cabecera">
              <span>Categoría</span>
              <span className="pl-row__num">Peso</span>
              <span className="pl-row__num">Gastado</span>
              <span className="pl-row__num">vs anterior</span>
            </div>
            {c.categorias.map((x) => (
              <div key={x.categoriaId} className="kan-fila">
                <span className="kan-fila__nombre" style={{ '--cat': x.categoriaColor ?? undefined } as CSSProperties}>
                  <i />
                  <span>{x.categoriaNombre}</span>
                </span>
                <span className="pl-row__num">{num(x.porcentaje)} %</span>
                <span className="money">{eur(x.gastado)}</span>
                <Delta c={x} />
              </div>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
}

// ----------------------------------------------------------------- comercios

function Comercios({ periodo }: Props) {
  const { desde, hasta } = rangoMes(periodo);
  const q = useComercios(desde, hasta, 10);
  if (q.isError) return <ErrorCarga error={q.error} reintentar={() => void q.refetch()} />;

  return (
    <Card delay={280} eyebrow="Comercios" title="Dónde más gastas">
      {!q.data ? (
        <p className="muted">Cargando…</p>
      ) : q.data.length === 0 ? (
        <p className="muted">Sin gastos con concepto este mes.</p>
      ) : (
        <div className="kan-filas">
          <div className="kan-fila kan-fila--com kan-cabecera">
            <span>#</span>
            <span>Concepto</span>
            <span className="pl-row__num">Veces</span>
            <span className="pl-row__num">Total</span>
            <span className="pl-row__num">Ticket medio</span>
          </div>
          {q.data.map((x, i) => (
            <div key={x.nombre} className="kan-fila kan-fila--com">
              <span className="pl-row__num">{i + 1}</span>
              <span className="kan-fila__nombre">
                <span>{x.nombre}</span>
              </span>
              <span className="pl-row__num">×{x.veces}</span>
              <span className="money">{eur(x.total)}</span>
              <span className="pl-row__num">{eur(x.ticketMedio)}</span>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
