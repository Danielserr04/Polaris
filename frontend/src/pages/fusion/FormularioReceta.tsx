import { useState, type FormEvent } from 'react';
import { type Alimento } from '../../api/fusion';
import {
  mensajeErrorReceta,
  useActualizarReceta,
  useBorrarReceta,
  useCrearReceta,
  useReceta,
  type RecetaCompleta,
} from '../../api/fusionRecetas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input } from '../../design-system';
import { num } from '../../lib/fechas';
import { BuscadorAlimento } from './BuscadorAlimento';

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado) de esa receta. */
  recetaId?: number;
  onClose: () => void;
}

interface Ingrediente {
  alimentoId: number;
  nombre: string;
  marca: string | null;
  cantidad: string;
  kcal100g: number;
  prot100g: number;
  carb100g: number;
  gras100g: number;
}

const CANTIDAD = /^\d{1,5}([.,]\d{1,2})?$/;
const ENTERO = /^\d{1,2}$/;

export function FormularioReceta({ recetaId, onClose }: Props) {
  useRestaurarFoco();
  const editando = recetaId !== undefined;
  const ficha = useReceta(recetaId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar receta" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeErrorReceta(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo receta={ficha.data} onClose={onClose} />;
}

/** Valor por 100 g a partir del de una cantidad (la ficha trae macros de la cantidad, no por 100 g). */
function por100(valor: number, gramos: number): number {
  return gramos > 0 ? (valor / gramos) * 100 : 0;
}

function Cuerpo({ receta, onClose }: { receta: RecetaCompleta | undefined; onClose: () => void }) {
  const editando = receta !== undefined;
  const crear = useCrearReceta();
  const actualizar = useActualizarReceta(receta?.id ?? 0);
  const borrar = useBorrarReceta(onClose);

  const [nombre, setNombre] = useState(receta?.nombre ?? '');
  const [raciones, setRaciones] = useState(String(receta?.raciones ?? 1));
  const [descripcion, setDescripcion] = useState(receta?.descripcion ?? '');
  const [instrucciones, setInstrucciones] = useState(receta?.instrucciones ?? '');
  const [ingredientes, setIngredientes] = useState<Ingrediente[]>(
    (receta?.ingredientes ?? []).map((i) => ({
      alimentoId: i.alimentoId,
      nombre: i.alimentoNombre,
      marca: i.alimentoMarca ?? null,
      cantidad: String(i.cantidadG).replace('.', ','),
      kcal100g: por100(i.kcal, i.cantidadG),
      prot100g: por100(i.proteinas, i.cantidadG),
      carb100g: por100(i.carbohidratos, i.cantidadG),
      gras100g: por100(i.grasas, i.cantidadG),
    })),
  );
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const cantidadNum = (s: string) => (CANTIDAD.test(s.trim()) ? Number(s.trim().replace(',', '.')) : NaN);
  const racionesNum = ENTERO.test(raciones.trim()) ? Number(raciones.trim()) : NaN;
  const errNombre = !nombre.trim() ? 'Ponle un nombre.' : nombre.trim().length > 120 ? 'Como mucho 120 caracteres.' : null;
  const errRaciones = !(racionesNum >= 1 && racionesNum <= 50) ? 'De 1 a 50.' : null;
  const errCantidad = (s: string) => {
    const n = cantidadNum(s);
    return !(n > 0 && n <= 10000) ? 'Gramos: más de 0 y hasta 10.000.' : null;
  };
  const errIngredientes = ingredientes.length === 0 ? 'Añade al menos un ingrediente.' : ingredientes.length > 50 ? 'Como mucho 50 ingredientes.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const total = ingredientes.reduce(
    (t, i) => {
      const g = cantidadNum(i.cantidad);
      if (!(g > 0)) return t;
      return { kcal: t.kcal + (i.kcal100g * g) / 100, p: t.p + (i.prot100g * g) / 100, c: t.c + (i.carb100g * g) / 100, g: t.g + (i.gras100g * g) / 100 };
    },
    { kcal: 0, p: 0, c: 0, g: 0 },
  );
  const div = racionesNum >= 1 ? racionesNum : 1;

  const anadir = (a: Alimento) => {
    setIngredientes((is) => [
      ...is,
      { alimentoId: a.id, nombre: a.nombre, marca: a.marca, cantidad: '100', kcal100g: a.kcal100g, prot100g: a.proteinas100g, carb100g: a.carbohidratos100g, gras100g: a.grasas100g },
    ]);
  };

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errNombre || errRaciones || errIngredientes || ingredientes.some((i) => errCantidad(i.cantidad))) return;
    const cuerpo = {
      nombre: nombre.trim(),
      raciones: racionesNum,
      descripcion: descripcion.trim() || null,
      instrucciones: instrucciones.trim() || null,
      ingredientes: ingredientes.map((i) => ({ alimentoId: i.alimentoId, cantidadG: cantidadNum(i.cantidad) })),
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeErrorReceta(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarReceta = () => {
    if (!receta) return;
    setErrorEnvio(null);
    borrar.mutate(receta.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeErrorReceta(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      width={600}
      title={editando ? 'Editar receta' : 'Nueva receta'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar esta receta?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarReceta}>
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
            <Button type="submit" form="fus-form-receta" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear receta'}
            </Button>
          </>
        )
      }
    >
      <form id="fus-form-receta" className="fus-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="fus-form__row fus-form__row--rec">
          <Input label="Nombre" autoFocus={!editando} placeholder="Lentejas con verduras" value={nombre} onChange={(e) => setNombre(e.target.value)} error={errNombre} validarAlSalir />
          <Input label="Raciones" inputMode="numeric" hint="Para cuántos sale" value={raciones} onChange={(e) => setRaciones(e.target.value)} error={errRaciones} validarAlSalir />
        </div>
        <Input label="Descripción" placeholder="Opcional" maxLength={500} value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />

        <BuscadorAlimento onElegir={anadir} />

        {ingredientes.length > 0 ? (
          <div className="fus-lineas">
            {ingredientes.map((i, idx) => {
              const n = cantidadNum(i.cantidad);
              return (
                <div key={`${i.alimentoId}-${idx}`} className="fus-linea">
                  <div className="fus-linea__n">
                    <b>{i.nombre}</b>
                    <span>{i.marca ?? `${num(i.kcal100g)} kcal / 100 g`}</span>
                  </div>
                  <Input
                    aria-label={`Gramos de ${i.nombre}`}
                    size="sm"
                    inputMode="decimal"
                    trailing={<span className="muted" style={{ fontSize: 12 }}>g</span>}
                    value={i.cantidad}
                    onChange={(e) => setIngredientes((is) => is.map((x, j) => (j === idx ? { ...x, cantidad: e.target.value } : x)))}
                    error={errCantidad(i.cantidad)}
                    validarAlSalir
                  />
                  <span className="fus-linea__k">{n > 0 ? `${num((i.kcal100g * n) / 100)} kcal` : '—'}</span>
                  <IconButton icon="x" label={`Quitar ${i.nombre}`} variant="ghost" size="sm" type="button" onClick={() => setIngredientes((is) => is.filter((_, j) => j !== idx))} />
                </div>
              );
            })}
            <div className="fus-total">
              <span>Receta entera</span>
              <b>{num(total.kcal)} kcal</b>
            </div>
            <div className="fus-racion">
              <span>Por ración</span>
              <b>{num(total.kcal / div)} kcal</b>
              <span>P {num(total.p / div, 1)} g</span>
              <span>C {num(total.c / div, 1)} g</span>
              <span>G {num(total.g / div, 1)} g</span>
            </div>
          </div>
        ) : (
          <p className="muted" style={{ margin: 0, fontSize: 13 }}>{(intentado && errIngredientes) || 'Busca arriba los ingredientes, con los gramos de la receta entera.'}</p>
        )}

        <div>
          <label htmlFor="fus-rec-inst" className="pl-eyebrow" style={{ display: 'block', marginBottom: 6 }}>
            Preparación
          </label>
          <textarea
            id="fus-rec-inst"
            className="fus-textarea"
            rows={4}
            maxLength={10000}
            placeholder="Opcional: los pasos para hacerla"
            value={instrucciones}
            onChange={(e) => setInstrucciones(e.target.value)}
          />
        </div>
      </form>
    </Dialog>
  );
}
