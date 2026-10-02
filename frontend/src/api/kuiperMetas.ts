import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Metas de ahorro de Kuiper. Reflejan los DTO de MetaAhorroController
// (src/main/java/com/polaris/kuiper/). Ver docs/decisiones/036-meta-ahorro-con-aportaciones.md.

/** Lo que pinta cada tarjeta. Todo lo que no es configuracion lo calcula el backend. */
export interface MetaAhorro {
  id: number;
  nombre: string;
  importeObjetivo: number;
  /** YYYY-MM-DD o null si no tiene fecha */
  fechaLimite: string | null;
  color: string | null;
  icono: string | null;
  /** Suma de las aportaciones (nunca negativa) */
  importeActual: number;
  /** Con un decimal, truncado; puede pasar de 100 */
  porcentaje: number;
  restante: number;
  completada: boolean;
  /** Negativo si la fecha ya paso; null sin fecha */
  diasRestantes: number | null;
  /** null sin fecha o si ya esta completada */
  ahorroMensualNecesario: number | null;
}

export interface MetaAhorroRequest {
  nombre: string;
  importeObjetivo: number;
  fechaLimite: string | null;
  color: string | null;
  icono: string | null;
}

export interface AportacionMeta {
  id: number;
  fecha: string;
  /** Positivo aporta, negativo retira */
  importe: number;
  nota: string | null;
}

export interface AportacionRequest {
  importe: number;
  fecha?: string;
  nota?: string | null;
}

const BASE = '/api/kuiper/meta';

export const clavesMetas = {
  metas: ['kuiper', 'metas'] as const,
  aportaciones: (metaId: number) => ['kuiper', 'metas', metaId, 'aportaciones'] as const,
};

export function useMetas() {
  return useQuery({
    queryKey: clavesMetas.metas,
    queryFn: () => api<MetaAhorro[]>(BASE),
  });
}

export function useAportaciones(metaId: number) {
  return useQuery({
    queryKey: clavesMetas.aportaciones(metaId),
    queryFn: () => api<AportacionMeta[]>(`${BASE}/${metaId}/aportacion`),
  });
}

/** Cualquier cambio en una meta o su historial invalida todas las metas (el listado trae los totales). */
function useInvalidarMetas() {
  const qc = useQueryClient();
  return () => qc.invalidateQueries({ queryKey: clavesMetas.metas });
}

export function useCrearMeta() {
  const invalidar = useInvalidarMetas();
  return useMutation({
    mutationFn: (cuerpo: MetaAhorroRequest) => api<MetaAhorro>(BASE, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarMeta(id: number) {
  const invalidar = useInvalidarMetas();
  return useMutation({
    mutationFn: (cuerpo: MetaAhorroRequest) => api<MetaAhorro>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useBorrarMeta() {
  const invalidar = useInvalidarMetas();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: invalidar,
  });
}

/** Aporta (importe positivo) o retira (negativo). Devuelve la meta con el total nuevo. */
export function useAportar(metaId: number) {
  const invalidar = useInvalidarMetas();
  return useMutation({
    mutationFn: (cuerpo: AportacionRequest) =>
      api<MetaAhorro>(`${BASE}/${metaId}/aportacion`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useBorrarAportacion(metaId: number) {
  const invalidar = useInvalidarMetas();
  return useMutation({
    mutationFn: (aportacionId: number) =>
      api<void>(`${BASE}/${metaId}/aportacion/${aportacionId}`, { metodo: 'DELETE' }),
    onSuccess: invalidar,
  });
}

/** Mensaje legible de un fallo de la API de metas (el backend los escribe sin tildes). */
export function mensajeErrorMeta(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 409) return 'Ya tienes una meta con ese nombre.';
    if (e.status === 404) return 'Esa meta ya no existe.';
    if (e.status === 400 && /retirar/i.test(e.message)) return 'No puedes retirar más de lo ahorrado.';
    if (e.status === 400 && /negativo/i.test(e.message)) {
      return 'No se puede borrar: el total quedaría en negativo. Borra antes las retiradas posteriores.';
    }
    if (e.status === 400 && /futura/i.test(e.message)) return 'La fecha no puede ser futura.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
