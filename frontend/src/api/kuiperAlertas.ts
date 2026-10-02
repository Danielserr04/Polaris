import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import { CategoriaConMovimientos, type EstadoPresupuesto, type MovimientoList, type Presupuesto } from './kuiper';

// Umbral de alerta de los presupuestos y resumen de los anuales.
// Ver docs/decisiones/035-presupuesto-umbral-de-alerta.md.

const BASE = '/api/kuiper';

export const ALERTA_POR_DEFECTO = 80;

export interface GastoAnualCategoria {
  categoriaId: number;
  categoriaNombre: string;
  categoriaColor: string | null;
  categoriaIcono: string | null;
  gastado: number;
  limite: number;
  restante: number;
  porcentaje: number;
  porcentajeAlerta: number;
  estado: EstadoPresupuesto;
}

export interface ResumenAnual {
  anio: number;
  /** Del mas consumido al menos */
  presupuestos: GastoAnualCategoria[];
}

export const clavesAlertas = {
  resumenAnual: (anio: number) => ['kuiper', 'resumen-anual', anio] as const,
  presupuestosAnuales: ['kuiper', 'presupuestos', 'anual'] as const,
};

export function useResumenAnual(anio: number) {
  return useQuery({
    queryKey: clavesAlertas.resumenAnual(anio),
    queryFn: () => api<ResumenAnual>(`${BASE}/resumen/anual`, { query: { anio } }),
  });
}

/** Los presupuestos anuales (uno por categoria de gasto como mucho). */
export function usePresupuestosAnuales() {
  return useQuery({
    queryKey: clavesAlertas.presupuestosAnuales,
    queryFn: () => api<Presupuesto[]>(`${BASE}/presupuesto`, { query: { periodo: 'ANUAL' } }),
  });
}

function useInvalidar() {
  const qc = useQueryClient();
  return () =>
    Promise.all([qc.invalidateQueries({ queryKey: ['kuiper'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
}

export interface GuardarPresupuesto {
  categoriaId: number;
  periodo: 'MENSUAL' | 'ANUAL';
  existente?: Presupuesto;
  /** null quita el presupuesto */
  importe: number | null;
  porcentajeAlerta: number;
}

/** Un presupuesto de un periodo, con su umbral: crea, actualiza o borra segun lo que haya y lo que se pida. */
export function useGuardarPresupuestoConAlerta() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: async ({ categoriaId, periodo, existente, importe, porcentajeAlerta }: GuardarPresupuesto) => {
      if (importe === null) {
        if (existente) await api<void>(`${BASE}/presupuesto/${existente.id}`, { metodo: 'DELETE' });
        return;
      }
      const cuerpo = { categoriaId, periodo, importeLimite: importe, porcentajeAlerta };
      if (existente) await api(`${BASE}/presupuesto/${existente.id}`, { metodo: 'PUT', cuerpo });
      else await api(`${BASE}/presupuesto`, { metodo: 'POST', cuerpo });
    },
    onSuccess: invalidar,
  });
}

/**
 * Como useBorrarCategoria de ./kuiper, pero quitando todos sus presupuestos (mensual y anual):
 * con uno anual el backend no deja borrar la categoria. Se comprueba antes que no tenga
 * movimientos, para que un fallo no la deje sin presupuestos.
 */
export function useBorrarCategoriaConPresupuestos() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: async ({ id, presupuestoIds }: { id: number; presupuestoIds: number[] }) => {
      const movimientos = await api<MovimientoList[]>(`${BASE}/movimiento`, { query: { categoriaId: id } });
      if (movimientos.length > 0) throw new CategoriaConMovimientos(movimientos.length);
      for (const p of presupuestoIds) await api<void>(`${BASE}/presupuesto/${p}`, { metodo: 'DELETE' });
      await api<void>(`${BASE}/categoria/${id}`, { metodo: 'DELETE' });
    },
    onSuccess: invalidar,
    onError: invalidar,
  });
}

/** Tono del design system para cada estado. */
export function tonoEstado(estado: EstadoPresupuesto): 'success' | 'warning' | 'danger' | 'neutral' {
  return estado === 'EXCEDIDO' ? 'danger' : estado === 'AVISO' ? 'warning' : estado === 'OK' ? 'success' : 'neutral';
}

/** Color de la barra para cada estado. */
export function colorEstado(estado: EstadoPresupuesto): string | undefined {
  return estado === 'EXCEDIDO' ? 'var(--danger)' : estado === 'AVISO' ? 'var(--warning)' : estado === 'OK' ? 'var(--success)' : undefined;
}

export function etiquetaEstado(estado: EstadoPresupuesto): string {
  return estado === 'EXCEDIDO' ? 'Excedido' : estado === 'AVISO' ? 'Aviso' : estado === 'OK' ? 'Bien' : 'Sin presupuesto';
}

/** 82.5 -> "82,5 %" */
export function pctTexto(p: number | null): string {
  return p === null ? '—' : `${p.toLocaleString('es-ES', { maximumFractionDigits: 1 })} %`;
}
