import { useMemo, useState } from 'react';
import {
  etiquetaFrecuencia,
  importeMensual,
  mensajeErrorRecurrente,
  terminado,
  useBorrarRecurrente,
  useCambiarActivoRecurrente,
  useRecurrentes,
  type RecurrenteList,
} from '../../api/kuiperRecurrentes';
import { Alert, Badge, Button, Card, Icon, ProgressBar, Stat } from '../../design-system';
import { deIso, diasEntre, eur } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { FormularioRecurrente } from './FormularioRecurrente';
import './recurrentes.css';

// Un recurrente abierto: nuevo (sin id) o editando uno existente.
type Abierto = { id?: number } | null;

/** "Hoy", "Mañana", "En 5 días"; una fecha pasada es un cargo que el job aun no ha generado. */
function cuando(proximaFecha: string, hoy: Date): { texto: string; pronto: boolean } {
  const dias = diasEntre(hoy, deIso(proximaFecha));
  if (dias < 0) return { texto: 'Pendiente', pronto: true };
  if (dias === 0) return { texto: 'Hoy', pronto: true };
  if (dias === 1) return { texto: 'Mañana', pronto: true };
  return { texto: `En ${dias} días`, pronto: dias <= 3 };
}

function fechaCorta(fechaIso: string): string {
  return deIso(fechaIso).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }).replace('.', '');
}

export function RecurrentesTab() {
  const hoy = useMemo(() => new Date(), []);
  const [abierto, setAbierto] = useState<Abierto>(null);
  const [error, setError] = useState<string | null>(null);
  const lista = useRecurrentes();

  const { activos, parados } = useMemo(() => {
    const todos = lista.data ?? [];
    return {
      // El backend ya los ordena por proximaFecha; se reordena por si acaso.
      activos: todos.filter((r) => r.activo).sort((a, b) => (a.proximaFecha < b.proximaFecha ? -1 : a.proximaFecha > b.proximaFecha ? 1 : a.id - b.id)),
      parados: todos.filter((r) => !r.activo),
    };
  }, [lista.data]);

  const gastoMensual = activos.filter((r) => r.tipo === 'GASTO').reduce((s, r) => s + importeMensual(r), 0);
  const ingresoMensual = activos.filter((r) => r.tipo === 'INGRESO').reduce((s, r) => s + importeMensual(r), 0);
  const proximo = activos[0];

  return (
    <>
      <div className="kui-toolbar">
        <p className="muted kui-rec__intro">Suscripciones, recibos y pagos a plazos. Cada cargo se apunta solo como movimiento.</p>
        <Button variant="secondary" icon="plus" style={{ marginLeft: 'auto' }} onClick={() => setAbierto({})}>
          Recurrente
        </Button>
      </div>

      {error && (
        <Alert tone="danger" action={<Button size="sm" variant="ghost" onClick={() => setError(null)}>Cerrar</Button>}>
          {error}
        </Alert>
      )}

      {lista.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar los recurrentes"
          action={<Button size="sm" variant="secondary" onClick={() => void lista.refetch()}>Reintentar</Button>}
        >
          {mensajeErrorRecurrente(lista.error)}
        </Alert>
      ) : lista.isPending ? (
        <p className="muted">Cargando…</p>
      ) : (
        <div className="kui-stack">
          <div className="stats kui-rec__stats pl-rise">
            <Stat label="Gasto mensual estimado" value={gastoMensual} decimals={2} unit="€" caption={`${eur(gastoMensual * 12, 0)} al año`} />
            <Stat label="Ingresos recurrentes" value={ingresoMensual} decimals={2} unit="€" caption="al mes" />
            <Stat
              label="Próximo cargo"
              value={proximo ? cuando(proximo.proximaFecha, hoy).texto : '—'}
              caption={proximo ? `${proximo.concepto} · ${eur(proximo.importe)}` : 'nada programado'}
            />
          </div>

          <Card delay={60} eyebrow="Calendario" title="Próximos cargos" padding="8px 0 0">
            {activos.length === 0 ? (
              <div className="kui-vacio">No tienes recurrentes activos. Añade una suscripción o un recibo y se apuntará solo cada mes.</div>
            ) : (
              <div className="table">
                {activos.map((r, i) => (
                  <Fila key={r.id} r={r} i={i} hoy={hoy} onEditar={() => setAbierto({ id: r.id })} onError={setError} />
                ))}
              </div>
            )}
          </Card>

          {parados.length > 0 && (
            <Card delay={120} eyebrow="Sin cobrar" title="En pausa y terminados" padding="8px 0 0">
              <div className="table">
                {parados.map((r, i) => (
                  <Fila key={r.id} r={r} i={i} hoy={hoy} onEditar={() => setAbierto({ id: r.id })} onError={setError} />
                ))}
              </div>
            </Card>
          )}
        </div>
      )}

      {abierto && <FormularioRecurrente recurrenteId={abierto.id} onClose={() => setAbierto(null)} />}
    </>
  );
}

