import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../../api/client';
import { ETIQUETA_MOMENTO, MOMENTOS, type ComidaRequest } from '../../api/fusion';
import { diaDeFecha, ETIQUETA_DIA, usePlanActivo } from '../../api/fusionPlanes';
import { pedirReceta } from '../../api/fusionRecetas';
import { Button } from '../../design-system';
import { avisar } from '../../lib/avisos';
import { deIso } from '../../lib/fechas';

interface Props {
  /** Dia que se esta viendo (yyyy-MM-dd); nunca futuro. */
  fecha: string;
}

/**
 * Registra como comidas de ese dia lo que el plan activo tiene para ese dia de la
 * semana: una comida por momento. Las recetas se apuntan como sus ingredientes,
 * en proporcion a las raciones (una comida guarda alimentos y gramos).
 */
export function ApuntarPlan({ fecha }: Props) {
  const plan = usePlanActivo();
  const qc = useQueryClient();
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const dia = diaDeFecha(deIso(fecha));
  const lineas = plan.data?.lineas.filter((l) => l.diaSemana === dia) ?? [];
  if (!plan.data || lineas.length === 0) return null;

  const apuntar = async () => {
    setOcupado(true);
    setError(null);
    try {
      const recetas = new Map<number, Awaited<ReturnType<typeof pedirReceta>>>();
      for (const id of new Set(lineas.flatMap((l) => (l.recetaId != null ? [l.recetaId] : [])))) recetas.set(id, await pedirReceta(id));

      const comidas: ComidaRequest[] = MOMENTOS.map((momento) => ({
        fecha,
        momento,
        lineas: lineas
          .filter((l) => l.momento === momento)
          .flatMap((l) => {
            if (l.recetaId == null) return [{ alimentoId: l.alimentoId as number, cantidadG: l.cantidadG as number }];
            const r = recetas.get(l.recetaId)!;
            return r.ingredientes.map((i) => ({ alimentoId: i.alimentoId, cantidadG: Math.round((i.cantidadG * (l.raciones ?? 1) * 100) / r.raciones) / 100 }));
          }),
      })).filter((c) => c.lineas.length > 0);

      for (const cuerpo of comidas) await api(`/api/fusion/comida`, { metodo: 'POST', cuerpo });
      avisar(`Apuntado: ${comidas.map((c) => ETIQUETA_MOMENTO[c.momento].toLowerCase()).join(', ')}`);
    } catch {
      setError('No se ha podido apuntar todo. Revisa las comidas del día.');
    } finally {
      setOcupado(false);
      await Promise.all([qc.invalidateQueries({ queryKey: ['fusion'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
    }
  };

  return (
    <div className="fus-apuntar">
      <span className="muted">
        Tu plan «{plan.data.nombre}» tiene {lineas.length} {lineas.length === 1 ? 'cosa' : 'cosas'} para el {ETIQUETA_DIA[dia].toLowerCase()}.
      </span>
      <Button size="sm" variant="secondary" icon="utensils" loading={ocupado} onClick={() => void apuntar()}>
        Apuntar lo del plan
      </Button>
      {error && <span className="fus-apuntar__err">{error}</span>}
    </div>
  );
}
