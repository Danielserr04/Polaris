import { useMemo, useState } from 'react';
import { mensajeError, useCategorias, useMovimientos, type MovimientoList, type TipoMovimiento } from '../../api/kuiper';
import { avisar, useBorrarMovimientos, useDuplicarMovimiento } from '../../api/kuiperPapelera';
import { useCuentas } from '../../api/kuiperCuentas';
import { Alert, Button, Card, SegmentedControl, Select } from '../../design-system';
import { rangoMes } from '../../lib/fechas';
import { ListaMovimientos } from './ListaMovimientos';
import { PapeleraDialog } from './PapeleraDialog';

interface Props {
  periodo: string;
  onEditar: (m: MovimientoList) => void;
}

type FiltroTipo = 'TODOS' | TipoMovimiento;

export function MovimientosTab({ periodo, onEditar }: Props) {
  const { desde, hasta } = rangoMes(periodo);
  const [tipo, setTipo] = useState<FiltroTipo>('TODOS');
  const [categoria, setCategoria] = useState('');
  // null = fuera del modo seleccion.
  const [seleccion, setSeleccion] = useState<Set<number> | null>(null);
  const [verPapelera, setVerPapelera] = useState(false);
  const categorias = useCategorias();
  const borrarVarios = useBorrarMovimientos();
  const duplicar = useDuplicarMovimiento();
  const [cuenta, setCuenta] = useState('');
  const cuentas = useCuentas();
  const movimientos = useMovimientos({
    desde,
    hasta,
    tipo: tipo === 'TODOS' ? undefined : tipo,
    categoriaId: categoria ? Number(categoria) : undefined,
    cuentaId: cuenta ? Number(cuenta) : undefined,
  });

  const opciones = useMemo(
    () => [
      { value: '', label: 'Todas las categorías' },
      ...(categorias.data ?? [])
        .filter((c) => tipo === 'TODOS' || c.tipo === tipo)
        .map((c) => ({ value: String(c.id), label: c.nombre })),
    ],
    [categorias.data, tipo],
  );

  const opcionesCuenta = useMemo(
    () => [
      { value: '', label: 'Todas las cuentas' },
      ...(cuentas.data ?? []).map((c) => ({ value: String(c.id), label: c.archivada ? `${c.nombre} (archivada)` : c.nombre })),
    ],
    [cuentas.data],
  );

  const filas = useMemo(
    () => [...(movimientos.data ?? [])].sort((a, b) => (a.fecha < b.fecha ? 1 : a.fecha > b.fecha ? -1 : b.id - a.id)),
    [movimientos.data],
  );
  const hayFiltros = tipo !== 'TODOS' || categoria !== '' || cuenta !== '';
  // Solo cuenta lo seleccionado que sigue a la vista (al cambiar de filtro o de mes no se borra lo oculto).
  const elegidos = seleccion ? filas.filter((m) => seleccion.has(m.id)).map((m) => m.id) : [];

  const alternar = (id: number) =>
    setSeleccion((s) => {
      const n = new Set(s ?? []);
      if (n.has(id)) n.delete(id);
      else n.add(id);
      return n;
    });

  const borrarElegidos = () =>
    borrarVarios.mutate(elegidos, {
      onSuccess: () => setSeleccion(null),
      onError: (e) => avisar({ texto: mensajeError(e), tono: 'danger' }),
    });

  const duplicarFila = (m: MovimientoList) =>
    duplicar.mutate(
      { id: m.id },
      {
        onSuccess: () => avisar({ texto: `«${m.concepto || m.categoriaNombre}» duplicado con fecha de hoy.`, tono: 'success' }),
        onError: (e) => avisar({ texto: mensajeError(e), tono: 'danger' }),
      },
    );

  return (
    <>
      <div className="kui-toolbar">
        <SegmentedControl
          value={tipo}
          onChange={(v) => {
            setTipo(v as FiltroTipo);
            setCategoria('');
          }}
          options={[
            { value: 'TODOS', label: 'Todos' },
            { value: 'GASTO', label: 'Gastos' },
            { value: 'INGRESO', label: 'Ingresos' },
          ]}
        />
        <div className="kui-toolbar__sel">
          <Select size="sm" aria-label="Categoría" value={categoria} onChange={(e) => setCategoria(e.target.value)} options={opciones} />
        </div>
        {opcionesCuenta.length > 1 && (
          <div className="kui-toolbar__sel">
            <Select size="sm" aria-label="Cuenta" value={cuenta} onChange={(e) => setCuenta(e.target.value)} options={opcionesCuenta} />
          </div>
        )}
        <span className="kui-selbar__sep" />
        {seleccion === null && filas.length > 0 && (
          <Button size="sm" variant="ghost" icon="check" onClick={() => setSeleccion(new Set())}>
            Seleccionar
          </Button>
        )}
        <Button size="sm" variant="ghost" icon="trash-2" onClick={() => setVerPapelera(true)}>
          Papelera
        </Button>
      </div>

      {seleccion !== null && (
        <div className="kui-selbar" role="toolbar" aria-label="Selección">
          <span>
            {elegidos.length === 0 ? 'Pulsa los movimientos que quieras borrar' : `${elegidos.length} seleccionado${elegidos.length === 1 ? '' : 's'}`}
          </span>
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setSeleccion(elegidos.length === filas.length ? new Set() : new Set(filas.map((m) => m.id)))}
          >
            {elegidos.length === filas.length && filas.length > 0 ? 'Ninguno' : 'Todos'}
          </Button>
          <span className="kui-selbar__sep" />
          <Button size="sm" variant="ghost" onClick={() => setSeleccion(null)}>
            Cancelar
          </Button>
          <Button size="sm" variant="danger" icon="trash-2" disabled={elegidos.length === 0} loading={borrarVarios.isPending} onClick={borrarElegidos}>
            Mandar a la papelera
          </Button>
        </div>
      )}

      {movimientos.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar los movimientos"
          action={
            <Button size="sm" variant="secondary" onClick={() => void movimientos.refetch()}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(movimientos.error)}
        </Alert>
      ) : movimientos.isPending ? (
        <p className="muted">Cargando…</p>
      ) : (
        <Card padding="8px 0 0">
          <ListaMovimientos
            movimientos={filas}
            vacio={hayFiltros ? 'Nada con estos filtros.' : 'Sin movimientos este mes.'}
            onEditar={onEditar}
            seleccion={seleccion ?? undefined}
            onAlternar={alternar}
            onDuplicar={duplicarFila}
          />
        </Card>
      )}
      {verPapelera && <PapeleraDialog onClose={() => setVerPapelera(false)} />}
    </>
  );
}
