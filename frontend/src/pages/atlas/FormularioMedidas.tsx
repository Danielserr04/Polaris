import { useEffect, useState, type FormEvent } from 'react';
import { mensajeError } from '../../api/atlas';
import {
  CAMPOS_MEDIDA,
  type CampoMedida,
  type MedidaRequest,
  useActualizarMedidas,
  useApuntarMedidas,
  useBorrarMedidas,
  useMedida,
} from '../../api/cuerpo';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input } from '../../design-system';
import { iso } from '../../lib/fechas';

const CM = /^\d{1,3}([.,]\d)?$/;
const dec = (s: string) => Number(s.trim().replace(',', '.'));
const texto = (n: number | null | undefined) => (n == null ? '' : String(n).replace('.', ','));

type Valores = Record<CampoMedida, string>;
const vacios = (): Valores => Object.fromEntries(CAMPOS_MEDIDA.map((c) => [c.campo, ''])) as Valores;

interface Props {
  /** Sin id: alta. Con id: edicion (y borrado). */
  medidaId?: number;
  onClose: () => void;
}

export function FormularioMedidas({ medidaId, onClose }: Props) {
  useRestaurarFoco();
  const editando = medidaId !== undefined;
  const hoy = iso(new Date());
  const ficha = useMedida(medidaId);
  const apuntar = useApuntarMedidas();
  const actualizar = useActualizarMedidas(medidaId ?? 0);
  const borrar = useBorrarMedidas(onClose);

  const [fecha, setFecha] = useState(hoy);
  const [valores, setValores] = useState<Valores>(vacios);
  const [notas, setNotas] = useState('');
  // Solo para el aviso de "ninguna medida": los campos se validan solos al salir.
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  // Al editar, el formulario se rellena una vez con la ficha (que trae las notas).
  useEffect(() => {
    if (!ficha.data) return;
    setFecha(ficha.data.fecha);
    setValores(Object.fromEntries(CAMPOS_MEDIDA.map((c) => [c.campo, texto(ficha.data[c.campo])])) as Valores);
    setNotas(ficha.data.notas ?? '');
  }, [ficha.data]);

  const errFecha = !fecha ? 'Elige una fecha.' : fecha > hoy ? 'No puede ser una fecha futura.' : null;
  const errCampo = (v: string) => (v.trim() === '' || (CM.test(v.trim()) && dec(v) > 0) ? null : 'Entre 0,1 y 999,9 cm.');
  const errores = CAMPOS_MEDIDA.map((c) => errCampo(valores[c.campo]));
  const ninguna = CAMPOS_MEDIDA.every((c) => valores[c.campo].trim() === '');
  const ocupado = apuntar.isPending || actualizar.isPending || borrar.isPending;

  const guardar = (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errFecha || ninguna || errores.some(Boolean)) return;
    const cuerpo: MedidaRequest = { fecha, notas: notas.trim() || null };
    for (const c of CAMPOS_MEDIDA) cuerpo[c.campo] = valores[c.campo].trim() === '' ? null : dec(valores[c.campo]);
    const opciones = { onSuccess: onClose, onError: (e: unknown) => setErrorEnvio(mensajeError(e)) };
    if (editando) actualizar.mutate(cuerpo, opciones);
    else apuntar.mutate(cuerpo, opciones);
  };

  const borrarMedidas = () => {
    if (medidaId === undefined) return;
    setErrorEnvio(null);
    borrar.mutate(medidaId, {
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
      width={520}
      title={editando ? 'Editar medidas' : 'Apuntar medidas'}
      footer={
        confirmando ? (
          <>
            <span className="muted" style={{ marginRight: 'auto' }}>¿Borrar estas medidas?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarMedidas}>
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
            <Button type="submit" form="atl-form-medidas" loading={ocupado} disabled={editando && ficha.isPending}>
              Guardar
            </Button>
          </>
        )
      }
    >
      <form id="atl-form-medidas" className="atl-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        {ficha.isError && <Alert tone="danger">{mensajeError(ficha.error)}</Alert>}
        {intentado && ninguna && <Alert tone="warning">Indica al menos una medida.</Alert>}
        <Input label="Fecha" type="date" max={hoy} value={fecha} onChange={(e) => setFecha(e.target.value)} error={errFecha} validarAlSalir hint="Una medición por día: si ya hay una, se reemplaza." />
        <div className="atl-form__row2">
          {CAMPOS_MEDIDA.map((c, i) => (
            <Input
              key={c.campo}
              label={c.nombre}
              autoFocus={i === 0}
              inputMode="decimal"
              trailing={<span className="muted" style={{ fontSize: 12 }}>cm</span>}
              value={valores[c.campo]}
              onChange={(e) => setValores((v) => ({ ...v, [c.campo]: e.target.value }))}
              error={errores[i]}
              validarAlSalir
            />
          ))}
        </div>
        <div className="pl-field">
          <label className="pl-field__label" htmlFor="atl-notas-medidas">
            Notas
          </label>
          <textarea id="atl-notas-medidas" className="atl-textarea" rows={2} maxLength={2000} placeholder="Opcional" value={notas} onChange={(e) => setNotas(e.target.value)} />
        </div>
      </form>
    </Dialog>
  );
}
