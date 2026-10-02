import { useQuery } from '@tanstack/react-query';
import type { TrabajoMuscular } from './atlas';
import { api } from './client';

// Lo que necesita el Inicio de cada modulo. Cada consulta es independiente: si una
// falla, solo se cae su banda. Los tipos reflejan los DTO de listado/resumen del backend.

export type EstadoEntrada = 'PENDIENTE' | 'EN_CURSO' | 'TERMINADO' | 'ABANDONADO';
export type TipoContenido = 'PELICULA' | 'SERIE' | 'JUEGO' | 'LIBRO';
export type MomentoComida = 'DESAYUNO' | 'COMIDA' | 'CENA' | 'SNACK';

export interface EntradaResumen {
  id: number;
  tituloTitulo: string;
  tituloTipo: TipoContenido;
  estado: EstadoEntrada;
  progreso?: number;
}

export interface GastoCategoria {
  categoriaId: number;
  categoriaNombre: string;
  gastado: number;
  limiteMensual?: number;
  restante?: number;
}

export interface ResumenMensual {
  periodo: string;
  ingresos: number;
  gastos: number;
  balance: number;
  gastoPorCategoria: GastoCategoria[];
}

export interface MovimientoResumen {
  id: number;
  fecha: string;
  importe: number;
  tipo: 'INGRESO' | 'GASTO';
  categoriaNombre: string;
  concepto?: string;
}

export interface MacroResumen {
  consumido: number;
  objetivo?: number;
  restante?: number;
  porcentaje?: number;
}

export interface ResumenDiario {
  fecha: string;
  objetivoVigenteDesde?: string;
  kcal: MacroResumen;
  proteinas: MacroResumen;
  carbohidratos: MacroResumen;
  grasas: MacroResumen;
}

export interface ComidaResumen {
  id: number;
  fecha: string;
  momento: MomentoComida;
  kcalTotal: number;
}

export interface SesionResumen {
  id: number;
  fecha: string;
  rutinaId?: number;
  rutinaNombre?: string;
  duracionMin?: number;
  numeroSeries: number;
}

export interface PesoResumen {
  id: number;
  fecha: string;
  pesoKg: number;
}

export const useEntradas = () =>
  useQuery({ queryKey: ['inicio', 'entradas'], queryFn: () => api<EntradaResumen[]>('/api/odisea/entrada') });

export const useResumenMes = (periodo: string) =>
  useQuery({
    queryKey: ['inicio', 'resumen-mes', periodo],
    queryFn: () => api<ResumenMensual>('/api/kuiper/resumen', { query: { periodo } }),
  });

export const useMovimientos = (desde: string, hasta: string) =>
  useQuery({
    queryKey: ['inicio', 'movimientos', desde, hasta],
    queryFn: () => api<MovimientoResumen[]>('/api/kuiper/movimiento', { query: { desde, hasta } }),
  });

export const useResumenDia = (fecha: string) =>
  useQuery({
    queryKey: ['inicio', 'resumen-dia', fecha],
    queryFn: () => api<ResumenDiario>('/api/fusion/resumen', { query: { fecha } }),
  });

export const useComidas = (desde: string, hasta: string) =>
  useQuery({
    queryKey: ['inicio', 'comidas', desde, hasta],
    queryFn: () => api<ComidaResumen[]>('/api/fusion/comida', { query: { desde, hasta } }),
  });

export const useSesiones = (desde: string, hasta: string) =>
  useQuery({
    queryKey: ['inicio', 'sesiones', desde, hasta],
    queryFn: () => api<SesionResumen[]>('/api/atlas/sesion', { query: { desde, hasta } }),
  });

/** Grupos musculares trabajados un dia (el de la ultima sesion); no pide nada hasta saber la fecha. */
export const useTrabajoDia = (fecha: string | undefined) =>
  useQuery({
    queryKey: ['inicio', 'trabajo-muscular', fecha],
    queryFn: () => api<TrabajoMuscular[]>('/api/atlas/trabajo-muscular', { query: { desde: fecha!, hasta: fecha! } }),
    enabled: fecha !== undefined,
  });

export const usePesos = (desde: string, hasta: string) =>
  useQuery({
    queryKey: ['inicio', 'pesos', desde, hasta],
    queryFn: () => api<PesoResumen[]>('/api/nucleo/registro-peso', { query: { desde, hasta } }),
  });
