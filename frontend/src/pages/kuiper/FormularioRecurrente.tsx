import { useMemo, useState, type FormEvent } from 'react';
import { useCategorias, useCrearCategoria, type TipoMovimiento } from '../../api/kuiper';
import {
  mensajeErrorRecurrente,
  useActualizarRecurrente,
  useBorrarRecurrente,
  useCrearRecurrente,
  useRecurrente,
  type FrecuenciaRecurrente,
  type RecurrenteForm,
  type RecurrenteRequest,
} from '../../api/kuiperRecurrentes';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input, SegmentedControl, Select, Switch } from '../../design-system';
import { iso } from '../../lib/fechas';

interface Props {
  /** Sin id: alta de un recurrente nuevo. Con id: edicion (y borrado) del existente. */
  recurrenteId?: number;
  onClose: () => void;
}

const NUEVA = '__nueva';
const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;
const CUOTAS = /^\d{1,3}$/;

export function FormularioRecurrente({ recurrenteId, onClose }: Props) {
  useRestaurarFoco();
  const editando = recurrenteId !== undefined;
  const ficha = useRecurrente(recurrenteId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar recurrente" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeErrorRecurrente(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo recurrente={ficha.data} onClose={onClose} />;
}

interface CuerpoProps {
  recurrente: RecurrenteForm | undefined;
  onClose: () => void;
}

function Cuerpo({ recurrente, onClose }: CuerpoProps) {
  const editando = recurrente !== undefined;
  const hoy = iso(new Date());
  const categorias = useCategorias();
  const crearCategoria = useCrearCategoria();
  const crear = useCrearRecurrente();
  const actualizar = useActualizarRecurrente(recurrente?.id ?? 0);
  const borrar = useBorrarRecurrente(onClose);

  const [tipo, setTipo] = useState<TipoMovimiento>(recurrente?.tipo ?? 'GASTO');
  const [concepto, setConcepto] = useState(recurrente?.concepto ?? '');
  const [importe, setImporte] = useState(recurrente ? String(recurrente.importe).replace('.', ',') : '');
  const [categoriaId, setCategoriaId] = useState(recurrente ? String(recurrente.categoriaId) : '');
  const [nuevaCategoria, setNuevaCategoria] = useState('');
  const [metodoPago, setMetodoPago] = useState(recurrente?.metodoPago ?? '');
  const [frecuencia, setFrecuencia] = useState<FrecuenciaRecurrente>(recurrente?.frecuencia ?? 'MENSUAL');
  const [fechaInicio, setFechaInicio] = useState(recurrente?.fechaInicio ?? hoy);
  const [cuotas, setCuotas] = useState(recurrente?.cuotasTotal != null ? String(recurrente.cuotasTotal) : '');
  const [activo, setActivo] = useState(recurrente?.activo ?? true);
  const [intentado, setIntentado] = useState(false);
  const [confirmandoBorrado, setConfirmandoBorrado] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const delTipo = useMemo(() => (categorias.data ?? []).filter((c) => c.tipo === tipo), [categorias.data, tipo]);
  const opciones = [
    ...(categoriaId === '' ? [{ value: '', label: 'Elige una categoría' }] : []),
    ...delTipo.map((c) => ({ value: String(c.id), label: c.nombre })),
    { value: NUEVA, label: '＋ Nueva categoría…' },
  ];
  // Si la categoria elegida es de otro tipo (se cambio Gasto/Ingreso), se descarta.
  const categoriaValida = categoriaId === NUEVA || delTipo.some((c) => String(c.id) === categoriaId);
  const categoriaEfectiva = categoriaValida ? categoriaId : '';
  const creandoCategoria = categoriaEfectiva === NUEVA || (categorias.isSuccess && delTipo.length === 0);

  const importeNum = IMPORTE.test(importe.trim()) ? Number(importe.trim().replace(',', '.')) : NaN;
  const cuotasNum = cuotas.trim() === '' ? null : CUOTAS.test(cuotas.trim()) ? Number(cuotas.trim()) : NaN;
  const pagadas = recurrente?.cuotasPagadas ?? 0;

  const errorConcepto = concepto.trim() === '' ? 'Escribe un concepto.' : null;
  const errorImporte = !(importeNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const errorFecha = !fechaInicio ? 'Elige la fecha del primer cargo.' : null;
  const errorCuotas =
    cuotasNum === null
      ? null
      : !(cuotasNum >= 1 && cuotasNum <= 600)
        ? 'Entre 1 y 600 cuotas, o vacío si no termina.'
        : null;
  const errorCategoria = creandoCategoria
    ? nuevaCategoria.trim() === ''
      ? 'Escribe el nombre de la categoría.'
      : null
    : categoriaEfectiva === ''
      ? 'Elige una categoría.'
      : null;
  const ocupado = crear.isPending || actualizar.isPending || crearCategoria.isPending || borrar.isPending;

  // Pistas de lo que hara el backend: no son errores, solo avisan.
  const pistaFecha = !editando && fechaInicio && fechaInicio < hoy
    ? 'Es pasada: se generarán los cargos atrasados.'
    : editando && recurrente && fechaInicio !== recurrente.fechaInicio
      ? 'El próximo cargo se recalcula desde hoy.'
      : 'Fija el día de cobro.';
  const cuotasValidas = cuotasNum !== null && Number.isFinite(cuotasNum);
  const pistaCuotas =
    cuotasValidas && editando && pagadas > 0
      ? `Ya ${pagadas === 1 ? 'se ha pagado 1' : `se han pagado ${pagadas}`}.`
      : 'Vacío si no termina nunca.';
  const quedaTerminado = cuotasValidas && pagadas >= (cuotasNum ?? 0);

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errorConcepto || errorImporte || errorFecha || errorCuotas || errorCategoria) return;
    try {
      let idCategoria = Number(categoriaEfectiva);
      if (creandoCategoria) {
        const c = await crearCategoria.mutateAsync({ nombre: nuevaCategoria.trim(), tipo });
        idCategoria = c.id;
      }
      const cuerpo: RecurrenteRequest = {
        concepto: concepto.trim(),
        importe: importeNum,
        tipo,
        categoriaId: idCategoria,
        metodoPago: metodoPago.trim() || null,
        frecuencia,
        fechaInicio,
        cuotasTotal: cuotasNum,
        activo,
      };
      if (editando) await actualizar.mutateAsync(cuerpo);
      else await crear.mutateAsync(cuerpo);
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeErrorRecurrente(e));
    }
  };

  const borrarRecurrente = () => {
    if (!recurrente) return;
    setErrorEnvio(null);
    borrar.mutate(recurrente.id, { onError: (e) => setErrorEnvio(mensajeErrorRecurrente(e)) });
  };

  const ver = (error: string | null) => (intentado ? error : null);

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      title={editando ? 'Editar recurrente' : 'Nuevo recurrente'}
      footer={
        confirmandoBorrado ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar? Los movimientos ya generados se quedan.</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmandoBorrado(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarRecurrente}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" className="kui-form__borrar" disabled={ocupado} onClick={() => setConfirmandoBorrado(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="kui-form-recurrente" loading={ocupado}>
              {editando ? 'Guardar' : 'Añadir'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-recurrente" className="kui-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <SegmentedControl
          value={tipo}
          onChange={(v) => setTipo(v as TipoMovimiento)}
          options={[
            { value: 'GASTO', label: 'Gasto' },
            { value: 'INGRESO', label: 'Ingreso' },
          ]}
        />
        <Input
          label="Concepto"
          placeholder={tipo === 'GASTO' ? 'Netflix, alquiler, móvil a plazos…' : 'Nómina, alquiler cobrado…'}
          maxLength={200}
          autoFocus
          value={concepto}
          onChange={(e) => setConcepto(e.target.value)}
          error={errorConcepto} validarAlSalir
        />
        <div className="kui-form__row">
          <Input
            label="Importe (€)"
            inputMode="decimal"
            placeholder="0,00"
            value={importe}
            onChange={(e) => setImporte(e.target.value)}
            error={errorImporte} validarAlSalir
          />
          <Input label="Método de pago" placeholder="Opcional" maxLength={50} value={metodoPago} onChange={(e) => setMetodoPago(e.target.value)} />
        </div>
        {creandoCategoria ? (
          <div className="kui-form__nuevacat">
            <Input
              label="Nueva categoría"
              placeholder={tipo === 'GASTO' ? 'Suscripciones, Casa…' : 'Nómina, Ventas…'}
              value={nuevaCategoria}
              onChange={(e) => setNuevaCategoria(e.target.value)}
              error={errorCategoria} validarAlSalir
            />
            {delTipo.length > 0 && (
              <Button variant="ghost" type="button" onClick={() => setCategoriaId('')}>
                Elegir existente
              </Button>
            )}
          </div>
        ) : (
          <Select
            label="Categoría"
            value={categoriaEfectiva}
            onChange={(e) => setCategoriaId(e.target.value)}
            options={opciones}
            error={ver(errorCategoria)}
          />
        )}
        <div>
          <span className="kui-field__label">Frecuencia</span>
          <SegmentedControl
            value={frecuencia}
            onChange={(v) => setFrecuencia(v as FrecuenciaRecurrente)}
            options={[
              { value: 'SEMANAL', label: 'Semanal' },
              { value: 'MENSUAL', label: 'Mensual' },
              { value: 'ANUAL', label: 'Anual' },
            ]}
          />
        </div>
        <div className="kui-form__row">
          <Input
            label="Primer cargo"
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            error={errorFecha} validarAlSalir
            hint={pistaFecha}
          />
          <Input
            label="Nº de cuotas"
            inputMode="numeric"
            placeholder="Sin fin"
            value={cuotas}
            onChange={(e) => setCuotas(e.target.value)}
            error={errorCuotas} validarAlSalir
            hint={pistaCuotas}
          />
        </div>
        <Switch label="Activo" checked={activo} onChange={setActivo} />
        {activo && editando && recurrente && !recurrente.activo && !quedaTerminado && (
          <p className="kui-pistas">Al reactivarlo no se cobra el tiempo en pausa: el próximo cargo será el primero desde hoy.</p>
        )}
        {activo && editando && quedaTerminado && (
          <p className="kui-pistas">Ya tiene todas sus cuotas pagadas: se quedará terminado. Sube el nº de cuotas para seguir.</p>
        )}
      </form>
    </Dialog>
  );
}
