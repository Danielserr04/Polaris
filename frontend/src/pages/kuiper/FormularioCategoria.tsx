import { useState, type FormEvent } from 'react';
import {
  mensajeError,
  useActualizarCategoria,
  useCrearCategoria,
  type Categoria,
  type GastoCategoria,
  type Presupuesto,
  type TipoMovimiento,
} from '../../api/kuiper';
import { ALERTA_POR_DEFECTO, useBorrarCategoriaConPresupuestos, useGuardarPresupuestoConAlerta } from '../../api/kuiperAlertas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Icon, Input, SegmentedControl } from '../../design-system';
import { eur } from '../../lib/fechas';
import { BarraPresupuesto } from './BarraPresupuesto';

interface Props {
  /** Sin categoria: alta. Con categoria: edicion (y borrado). */
  categoria?: Categoria;
  presupuesto?: Presupuesto;
  presupuestoAnual?: Presupuesto;
  /** Como va este mes, para enseñar el estado del presupuesto mensual */
  gasto?: GastoCategoria;
  tipoInicial: TipoMovimiento;
  onClose: () => void;
}

// Colores del propio sistema (acentos de modulo y estados) para que ninguna categoria rompa la paleta.
const COLORES = ['#5bb3a0', '#d0799f', '#e0b04a', '#e8845a', '#7d95e0', '#cdbf98', '#8fb97a', '#d36b5e'];
const ICONOS = ['wallet', 'house', 'shopping-basket', 'utensils', 'train-front', 'ticket', 'repeat', 'flame', 'dumbbell', 'book-open', 'gamepad-2', 'tv', 'star', 'sparkles'];
const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;

const comoTexto = (p?: Presupuesto) => (p ? String(p.importeLimite).replace('.', ',') : '');

