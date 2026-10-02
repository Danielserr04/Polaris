import { useMemo, useState } from 'react';
import { mensajeErrorMeta, useMetas, type MetaAhorro } from '../../api/kuiperMetas';
import { Alert, Badge, Button, Card, Icon, ProgressBar, RingChart, Stat } from '../../design-system';
import { deIso, eur } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { DialogoAportaciones } from './DialogoAportaciones';
import { FormularioMeta } from './FormularioMeta';
import './metas.css';

// Meta abierta en el formulario: nueva (sin meta) o editando una.
type Editando = { meta?: MetaAhorro } | null;

/** "Quedan 23 días", "Vence hoy", "Venció hace 4 días" */
function textoPlazo(dias: number): string {
  if (dias === 0) return 'Vence hoy';
  if (dias < 0) return `Venció hace ${-dias} ${dias === -1 ? 'día' : 'días'}`;
  return `Quedan ${dias} ${dias === 1 ? 'día' : 'días'}`;
}

function fechaCorta(s: string): string {
  return deIso(s).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' });
}

export function MetasTab() {
  const metas = useMetas();
  const [editando, setEditando] = useState<Editando>(null);
  const [aportandoId, setAportandoId] = useState<number | null>(null);

  const lista = metas.data ?? [];
  const totales = useMemo(
    () => ({
      ahorrado: lista.reduce((s, m) => s + m.importeActual, 0),
      objetivo: lista.reduce((s, m) => s + m.importeObjetivo, 0),
      completadas: lista.filter((m) => m.completada).length,
      mensual: lista.reduce((s, m) => s + (m.ahorroMensualNecesario ?? 0), 0),
    }),
    [lista],
  );
  // Se busca en los datos frescos: tras aportar, el dialogo ve el total nuevo.
  const aportando = aportandoId === null ? undefined : lista.find((m) => m.id === aportandoId);

  return (
    <>
      <div className="kui-toolbar">
        <Button variant="secondary" icon="plus" style={{ marginLeft: 'auto' }} onClick={() => setEditando({})}>
          Meta
        </Button>
      </div>

      {metas.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar las metas"
          action={
            <Button size="sm" variant="secondary" onClick={() => void metas.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeErrorMeta(metas.error)}
        </Alert>
      ) : metas.isPending ? (
        <p className="muted">Cargando…</p>
      ) : lista.length === 0 ? (
        <Card>
          <div className="kui-vacio">
            Aún no tienes metas de ahorro. Crea una para ver cuánto llevas y cuánto apartar cada mes.
          </div>
        </Card>
      ) : (
        <>
          <div className="kui-metas__resumen">
            <Card>
              <Stat label="Ahorrado" value={totales.ahorrado} decimals={2} unit="€" caption={`de ${eur(totales.objetivo, 0)}`} />
            </Card>
            <Card>
              <Stat label="Al mes para llegar" value={totales.mensual} decimals={2} unit="€" caption="Metas con fecha" />
            </Card>
            <Card>
              <Stat label="Completadas" value={totales.completadas} caption={`de ${lista.length} ${lista.length === 1 ? 'meta' : 'metas'}`} />
            </Card>
          </div>

          <div className="kui-metas">
            {lista.map((m, i) => (
              <Card
                key={m.id}
                delay={Math.min(i, 12) * 40}
                className="kui-meta"
                footer={
                  <Button variant="secondary" size="sm" icon="wallet" block onClick={() => setAportandoId(m.id)}>
                    Aportar o retirar
                  </Button>
                }
                action={
                  <Button
                    variant="ghost"
                    size="sm"
                    aria-label={`Editar ${m.nombre}`}
                    onClick={() => setEditando({ meta: m })}
                  >
                    Editar
                  </Button>
                }
                title={
                  <span className="kui-meta__titulo">
                    <span className="kui-catchip" style={m.color ? ({ ['--c' as string]: m.color }) : undefined}>
                      <Icon name={iconoOr(m.icono, 'star')} size={16} />
                    </span>
                    <span className="kui-meta__nombre">{m.nombre}</span>
                  </span>
                }
              >
                <div className="kui-meta__cuerpo">
                  <RingChart
                    value={Math.min(m.importeActual, m.importeObjetivo)}
                    max={m.importeObjetivo}
                    size={88}
                    color={m.color ?? undefined}
                    label={`${Math.floor(m.porcentaje)}%`}
                  />
                  <div className="kui-meta__datos">
                    <span className="money kui-meta__actual">{eur(m.importeActual)}</span>
                    <span className="muted">de {eur(m.importeObjetivo)}</span>
                    {m.completada ? (
                      <Badge tone="success">Completada</Badge>
                    ) : (
                      <span className="muted">Faltan {eur(m.restante)}</span>
                    )}
                  </div>
                </div>
                <ProgressBar
                  value={Math.min(m.importeActual, m.importeObjetivo)}
                  max={m.importeObjetivo}
                  color={m.color ?? undefined}
                  valueLabel={false}
                />
                <div className="kui-meta__pie">
                  {m.fechaLimite && m.diasRestantes !== null ? (
                    <>
                      <span>
                        <Icon name="calendar" size={13} /> {fechaCorta(m.fechaLimite)} · {textoPlazo(m.diasRestantes)}
                      </span>
                      {m.ahorroMensualNecesario !== null && (
                        <span className="money">{eur(m.ahorroMensualNecesario)}/mes</span>
                      )}
                    </>
                  ) : (
                    <span className="muted">Sin fecha límite</span>
                  )}
                </div>
              </Card>
            ))}
          </div>
        </>
      )}

      {editando && (
        <FormularioMeta
          meta={editando.meta}
          onClose={() => setEditando(null)}
        />
      )}
      {aportando && <DialogoAportaciones meta={aportando} onClose={() => setAportandoId(null)} />}
    </>
  );
}
