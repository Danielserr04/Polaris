import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Contratos de Kuiper (gastos). Reflejan los DTO de src/main/java/com/polaris/kuiper/.

export type TipoMovimiento = 'INGRESO' | 'GASTO';

export interface GastoCategoria {
  categoriaId: number;
  categoriaNombre: string;
  categoriaColor: string | null;
  categoriaIcono: string | null;
  gastado: number;
  /** Presupuesto mensual de la categoria, si tiene */
  limiteMensual: number | null;
  restante: number | null;
}

export interface ResumenMensual {
  periodo: string;
  ingresos: number;
  gastos: number;
  balance: number;
  gastoPorCategoria: GastoCategoria[];
}

export interface MovimientoList {
  id: number;
  fecha: string;
  importe: number;
  tipo: TipoMovimiento;
  categoriaId: number;
  categoriaNombre: string;
  categoriaColor: string | null;
  categoriaIcono: string | null;
  concepto: string | null;
}

export interface MovimientoForm extends MovimientoList {
  metodoPago: string | null;
  recurrente: boolean;
}

/** Cuerpo de POST/PUT /movimiento. El PUT reemplaza todo, asi que se manda siempre completo. */
export interface MovimientoRequest {
  fecha: string;
  importe: number;
  tipo: TipoMovimiento;
  categoriaId: number;
  concepto: string | null;
  metodoPago: string | null;
  recurrente: boolean;
}

export interface Categoria {
  id: number;
  nombre: string;
  color: string | null;
  icono: string | null;
  tipo: TipoMovimiento;
}

export interface FiltroMovimientos {
  desde: string;
  hasta: string;
  tipo?: TipoMovimiento;
  categoriaId?: number;
}

const BASE = '/api/kuiper';

export const claves = {
  resumen: (periodo: string) => ['kuiper', 'resumen', periodo] as const,
  movimientos: ['kuiper', 'movimientos'] as const,
  movimiento: (id: number) => ['kuiper', 'movimiento', id] as const,
  categorias: ['kuiper', 'categorias'] as const,
};

export function useResumen(periodo: string) {
  return useQuery({
    queryKey: claves.resumen(periodo),
    queryFn: () => api<ResumenMensual>(`${BASE}/resumen`, { query: { periodo } }),
  });
}

export function useMovimientos(filtro: FiltroMovimientos) {
  return useQuery({
    queryKey: [...claves.movimientos, filtro] as const,
    queryFn: () => api<MovimientoList[]>(`${BASE}/movimiento`, { query: { ...filtro } }),
  });
}

/** La ficha completa de un movimiento: el listado no trae metodoPago ni recurrente. */
export function useMovimiento(id: number | undefined) {
  return useQuery({
    queryKey: claves.movimiento(id ?? 0),
    queryFn: () => api<MovimientoForm>(`${BASE}/movimiento/${id}`),
    enabled: id !== undefined,
    // Se vuelve a pedir cada vez que se abre: nada de editar sobre una copia vieja.
    gcTime: 0,
  });
}

export function useCategorias() {
  return useQuery({
    queryKey: claves.categorias,
    queryFn: () => api<Categoria[]>(`${BASE}/categoria`),
    staleTime: 60_000,
  });
}

/** Un movimiento cambia el listado, los resumenes de varios meses y la ficha. */
function useInvalidarKuiper() {
  const qc = useQueryClient();
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: ['kuiper'] }),
      // El Inicio tambien enseña gastos del mes y de hoy.
      qc.invalidateQueries({ queryKey: ['inicio'] }),
    ]);
}

export function useCrearMovimiento() {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    mutationFn: (cuerpo: MovimientoRequest) => api<MovimientoForm>(`${BASE}/movimiento`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarMovimiento(id: number) {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    mutationFn: (cuerpo: MovimientoRequest) => api<MovimientoForm>(`${BASE}/movimiento/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useBorrarMovimiento() {
  const qc = useQueryClient();
  const invalidar = useInvalidarKuiper();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/movimiento/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      // La ficha ya no existe: se quita de la cache para que no se vuelva a pedir (daria 404).
      qc.removeQueries({ queryKey: claves.movimiento(id) });
      return invalidar();
    },
  });
}

export function useCrearCategoria() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cuerpo: { nombre: string; tipo: TipoMovimiento }) =>
      api<Categoria>(`${BASE}/categoria`, { metodo: 'POST', cuerpo }),
    onSuccess: () => qc.invalidateQueries({ queryKey: claves.categorias }),
  });
}

/** Mensaje legible de un fallo de la API (el backend los escribe sin tildes). */
export function mensajeError(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 404 && /categoria/i.test(e.message)) return 'Esa categoría ya no existe.';
    if (e.status === 400 && /tipo/i.test(e.message)) return 'El tipo del movimiento no coincide con el de la categoría.';
    if (e.status === 404) return 'Ese movimiento ya no existe.';
    if (e.status === 409) return 'Ya tienes una categoría con ese nombre.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
