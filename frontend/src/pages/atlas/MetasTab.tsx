import { type Logro, type MetaEntreno, mensajeError, useLogros, useMetas } from '../../api/atlas';
import { Alert, Badge, Button, Card, Icon, ProgressBar, Stat } from '../../design-system';
import { deIso, diasEntre, num } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { NOMBRE_TIPO } from './FormularioMetaEntreno';

const UNIDAD: Record<Logro['metrica'], string> = {
  SESIONES: 'sesiones',
  EJERCICIOS: 'ejercicios',
  TONELADAS: 't',
  PESAJES: 'pesajes',
  RACHA_SEMANAS: 'semanas',
};

function valor(m: MetaEntreno, n: number | null | undefined): string {
  if (n == null) return '—';
  return m.tipo === 'SESIONES_SEMANA' ? String(n) : `${num(n, n % 1 ? 1 : 0)} kg`;
}

function titulo(m: MetaEntreno): string {
  if (m.tipo === 'MARCA_EJERCICIO') return `${m.ejercicioNombre ?? 'Ejercicio'} a ${valor(m, m.valorObjetivo)}`;
  if (m.tipo === 'SESIONES_SEMANA') return `${m.valorObjetivo} sesiones por semana`;
  return `Llegar a ${valor(m, m.valorObjetivo)}`;
}

function plazo(fecha: string): string {
  const dias = diasEntre(new Date(), deIso(fecha));
  if (dias === 0) return 'Vence hoy';
  if (dias < 0) return `Venció hace ${-dias} ${dias === -1 ? 'día' : 'días'}`;
  return `Quedan ${dias} ${dias === 1 ? 'día' : 'días'}`;
}

/** Metas personales y logros de entreno, traidos de FitCore. */
export function MetasTab({ onEditar }: { onEditar: (m: MetaEntreno) => void }) {
  const metas = useMetas();
  const logros = useLogros();

  const lista = metas.data ?? [];
  const conseguidos = (logros.data ?? []).filter((l) => l.conseguido).length;

  if (metas.isError || logros.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar las metas"
        action={
          <Button size="sm" variant="secondary" onClick={() => void (metas.isError ? metas.refetch() : logros.refetch())}>
            Reintentar
          </Button>
        }
      >
        {mensajeError(metas.error ?? logros.error)}
      </Alert>
    );
  }

  return (
    <div className="grid">
      <div className="span-12 stats pl-rise">
        <Stat label="Metas" value={lista.length} size={34} caption={`${lista.filter((m) => m.conseguida).length} conseguidas`} />
        <Stat label="Logros" value={conseguidos} size={34} caption={`de ${logros.data?.length ?? 0}`} />
      </div>

      <div className="span-12">
        {metas.isPending ? (
          <p className="muted">Cargando…</p>
        ) : lista.length === 0 ? (
          <Card>
            <div className="atl-vacio">Aún no tienes metas. Crea una con el botón Meta: un peso, una marca en un ejercicio o cuántos días entrenar a la semana.</div>
          </Card>
        ) : (
          <div className="atl-metas">
            {lista.map((m, i) => (
              <Card
                key={m.id}
                delay={Math.min(i, 12) * 40}
                className="atl-meta"
                eyebrow={NOMBRE_TIPO[m.tipo]}
                title={titulo(m)}
                action={
                  <Button variant="ghost" size="sm" aria-label={`Editar ${titulo(m)}`} onClick={() => onEditar(m)}>
                    Editar
                  </Button>
                }
              >
                <div className="atl-meta__cifras">
                  <span className="money atl-meta__actual">{valor(m, m.valorActual)}</span>
                  {m.conseguida ? <Badge tone="success">Conseguida</Badge> : <span className="muted">{m.progresoPct} %</span>}
                </div>
                <ProgressBar value={m.progresoPct} max={100} valueLabel={false} />
                <div className="atl-meta__pie">
                  <span className="muted">{m.tipo === 'SESIONES_SEMANA' ? 'Esta semana' : m.valorActual == null ? 'Apunta tu peso para medirla' : 'Ahora'}</span>
                  {m.fechaLimite ? (
                    <span>
                      <Icon name="calendar" size={13} /> {plazo(m.fechaLimite)}
                    </span>
                  ) : (
                    <span className="muted">Sin fecha límite</span>
                  )}
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <div className="span-12">
        <Card eyebrow="Logros" title="Lo que llevas conseguido" padding="12px 18px 18px">
          {logros.isPending ? (
            <p className="muted" style={{ margin: 0 }}>Cargando…</p>
          ) : (
            <div className="atl-logros">
              {logros.data.map((l) => (
                <div key={l.codigo} className={'atl-logro' + (l.conseguido ? ' atl-logro--si' : '')} title={l.descripcion}>
                  <span className="atl-logro__icono">
                    <Icon name={iconoOr(l.icono, 'trophy')} size={18} />
                  </span>
                  <div className="atl-logro__txt">
                    <b>{l.nombre}</b>
                    <span className="muted">{l.descripcion}</span>
                    {!l.conseguido && (
                      <span className="pl-row__num">
                        {num(l.progreso)} / {num(l.objetivo)} {UNIDAD[l.metrica]}
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
