import { useState, type FormEvent } from 'react';
import {
  mensajeErrorMeta,
  useActualizarMeta,
  useBorrarMeta,
  useCrearMeta,
  type MetaAhorro,
} from '../../api/kuiperMetas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Icon, Input } from '../../design-system';

interface Props {
  /** Sin meta: alta. Con meta: edicion (y borrado). */
  meta?: MetaAhorro;
  onClose: () => void;
}

// Los mismos colores que las categorias, para que nada rompa la paleta.
const COLORES = ['#5bb3a0', '#d0799f', '#e0b04a', '#e8845a', '#7d95e0', '#cdbf98', '#8fb97a', '#d36b5e'];
// Solo iconos que existen en public/icons (ver lib/iconos.ts).
const ICONOS = ['star', 'trophy', 'house', 'train-front', 'compass', 'sparkles', 'gamepad-2', 'tv', 'book-open', 'dumbbell', 'ticket', 'wallet'];
const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;
const FECHA = /^\d{4}-\d{2}-\d{2}$/;

export function FormularioMeta({ meta, onClose }: Props) {
  useRestaurarFoco();
  const editando = meta !== undefined;
  const crear = useCrearMeta();
  const actualizar = useActualizarMeta(meta?.id ?? 0);
  const borrar = useBorrarMeta();

  const [nombre, setNombre] = useState(meta?.nombre ?? '');
  const [objetivo, setObjetivo] = useState(meta ? String(meta.importeObjetivo).replace('.', ',') : '');
  const [fechaLimite, setFechaLimite] = useState(meta?.fechaLimite ?? '');
  const [color, setColor] = useState<string | null>(meta?.color ?? COLORES[0]);
  const [icono, setIcono] = useState<string | null>(meta?.icono ?? 'star');
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const objetivoNum = IMPORTE.test(objetivo.trim()) ? Number(objetivo.trim().replace(',', '.')) : NaN;
  const errorNombre = nombre.trim() === '' ? 'Escribe un nombre.' : null;
  const errorObjetivo = !(objetivoNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const errorFecha = fechaLimite !== '' && !FECHA.test(fechaLimite) ? 'Fecha no válida.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    if (errorNombre || errorObjetivo || errorFecha) return;
    const cuerpo = {
      nombre: nombre.trim(),
      importeObjetivo: objetivoNum,
      fechaLimite: fechaLimite === '' ? null : fechaLimite,
      color,
      icono,
    };
    try {
      if (editando) await actualizar.mutateAsync(cuerpo);
      else await crear.mutateAsync(cuerpo);
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeErrorMeta(e));
    }
  };

  const borrarMeta = () => {
    if (!meta) return;
    setErrorEnvio(null);
    borrar.mutate(meta.id, {
      onSuccess: onClose,
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeErrorMeta(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      width={760}
      title={editando ? 'Editar meta' : 'Nueva meta de ahorro'}
      footer={
        confirmando ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar la meta y todo su historial?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarMeta}>
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
            <Button type="submit" form="kui-form-meta" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-meta" className="pl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <div className="pl-form__cols">
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Meta</h3>
            <Input
              label="Nombre"
              autoFocus
              maxLength={100}
              placeholder="Viaje, coche, colchón…"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              error={errorNombre} validarAlSalir
            />
            <div className="pl-form__grid">
              <Input
                label="Objetivo (€)"
                inputMode="decimal"
                placeholder="3000"
                value={objetivo}
                onChange={(e) => setObjetivo(e.target.value)}
                error={errorObjetivo} validarAlSalir
              />
              <Input
                label="Fecha límite"
                type="date"
                value={fechaLimite}
                onChange={(e) => setFechaLimite(e.target.value)}
                error={errorFecha} validarAlSalir
                hint="Opcional. Con fecha, calcula cuánto apartar al mes."
              />
            </div>
            {editando && meta.importeActual > 0 && (
              <p className="kui-pistas">Lo ahorrado ({meta.importeActual.toLocaleString('es-ES', { minimumFractionDigits: 2 })} €) no cambia al editar: solo con aportaciones.</p>
            )}
          </section>
          <section className="pl-form__sec">
            <h3 className="pl-form__titulo">Aspecto</h3>
            <div>
              <span className="kui-field__label">Color</span>
              <div className="kui-swatches" role="group" aria-label="Color">
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
        </div>
      </form>
    </Dialog>
  );
}
