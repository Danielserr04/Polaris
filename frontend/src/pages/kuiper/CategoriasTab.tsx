import { useMemo, useState } from 'react';
import { mensajeError, useCategorias, usePresupuestosMensuales, type Categoria, type Presupuesto, type TipoMovimiento } from '../../api/kuiper';
import { Alert, Button, Card, Icon, SegmentedControl } from '../../design-system';
import { eur } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { FormularioCategoria } from './FormularioCategoria';

// Una categoria abierta: nueva (sin categoria) o editando una existente.
type Abierta = { categoria?: Categoria; presupuesto?: Presupuesto } | null;

export function CategoriasTab() {
  const [tipo, setTipo] = useState<TipoMovimiento>('GASTO');
  const [abierta, setAbierta] = useState<Abierta>(null);
  const categorias = useCategorias();
  const presupuestos = usePresupuestosMensuales();

  const porCategoria = useMemo(() => new Map((presupuestos.data ?? []).map((p) => [p.categoriaId, p])), [presupuestos.data]);
  const filas = useMemo(
    () => (categorias.data ?? []).filter((c) => c.tipo === tipo).sort((a, b) => a.nombre.localeCompare(b.nombre, 'es')),
    [categorias.data, tipo],
  );

  const error = categorias.error ?? presupuestos.error;

  return (
    <>
      <div className="kui-toolbar">
        <SegmentedControl
          value={tipo}
          onChange={(v) => setTipo(v as TipoMovimiento)}
          options={[
            { value: 'GASTO', label: 'Gastos' },
            { value: 'INGRESO', label: 'Ingresos' },
          ]}
        />
        <Button variant="secondary" icon="plus" style={{ marginLeft: 'auto' }} onClick={() => setAbierta({})}>
          Categoría
        </Button>
      </div>

      {categorias.isError || presupuestos.isError ? (
        <Alert
          tone="danger"
          title="No se han podido cargar las categorías"
          action={
            <Button size="sm" variant="secondary" onClick={() => { void categorias.refetch(); void presupuestos.refetch(); }}>
              Reintentar
            </Button>
          }
        >
          {mensajeError(error)}
        </Alert>
      ) : categorias.isPending || presupuestos.isPending ? (
        <p className="muted">Cargando…</p>
      ) : (
        <Card padding="8px 0 0">
          {filas.length === 0 ? (
            <div className="kui-vacio">{tipo === 'GASTO' ? 'Aún no tienes categorías de gasto.' : 'Aún no tienes categorías de ingreso.'}</div>
          ) : (
            <div className="table">
              {filas.map((c, i) => {
                const pres = porCategoria.get(c.id);
                return (
                  <button
                    key={c.id}
                    type="button"
                    className="table__r table__r--cat pl-rise"
                    style={{ animationDelay: Math.min(i, 12) * 30 + 'ms' }}
                    onClick={() => setAbierta({ categoria: c, presupuesto: pres })}
                    aria-label={`Editar ${c.nombre}`}
                  >
                    <span className="kui-catchip" style={c.color ? ({ ['--c' as string]: c.color }) : undefined}>
                      <Icon name={iconoOr(c.icono)} size={16} />
                    </span>
                    <b>{c.nombre}</b>
                    {tipo === 'GASTO' ? (
                      pres ? (
                        <span className="money kui-catpres">
                          {eur(pres.importeLimite, 0)}
                          <span className="muted"> /mes</span>
                        </span>
                      ) : (
                        <span className="muted kui-catpres">Sin presupuesto</span>
                      )
                    ) : (
                      <span />
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </Card>
      )}

      {abierta && (
        <FormularioCategoria
          categoria={abierta.categoria}
          presupuesto={abierta.presupuesto}
          tipoInicial={tipo}
          onClose={() => setAbierta(null)}
        />
      )}
    </>
  );
}
