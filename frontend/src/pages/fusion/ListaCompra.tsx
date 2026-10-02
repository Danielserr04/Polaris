import { useEffect, useState } from 'react';
import { gramos, mensajeErrorPlan, useListaCompra, type ArticuloCompra } from '../../api/fusionPlanes';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Checkbox, Dialog } from '../../design-system';
import { avisar } from '../../lib/avisos';

interface Props {
  planId: number;
  nombre: string;
  onClose: () => void;
}

// Lo ya cogido se recuerda en este navegador, por plan. Si el almacenamiento no esta
// disponible (modo privado), simplemente no se recuerda.
const clave = (planId: number) => `polaris.fusion.compra.${planId}`;

function leerCogidos(planId: number): number[] {
  try {
    return JSON.parse(localStorage.getItem(clave(planId)) ?? '[]') as number[];
  } catch {
    return [];
  }
}

function texto(lista: ArticuloCompra[]): string {
  return lista.map((a) => `- ${a.nombre}${a.marca ? ` (${a.marca})` : ''}: ${gramos(a.cantidadG)}`).join('\n');
}

/** Lo que hay que comprar para toda la semana del plan, con las recetas ya desplegadas. */
export function ListaCompra({ planId, nombre, onClose }: Props) {
  useRestaurarFoco();
  const lista = useListaCompra(planId);
  const [cogidos, setCogidos] = useState<number[]>(() => leerCogidos(planId));

  useEffect(() => {
    try {
      localStorage.setItem(clave(planId), JSON.stringify(cogidos));
    } catch {
      // sin almacenamiento: no se recuerda
    }
  }, [cogidos, planId]);

  const marcar = (id: number, si: boolean) => setCogidos((c) => (si ? [...c, id] : c.filter((x) => x !== id)));

  const copiar = async () => {
    if (!lista.data) return;
    try {
      await navigator.clipboard.writeText(`${nombre}\n${texto(lista.data)}`);
      avisar('Lista copiada');
    } catch {
      avisar('No se ha podido copiar');
    }
  };

  const pendientes = lista.data?.filter((a) => !cogidos.includes(a.alimentoId)).length ?? 0;

  return (
    <Dialog
      open
      onClose={onClose}
      width={480}
      title="Lista de la compra"
      footer={
        <>
          {cogidos.length > 0 && (
            <Button variant="ghost" type="button" style={{ marginRight: 'auto' }} onClick={() => setCogidos([])}>
              Desmarcar todo
            </Button>
          )}
          <Button variant="secondary" type="button" icon="copy" disabled={!lista.data?.length} onClick={() => void copiar()}>
            Copiar
          </Button>
          <Button type="button" onClick={onClose}>
            Cerrar
          </Button>
        </>
      }
    >
      {lista.isError ? (
        <Alert tone="danger">{mensajeErrorPlan(lista.error)}</Alert>
      ) : lista.isPending ? (
        <p className="muted" style={{ margin: 0 }}>Cargando…</p>
      ) : lista.data.length === 0 ? (
        <p className="muted" style={{ margin: 0 }}>El plan está vacío: añade comidas para tener lista de la compra.</p>
      ) : (
        <div className="fus-form">
          <p className="muted" style={{ margin: 0, fontSize: 13 }}>
            Para toda la semana de «{nombre}». {pendientes === 0 ? 'Ya lo tienes todo.' : `Te faltan ${pendientes}.`}
          </p>
          <div className="fus-compra">
            {lista.data.map((a) => {
              const cogido = cogidos.includes(a.alimentoId);
              return (
                <div key={a.alimentoId} className={'fus-compra__r' + (cogido ? ' fus-compra__r--cogido' : '')}>
                  <Checkbox
                    checked={cogido}
                    onChange={(si) => marcar(a.alimentoId, si)}
                    label={
                      <span>
                        {a.nombre}
                        {a.marca && <span className="muted"> · {a.marca}</span>}
                      </span>
                    }
                  />
                  <span className="money">{gramos(a.cantidadG)}</span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </Dialog>
  );
}
