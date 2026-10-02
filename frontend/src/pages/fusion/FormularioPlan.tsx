import { useMemo, useState, type FormEvent } from 'react';
import { ETIQUETA_MOMENTO, MOMENTOS, type Alimento, type MomentoComida } from '../../api/fusion';
import {
  DIAS,
  ETIQUETA_DIA,
  INICIAL_DIA,
  mensajeErrorPlan,
  useActualizarPlan,
  useBorrarPlan,
  useCrearPlan,
  usePlan,
  type DiaSemana,
  type PlanCompleto,
} from '../../api/fusionPlanes';
import { useRecetas } from '../../api/fusionRecetas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input, SegmentedControl, Select } from '../../design-system';
import { num } from '../../lib/fechas';
import { BuscadorAlimento } from './BuscadorAlimento';

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado) de ese plan. */
  planId?: number;
  onClose: () => void;
}

interface Linea {
  clave: number;
  dia: DiaSemana;
  momento: MomentoComida;
  tipo: 'alimento' | 'receta';
  refId: number;
  nombre: string;
  /** Gramos (alimento) o raciones (receta), tal y como se escriben. */
  cantidad: string;
  /** kcal por gramo (alimento) o por racion (receta). */
  kcalUnidad: number;
}

const GRAMOS = /^\d{1,5}([.,]\d{1,2})?$/;
const RACIONES = /^\d{1,2}([.,]\d{1,2})?$/;

let siguienteClave = 1;

function desdeFicha(plan: PlanCompleto | undefined): Linea[] {
  return (plan?.lineas ?? []).map((l) => {
    const receta = l.recetaId != null;
    const cantidad = receta ? (l.raciones ?? 1) : (l.cantidadG ?? 0);
    return {
      clave: siguienteClave++,
      dia: l.diaSemana,
      momento: l.momento,
      tipo: receta ? 'receta' : 'alimento',
      refId: (receta ? l.recetaId : l.alimentoId) as number,
      nombre: (receta ? l.recetaNombre : l.alimentoNombre) ?? '',
      cantidad: String(cantidad).replace('.', ','),
      kcalUnidad: cantidad > 0 ? l.kcal / cantidad : 0,
    };
  });
}

