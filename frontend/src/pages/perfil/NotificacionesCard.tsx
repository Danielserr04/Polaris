import { useEffect, useState } from 'react';
import {
  useGuardarRecordatorio,
  useProbarPush,
  useRecordatorios,
  type Recordatorio,
  type TipoRecordatorio,
} from '../../api/recordatorios';
import { Alert, Button, Card, Icon, Input, Switch } from '../../design-system';
import { activarPush, desactivarPush, esIosSinInstalar, estadoPush, type EstadoPush } from '../../lib/push';
import './notificaciones.css';

// Todo lo de las notificaciones en un sitio: si este dispositivo recibe avisos y que
// recordatorios hay, a que hora y que dias. Ver docs/decisiones/045-recordatorios.md.

interface Aviso {
  tono: 'success' | 'danger';
  texto: string;
}

const INFO: Record<TipoRecordatorio, { titulo: string; texto: string; icono: string }> = {
  COMIDAS: { titulo: 'Apuntar comidas', texto: 'Si a esa hora no hay ninguna comida de hoy en Fusión.', icono: 'utensils' },
  GASTOS: { titulo: 'Apuntar gastos', texto: 'Si a esa hora no hay ningún gasto de hoy en Kuiper.', icono: 'wallet' },
  ENTRENO: { titulo: 'Entrenar', texto: 'Los días elegidos, si aún no hay sesión en Atlas.', icono: 'dumbbell' },
  PRESUPUESTO: {
    titulo: 'Presupuesto al límite',
    texto: 'Si alguna categoría está cerca del límite o lo ha pasado.',
    icono: 'circle-alert',
  },
};

const DIAS = [
  { n: 1, corto: 'L', largo: 'lunes' },
  { n: 2, corto: 'M', largo: 'martes' },
  { n: 3, corto: 'X', largo: 'miércoles' },
  { n: 4, corto: 'J', largo: 'jueves' },
  { n: 5, corto: 'V', largo: 'viernes' },
  { n: 6, corto: 'S', largo: 'sábado' },
  { n: 7, corto: 'D', largo: 'domingo' },
];

export function NotificacionesCard({ avisar }: { avisar: (a: Aviso) => void }) {
  const recordatorios = useRecordatorios();
  const guardar = useGuardarRecordatorio();
  const lista = recordatorios.data ?? [];
  const algunoActivo = lista.some((r) => r.activo);

  const cambiar = (r: Recordatorio) =>
    guardar.mutate(r, {
      onError: () => avisar({ tono: 'danger', texto: 'No se ha podido guardar el recordatorio' }),
    });

  const todos = (activo: boolean) =>
    Promise.all(lista.map((r) => guardar.mutateAsync({ ...r, activo })))
      .then(() => avisar({ tono: 'success', texto: activo ? 'Recordatorios encendidos' : 'Recordatorios apagados' }))
      .catch(() => avisar({ tono: 'danger', texto: 'No se han podido guardar los recordatorios' }));

  return (
    <Card
      delay={360}
      eyebrow="06"
      title="Notificaciones"
      action={
        lista.length > 0 && (
          <Button size="sm" variant="ghost" onClick={() => void todos(!algunoActivo)} disabled={guardar.isPending}>
            {algunoActivo ? 'Apagar todos' : 'Encender todos'}
          </Button>
        )
      }
    >
      <div className="stack-16">
        <EsteDispositivo avisar={avisar} />
        {recordatorios.isError ? (
          <Alert
            tone="danger"
            title="No se han podido cargar tus recordatorios"
            action={
              <Button size="sm" variant="secondary" onClick={() => void recordatorios.refetch()}>
                Reintentar
              </Button>
            }
          />
        ) : recordatorios.isPending ? (
          <p className="muted" role="status">
            Cargando tus recordatorios…
          </p>
        ) : (
          <div className="pf-recs">
            {lista.map((r) => (
              <FilaRecordatorio key={r.tipo} r={r} onCambio={cambiar} />
            ))}
          </div>
        )}
      </div>
    </Card>
  );
}

// ---------------------------------------------------------------- este dispositivo

