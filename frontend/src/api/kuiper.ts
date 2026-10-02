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
  /** gastado / limite * 100 con un decimal; null sin presupuesto */
  porcentaje: number | null;
  porcentajeAlerta: number | null;
  estado: EstadoPresupuesto;
}

export type EstadoPresupuesto = 'SIN_PRESUPUESTO' | 'OK' | 'AVISO' | 'EXCEDIDO';

export interface ResumenMensual {
  periodo: string;
  ingresos: number;
  gastos: number;
  balance: number;
  gastoPorCategoria: GastoCategoria[];
  /** Suma de los presupuestos mensuales */
  presupuestoTotal: number;
  categoriasEnAviso: number;
  categoriasExcedidas: number;
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

export interface Presupuesto {
  id: number;
  categoriaId: number;
  periodo: 'MENSUAL' | 'ANUAL';
  importeLimite: number;
  /** % del limite a partir del cual avisa (1 a 100) */
  porcentajeAlerta: number;
}

export interface CategoriaRequest {
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
  presupuestos: ['kuiper', 'presupuestos'] as const,
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

/** Los presupuestos mensuales (uno por categoria de gasto como mucho). */
export function usePresupuestosMensuales() {
  return useQuery({
    queryKey: claves.presupuestos,
    queryFn: () => api<Presupuesto[]>(`${BASE}/presupuesto`, { query: { periodo: 'MENSUAL' } }),
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
    meta: { aviso: 'Movimiento añadido' },
    mutationFn: (cuerpo: MovimientoRequest) => api<MovimientoForm>(`${BASE}/movimiento`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarMovimiento(id: number) {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    meta: { aviso: 'Movimiento guardado' },
    mutationFn: (cuerpo: MovimientoRequest) => api<MovimientoForm>(`${BASE}/movimiento/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/**
 * `alBorrar` se llama nada mas borrar, antes de invalidar: el dialogo de edicion debe cerrarse
 * antes de que la ficha ya borrada se vuelva a pedir (daria 404). Va en el hook y no en `mutate`
 * porque ese componente se desmonta durante la recarga y sus callbacks ya no se ejecutan.
 */
export function useBorrarMovimiento(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarKuiper();
  return useMutation({
    meta: { aviso: 'Movimiento borrado' },
    mutationFn: (id: number) => api<void>(`${BASE}/movimiento/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: claves.movimiento(id) });
      return invalidar();
    },
  });
}

export function useCrearCategoria() {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    meta: { aviso: 'Categoría añadida' },
    mutationFn: (cuerpo: Partial<CategoriaRequest> & Pick<CategoriaRequest, 'nombre' | 'tipo'>) =>
      api<Categoria>(`${BASE}/categoria`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarCategoria(id: number) {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    meta: { aviso: 'Categoría guardada' },
    mutationFn: (cuerpo: CategoriaRequest) => api<Categoria>(`${BASE}/categoria/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/**
 * Borra una categoria. Si tiene presupuesto se borra antes (no tiene sentido sin ella), pero
 * solo despues de comprobar que no tiene movimientos: asi un fallo no deja la categoria
 * sin su presupuesto.
 */
export function useBorrarCategoria() {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    meta: { aviso: 'Categoría borrada' },
    mutationFn: async ({ id, presupuestoId }: { id: number; presupuestoId?: number }) => {
      const movimientos = await api<MovimientoList[]>(`${BASE}/movimiento`, { query: { categoriaId: id } });
      if (movimientos.length > 0) throw new CategoriaConMovimientos(movimientos.length);
      if (presupuestoId !== undefined) await api<void>(`${BASE}/presupuesto/${presupuestoId}`, { metodo: 'DELETE' });
      await api<void>(`${BASE}/categoria/${id}`, { metodo: 'DELETE' });
    },
    onSuccess: invalidar,
    // Aunque falle a medias, se vuelve a pedir lo que haya cambiado.
    onError: invalidar,
  });
}

/** Un presupuesto mensual: crea, actualiza o borra segun lo que haya y lo que se pida. */
export function useGuardarPresupuesto() {
  const invalidar = useInvalidarKuiper();
  return useMutation({
    mutationFn: async ({ categoriaId, existente, importe }: { categoriaId: number; existente?: Presupuesto; importe: number | null }) => {
      if (importe === null) {
        if (existente) await api<void>(`${BASE}/presupuesto/${existente.id}`, { metodo: 'DELETE' });
        return;
      }
      const cuerpo = { categoriaId, periodo: 'MENSUAL', importeLimite: importe };
      if (existente) await api(`${BASE}/presupuesto/${existente.id}`, { metodo: 'PUT', cuerpo });
      else await api(`${BASE}/presupuesto`, { metodo: 'POST', cuerpo });
    },
    onSuccess: invalidar,
  });
}

export class CategoriaConMovimientos extends Error {
  readonly cantidad: number;

  constructor(cantidad: number) {
    super('La categoria tiene movimientos');
    this.cantidad = cantidad;
  }
}

/** Mensaje legible de un fallo de la API (el backend los escribe sin tildes). */
export function mensajeError(e: unknown): string {
  if (e instanceof CategoriaConMovimientos) {
    return `No se puede borrar: tiene ${e.cantidad} ${e.cantidad === 1 ? 'movimiento' : 'movimientos'}. Muévelos a otra categoría o bórralos antes.`;
  }
  if (e instanceof ApiError) {
    if (e.status === 404 && /categoria/i.test(e.message)) return 'Esa categoría ya no existe.';
    if (e.status === 400 && /tipo/i.test(e.message)) return 'El tipo del movimiento no coincide con el de la categoría.';
    if (e.status === 404) return 'Ese movimiento ya no existe.';
    if (e.status === 409) return 'Ya tienes una categoría con ese nombre.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
