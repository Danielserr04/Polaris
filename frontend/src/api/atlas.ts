import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
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
  /** Ejercicios distintos y volumen total (repeticiones x peso, kg) de la sesion. */
  numeroEjercicios: number;
  volumen: number;
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
  ejercicios: ['atlas', 'ejercicios'] as const,
  rutinas: ['atlas', 'rutinas'] as const,
};

/** Listado ligero de sesiones (sin series), de la mas reciente a la mas antigua. */
export function useSesiones(desde: string, hasta: string) {
  return useQuery({
    queryKey: [...claves.sesiones, 'rango', desde, hasta] as const,
    queryFn: () => api<Sesion[]>(`${BASE}/sesion`, { query: { desde, hasta } }),
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

export interface Ejercicio {
  id: number;
  nombre: string;
  grupoMuscular: string;
  equipamiento?: string | null;
  esPropio: boolean;
}

export interface EjercicioRequest {
  nombre: string;
  grupoMuscular: string;
  equipamiento?: string | null;
}

export interface Rutina {
  id: number;
  nombre: string;
  descripcion?: string | null;
  activa: boolean;
  numeroEjercicios: number;
}

export interface RutinaCompleta extends Omit<Rutina, 'numeroEjercicios'> {
  lineas: { id: number; ejercicioId: number; ejercicioNombre: string; ejercicioGrupoMuscular?: string | null; orden: number; seriesObjetivo: number; repsObjetivo?: string | null }[];
}

export interface SesionRequest {
  rutinaId: number | null;
  fecha: string;
  duracionMin: number | null;
  notas: string | null;
  series: { ejercicioId: number; numeroSerie: number; reps: number; pesoKg: number; rpe: number | null }[];
}

export function useEjercicios() {
  return useQuery({ queryKey: [...claves.ejercicios, 'lista'] as const, queryFn: () => api<Ejercicio[]>(`${BASE}/ejercicio`) });
}

export function useRutinas() {
  return useQuery({ queryKey: [...claves.rutinas, 'lista'] as const, queryFn: () => api<Rutina[]>(`${BASE}/rutina`) });
}

/** La rutina con sus lineas; se pide al elegirla en una sesion nueva para precargar los ejercicios. */
export function pedirRutina(id: number) {
  return api<RutinaCompleta>(`${BASE}/rutina/${id}`);
}

/** La sesion completa, con sus series (el listado trae solo el numero de series). */
export function useSesion(id: number | undefined) {
  return useQuery({
    queryKey: [...claves.sesiones, 'ficha', id ?? 0] as const,
    queryFn: () => api<SesionCompleta>(`${BASE}/sesion/${id}`),
    enabled: id !== undefined,
    gcTime: 0,
  });
}

/** Lo que cambia una sesion: sus listados, la progresion, los records y el Inicio. */
function useInvalidarAtlas() {
  const qc = useQueryClient();
  return () => Promise.all([qc.invalidateQueries({ queryKey: ['atlas'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
}

export function useCrearSesion() {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Sesión añadida' },
    mutationFn: (cuerpo: SesionRequest) => api<SesionCompleta>(`${BASE}/sesion`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarSesion(id: number) {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Sesión guardada' },
    mutationFn: (cuerpo: SesionRequest) => api<SesionCompleta>(`${BASE}/sesion/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/** `alBorrar` se llama nada mas borrar, antes de invalidar (mismo motivo que useBorrarComida de Fusion). */
export function useBorrarSesion(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Sesión borrada' },
    mutationFn: (id: number) => api<void>(`${BASE}/sesion/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: [...claves.sesiones, 'ficha', id] });
      return invalidar();
    },
  });
}

export function useCrearEjercicio() {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Ejercicio añadido' },
    mutationFn: (cuerpo: EjercicioRequest) => api<Ejercicio>(`${BASE}/ejercicio`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarEjercicio(id: number) {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Ejercicio guardado' },
    mutationFn: (cuerpo: EjercicioRequest) => api<Ejercicio>(`${BASE}/ejercicio/${id}`, { metodo: 'PUT', cuerpo }),
    // Cambia el nombre en rutinas, sesiones y records.
    onSuccess: invalidar,
  });
}

/** `alBorrar` se llama nada mas borrar, antes de invalidar (mismo motivo que useBorrarComida de Fusion). */
export function useBorrarEjercicio(alBorrar?: () => void) {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Ejercicio borrado' },
    mutationFn: (id: number) => api<void>(`${BASE}/ejercicio/${id}`, { metodo: 'DELETE' }),
    onSuccess: () => {
      alBorrar?.();
      return invalidar();
    },
  });
}

export interface RutinaRequest {
  nombre: string;
  descripcion: string | null;
  activa: boolean;
  lineas: { ejercicioId: number; orden: number; seriesObjetivo: number; repsObjetivo: string }[];
}

/** La rutina completa con sus lineas, para editarla. */
export function useRutina(id: number | undefined) {
  return useQuery({
    queryKey: [...claves.rutinas, 'ficha', id ?? 0] as const,
    queryFn: () => pedirRutina(id as number),
    enabled: id !== undefined,
    gcTime: 0,
  });
}

export function useCrearRutina() {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Rutina añadida' },
    mutationFn: (cuerpo: RutinaRequest) => api<RutinaCompleta>(`${BASE}/rutina`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarRutina(id: number) {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Rutina guardada' },
    mutationFn: (cuerpo: RutinaRequest) => api<RutinaCompleta>(`${BASE}/rutina/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/** `alBorrar` se llama nada mas borrar, antes de invalidar (mismo motivo que useBorrarComida de Fusion). */
export function useBorrarRutina(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Rutina borrada' },
    mutationFn: (id: number) => api<void>(`${BASE}/rutina/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: [...claves.rutinas, 'ficha', id] });
      return invalidar();
    },
  });
}

export interface PesoRequest {
  fecha: string;
  pesoKg: number;
  grasaPct: number | null;
  notas: string | null;
}

/** Un peso por dia: si ya hay registro en esa fecha, lo reemplaza. */
export function useApuntarPeso() {
  const invalidar = useInvalidarAtlas();
  return useMutation({
    meta: { aviso: 'Peso apuntado' },
    mutationFn: (cuerpo: PesoRequest) => api<PesoCorporal>(`${BASE}/peso`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}
