import { useState, type FormEvent } from 'react';
import { mensajeError, type MetaEntreno, type TipoMeta, useActualizarMeta, useBorrarMeta, useCrearMeta, useEjercicios } from '../../api/atlas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input, SegmentedControl, Select } from '../../design-system';
import { iso } from '../../lib/fechas';

const KG = /^\d{1,4}([.,]\d{1,2})?$/;
const dec = (s: string) => Number(s.trim().replace(',', '.'));
const texto = (n: number) => String(n).replace('.', ',');

export const NOMBRE_TIPO: Record<TipoMeta, string> = {
  PESO_CORPORAL: 'Peso corporal',
  MARCA_EJERCICIO: 'Marca',
  SESIONES_SEMANA: 'Sesiones por semana',
};

interface Props {
  /** Sin meta: alta. Con meta: edicion (y borrado). */
  meta?: MetaEntreno;
  onClose: () => void;
}

export function FormularioMetaEntreno({ meta, onClose }: Props) {
  useRestaurarFoco();
  const editando = meta !== undefined;
  const hoy = iso(new Date());
  const crear = useCrearMeta();
  const actualizar = useActualizarMeta(meta?.id ?? 0);
  const borrar = useBorrarMeta(onClose);
  const ejercicios = useEjercicios();

  const [tipo, setTipo] = useState<TipoMeta>(meta?.tipo ?? 'PESO_CORPORAL');
  const [ejercicioId, setEjercicioId] = useState(meta?.ejercicioId != null ? String(meta.ejercicioId) : '');
  const [objetivo, setObjetivo] = useState(meta ? texto(meta.valorObjetivo) : '');
  const [fechaLimite, setFechaLimite] = useState(meta?.fechaLimite ?? '');
  // El Select no valida al salir: su error se ve tras intentar guardar.
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const sesiones = tipo === 'SESIONES_SEMANA';
  const errObjetivo = sesiones
    ? /^[1-7]$/.test(objetivo.trim()) ? null : 'De 1 a 7 sesiones.'
    : KG.test(objetivo.trim()) && dec(objetivo) > 0 ? null : 'Un peso en kg, mayor que 0.';
  const errEjercicio = tipo === 'MARCA_EJERCICIO' && !ejercicioId ? 'Elige el ejercicio.' : null;
  const errFecha = fechaLimite && fechaLimite < hoy ? 'No puede ser una fecha pasada.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errObjetivo || errEjercicio || errFecha) return;
    const cuerpo = {
      tipo,
      ejercicioId: tipo === 'MARCA_EJERCICIO' ? Number(ejercicioId) : null,
      valorObjetivo: dec(objetivo),
      fechaLimite: fechaLimite || null,
    };
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else crear.mutate(cuerpo, opciones);
  };

  const borrarMeta = () => {
    if (!meta) return;
    setErrorEnvio(null);
    borrar.mutate(meta.id, {
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
      width={460}
      title={editando ? 'Editar meta' : 'Nueva meta'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar esta meta?</span>
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
              <Button variant="ghost" type="button" style={{ marginRight: 'auto' }} disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="atl-form-meta" loading={ocupado}>
              {editando ? 'Guardar' : 'Crear'}
            </Button>
          </>
        )
      }
    >
      <form id="atl-form-meta" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <SegmentedControl value={tipo} onChange={(v) => setTipo(v as TipoMeta)} options={(Object.keys(NOMBRE_TIPO) as TipoMeta[]).map((t) => ({ value: t, label: NOMBRE_TIPO[t] }))} />
        {tipo === 'MARCA_EJERCICIO' && (
          <Select
            label="Ejercicio"
            value={ejercicioId}
            onChange={(e) => setEjercicioId(e.target.value)}
            error={intentado ? errEjercicio : null}
            options={[{ value: '', label: ejercicios.isPending ? 'Cargando…' : 'Elige un ejercicio' }, ...(ejercicios.data ?? []).map((e) => ({ value: String(e.id), label: e.nombre }))]}
          />
        )}
        <Input
          label={sesiones ? 'Sesiones por semana' : tipo === 'MARCA_EJERCICIO' ? 'Peso a levantar' : 'Peso objetivo'}
          autoFocus
          inputMode={sesiones ? 'numeric' : 'decimal'}
          trailing={sesiones ? undefined : <span className="muted" style={{ fontSize: 12 }}>kg</span>}
          hint={tipo === 'PESO_CORPORAL' ? 'Por debajo de tu peso actual es para bajar; por encima, para subir.' : tipo === 'MARCA_EJERCICIO' ? 'En una sola serie.' : undefined}
          value={objetivo}
          onChange={(e) => setObjetivo(e.target.value)}
          error={errObjetivo}
          validarAlSalir
        />
        <Input label="Fecha límite" type="date" min={hoy} hint="Opcional" value={fechaLimite} onChange={(e) => setFechaLimite(e.target.value)} error={errFecha} validarAlSalir />
      </form>
    </Dialog>
  );
}
