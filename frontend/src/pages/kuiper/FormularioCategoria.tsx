import { useState, type FormEvent } from 'react';
import {
  mensajeError,
  useActualizarCategoria,
  useBorrarCategoria,
  useCrearCategoria,
  useGuardarPresupuesto,
  type Categoria,
  type Presupuesto,
  type TipoMovimiento,
} from '../../api/kuiper';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Icon, Input, SegmentedControl } from '../../design-system';

interface Props {
  /** Sin categoria: alta. Con categoria: edicion (y borrado). */
  categoria?: Categoria;
  presupuesto?: Presupuesto;
  tipoInicial: TipoMovimiento;
  onClose: () => void;
}

// Colores del propio sistema (acentos de modulo y estados) para que ninguna categoria rompa la paleta.
const COLORES = ['#5bb3a0', '#d0799f', '#e0b04a', '#e8845a', '#7d95e0', '#cdbf98', '#8fb97a', '#d36b5e'];
const ICONOS = ['wallet', 'house', 'shopping-basket', 'utensils', 'train-front', 'ticket', 'repeat', 'flame', 'dumbbell', 'book-open', 'gamepad-2', 'tv', 'star', 'sparkles'];
const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;

export function FormularioCategoria({ categoria, presupuesto, tipoInicial, onClose }: Props) {
  useRestaurarFoco();
  const editando = categoria !== undefined;
  const crear = useCrearCategoria();
  const actualizar = useActualizarCategoria(categoria?.id ?? 0);
  const borrar = useBorrarCategoria();
  const guardarPresupuesto = useGuardarPresupuesto();

  const [tipo, setTipo] = useState<TipoMovimiento>(categoria?.tipo ?? tipoInicial);
  const [nombre, setNombre] = useState(categoria?.nombre ?? '');
  const [color, setColor] = useState<string | null>(categoria?.color ?? null);
  const [icono, setIcono] = useState<string | null>(categoria?.icono ?? null);
  const [limite, setLimite] = useState(presupuesto ? String(presupuesto.importeLimite).replace('.', ',') : '');
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const conPresupuesto = tipo === 'GASTO';
  const limiteVacio = limite.trim() === '';
  const limiteNum = !limiteVacio && IMPORTE.test(limite.trim()) ? Number(limite.trim().replace(',', '.')) : NaN;
  const errorNombre = nombre.trim() === '' ? 'Escribe un nombre.' : null;
  const errorLimite = conPresupuesto && !limiteVacio && !(limiteNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const ocupado = crear.isPending || actualizar.isPending || guardarPresupuesto.isPending || borrar.isPending;
  const ver = (e: string | null) => (intentado ? e : null);

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errorNombre || errorLimite) return;
    try {
      const cuerpo = { nombre: nombre.trim(), color, icono, tipo };
      const guardada = editando ? await actualizar.mutateAsync(cuerpo) : await crear.mutateAsync(cuerpo);
      if (conPresupuesto) {
        const importe = limiteVacio ? null : limiteNum;
        // Solo se toca el presupuesto si hay algo que hacer: crear, cambiar o quitar.
        if (importe !== null || presupuesto) {
          await guardarPresupuesto.mutateAsync({ categoriaId: guardada.id, existente: presupuesto, importe });
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
      { id: categoria.id, presupuestoId: presupuesto?.id },
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
      title={editando ? 'Editar categoría' : 'Nueva categoría'}
      footer={
        confirmando ? (
          <>
            <span className="muted kui-form__borrar">
              {presupuesto ? '¿Borrar la categoría y su presupuesto?' : '¿Borrar esta categoría?'}
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
      <form id="kui-form-categoria" className="kui-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
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
          error={ver(errorNombre)}
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
        {conPresupuesto && (
          <Input
            label="Presupuesto mensual (€)"
            inputMode="decimal"
            placeholder="Sin presupuesto"
            value={limite}
            onChange={(e) => setLimite(e.target.value)}
            error={ver(errorLimite)}
            hint="Déjalo vacío para no ponerle límite."
          />
        )}
      </form>
    </Dialog>
  );
}