interface FilaProps {
  r: RecurrenteList;
  i: number;
  hoy: Date;
  onEditar: () => void;
  onError: (mensaje: string) => void;
}

function Fila({ r, i, hoy, onEditar, onError }: FilaProps) {
  const [confirmando, setConfirmando] = useState(false);
  const cambiarActivo = useCambiarActivoRecurrente();
  const borrar = useBorrarRecurrente();
  const fin = terminado(r);
  const c = cuando(r.proximaFecha, hoy);
  const ingreso = r.tipo === 'INGRESO';

  const alternar = () =>
    cambiarActivo.mutate({ id: r.id, activo: !r.activo }, { onError: (e) => onError(mensajeErrorRecurrente(e)) });
  const confirmarBorrado = () =>
    borrar.mutate(r.id, { onError: (e) => { setConfirmando(false); onError(mensajeErrorRecurrente(e)); } });

  return (
    <div className={`table__r kui-rec pl-rise${r.activo ? '' : ' kui-rec--parado'}`} style={{ animationDelay: Math.min(i, 12) * 30 + 'ms' }}>
      <span className="kui-catchip" style={r.categoriaColor ? ({ ['--c' as string]: r.categoriaColor }) : undefined}>
        <Icon name={iconoOr(r.categoriaIcono)} size={16} />
      </span>
      <div className="kui-rec__main">
        <b className="kui-rec__concepto">{r.concepto}</b>
        <span className="kui-rec__meta muted">
          {r.categoriaNombre} · {etiquetaFrecuencia[r.frecuencia]}
          {r.cuotasTotal !== null && ` · cuota ${Math.min(r.cuotasPagadas + (fin ? 0 : 1), r.cuotasTotal)} de ${r.cuotasTotal}`}
        </span>
        {r.cuotasTotal !== null && (
          <div className="kui-rec__cuotas">
            <ProgressBar value={r.cuotasPagadas} max={r.cuotasTotal} valueLabel={`${r.cuotasPagadas}/${r.cuotasTotal}`} />
          </div>
        )}
      </div>
      <span className="kui-rec__cuando">
        {r.activo ? (
          <>
            <Badge tone={c.pronto ? 'warning' : 'neutral'}>{c.texto}</Badge>
            <span className="muted kui-rec__fecha">{fechaCorta(r.proximaFecha)}</span>
          </>
        ) : fin ? (
          <Badge tone="success">Terminado</Badge>
        ) : (
          <Badge>En pausa</Badge>
        )}
      </span>
      <span className={`money kui-rec__importe${ingreso ? ' kui-rec__importe--ingreso' : ''}`}>
        {ingreso ? '+' : '−'}
        {eur(r.importe)}
      </span>
      <span className="kui-rec__acciones">
        {confirmando ? (
          <>
            <span className="muted kui-rec__pregunta">¿Borrar?</span>
            <Button size="sm" variant="ghost" autoFocus onClick={() => setConfirmando(false)}>No</Button>
            <Button size="sm" variant="danger" loading={borrar.isPending} onClick={confirmarBorrado}>Sí</Button>
          </>
        ) : (
          <>
            <Button size="sm" variant="ghost" onClick={onEditar} aria-label={`Editar ${r.concepto}`}>
              Editar
            </Button>
            {!fin && (
              <Button
                size="sm"
                variant="ghost"
                loading={cambiarActivo.isPending}
                onClick={alternar}
                aria-label={r.activo ? `Pausar ${r.concepto}` : `Reactivar ${r.concepto}`}
              >
                {r.activo ? 'Pausar' : 'Reactivar'}
              </Button>
            )}
            <Button size="sm" variant="ghost" onClick={() => setConfirmando(true)} aria-label={`Borrar ${r.concepto}`}>
              Borrar
            </Button>
          </>
        )}
      </span>
    </div>
  );
}
