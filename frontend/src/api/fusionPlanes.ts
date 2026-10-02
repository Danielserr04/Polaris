import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';
import type { MomentoComida } from './fusion';

// Planes de comidas de Fusion. Reflejan los DTO de PlanComidaController.

export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO';

export const DIAS: DiaSemana[] = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

export const ETIQUETA_DIA: Record<DiaSemana, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo',
};

export const INICIAL_DIA: Record<DiaSemana, string> = {
  LUNES: 'L',
  MARTES: 'M',
  MIERCOLES: 'X',
  JUEVES: 'J',
  VIERNES: 'V',
  SABADO: 'S',
  DOMINGO: 'D',
};

/** Dia del plan que toca en una fecha (getDay: 0 = domingo). */
export function diaDeFecha(d: Date): DiaSemana {
  return DIAS[(d.getDay() + 6) % 7];
}

/** Una linea: alimento con gramos o receta con raciones; los campos del otro tipo no llegan. */
export interface PlanLinea {
  id: number;
  diaSemana: DiaSemana;
  momento: MomentoComida;
  alimentoId?: number | null;
  alimentoNombre?: string | null;
  alimentoMarca?: string | null;
  cantidadG?: number | null;
  recetaId?: number | null;
  recetaNombre?: string | null;
  raciones?: number | null;
  kcal: number;
  proteinas: number;
  carbohidratos: number;
  grasas: number;
}

export interface PlanComida {
  id: number;
  nombre: string;
  activo: boolean;
  numLineas: number;
  diasConLineas: number;
  kcalMediaDiaria: number;
  proteinasMediaDiaria: number;
  carbohidratosMediaDiaria: number;
  grasasMediaDiaria: number;
}

export interface PlanCompleto {
  id: number;
  nombre: string;
  activo: boolean;
  lineas: PlanLinea[];
  diasConLineas: number;
  kcalMediaDiaria: number;
  proteinasMediaDiaria: number;
  carbohidratosMediaDiaria: number;
  grasasMediaDiaria: number;
}

export interface PlanLineaRequest {
  diaSemana: DiaSemana;
  momento: MomentoComida;
  alimentoId: number | null;
  cantidadG: number | null;
  recetaId: number | null;
  raciones: number | null;
}

export interface PlanRequest {
  nombre: string;
  lineas: PlanLineaRequest[];
}

export interface ArticuloCompra {
  alimentoId: number;
  nombre: string;
  marca?: string | null;
  cantidadG: number;
}

const BASE = '/api/fusion/plan';

export const clavesPlan = {
  todos: ['fusion', 'planes'] as const,
  uno: (id: number) => ['fusion', 'plan', id] as const,
  compra: (id: number) => ['fusion', 'plan', id, 'compra'] as const,
};

export function usePlanes() {
  return useQuery({
    queryKey: clavesPlan.todos,
    queryFn: () => api<PlanComida[]>(BASE),
  });
}

export function usePlan(id: number | undefined) {
  return useQuery({
    queryKey: clavesPlan.uno(id ?? 0),
    queryFn: () => api<PlanCompleto>(`${BASE}/${id}`),
    enabled: id !== undefined,
  });
}

/** El plan activo completo, o null si no hay ninguno. */
export function usePlanActivo() {
  return useQuery({
    queryKey: [...clavesPlan.todos, 'activo'] as const,
    queryFn: async () => {
      const activos = await api<PlanComida[]>(BASE, { query: { activo: 'true' } });
      return activos.length ? api<PlanCompleto>(`${BASE}/${activos[0].id}`) : null;
    },
  });
}

export function useListaCompra(id: number) {
  return useQuery({
    queryKey: clavesPlan.compra(id),
    queryFn: () => api<ArticuloCompra[]>(`${BASE}/${id}/lista-compra`),
  });
}

function useInvalidarPlanes() {
  const qc = useQueryClient();
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: clavesPlan.todos }),
      qc.invalidateQueries({ queryKey: ['fusion', 'plan'] }),
    ]);
}

export function useCrearPlan() {
  const invalidar = useInvalidarPlanes();
  return useMutation({
    mutationFn: (cuerpo: PlanRequest) => api<PlanCompleto>(BASE, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
    meta: { aviso: 'Plan creado' },
  });
}

export function useActualizarPlan(id: number) {
  const invalidar = useInvalidarPlanes();
  return useMutation({
    mutationFn: (cuerpo: PlanRequest) => api<PlanCompleto>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
    meta: { aviso: 'Plan guardado' },
  });
}

export function useActivarPlan() {
  const invalidar = useInvalidarPlanes();
  return useMutation({
    mutationFn: (id: number) => api<PlanCompleto>(`${BASE}/${id}/activar`, { metodo: 'POST' }),
    onSuccess: invalidar,
    meta: { aviso: 'Plan activado' },
  });
}

/** `alBorrar` se llama antes de invalidar, por lo mismo que en useBorrarComida. */
export function useBorrarPlan(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarPlanes();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: clavesPlan.uno(id) });
      return invalidar();
    },
    meta: { aviso: 'Plan borrado' },
  });
}

/** Mensaje legible de un fallo de la API de planes. */
export function mensajeErrorPlan(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 404 && /receta/i.test(e.message)) return 'Alguna de las recetas ya no existe.';
    if (e.status === 404 && /alimento/i.test(e.message)) return 'Alguno de los alimentos ya no existe.';
    if (e.status === 404) return 'Ese plan ya no existe.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}

/** Gramos legibles: a partir de un kilo, en kg. */
export function gramos(g: number): string {
  return g >= 1000
    ? `${(g / 1000).toLocaleString('es-ES', { maximumFractionDigits: 2 })} kg`
    : `${Math.round(g).toLocaleString('es-ES')} g`;
}
