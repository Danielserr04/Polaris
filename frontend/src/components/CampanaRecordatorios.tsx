import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  useDescartarRecordatorio,
  useRecordatoriosPendientes,
  type RecordatorioPendiente,
  type TipoRecordatorio,
} from '../api/recordatorios';
import { Button, Dialog, Icon, IconButton } from '../design-system';
import './campana.css';

// Campana de la cabecera con lo que queda por hacer hoy. Lo mismo que llega al movil, pero
// dentro de la app. Ver docs/decisiones/045-recordatorios.md.

const ICONO: Record<TipoRecordatorio, { icono: string; tono: string }> = {
  COMIDAS: { icono: 'utensils', tono: 'var(--mod-fusion, var(--accent))' },
  GASTOS: { icono: 'wallet', tono: 'var(--mod-kuiper, var(--accent))' },
  ENTRENO: { icono: 'dumbbell', tono: 'var(--mod-atlas, var(--accent))' },
  PRESUPUESTO: { icono: 'circle-alert', tono: 'var(--warning)' },
};

/** El boton toma la clase de los iconos vecinos de la cabecera (pl-nav__act, m-logros). */
export function CampanaRecordatorios({ className, size }: { className: string; size: number }) {
  const [abierto, setAbierto] = useState(false);
  const navigate = useNavigate();
  const pendientes = useRecordatoriosPendientes();
  const descartar = useDescartarRecordatorio();
  const lista = pendientes.data ?? [];
  const total = lista.length;

  const ir = (p: RecordatorioPendiente) => {
    setAbierto(false);
    navigate(p.enlace);
  };

  return (
    <>
      <button
        type="button"
        className={className + ' campana'}
        aria-label={total > 0 ? `Recordatorios (${total} pendientes)` : 'Recordatorios'}
        title="Recordatorios"
        onClick={() => setAbierto(true)}
      >
        <Icon name="bell" size={size} />
        {total > 0 && (
          <span className="campana__n" aria-hidden>
            {total}
          </span>
        )}
      </button>
      <Dialog
        open={abierto}
        onClose={() => setAbierto(false)}
        title="Para hoy"
        width={460}
        footer={
          <Button
            variant="ghost"
            icon="calendar"
            onClick={() => {
              setAbierto(false);
              navigate('/perfil');
            }}
          >
            Ajustar recordatorios
          </Button>
        }
      >
        {total === 0 ? (
          <div className="campana__vacio">
            <Icon name="circle-check" size={22} />
            <span>Nada pendiente. Cuando llegue la hora de un recordatorio y aún no lo hayas hecho, saldrá aquí.</span>
          </div>
        ) : (
          <div className="campana__lista">
            {lista.map((p) => {
              const { icono, tono } = ICONO[p.tipo];
              return (
                <div key={p.tipo} className="campana__item">
                  <span className="campana__ico" style={{ ['--tone' as string]: tono }}>
                    <Icon name={icono} size={15} />
                  </span>
                  <button type="button" className="campana__main" onClick={() => ir(p)}>
                    <span className="campana__t">{p.titulo}</span>
                    <span className="campana__x">{p.texto}</span>
                  </button>
                  <IconButton
                    icon="check"
                    size="sm"
                    label="Hecho por hoy"
                    onClick={() => descartar.mutate(p.tipo)}
                    disabled={descartar.isPending}
                  />
                </div>
              );
            })}
          </div>
        )}
      </Dialog>
    </>
  );
}
