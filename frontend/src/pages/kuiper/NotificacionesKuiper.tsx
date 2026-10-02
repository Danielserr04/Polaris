import { useState } from 'react';
import { mensajeError } from '../../api/kuiper';
import {
  useBorrarLeidas,
  useBorrarNotificacion,
  useLeerTodas,
  useMarcarLeida,
  useNotificaciones,
  useNotificacionesNoLeidas,
  type EnlaceNotificacion,
  type Notificacion,
  type TipoNotificacion,
} from '../../api/kuiperNotificaciones';
import { Alert, Badge, Button, Dialog, Icon, IconButton } from '../../design-system';
import './notificaciones.css';

/**
 * Pestaña de Kuiper.tsx a la que lleva cada enlace. Las que aun no tienen pestaña propia
 * caen en la mas cercana: los recurrentes generan movimientos y el progreso de los
 * presupuestos se ve en el resumen.
 */
const PESTANA_DE: Record<EnlaceNotificacion, string> = {
  movimientos: 'mov',
  recurrentes: 'rec',
  presupuestos: 'resumen',
  resumen: 'resumen',
  metas: 'metas',
};

const ICONO: Record<TipoNotificacion, { icono: string; tono: string }> = {
  CARGO_RECURRENTE: { icono: 'repeat', tono: 'var(--info)' },
  CARGO_PROXIMO: { icono: 'calendar', tono: 'var(--info)' },
  PRESUPUESTO_AVISO: { icono: 'circle-alert', tono: 'var(--warning)' },
  PRESUPUESTO_EXCEDIDO: { icono: 'circle-alert', tono: 'var(--danger)' },
  META_ALCANZADA: { icono: 'trophy', tono: 'var(--success)' },
  RESUMEN_MENSUAL: { icono: 'wallet', tono: 'var(--accent)' },
};

const FECHA = new Intl.DateTimeFormat('es-ES', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });

function cuando(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? '' : FECHA.format(d);
}

interface Props {
  /** Recibe el valor de pestaña de Kuiper.tsx al pulsar una notificacion con enlace. */
  onIr: (pestana: string) => void;
}

/** Campana con el numero de no leidas y el panel con la lista. Va en las acciones del PageHeader. */
export function NotificacionesKuiper({ onIr }: Props) {
  const [abierto, setAbierto] = useState(false);
  const noLeidas = useNotificacionesNoLeidas();
  const total = noLeidas.data ?? 0;

  return (
    <>
      <span className="kui-bell">
        <IconButton
          icon="bell"
          variant="outline"
          label={total > 0 ? `Notificaciones (${total} sin leer)` : 'Notificaciones'}
          pressed={abierto}
          onClick={() => setAbierto(true)}
        />
        {total > 0 && (
          <span className="kui-bell__n" aria-hidden>
            <Badge tone="danger" variant="solid">
              {total > 99 ? '99+' : total}
            </Badge>
          </span>
        )}
      </span>
      {abierto && (
        <PanelNotificaciones
          onClose={() => setAbierto(false)}
          onIr={(p) => {
            setAbierto(false);
            onIr(p);
          }}
        />
      )}
    </>
  );
}

function PanelNotificaciones({ onClose, onIr }: { onClose: () => void; onIr: (pestana: string) => void }) {
  const lista = useNotificaciones(true);
  const marcar = useMarcarLeida();
  const borrar = useBorrarNotificacion();
  const leerTodas = useLeerTodas();
  const borrarLeidas = useBorrarLeidas();

  const notificaciones = lista.data ?? [];
  const hayNoLeidas = notificaciones.some((n) => !n.leida);
  const hayLeidas = notificaciones.some((n) => n.leida);
  const error = marcar.error ?? borrar.error ?? leerTodas.error ?? borrarLeidas.error;

  const abrir = (n: Notificacion) => {
    if (!n.leida) marcar.mutate({ id: n.id });
    if (n.enlace) onIr(PESTANA_DE[n.enlace] ?? 'resumen');
  };

  return (
    <Dialog
      open
      title="Notificaciones"
      onClose={onClose}
      width={480}
      footer={
        <>
          <Button variant="ghost" size="sm" disabled={!hayLeidas || borrarLeidas.isPending} onClick={() => borrarLeidas.mutate()}>
            Borrar leídas
          </Button>
          <Button variant="secondary" size="sm" icon="check" disabled={!hayNoLeidas || leerTodas.isPending} onClick={() => leerTodas.mutate()}>
            Marcar todas como leídas
          </Button>
        </>
      }
    >
      {error && (
        <Alert tone="danger" title="No se ha podido completar">
          {mensajeError(error)}
        </Alert>
      )}
      {lista.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar las notificaciones"
          action={
            <Button size="sm" variant="secondary" onClick={() => void lista.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(lista.error)}
        </Alert>
      ) : lista.isPending ? (
        <p className="muted">Cargando…</p>
      ) : notificaciones.length === 0 ? (
        <div className="kui-vacio">No tienes notificaciones.</div>
      ) : (
        <div className="kui-notifs">
          {notificaciones.map((n) => {
            const { icono, tono } = ICONO[n.tipo] ?? { icono: 'info', tono: 'var(--text-2)' };
            return (
              <div key={n.id} className={'kui-notif' + (n.leida ? '' : ' kui-notif--nueva')}>
                <span className="kui-notif__ico" style={{ ['--tone' as string]: tono }}>
                  <Icon name={icono} size={15} />
                </span>
                <button type="button" className="kui-notif__main" onClick={() => abrir(n)}>
                  <span className="kui-notif__t">{n.titulo}</span>
                  <span className="kui-notif__x">{n.texto}</span>
                  <span className="kui-notif__f">{cuando(n.creadaEn)}</span>
                </button>
                <span className="kui-notif__acc">
                  {!n.leida && (
                    <IconButton icon="check" size="sm" label="Marcar como leída" onClick={() => marcar.mutate({ id: n.id })} />
                  )}
                  <IconButton icon="x" size="sm" label="Borrar notificación" onClick={() => borrar.mutate(n.id)} />
                </span>
              </div>
            );
          })}
        </div>
      )}
    </Dialog>
  );
}
