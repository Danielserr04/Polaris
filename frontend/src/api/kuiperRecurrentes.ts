import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError, api } from './client';
import type { TipoMovimiento } from './kuiper';

// Recurrentes de Kuiper: suscripciones, recibos y pagos a plazos que generan un movimiento solos.
// Reflejan RecurrenteListDto, RecurrenteFormDto y RecurrenteRequestDto (docs/decisiones/034).

export type FrecuenciaRecurrente = 'SEMANAL' | 'MENSUAL' | 'ANUAL';

export interface RecurrenteList {
  id: number;
  concepto: string;
  importe: number;
  tipo: TipoMovimiento;
  categoriaId: number;
  categoriaNombre: string;
  categoriaColor: string | null;
  categoriaIcono: string | null;
  frecuencia: FrecuenciaRecurrente;
  /** Siguiente cargo pendiente (YYYY-MM-DD) */
  proximaFecha: string;
  /** Numero de plazos; null si no termina nunca */
  cuotasTotal: number | null;
  cuotasPagadas: number;
  activo: boolean;
}

export interface RecurrenteForm extends RecurrenteList {
  metodoPago: string | null;
  fechaInicio: string;
}

/** Cuerpo de POST/PUT. El PUT reemplaza todo: se manda siempre completo. */
export interface RecurrenteRequest {
  concepto: string;
  importe: number;
  tipo: TipoMovimiento;
  categoriaId: number;
  metodoPago: string | null;
  frecuencia: FrecuenciaRecurrente;
  fechaInicio: string;
  cuotasTotal: number | null;
  activo: boolean;
}

export interface FiltroRecurrentes {
  activo?: boolean;
  tipo?: TipoMovimiento;
  categoriaId?: number;
}

const BASE = '/api/kuiper/recurrente';

export const clavesRecurrentes = {
  lista: ['kuiper', 'recurrentes'] as const,
  ficha: (id: number) => ['kuiper', 'recurrente', id] as const,
};

export function useRecurrentes(filtro: FiltroRecurrentes = {}) {
  return useQuery({
    queryKey: [...clavesRecurrentes.lista, filtro] as const,
    queryFn: () => api<RecurrenteList[]>(BASE, { query: { ...filtro } }),
  });
}

/** La ficha completa: el listado no trae metodoPago ni fechaInicio. */
export function useRecurrente(id: number | undefined) {
  return useQuery({
    queryKey: clavesRecurrentes.ficha(id ?? 0),
    queryFn: () => api<RecurrenteForm>(`${BASE}/${id}`),
    enabled: id !== undefined,
    // Se vuelve a pedir cada vez que se abre: nada de editar sobre una copia vieja.
    gcTime: 0,
  });
}

/**
 * Un recurrente cambia su lista y, en cuanto pasa el job, los movimientos y resumenes: se
 * invalida todo Kuiper. El Inicio tambien enseña gastos.
 */
function useInvalidar() {
  const qc = useQueryClient();
  return () =>
    Promise.all([qc.invalidateQueries({ queryKey: ['kuiper'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
}

export function useCrearRecurrente() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (cuerpo: RecurrenteRequest) => api<RecurrenteForm>(BASE, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarRecurrente(id: number) {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (cuerpo: RecurrenteRequest) => api<RecurrenteForm>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/**
 * Pausa o reactiva. El PUT reemplaza todos los campos y el listado no trae metodoPago ni
 * fechaInicio, asi que se pide la ficha y se reenvia con `activo` cambiado.
 */
export function useCambiarActivoRecurrente() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: async ({ id, activo }: { id: number; activo: boolean }) => {
      const r = await api<RecurrenteForm>(`${BASE}/${id}`);
      return api<RecurrenteForm>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo: requestDe(r, { activo }) });
    },
    onSettled: invalidar,
  });
}

/** `alBorrar` se llama antes de invalidar, como en useBorrarMovimiento: cierra el dialogo antes de que la ficha se vuelva a pedir. */
export function useBorrarRecurrente(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: clavesRecurrentes.ficha(id) });
      return invalidar();
    },
  });
}

export function requestDe(r: RecurrenteForm, cambios: Partial<RecurrenteRequest> = {}): RecurrenteRequest {
  return {
    concepto: r.concepto,
    importe: r.importe,
    tipo: r.tipo,
    categoriaId: r.categoriaId,
    metodoPago: r.metodoPago,
    frecuencia: r.frecuencia,
    fechaInicio: r.fechaInicio,
    cuotasTotal: r.cuotasTotal,
    activo: r.activo,
    ...cambios,
  };
}

/** Lo que cuesta al mes: una semana son 52/12 cargos al mes y un año, 1/12. */
export function importeMensual(r: Pick<RecurrenteList, 'importe' | 'frecuencia'>): number {
  switch (r.frecuencia) {
    case 'SEMANAL':
      return (r.importe * 52) / 12;
    case 'ANUAL':
      return r.importe / 12;
    default:
      return r.importe;
  }
}

/** Con todas sus cuotas pagadas: el backend lo desactiva solo y no se puede reactivar sin mas plazos. */
export function terminado(r: Pick<RecurrenteList, 'cuotasTotal' | 'cuotasPagadas'>): boolean {
  return r.cuotasTotal !== null && r.cuotasPagadas >= r.cuotasTotal;
}

export const etiquetaFrecuencia: Record<FrecuenciaRecurrente, string> = {
  SEMANAL: 'Semanal',
  MENSUAL: 'Mensual',
  ANUAL: 'Anual',
};

/** Mensaje legible de un fallo de la API de recurrentes (el backend los escribe sin tildes). */
export function mensajeErrorRecurrente(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 404 && /categoria/i.test(e.message)) return 'Esa categoría ya no existe.';
    if (e.status === 400 && /tipo/i.test(e.message)) return 'El tipo del recurrente no coincide con el de la categoría.';
    if (e.status === 404) return 'Ese recurrente ya no existe.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
