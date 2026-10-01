import { useMemo, useState, type FormEvent } from 'react';
import {
  mensajeError,
  useActualizarMovimiento,
  useBorrarMovimiento,
  useCategorias,
  useCrearCategoria,
  useCrearMovimiento,
  useMovimiento,
  type MovimientoForm,
  type MovimientoRequest,
  type TipoMovimiento,
} from '../../api/kuiper';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input, SegmentedControl, Select, Switch } from '../../design-system';
import { iso } from '../../lib/fechas';

interface Props {
  /** Sin id: alta de un movimiento nuevo. Con id: edicion (y borrado) del existente. */
  movimientoId?: number;
  /** Periodo YYYY-MM que se esta viendo: la fecha de un alta nueva arranca dentro de el. */
  periodo: string;
  onClose: () => void;
}

const NUEVA = '__nueva';
const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;

export function FormularioMovimiento({ movimientoId, periodo, onClose }: Props) {
  useRestaurarFoco();
  const editando = movimientoId !== undefined;
  const ficha = useMovimiento(movimientoId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar movimiento" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeError(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo movimiento={ficha.data} periodo={periodo} onClose={onClose} />;
}

interface CuerpoProps {
  movimiento: MovimientoForm | undefined;
  periodo: string;
  onClose: () => void;
}

function fechaInicial(periodo: string): string {
  const hoy = iso(new Date());
  return hoy.startsWith(periodo) ? hoy : `${periodo}-01`;
}

function Cuerpo({ movimiento, periodo, onClose }: CuerpoProps) {
  const editando = movimiento !== undefined;
  const hoy = iso(new Date());
  const categorias = useCategorias();
  const crearCategoria = useCrearCategoria();
  const crear = useCrearMovimiento();
  const actualizar = useActualizarMovimiento(movimiento?.id ?? 0);
  const borrar = useBorrarMovimiento(onClose);

  const [tipo, setTipo] = useState<TipoMovimiento>(movimiento?.tipo ?? 'GASTO');
  const [importe, setImporte] = useState(movimiento ? String(movimiento.importe).replace('.', ',') : '');
  const [fecha, setFecha] = useState(movimiento?.fecha ?? fechaInicial(periodo));
  const [categoriaId, setCategoriaId] = useState(movimiento ? String(movimiento.categoriaId) : '');
  const [nuevaCategoria, setNuevaCategoria] = useState('');
  const [concepto, setConcepto] = useState(movimiento?.concepto ?? '');
  const [metodoPago, setMetodoPago] = useState(movimiento?.metodoPago ?? '');
  const [recurrente, setRecurrente] = useState(movimiento?.recurrente ?? false);
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
  const errorImporte = !(importeNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const errorFecha = !fecha ? 'Elige una fecha.' : fecha > hoy ? 'No puede ser una fecha futura.' : null;
  const errorCategoria = creandoCategoria
    ? nuevaCategoria.trim() === ''
      ? 'Escribe el nombre de la categoría.'
      : null
    : categoriaEfectiva === ''
      ? 'Elige una categoría.'
      : null;
  const ocupado = crear.isPending || actualizar.isPending || crearCategoria.isPending || borrar.isPending;

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errorImporte || errorFecha || errorCategoria) return;
    try {
      let idCategoria = Number(categoriaEfectiva);
      if (creandoCategoria) {
        const c = await crearCategoria.mutateAsync({ nombre: nuevaCategoria.trim(), tipo });
        idCategoria = c.id;
      }
      const cuerpo: MovimientoRequest = {
        fecha,
        importe: importeNum,
        tipo,
        categoriaId: idCategoria,
        concepto: concepto.trim() || null,
        metodoPago: metodoPago.trim() || null,
        recurrente,
      };
      if (editando) await actualizar.mutateAsync(cuerpo);
      else await crear.mutateAsync(cuerpo);
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeError(e));
    }
  };

  const borrarMovimiento = () => {
    if (!movimiento) return;
    setErrorEnvio(null);
    borrar.mutate(movimiento.id, { onError: (e) => setErrorEnvio(mensajeError(e)) });
  };

  const ver = (error: string | null) => (intentado ? error : null);

  return (
    <Dialog
      open
      onClose={onClose}
      title={editando ? 'Editar movimiento' : 'Nuevo movimiento'}
      footer={
        confirmandoBorrado ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar este movimiento?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmandoBorrado(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarMovimiento}>
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
            <Button type="submit" form="kui-form-movimiento" loading={ocupado}>
              {editando ? 'Guardar' : 'Añadir'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-movimiento" className="kui-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <SegmentedControl
          value={tipo}
          onChange={(v) => setTipo(v as TipoMovimiento)}
          options={[
            { value: 'GASTO', label: 'Gasto' },
            { value: 'INGRESO', label: 'Ingreso' },
          ]}
        />
        <div className="kui-form__row">
          <Input
            label="Importe (€)"
            inputMode="decimal"
            placeholder="0,00"
            autoFocus
            value={importe}
            onChange={(e) => setImporte(e.target.value)}
            error={ver(errorImporte)}
          />
          <Input label="Fecha" type="date" max={hoy} value={fecha} onChange={(e) => setFecha(e.target.value)} error={ver(errorFecha)} />
        </div>
        {creandoCategoria ? (
          <div className="kui-form__nuevacat">
            <Input
              label="Nueva categoría"
              placeholder={tipo === 'GASTO' ? 'Casa, Comida, Ocio…' : 'Nómina, Ventas…'}
              value={nuevaCategoria}
              onChange={(e) => setNuevaCategoria(e.target.value)}
              error={ver(errorCategoria)}
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
            hint={ver(errorCategoria) ?? undefined}
          />
        )}
        <Input label="Concepto" placeholder="Opcional" maxLength={255} value={concepto} onChange={(e) => setConcepto(e.target.value)} />
        <div className="kui-form__row">
          <Input label="Método de pago" placeholder="Opcional" maxLength={50} value={metodoPago} onChange={(e) => setMetodoPago(e.target.value)} />
          <div style={{ alignSelf: 'end', paddingBottom: 8 }}>
            <Switch label="Recurrente" checked={recurrente} onChange={setRecurrente} />
          </div>
        </div>
      </form>
    </Dialog>
  );
}