export function FormularioPlan({ planId, onClose }: Props) {
  useRestaurarFoco();
  const editando = planId !== undefined;
  const ficha = usePlan(planId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar plan" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeErrorPlan(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo plan={ficha.data} onClose={onClose} />;
}

function Cuerpo({ plan, onClose }: { plan: PlanCompleto | undefined; onClose: () => void }) {
  const editando = plan !== undefined;
  const crear = useCrearPlan();
  const actualizar = useActualizarPlan(plan?.id ?? 0);
  const borrar = useBorrarPlan(onClose);
  const recetas = useRecetas();

  const [nombre, setNombre] = useState(plan?.nombre ?? '');
  const [lineas, setLineas] = useState<Linea[]>(() => desdeFicha(plan));
  const [dia, setDia] = useState<DiaSemana>('LUNES');
  const [momento, setMomento] = useState<MomentoComida>('DESAYUNO');
  const [copiarA, setCopiarA] = useState('');
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const cantidadNum = (l: Linea) => {
    const s = l.cantidad.trim();
    return (l.tipo === 'alimento' ? GRAMOS : RACIONES).test(s) ? Number(s.replace(',', '.')) : NaN;
  };
  const errCantidad = (l: Linea) => {
    const n = cantidadNum(l);
    if (l.tipo === 'alimento') return !(n > 0 && n <= 10000) ? 'Más de 0 y hasta 10.000 g.' : null;
    return !(n > 0 && n <= 50) ? 'Más de 0 y hasta 50.' : null;
  };
  const kcalDe = (l: Linea) => {
    const n = cantidadNum(l);
    return n > 0 ? l.kcalUnidad * n : 0;
  };
  const errNombre = !nombre.trim() ? 'Ponle un nombre.' : nombre.trim().length > 120 ? 'Como mucho 120 caracteres.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const delDia = lineas.filter((l) => l.dia === dia);
  const kcalDia = delDia.reduce((s, l) => s + kcalDe(l), 0);
  const porDia = useMemo(() => {
    const m = new Map<DiaSemana, number>();
    for (const l of lineas) m.set(l.dia, (m.get(l.dia) ?? 0) + 1);
    return m;
  }, [lineas]);

  const anadirAlimento = (a: Alimento) =>
    setLineas((ls) => [...ls, { clave: siguienteClave++, dia, momento, tipo: 'alimento', refId: a.id, nombre: a.nombre, cantidad: '100', kcalUnidad: a.kcal100g / 100 }]);

  const anadirReceta = (id: string) => {
    const r = recetas.data?.find((x) => String(x.id) === id);
    if (!r) return;
    setLineas((ls) => [...ls, { clave: siguienteClave++, dia, momento, tipo: 'receta', refId: r.id, nombre: r.nombre, cantidad: '1', kcalUnidad: r.kcalRacion }]);
  };

  const copiarDia = (destino: string) => {
    setCopiarA('');
    if (!destino) return;
    const destinos = destino === 'TODOS' ? DIAS.filter((d) => d !== dia) : [destino as DiaSemana];
    setLineas((ls) => [
      ...ls.filter((l) => !destinos.includes(l.dia)),
      ...destinos.flatMap((d) => ls.filter((l) => l.dia === dia).map((l) => ({ ...l, clave: siguienteClave++, dia: d }))),
    ]);
  };

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    const malas = lineas.filter((l) => errCantidad(l));
    if (malas.length && !errNombre) {
      // Lleva al dia de la primera linea mala para que el error se vea.
      setDia(malas[0].dia);
    }
    if (errNombre || malas.length) return;
    if (lineas.length > 200) {
      setErrorEnvio('Como mucho 200 líneas por plan.');
      return;
    }
    const cuerpo = {
      nombre: nombre.trim(),
      lineas: lineas.map((l) => ({
        diaSemana: l.dia,
        momento: l.momento,
        alimentoId: l.tipo === 'alimento' ? l.refId : null,
        cantidadG: l.tipo === 'alimento' ? cantidadNum(l) : null,
        recetaId: l.tipo === 'receta' ? l.refId : null,
        raciones: l.tipo === 'receta' ? cantidadNum(l) : null,
      })),
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeErrorPlan(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarPlan = () => {
    if (!plan) return;
    setErrorEnvio(null);
    borrar.mutate(plan.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeErrorPlan(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      width={680}
      title={editando ? 'Editar plan' : 'Nuevo plan de comidas'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar este plan?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarPlan}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" style={{ marginRight: 'auto' }} disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="fus-form-plan" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear plan'}
            </Button>
          </>
        )
      }
    >
      <form id="fus-form-plan" className="fus-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <Input label="Nombre" autoFocus={!editando} placeholder="Semana de definición" value={nombre} onChange={(e) => setNombre(e.target.value)} error={errNombre} validarAlSalir />

        <div className="fus-plan-dias">
          <SegmentedControl
            value={dia}
            onChange={(v) => setDia(v as DiaSemana)}
            options={DIAS.map((d) => ({ value: d, label: <abbr title={ETIQUETA_DIA[d]}>{INICIAL_DIA[d]}</abbr>, count: porDia.get(d) }))}
          />
        </div>

        <div className="fus-plan-cab">
          <b>{ETIQUETA_DIA[dia]}</b>
          <span className="money">{num(kcalDia)} kcal</span>
          <div style={{ marginLeft: 'auto', width: 190 }}>
            <Select
              size="sm"
              aria-label="Copiar este día a"
              value={copiarA}
              disabled={delDia.length === 0}
              onChange={(e) => copiarDia(e.target.value)}
              options={[
                { value: '', label: 'Copiar este día a…' },
                { value: 'TODOS', label: 'Todos los demás días' },
                ...DIAS.filter((d) => d !== dia).map((d) => ({ value: d, label: ETIQUETA_DIA[d] })),
              ]}
            />
          </div>
        </div>

        <div className="fus-plan-anadir">
          <SegmentedControl value={momento} onChange={(v) => setMomento(v as MomentoComida)} options={MOMENTOS.map((m) => ({ value: m, label: ETIQUETA_MOMENTO[m] }))} />
          <div className="fus-form__row">
            <BuscadorAlimento onElegir={anadirAlimento} />
            <Select
              label="O una receta"
              value=""
              disabled={!recetas.data?.length}
              onChange={(e) => anadirReceta(e.target.value)}
              options={[
                { value: '', label: recetas.data?.length ? 'Elige una receta…' : 'Aún no tienes recetas' },
                ...(recetas.data ?? []).map((r) => ({ value: String(r.id), label: `${r.nombre} · ${num(r.kcalRacion)} kcal/ración` })),
              ]}
            />
          </div>
        </div>

        {delDia.length === 0 ? (
          <p className="muted" style={{ margin: 0, fontSize: 13 }}>Nada planeado para el {ETIQUETA_DIA[dia].toLowerCase()}. Elige el momento y añade alimentos o recetas.</p>
        ) : (
          MOMENTOS.filter((m) => delDia.some((l) => l.momento === m)).map((m) => (
            <div key={m} className="fus-lineas">
              <span className="pl-eyebrow">{ETIQUETA_MOMENTO[m]}</span>
              {delDia
                .filter((l) => l.momento === m)
                .map((l) => (
                  <div key={l.clave} className="fus-linea">
                    <div className="fus-linea__n">
                      <b>{l.nombre}</b>
                      <span>{l.tipo === 'receta' ? `Receta · ${num(l.kcalUnidad)} kcal/ración` : `${num(l.kcalUnidad * 100)} kcal / 100 g`}</span>
                    </div>
                    <Input
                      aria-label={l.tipo === 'receta' ? `Raciones de ${l.nombre}` : `Gramos de ${l.nombre}`}
                      size="sm"
                      inputMode="decimal"
                      trailing={<span className="muted" style={{ fontSize: 12 }}>{l.tipo === 'receta' ? 'rac.' : 'g'}</span>}
                      value={l.cantidad}
                      onChange={(e) => setLineas((ls) => ls.map((x) => (x.clave === l.clave ? { ...x, cantidad: e.target.value } : x)))}
                      error={errCantidad(l)}
                      validarAlSalir
                    />
                    <span className="fus-linea__k">{num(kcalDe(l))} kcal</span>
                    <IconButton icon="x" label={`Quitar ${l.nombre}`} variant="ghost" size="sm" type="button" onClick={() => setLineas((ls) => ls.filter((x) => x.clave !== l.clave))} />
                  </div>
                ))}
            </div>
          ))
        )}
      </form>
    </Dialog>
  );
}
