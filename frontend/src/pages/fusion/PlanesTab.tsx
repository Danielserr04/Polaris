import { useState } from 'react';
import { ETIQUETA_MOMENTO, MOMENTOS } from '../../api/fusion';
import { DIAS, ETIQUETA_DIA, gramos, mensajeErrorPlan, useActivarPlan, usePlan, usePlanes, type PlanCompleto } from '../../api/fusionPlanes';
import { Alert, Badge, Button, Card } from '../../design-system';
import { num } from '../../lib/fechas';
import { ListaCompra } from './ListaCompra';

interface Props {
  onEditar: (id: number) => void;
  onNuevo: () => void;
}

export function PlanesTab({ onEditar, onNuevo }: Props) {
  const planes = usePlanes();
  const [elegido, setElegido] = useState<number | null>(null);

  if (planes.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar los planes"
        action={
          <Button size="sm" variant="secondary" onClick={() => void planes.refetch()}>
            Reintentar
          </Button>
        }
      >
        {mensajeErrorPlan(planes.error)}
      </Alert>
    );
  }
  if (planes.isPending) return <p className="muted">Cargando…</p>;
  if (planes.data.length === 0) {
    return (
      <Card>
        <div className="fus-vacio">
          Aún no tienes planes. Organiza qué comer cada día de la semana y saca la lista de la compra de un clic.{' '}
          <Button size="sm" variant="ghost" icon="plus" onClick={onNuevo}>
            Crear el primero
          </Button>
        </div>
      </Card>
    );
  }

  // El elegido si sigue existiendo; si no, el activo (el listado lo trae primero).
  const id = planes.data.some((p) => p.id === elegido) ? (elegido as number) : planes.data[0].id;

  return (
    <div className="grid">
      <div className="span-4">
        <Card eyebrow="Tus planes" title={`${planes.data.length} ${planes.data.length === 1 ? 'plan' : 'planes'}`} padding="4px 0 8px">
          {planes.data.map((p, i) => (
            <button
              key={p.id}
              type="button"
              className={'fus-meal pl-rise' + (p.id === id ? ' fus-plan--sel' : '')}
              style={{ animationDelay: i * 40 + 'ms' }}
              aria-pressed={p.id === id}
              onClick={() => setElegido(p.id)}
            >
              <div className="fus-meal-h">
                <b>{p.nombre}</b>
                {p.activo && <Badge tone="success">Activo</Badge>}
              </div>
              <span className="muted" style={{ fontSize: 12.5 }}>
                {p.diasConLineas === 0 ? 'Vacío' : `${num(p.kcalMediaDiaria)} kcal/día de media · ${p.diasConLineas} ${p.diasConLineas === 1 ? 'día' : 'días'}`}
              </span>
            </button>
          ))}
        </Card>
      </div>
      <div className="span-8">
        <DetallePlan key={id} id={id} onEditar={() => onEditar(id)} />
      </div>
    </div>
  );
}

function DetallePlan({ id, onEditar }: { id: number; onEditar: () => void }) {
  const plan = usePlan(id);
  const activar = useActivarPlan();
  const [compra, setCompra] = useState(false);

  if (plan.isError) return <Alert tone="danger">{mensajeErrorPlan(plan.error)}</Alert>;
  if (plan.isPending) return <p className="muted">Cargando…</p>;
  const p = plan.data;

  return (
    <Card
      eyebrow={p.activo ? 'Plan activo' : 'Plan'}
      title={p.nombre}
      action={
        <span className="fus-dia">
          {!p.activo && (
            <Button size="sm" variant="ghost" icon="check" loading={activar.isPending} onClick={() => activar.mutate(p.id)}>
              Activar
            </Button>
          )}
          <Button size="sm" variant="ghost" icon="shopping-basket" onClick={() => setCompra(true)}>
            Lista de la compra
          </Button>
          <Button size="sm" variant="secondary" onClick={onEditar}>
            Editar
          </Button>
        </span>
      }
    >
      {p.lineas.length === 0 ? (
        <p className="muted" style={{ margin: 0 }}>
          Este plan está vacío.{' '}
          <Button size="sm" variant="ghost" onClick={onEditar}>
            Añadir comidas
          </Button>
        </p>
      ) : (
        <>
          <div className="fus-plan-media">
            <span>Media diaria</span>
            <b>{num(p.kcalMediaDiaria)} kcal</b>
            <span>P {num(p.proteinasMediaDiaria)} g</span>
            <span>C {num(p.carbohidratosMediaDiaria)} g</span>
            <span>G {num(p.grasasMediaDiaria)} g</span>
          </div>
          <Semana plan={p} />
        </>
      )}
      {compra && <ListaCompra planId={p.id} nombre={p.nombre} onClose={() => setCompra(false)} />}
    </Card>
  );
}

function Semana({ plan }: { plan: PlanCompleto }) {
  return (
    <div className="fus-semana">
      {DIAS.map((d) => {
        const lineas = plan.lineas.filter((l) => l.diaSemana === d);
        const kcal = lineas.reduce((s, l) => s + l.kcal, 0);
        return (
          <div key={d} className={'fus-semana__dia' + (lineas.length ? '' : ' fus-semana__dia--vacio')}>
            <div className="fus-meal-h">
              <b>{ETIQUETA_DIA[d]}</b>
              {lineas.length > 0 && <span className="money">{num(kcal)} kcal</span>}
            </div>
            {lineas.length === 0 ? (
              <span className="fus-vacio">Sin plan</span>
            ) : (
              MOMENTOS.filter((m) => lineas.some((l) => l.momento === m)).map((m) => (
                <div key={m} className="fus-semana__mom">
                  <span className="pl-eyebrow">{ETIQUETA_MOMENTO[m]}</span>
                  {lineas
                    .filter((l) => l.momento === m)
                    .map((l) => (
                      <div key={l.id} className="fus-semana__l">
                        <span>{l.recetaNombre ?? l.alimentoNombre}</span>
                        <span className="pl-row__num">
                          {l.recetaId != null ? `${num(l.raciones ?? 0, (l.raciones ?? 0) % 1 ? 1 : 0)} rac.` : gramos(l.cantidadG ?? 0)}
                        </span>
                      </div>
                    ))}
                </div>
              ))
            )}
          </div>
        );
      })}
    </div>
  );
}
