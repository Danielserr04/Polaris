import { useQuery } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Contratos de Atlas (gym). Reflejan los DTO de src/main/java/com/polaris/atlas/.
// El backend omite los campos nulos: un campo opcional llega como `undefined`, no como `null`.

export interface Sesion {
  id: number;
  fecha: string;
  rutinaId?: number | null;
  rutinaNombre?: string | null;
  duracionMin?: number | null;
  numeroSeries: number;
}

export interface Serie {
  id: number;
  ejercicioId: number;
  ejercicioNombre: string;
  ejercicioGrupoMuscular?: string | null;
  numeroSerie: number;
  reps: number;
  pesoKg: number;
  rpe?: number | null;
}

/** Lo que devuelve GET /sesion/{id}: la sesion con sus series. */
export interface SesionCompleta {
  id: number;
  fecha: string;
  rutinaId?: number | null;
  rutinaNombre?: string | null;
  duracionMin?: number | null;
  notas?: string | null;
  series: Serie[];
}

/** Una fila por sesion en la que se hizo el ejercicio. */
export interface PuntoProgresion {
  sesionId: number;
  fecha: string;
  volumen: number;
  numeroSeries: number;
  pesoMaximo: number;
  repsTotales: number;
}

export interface RecordEjercicio {
  ejercicioId: number;
  ejercicioNombre: string;
  ejercicioGrupoMuscular?: string | null;
  pesoMaximo: number;
  repsPesoMaximo: number;
  fechaPesoMaximo: string;
  volumenMaximoSesion?: number | null;
  fechaVolumenMaximo?: string | null;
}

export interface PesoCorporal {
  fecha: string;
  pesoKg: number;
  grasaPct?: number | null;
  notas?: string | null;
}

export const mensajeError = (e: unknown): string => (e instanceof ApiError ? e.message : 'No se ha podido completar la operación.');

const BASE = '/api/atlas';

export const claves = {
  sesiones: ['atlas', 'sesiones'] as const,
  records: ['atlas', 'records'] as const,
  progresion: ['atlas', 'progresion'] as const,
  pesos: ['atlas', 'pesos'] as const,
};

/** Listado ligero de sesiones (sin series), de la mas reciente a la mas antigua. */
export function useSesiones(desde: string, hasta: string) {
  return useQuery({
    queryKey: [...claves.sesiones, 'rango', desde, hasta] as const,
    queryFn: () => api<Sesion[]>(`${BASE}/sesion`, { query: { desde, hasta } }),
  });
}

/**
 * Las sesiones con sus series. El listado solo trae el numero de series, asi que se pide cada
 * una completa (el llamador acota cuantas) dentro de una sola consulta.
 */
export function useSesionesCompletas(ids: number[]) {
  return useQuery({
    queryKey: [...claves.sesiones, 'completas', ids] as const,
    queryFn: () => Promise.all(ids.map((id) => api<SesionCompleta>(`${BASE}/sesion/${id}`))),
    enabled: ids.length > 0,
  });
}

export function useRecords() {
  return useQuery({ queryKey: claves.records, queryFn: () => api<RecordEjercicio[]>(`${BASE}/records`) });
}

export function useProgresion(ejercicioId: number | undefined, desde: string, hasta: string) {
  return useQuery({
    queryKey: [...claves.progresion, ejercicioId, desde, hasta] as const,
    queryFn: () => api<PuntoProgresion[]>(`${BASE}/progresion`, { query: { ejercicioId, desde, hasta } }),
    enabled: ejercicioId !== undefined,
  });
}

export function usePesos(desde: string, hasta: string) {
  return useQuery({
    queryKey: [...claves.pesos, desde, hasta] as const,
    queryFn: () => api<PesoCorporal[]>(`${BASE}/peso`, { query: { desde, hasta } }),
  });
}