function EsteDispositivo({ avisar }: { avisar: (a: Aviso) => void }) {
  const [estado, setEstado] = useState<EstadoPush | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const probar = useProbarPush();

  useEffect(() => {
    void estadoPush().then(setEstado);
  }, []);

  const alternar = async () => {
    setOcupado(true);
    try {
      const nuevo = estado === 'activo' ? await desactivarPush() : await activarPush();
      setEstado(nuevo);
      if (nuevo === 'activo') avisar({ tono: 'success', texto: 'Este dispositivo recibirá los avisos' });
      else if (nuevo === 'inactivo' && estado === 'activo') avisar({ tono: 'success', texto: 'Avisos desactivados aquí' });
    } catch {
      avisar({ tono: 'danger', texto: 'No se han podido activar los avisos en este dispositivo' });
    } finally {
      setOcupado(false);
    }
  };

  const enviarPrueba = () =>
    probar.mutate(undefined, {
      onSuccess: ({ enviados }) =>
        avisar(
          enviados > 0
            ? { tono: 'success', texto: 'Aviso de prueba enviado' }
            : { tono: 'danger', texto: 'No ha llegado a ningún dispositivo' },
        ),
      onError: () => avisar({ tono: 'danger', texto: 'No se ha podido enviar la prueba' }),
    });

  if (estado === null) return null;

  if (estado === 'no-soportado') {
    return (
      <Alert tone="info" title="Avisos al móvil">
        {esIosSinInstalar()
          ? 'En iPhone, añade Polaris a la pantalla de inicio (Compartir → Añadir a pantalla de inicio) y ábrelo desde ahí para activarlos.'
          : 'Este navegador no admite avisos. Prueba con Chrome, Edge, Firefox o Safari.'}
      </Alert>
    );
  }

  return (
    <div className="pf-push">
      <span className="pf-push__ico">
        <Icon name="bell" size={16} />
      </span>
      <div className="stack-4" style={{ flex: 1, minWidth: 0 }}>
        <b>Avisos en este dispositivo</b>
        <span className="muted" style={{ fontSize: 13 }}>
          {estado === 'activo'
            ? 'Activados. Te llegan aunque Polaris esté cerrado.'
            : estado === 'bloqueado'
              ? 'Bloqueados. Permite las notificaciones de Polaris en los ajustes del navegador.'
              : 'Desactivados. Actívalos para que los recordatorios te lleguen al móvil o al ordenador.'}
        </span>
      </div>
      <div className="pf-push__acc">
        {estado === 'activo' && (
          <Button size="sm" variant="ghost" icon="send" onClick={enviarPrueba} disabled={probar.isPending}>
            Probar
          </Button>
        )}
        {estado !== 'bloqueado' && (
          <Button
            size="sm"
            variant={estado === 'activo' ? 'secondary' : 'primary'}
            onClick={() => void alternar()}
            disabled={ocupado}
          >
            {estado === 'activo' ? 'Desactivar' : 'Activar'}
          </Button>
        )}
      </div>
    </div>
  );
}

// ---------------------------------------------------------------- un recordatorio

function FilaRecordatorio({ r, onCambio }: { r: Recordatorio; onCambio: (r: Recordatorio) => void }) {
  const info = INFO[r.tipo];
  const [hora, setHora] = useState(r.hora);
  useEffect(() => setHora(r.hora), [r.hora]);

  const alternarDia = (n: number) => {
    const dias = r.dias.includes(n) ? r.dias.filter((d) => d !== n) : [...r.dias, n].sort((a, b) => a - b);
    if (dias.length > 0) onCambio({ ...r, dias });
  };

  const guardarHora = () => {
    if (/^\d{2}:\d{2}$/.test(hora) && hora !== r.hora) onCambio({ ...r, hora });
  };

  return (
    <div className={'pf-rec' + (r.activo ? '' : ' pf-rec--off')}>
      <span className="pf-rec__ico">
        <Icon name={info.icono} size={16} />
      </span>
      <div className="pf-rec__txt">
        <b>{info.titulo}</b>
        <span className="muted">{info.texto}</span>
      </div>
      <Switch checked={r.activo} onChange={(activo) => onCambio({ ...r, activo })} />
      {r.activo && (
        <div className="pf-rec__cfg">
          <div className="pf-rec__hora">
            <Input
              type="time"
              size="sm"
              aria-label={`Hora de ${info.titulo.toLowerCase()}`}
              value={hora}
              onChange={(e) => setHora(e.target.value)}
              onBlur={guardarHora}
            />
          </div>
          <div className="pf-dias" role="group" aria-label="Días de la semana">
            {DIAS.map((d) => {
              const marcado = r.dias.includes(d.n);
              return (
                <button
                  key={d.n}
                  type="button"
                  className="pf-dia"
                  aria-pressed={marcado}
                  aria-label={d.largo}
                  title={marcado && r.dias.length === 1 ? 'Al menos un día' : d.largo}
                  onClick={() => alternarDia(d.n)}
                >
                  {d.corto}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
