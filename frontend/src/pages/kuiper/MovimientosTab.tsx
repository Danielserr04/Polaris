import { useMemo, useState } from 'react';
import { mensajeError, useCategorias, useMovimientos, type MovimientoList, type TipoMovimiento } from '../../api/kuiper';
import { Alert, Button, Card, SegmentedControl, Select } from '../../design-system';
import { rangoMes } from '../../lib/fechas';
import { ListaMovimientos } from './ListaMovimientos';

interface Props {
  periodo: string;
  onEditar: (m: MovimientoList) => void;
}

type FiltroTipo = 'TODOS' | TipoMovimiento;

export function MovimientosTab({ periodo, onEditar }: Props) {
  const { desde, hasta } = rangoMes(periodo);
  const [tipo, setTipo] = useState<FiltroTipo>('TODOS');
  const [categoria, setCategoria] = useState('');
  const categorias = useCategorias();
  const movimientos = useMovimientos({
    desde,
    hasta,
    tipo: tipo === 'TODOS' ? undefined : tipo,
    categoriaId: categoria ? Number(categoria) : undefined,
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

  const filas = useMemo(
    () => [...(movimientos.data ?? [])].sort((a, b) => (a.fecha < b.fecha ? 1 : a.fecha > b.fecha ? -1 : b.id - a.id)),
    [movimientos.data],
  );
  const hayFiltros = tipo !== 'TODOS' || categoria !== '';

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
      </div>

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
          />
        </Card>
      )}
    </>
  );
}
