import { useState } from 'react';
import { pedirReceta, useRecetas, type RecetaIngrediente } from '../../api/fusionRecetas';
import { Button, Input, Select } from '../../design-system';
import { num } from '../../lib/fechas';

/** Un ingrediente ya escalado a las raciones elegidas. */
export interface IngredienteEscalado {
  ingrediente: RecetaIngrediente;
  cantidadG: number;
}

interface Props {
  onAnadir: (ingredientes: IngredienteEscalado[]) => void;
}

const RACIONES = /^\d{1,2}([.,]\d{1,2})?$/;

/**
 * Añade a una comida los ingredientes de una receta, en proporcion a las raciones.
 * La comida guarda alimentos y gramos: la receta solo sirve para rellenarlos de golpe.
 */
export function AnadirReceta({ onAnadir }: Props) {
  const recetas = useRecetas();
  const [recetaId, setRecetaId] = useState('');
  const [raciones, setRaciones] = useState('1');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!recetas.data?.length) return null;

  const n = RACIONES.test(raciones.trim()) ? Number(raciones.trim().replace(',', '.')) : NaN;
  const errRaciones = !(n > 0 && n <= 50) ? 'De 0,25 a 50.' : null;

  const anadir = async () => {
    if (!recetaId || errRaciones) return;
    setCargando(true);
    setError(null);
    try {
      const r = await pedirReceta(Number(recetaId));
      onAnadir(r.ingredientes.map((i) => ({ ingrediente: i, cantidadG: Math.round((i.cantidadG * n * 100) / r.raciones) / 100 })));
      setRecetaId('');
      setRaciones('1');
    } catch {
      setError('No se ha podido cargar la receta.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="fus-add-receta">
      <Select
        label="O de una receta"
        value={recetaId}
        onChange={(e) => setRecetaId(e.target.value)}
        options={[{ value: '', label: 'Elige una receta…' }, ...recetas.data.map((r) => ({ value: String(r.id), label: `${r.nombre} · ${num(r.kcalRacion)} kcal/ración` }))]}
        error={error}
      />
      <Input label="Raciones" inputMode="decimal" value={raciones} onChange={(e) => setRaciones(e.target.value)} error={recetaId ? errRaciones : null} />
      <Button type="button" variant="secondary" disabled={!recetaId || !!errRaciones} loading={cargando} onClick={() => void anadir()}>
        Añadir
      </Button>
    </div>
  );
}
