import { useState, type FormEvent } from 'react';
import {
  ETIQUETA_MOMENTO,
  mensajeError,
  MOMENTOS,
  useActualizarComida,
  useBorrarComida,
  useComida,
  useCrearComida,
  type Alimento,
  type ComidaCompleta,
  type MomentoComida,
} from '../../api/fusion';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input, SegmentedControl } from '../../design-system';
import { iso, num } from '../../lib/fechas';
import { AnadirReceta, type IngredienteEscalado } from './AnadirReceta';
import { BuscadorAlimento } from './BuscadorAlimento';

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado) de esa comida. */
  comidaId?: number;
  momentoInicial?: MomentoComida;
  /** Dia que se esta viendo: la comida nueva se apunta a ese dia. */
  fecha: string;
  onClose: () => void;
}

interface Linea {
  alimentoId: number;
  nombre: string;
  marca: string | null;
  cantidad: string;
  kcal100g: number;
}

const CANTIDAD = /^\d{1,5}([.,]\d{1,2})?$/;

function momentoSugerido(): MomentoComida {
  const h = new Date().getHours();
  return h < 11 ? 'DESAYUNO' : h < 17 ? 'COMIDA' : h < 20 ? 'SNACK' : 'CENA';
}

export function FormularioComida({ comidaId, momentoInicial, fecha, onClose }: Props) {
  useRestaurarFoco();
  const editando = comidaId !== undefined;
  const ficha = useComida(comidaId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar comida" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeError(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo comida={ficha.data} momentoInicial={momentoInicial} fecha={fecha} onClose={onClose} />;
}

function Cuerpo({ comida, momentoInicial, fecha, onClose }: { comida: ComidaCompleta | undefined; momentoInicial?: MomentoComida; fecha: string; onClose: () => void }) {
  const editando = comida !== undefined;
  const hoy = iso(new Date());
  const crear = useCrearComida();
  const actualizar = useActualizarComida(comida?.id ?? 0);
  const borrar = useBorrarComida(onClose);

  const [momento, setMomento] = useState<MomentoComida>(comida?.momento ?? momentoInicial ?? momentoSugerido());
  const [dia, setDia] = useState(comida?.fecha ?? fecha);
  const [lineas, setLineas] = useState<Linea[]>(
    (comida?.lineas ?? []).map((l) => ({
      alimentoId: l.alimentoId,
      nombre: l.alimentoNombre,
      marca: l.alimentoMarca,
      cantidad: String(l.cantidadG).replace('.', ','),
      kcal100g: l.cantidadG > 0 ? (l.kcal / l.cantidadG) * 100 : 0,
    })),
  );
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const cantidadNum = (s: string) => (CANTIDAD.test(s.trim()) ? Number(s.trim().replace(',', '.')) : NaN);
  const errCantidad = (s: string) => {
    const n = cantidadNum(s);
    return !(n > 0 && n <= 10000) ? 'Gramos: más de 0 y hasta 10.000.' : null;
  };
  const errLineas = lineas.length === 0 ? 'Añade al menos un alimento.' : lineas.length > 50 ? 'Como mucho 50 alimentos.' : null;
  const errDia = !dia ? 'Elige una fecha.' : dia > hoy ? 'No puede ser una fecha futura.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;
  const kcalTotal = lineas.reduce((s, l) => s + (cantidadNum(l.cantidad) > 0 ? (l.kcal100g * cantidadNum(l.cantidad)) / 100 : 0), 0);
  const ver = (e: string | null) => (intentado ? e : null);

  const anadir = (a: Alimento) => {
    setLineas((ls) => [...ls, { alimentoId: a.id, nombre: a.nombre, marca: a.marca, cantidad: '100', kcal100g: a.kcal100g }]);
  };

  const anadirReceta = (ingredientes: IngredienteEscalado[]) => {
    setLineas((ls) => [
      ...ls,
      ...ingredientes.map(({ ingrediente: i, cantidadG }) => ({
        alimentoId: i.alimentoId,
        nombre: i.alimentoNombre,
        marca: i.alimentoMarca ?? null,
        cantidad: String(cantidadG).replace('.', ','),
        kcal100g: i.cantidadG > 0 ? (i.kcal / i.cantidadG) * 100 : 0,
      })),
    ]);
  };

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errLineas || errDia || lineas.some((l) => errCantidad(l.cantidad))) return;
    const cuerpo = {
      fecha: dia,
      momento,
      lineas: lineas.map((l) => ({ alimentoId: l.alimentoId, cantidadG: cantidadNum(l.cantidad) })),
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarComida = () => {
    if (!comida) return;
    setErrorEnvio(null);
    borrar.mutate(comida.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeError(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      width={920}
      title={editando ? 'Editar comida' : 'Registrar comida'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar esta comida?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarComida}>
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
            <Button type="submit" form="fus-form-comida" loading={ocupado}>
              {editando ? 'Guardar' : 'Registrar'}
            </Button>
          </>
        )
      }
    >
      <form id="fus-form-comida" className="pl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="pl-form__cols">
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Comida</h3>
            <SegmentedControl
              value={momento}
              onChange={(v) => setMomento(v as MomentoComida)}
              options={MOMENTOS.map((m) => ({ value: m, label: ETIQUETA_MOMENTO[m] }))}
            />
            <Input label="Fecha" type="date" max={hoy} value={dia} onChange={(e) => setDia(e.target.value)} error={errDia} validarAlSalir />

            <BuscadorAlimento onElegir={anadir} />
            <AnadirReceta onAnadir={anadirReceta} />
          </section>

          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Lo que has comido</h3>
            {lineas.length > 0 ? (
              <div className="fus-lineas">
                {lineas.map((l, i) => {
                  const n = cantidadNum(l.cantidad);
                  return (
                    <div key={`${l.alimentoId}-${i}`} className="fus-linea">
                      <div className="fus-linea__n">
                        <b>{l.nombre}</b>
                        <span>{l.marca ?? `${num(l.kcal100g)} kcal / 100 g`}</span>
                      </div>
                      <Input
                        aria-label={`Gramos de ${l.nombre}`}
                        size="sm"
                        inputMode="decimal"
                        trailing={<span className="muted" style={{ fontSize: 12 }}>g</span>}
                        value={l.cantidad}
                        onChange={(e) => setLineas((ls) => ls.map((x, j) => (j === i ? { ...x, cantidad: e.target.value } : x)))}
                        error={errCantidad(l.cantidad)} validarAlSalir
                      />
                      <span className="fus-linea__k">{n > 0 ? `${num((l.kcal100g * n) / 100)} kcal` : '—'}</span>
                      <IconButton icon="x" label={`Quitar ${l.nombre}`} variant="ghost" size="sm" type="button" onClick={() => setLineas((ls) => ls.filter((_, j) => j !== i))} />
                    </div>
                  );
                })}
                <div className="fus-total">
                  <span>Total</span>
                  <b>{num(kcalTotal)} kcal</b>
                </div>
              </div>
            ) : (
              <p className="muted" style={{ margin: 0, fontSize: 13 }}>{ver(errLineas) ?? 'Busca arriba lo que has comido.'}</p>
            )}
          </section>
        </div>
      </form>
    </Dialog>
  );
}
