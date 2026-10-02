import { useNavigate } from 'react-router-dom';
import { type MetaEntreno, mensajeError, useLogros, useMetas } from '../../api/atlas';
import { Alert, Badge, Button, Card, Icon, ProgressBar, Stat } from '../../design-system';
import { deIso, diasEntre, num } from '../../lib/fechas';
import { NOMBRE_TIPO } from './FormularioMetaEntreno';

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
  const navigate = useNavigate();
  const metas = useMetas();
  const logros = useLogros();

  const lista = metas.data ?? [];
  const conseguidos = (logros.data ?? []).filter((l) => l.conseguido).length;
  const sinPlazo = lista.filter((m) => !m.fechaLimite).length;
  // El logro pendiente al que menos le falta, en proporcion.
  const siguiente = (logros.data ?? []).filter((l) => !l.conseguido).sort((a, b) => b.progreso / b.objetivo - a.progreso / a.objetivo)[0];

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
        <Stat label="Metas" value={lista.length} size={34} caption={sinPlazo ? `${sinPlazo} sin fecha límite` : undefined} />
        <Stat label="Conseguidas" value={lista.filter((m) => m.conseguida).length} size={34} caption={`de ${lista.length}`} />
        <Stat label="Logros" value={conseguidos} size={34} caption={`de ${logros.data?.length ?? 0}`} />
        <Stat
          label="Siguiente logro"
          value={siguiente ? Math.floor((siguiente.progreso / siguiente.objetivo) * 100) : 0}
          unit="%"
          size={34}
          caption={siguiente ? siguiente.nombre : 'todos conseguidos'}
        />
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
        <Card
          eyebrow="Logros"
          title="Tus logros están ahora en su propio apartado"
          padding="12px 18px 18px"
          action={
            <Button size="sm" variant="secondary" onClick={() => navigate('/logros?modulo=atlas')}>
              <Icon name="trophy" size={14} /> Ver logros
            </Button>
          }
        >
          <p className="muted" style={{ margin: 0 }}>
            Los de entreno y los de todo Polaris juntos, con el día en que conseguiste cada uno. Están junto a tu perfil.
          </p>
        </Card>
      </div>
    </div>
  );
}
