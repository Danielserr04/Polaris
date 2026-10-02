import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';

// Recordatorios y avisos al movil. Reflejan RecordatorioListDto, RecordatorioPendienteDto y
// los DTO de PushController de src/main/java/com/polaris/nucleo/.
// Ver docs/decisiones/044-recordatorios.md.

export type TipoRecordatorio = 'COMIDAS' | 'GASTOS' | 'ENTRENO' | 'PRESUPUESTO';

export interface Recordatorio {
  tipo: TipoRecordatorio;
  activo: boolean;
  /** HH:mm, hora de Madrid */
  hora: string;
  /** 1 = lunes … 7 = domingo, ordenados */
  dias: number[];
}

export interface RecordatorioPendiente {
  tipo: TipoRecordatorio;
  titulo: string;
  texto: string;
  /** Ruta del frontend: /fusion, /kuiper, /atlas */
  enlace: string;
}

const BASE = '/api/nucleo/recordatorio';

export const clavesRecordatorios = {
  todas: ['recordatorios'] as const,
  lista: ['recordatorios', 'lista'] as const,
  pendientes: ['recordatorios', 'pendientes'] as const,
};

/** La campana vuelve a mirar cada minuto: los recordatorios aparecen al llegar su hora. */
export const INTERVALO_PENDIENTES_MS = 60_000;

export function useRecordatorios() {
  return useQuery({ queryKey: clavesRecordatorios.lista, queryFn: () => api<Recordatorio[]>(BASE) });
}

export function useRecordatoriosPendientes() {
  return useQuery({
    queryKey: clavesRecordatorios.pendientes,
    queryFn: () => api<RecordatorioPendiente[]>(`${BASE}/pendientes`),
    refetchInterval: INTERVALO_PENDIENTES_MS,
    refetchOnWindowFocus: true,
  });
}

export function useGuardarRecordatorio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ tipo, ...cuerpo }: Recordatorio) =>
      api<Recordatorio>(`${BASE}/${tipo}`, { metodo: 'PUT', cuerpo }),
    onSuccess: () => qc.invalidateQueries({ queryKey: clavesRecordatorios.todas }),
  });
}

export function useDescartarRecordatorio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (tipo: TipoRecordatorio) => api<void>(`${BASE}/${tipo}/descartar`, { metodo: 'POST' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: clavesRecordatorios.pendientes }),
  });
}

// ---------------------------------------------------------------- push

export const obtenerClavePush = () => api<{ clavePublica: string }>('/api/nucleo/push/clave');

export const guardarSuscripcionPush = (s: { endpoint: string; p256dh: string; auth: string }) =>
  api<unknown>('/api/nucleo/push/suscripcion', { metodo: 'POST', cuerpo: s });

export const borrarSuscripcionPush = (endpoint: string) =>
  api<void>('/api/nucleo/push/suscripcion', { metodo: 'DELETE', query: { endpoint } });

export function useProbarPush() {
  return useMutation({
    mutationFn: () => api<{ enviados: number }>('/api/nucleo/push/prueba', { metodo: 'POST' }),
  });
}