export function FormularioCategoria({ categoria, presupuesto, presupuestoAnual, gasto, tipoInicial, onClose }: Props) {
  useRestaurarFoco();
  const editando = categoria !== undefined;
  const crear = useCrearCategoria();
  const actualizar = useActualizarCategoria(categoria?.id ?? 0);
  const borrar = useBorrarCategoriaConPresupuestos();
  const guardarPresupuesto = useGuardarPresupuestoConAlerta();

  const [tipo, setTipo] = useState<TipoMovimiento>(categoria?.tipo ?? tipoInicial);
  const [nombre, setNombre] = useState(categoria?.nombre ?? '');
  const [color, setColor] = useState<string | null>(categoria?.color ?? null);
  const [icono, setIcono] = useState<string | null>(categoria?.icono ?? null);
  const [limite, setLimite] = useState(comoTexto(presupuesto));
  const [limiteAnual, setLimiteAnual] = useState(comoTexto(presupuestoAnual));
  // Un solo umbral para los dos periodos: el formulario los guarda siempre iguales.
  const [alerta, setAlerta] = useState(presupuesto?.porcentajeAlerta ?? presupuestoAnual?.porcentajeAlerta ?? ALERTA_POR_DEFECTO);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const conPresupuesto = tipo === 'GASTO';
  const limiteVacio = limite.trim() === '';
  const limiteNum = !limiteVacio && IMPORTE.test(limite.trim()) ? Number(limite.trim().replace(',', '.')) : NaN;
  const anualVacio = limiteAnual.trim() === '';
  const anualNum = !anualVacio && IMPORTE.test(limiteAnual.trim()) ? Number(limiteAnual.trim().replace(',', '.')) : NaN;
  const errorNombre = nombre.trim() === '' ? 'Escribe un nombre.' : null;
  const errorLimite = conPresupuesto && !limiteVacio && !(limiteNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const errorAnual = conPresupuesto && !anualVacio && !(anualNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const tienePresupuesto = presupuesto !== undefined || presupuestoAnual !== undefined;
  const ocupado = crear.isPending || actualizar.isPending || guardarPresupuesto.isPending || borrar.isPending;

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    if (errorNombre || errorLimite || errorAnual) return;
    try {
      const cuerpo = { nombre: nombre.trim(), color, icono, tipo };
      const guardada = editando ? await actualizar.mutateAsync(cuerpo) : await crear.mutateAsync(cuerpo);
      if (conPresupuesto) {
        const periodos = [
          { periodo: 'MENSUAL' as const, existente: presupuesto, importe: limiteVacio ? null : limiteNum },
          { periodo: 'ANUAL' as const, existente: presupuestoAnual, importe: anualVacio ? null : anualNum },
        ];
        for (const { periodo, existente, importe } of periodos) {
          // Solo se toca el presupuesto si hay algo que hacer: crear, cambiar o quitar.
          if (importe !== null || existente) {
            await guardarPresupuesto.mutateAsync({ categoriaId: guardada.id, periodo, existente, importe, porcentajeAlerta: alerta });
          }
        }
      }
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeError(e));
    }
  };

  const borrarCategoria = () => {
    if (!categoria) return;
    setErrorEnvio(null);
    borrar.mutate(
      { id: categoria.id, presupuestoIds: [presupuesto?.id, presupuestoAnual?.id].filter((id): id is number => id !== undefined) },
      {
        onSuccess: onClose,
        onError: (e) => {
          setConfirmando(false);
          setErrorEnvio(mensajeError(e));
        },
      },
    );
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      width={conPresupuesto ? 880 : 480}
      title={editando ? 'Editar categoría' : 'Nueva categoría'}
      footer={
        confirmando ? (
          <>
            <span className="muted kui-form__borrar">
              {tienePresupuesto ? '¿Borrar la categoría y su presupuesto?' : '¿Borrar esta categoría?'}
            </span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarCategoria}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" className="kui-form__borrar" disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="kui-form-categoria" loading={ocupado}>
              {editando ? 'Guardar' : 'Añadir'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-categoria" className="pl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className={conPresupuesto ? 'pl-form__cols' : 'pl-form__col'}>
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Categoría</h3>
            {editando ? (
              // El tipo no se cambia: con movimientos o presupuestos asociados el backend lo rechaza.
              <Input label="Tipo" value={tipo === 'GASTO' ? 'Gasto' : 'Ingreso'} locked readOnly />
            ) : (
              <SegmentedControl
                value={tipo}
                onChange={(v) => setTipo(v as TipoMovimiento)}
                options={[
                  { value: 'GASTO', label: 'Gasto' },
                  { value: 'INGRESO', label: 'Ingreso' },
                ]}
              />
            )}
            <Input
              label="Nombre"
              autoFocus
              maxLength={100}
              placeholder={tipo === 'GASTO' ? 'Casa, Comida, Ocio…' : 'Nómina, Ventas…'}
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              error={errorNombre} validarAlSalir
            />
            <div>
              <span className="kui-field__label">Color</span>
              <div className="kui-swatches" role="group" aria-label="Color">
                <button
                  type="button"
                  className="kui-swatch kui-swatch--none"
                  aria-pressed={color === null}
                  aria-label="Sin color"
                  onClick={() => setColor(null)}
                >
                  —
                </button>
                {COLORES.map((c) => (
                  <button
                    key={c}
                    type="button"
                    className="kui-swatch"
                    style={{ ['--c' as string]: c }}
                    aria-pressed={color === c}
                    aria-label={`Color ${c}`}
                    onClick={() => setColor(c)}
                  />
                ))}
              </div>
            </div>
            <div>
              <span className="kui-field__label">Icono</span>
              <div className="kui-icons" role="group" aria-label="Icono">
                {ICONOS.map((n) => (
                  <button key={n} type="button" className="kui-icon" aria-pressed={icono === n} aria-label={n} onClick={() => setIcono(icono === n ? null : n)}>
                    <Icon name={n} size={16} />
                  </button>
                ))}
              </div>
            </div>
          </section>
          {conPresupuesto && (
            <section className="pl-form__sec">
              <h3 className="pl-form__titulo">Presupuesto</h3>
              {presupuesto && gasto && gasto.limiteMensual !== null && (
                <div>
                  <span className="kui-field__label">Este mes</span>
                  <BarraPresupuesto
                    nombre={categoria?.nombre ?? ''}
                    icono={categoria?.icono ?? null}
                    gastado={gasto.gastado}
                    limite={gasto.limiteMensual}
                    porcentaje={gasto.porcentaje}
                    porcentajeAlerta={gasto.porcentajeAlerta}
                    estado={gasto.estado}
                  />
                </div>
              )}
              <div className="pl-form__grid">
                <Input
                  label="Presupuesto mensual (€)"
                  inputMode="decimal"
                  placeholder="Sin presupuesto"
                  value={limite}
                  onChange={(e) => setLimite(e.target.value)}
                  error={errorLimite} validarAlSalir
                  hint="Déjalo vacío para no ponerle límite."
                />
                <Input
                  label="Presupuesto anual (€)"
                  inputMode="decimal"
                  placeholder="Sin presupuesto"
                  value={limiteAnual}
                  onChange={(e) => setLimiteAnual(e.target.value)}
                  error={errorAnual} validarAlSalir
                  hint="Para gastos de todo el año: viajes, seguros…"
                />
              </div>
              <div className="kui-alerta">
                <label className="kui-field__label" htmlFor="kui-alerta">
                  Avisar al llegar al <b className="money">{alerta} %</b>
                </label>
                <input
                  id="kui-alerta"
                  type="range"
                  min={1}
                  max={100}
                  step={1}
                  value={alerta}
                  disabled={limiteVacio && anualVacio}
                  onChange={(e) => setAlerta(Number(e.target.value))}
                />
                <p className="kui-pistas">
                  {limiteNum > 0
                    ? `Con ${eur(limiteNum, 0)} al mes, avisa a partir de ${eur((limiteNum * alerta) / 100, 0)}.`
                    : anualNum > 0
                      ? `Con ${eur(anualNum, 0)} al año, avisa a partir de ${eur((anualNum * alerta) / 100, 0)}.`
                      : 'Ponle un presupuesto para elegir cuándo avisar.'}
                </p>
              </div>
            </section>
          )}
        </div>
      </form>
    </Dialog>
  );
}
