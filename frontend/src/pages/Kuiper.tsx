import { useMemo, useState } from 'react';
import { PageHeader } from '../components/PageHeader';
import { Button, Select, Tabs } from '../design-system';
import { etiquetaMes, periodo as periodoDe, ultimosMeses } from '../lib/fechas';
import type { MovimientoList } from '../api/kuiper';
import { AnalisisTab } from './kuiper/AnalisisTab';
import { CategoriasTab } from './kuiper/CategoriasTab';
import { CuentasTab } from './kuiper/CuentasTab';
import { FormularioMovimiento } from './kuiper/FormularioMovimiento';
import { MetasTab } from './kuiper/MetasTab';
import { MovimientosTab } from './kuiper/MovimientosTab';
import { RecurrentesTab } from './kuiper/RecurrentesTab';
import { NotificacionesKuiper } from './kuiper/NotificacionesKuiper';
import { ResumenTab } from './kuiper/ResumenTab';
import './kuiper/kuiper.css';

type Pestana = 'resumen' | 'mov' | 'cat' | 'rec' | 'metas' | 'analisis' | 'cuentas';

// Un movimiento abierto: nuevo (sin id) o editando uno existente.
type Abierto = { id?: number } | null;

export function Kuiper() {
  const hoy = useMemo(() => new Date(), []);
  const meses = useMemo(() => ultimosMeses(hoy, 12), [hoy]);
  const [periodo, setPeriodo] = useState(periodoDe(hoy));
  const [pestana, setPestana] = useState<Pestana>('resumen');
  const [abierto, setAbierto] = useState<Abierto>(null);

  const editar = (m: MovimientoList) => setAbierto({ id: m.id });

  return (
    <div>
      <PageHeader
        eyebrow="Kuiper"
        coord={etiquetaMes(periodo).toUpperCase()}
        title="Gastos"
        actions={
          <>
            <div style={{ width: 190 }}>
              <Select
                size="sm"
                icon="calendar"
                aria-label="Mes"
                value={periodo}
                onChange={(e) => setPeriodo(e.target.value)}
                options={meses.map((m) => ({ value: m, label: etiquetaMes(m) }))}
              />
            </div>
            <NotificacionesKuiper onIr={(p) => setPestana(p as Pestana)} />
            <Button icon="plus" onClick={() => setAbierto({})}>
              Movimiento
            </Button>
          </>
        }
      />
      <Tabs
        value={pestana}
        onChange={(v) => setPestana(v as Pestana)}
        items={[
          { value: 'resumen', label: 'Resumen' },
          { value: 'mov', label: 'Movimientos' },
          { value: 'cat', label: 'Categorías' },
          { value: 'rec', label: 'Recurrentes' },
          { value: 'metas', label: 'Metas' },
          { value: 'analisis', label: 'Análisis' },
          { value: 'cuentas', label: 'Cuentas' },
        ]}
        style={{ marginBottom: 24 }}
      />
      <div key={pestana} className="pl-tabpanel">
        {pestana === 'resumen' ? (
          <ResumenTab periodo={periodo} onVerTodos={() => setPestana('mov')} onEditar={editar} onIrACategorias={() => setPestana('cat')} />
        ) : pestana === 'mov' ? (
          <MovimientosTab periodo={periodo} onEditar={editar} />
        ) : pestana === 'rec' ? (
          <RecurrentesTab />
        ) : pestana === 'metas' ? (
          <MetasTab />
        ) : pestana === 'analisis' ? (
          <AnalisisTab periodo={periodo} />
        ) : pestana === 'cuentas' ? (
          <CuentasTab periodo={periodo} />
        ) : (
          <CategoriasTab />
        )}
      </div>
      {abierto && <FormularioMovimiento movimientoId={abierto.id} periodo={periodo} onClose={() => setAbierto(null)} />}
    </div>
  );
}
