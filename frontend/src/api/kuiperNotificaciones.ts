import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';

// Notificaciones de Kuiper. Reflejan NotificacionListDto / NotificacionTotalDto de
// src/main/java/com/polaris/kuiper/. Ver docs/decisiones/040-notificaciones-de-kuiper.md.

export type TipoNotificacion =
  | 'CARGO_RECURRENTE'
  | 'CARGO_PROXIMO'
  | 'PRESUPUESTO_AVISO'
  | 'PRESUPUESTO_EXCEDIDO'
  | 'META_ALCANZADA'
  | 'RESUMEN_MENSUAL';

/** A que pestaña de Kuiper lleva la notificacion. Lo decide el backend. */
export type EnlaceNotificacion = 'movimientos' | 'recurrentes' | 'presupuestos' | 'resumen' | 'metas';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  titulo: string;
  texto: string;
  enlace: EnlaceNotificacion | null;
  leida: boolean;
  /** ISO local, sin zona: 2026-10-02T08:00:00 */
  creadaEn: string;
}

const BASE = '/api/kuiper/notificacion';

export const clavesNotificaciones = {
  todas: ['kuiper', 'notificaciones'] as const,
  lista: ['kuiper', 'notificaciones', 'lista'] as const,
  noLeidas: ['kuiper', 'notificaciones', 'no-leidas'] as const,
};

/** Cada minuto se vuelve a pedir el contador: los avisos llegan solos (job diario, cargos, presupuestos). */
export const INTERVALO_CONTADOR_MS = 60_000;

export function useNotificacionesNoLeidas() {
  return useQuery({
    queryKey: clavesNotificaciones.noLeidas,
    queryFn: () => api<{ total: number }>(`${BASE}/no-leidas/total`),
    select: (r) => r.total,
    refetchInterval: INTERVALO_CONTADOR_MS,
  });
}

/** La lista solo se pide con el panel abierto. */
export function useNotificaciones(abierto: boolean) {
  return useQuery({
    queryKey: clavesNotificaciones.lista,
    queryFn: () => api<Notificacion[]>(BASE),
    enabled: abierto,
  });
}

function useInvalidarNotificaciones() {
  const qc = useQueryClient();
  return () => qc.invalidateQueries({ queryKey: clavesNotificaciones.todas });
}

export function useMarcarLeida() {
  const invalidar = useInvalidarNotificaciones();
  return useMutation({
    mutationFn: ({ id, leida = true }: { id: number; leida?: boolean }) =>
      api<Notificacion>(`${BASE}/${id}/leida`, { metodo: 'PUT', cuerpo: { leida } }),
    onSuccess: invalidar,
  });
}

export function useLeerTodas() {
  const invalidar = useInvalidarNotificaciones();
  return useMutation({
    mutationFn: () => api<void>(`${BASE}/leer-todas`, { metodo: 'POST' }),
    onSuccess: invalidar,
  });
}

export function useBorrarNotificacion() {
  const invalidar = useInvalidarNotificaciones();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: invalidar,
  });
}

export function useBorrarLeidas() {
  const invalidar = useInvalidarNotificaciones();
  return useMutation({
    mutationFn: () => api<void>(BASE, { metodo: 'DELETE' }),
    onSuccess: invalidar,
  });
}
