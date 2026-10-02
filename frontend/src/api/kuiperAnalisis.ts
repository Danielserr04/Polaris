import { useQuery } from '@tanstack/react-query';
import { api } from './client';

// Contratos de /api/kuiper/analisis. Reflejan los DTO de AnalisisController.
// Los null no viajan (Jackson non_null): por eso los campos opcionales van con `?`.

export interface EvolucionMensual {
  periodo: string;
  ingresos: number;
  gastos: number;
  balance: number;
  /** balance / ingresos en %; no viene si el mes no tiene ingresos */
  tasaAhorro?: number | null;
}

export interface CategoriaComparada {
  categoriaId: number;
  categoriaNombre: string;
  categoriaColor?: string | null;
  categoriaIcono?: string | null;
  gastado: number;
  /** Peso sobre el total del rango, en % */
  porcentaje: number;
  gastadoAnterior: number;
  diferencia: number;
  /** diferencia / gastadoAnterior en %; no viene si antes no hubo gasto */
  variacion?: number | null;
}

export interface ComparativaCategorias {
  desde: string;
  hasta: string;
  anteriorDesde: string;
  anteriorHasta: string;
  total: number;
  totalAnterior: number;
  categorias: CategoriaComparada[];
}

export interface ComercioFrecuente {
  nombre: string;
  total: number;
  veces: number;
  ticketMedio: number;
}

export type SeveridadInsight = 'AVISO' | 'BIEN' | 'INFO';

export interface Insight {
  tipo: string;
  severidad: SeveridadInsight;
  titulo: string;
  texto: string;
}

export interface ProyeccionMensual {
  periodo: string;
  estado: 'CERRADO' | 'EN_CURSO' | 'FUTURO';
  diasMes: number;
  diasTranscurridos: number;
  gastoActual: number;
  gastoVariable: number;
  ritmoDiario: number;
  proyeccionVariable: number;
  recurrentesPendientes: number;
  cargosPendientes: number;
  gastoProyectado: number;
  /** Suma de presupuestos mensuales; no viene si no hay ninguno */
  presupuestoMensual?: number | null;
}

const BASE = '/api/kuiper/analisis';

// Bajo ['kuiper', ...]: cualquier cambio en Kuiper ya invalida ese prefijo.
export const clavesAnalisis = {
  evolucion: (hasta: string, meses: number) => ['kuiper', 'analisis', 'evolucion', hasta, meses] as const,
  categorias: (desde: string, hasta: string) => ['kuiper', 'analisis', 'categorias', desde, hasta] as const,
  comercios: (desde: string, hasta: string, limite: number) =>
    ['kuiper', 'analisis', 'comercios', desde, hasta, limite] as const,
  insights: (periodo: string) => ['kuiper', 'analisis', 'insights', periodo] as const,
  proyeccion: (periodo: string) => ['kuiper', 'analisis', 'proyeccion', periodo] as const,
};

export function useEvolucion(hasta: string, meses = 6) {
  return useQuery({
    queryKey: clavesAnalisis.evolucion(hasta, meses),
    queryFn: () => api<EvolucionMensual[]>(`${BASE}/evolucion`, { query: { hasta, meses } }),
  });
}

export function useComparativaCategorias(desde: string, hasta: string) {
  return useQuery({
    queryKey: clavesAnalisis.categorias(desde, hasta),
    queryFn: () => api<ComparativaCategorias>(`${BASE}/categorias`, { query: { desde, hasta } }),
  });
}

export function useComercios(desde: string, hasta: string, limite = 10) {
  return useQuery({
    queryKey: clavesAnalisis.comercios(desde, hasta, limite),
    queryFn: () => api<ComercioFrecuente[]>(`${BASE}/comercios`, { query: { desde, hasta, limite } }),
  });
}

export function useInsights(periodo: string) {
  return useQuery({
    queryKey: clavesAnalisis.insights(periodo),
    queryFn: () => api<Insight[]>(`${BASE}/insights`, { query: { periodo } }),
  });
}

export function useProyeccion(periodo: string) {
  return useQuery({
    queryKey: clavesAnalisis.proyeccion(periodo),
    queryFn: () => api<ProyeccionMensual>(`${BASE}/proyeccion`, { query: { periodo } }),
  });
}
