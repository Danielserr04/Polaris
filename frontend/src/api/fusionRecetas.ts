import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Recetas de Fusion. Reflejan los DTO de RecetaController (src/main/java/com/polaris/fusion/).

export interface RecetaIngrediente {
  id: number;
  alimentoId: number;
  alimentoNombre: string;
  alimentoMarca?: string | null;
  /** Gramos para la receta entera. */
  cantidadG: number;
  kcal: number;
  proteinas: number;
  carbohidratos: number;
  grasas: number;
}

/** Lo que devuelve el listado: sin ingredientes, con los macros por racion. */
export interface Receta {
  id: number;
  nombre: string;
  descripcion?: string | null;
  raciones: number;
  numIngredientes: number;
  kcalRacion: number;
  proteinasRacion: number;
  carbohidratosRacion: number;
  grasasRacion: number;
}

/** Lo que devuelve GET /{id}. */
export interface RecetaCompleta {
  id: number;
  nombre: string;
  descripcion?: string | null;
  raciones: number;
  instrucciones?: string | null;
  ingredientes: RecetaIngrediente[];
  kcalTotal: number;
  proteinasTotal: number;
  carbohidratosTotal: number;
  grasasTotal: number;
  kcalRacion: number;
  proteinasRacion: number;
  carbohidratosRacion: number;
  grasasRacion: number;
}

export interface RecetaRequest {
  nombre: string;
  descripcion: string | null;
  raciones: number;
  instrucciones: string | null;
  ingredientes: { alimentoId: number; cantidadG: number }[];
}

const BASE = '/api/fusion/receta';

export const clavesReceta = {
  todas: ['fusion', 'recetas'] as const,
  una: (id: number) => ['fusion', 'receta', id] as const,
};

export function useRecetas(q = '') {
  const texto = q.trim();
  return useQuery({
    queryKey: [...clavesReceta.todas, texto] as const,
    queryFn: () => api<Receta[]>(BASE, { query: { q: texto } }),
  });
}

export function useReceta(id: number | undefined) {
  return useQuery({
    queryKey: clavesReceta.una(id ?? 0),
    queryFn: () => api<RecetaCompleta>(`${BASE}/${id}`),
    enabled: id !== undefined,
    gcTime: 0,
  });
}

/** Pide una receta completa fuera de un componente (al añadirla a una comida). */
export function pedirReceta(id: number) {
  return api<RecetaCompleta>(`${BASE}/${id}`);
}

/** Una receta cambia sus propias fichas y los macros de los planes que la usan. */
function useInvalidarRecetas() {
  const qc = useQueryClient();
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: clavesReceta.todas }),
      qc.invalidateQueries({ queryKey: ['fusion', 'receta'] }),
      qc.invalidateQueries({ queryKey: ['fusion', 'planes'] }),
      qc.invalidateQueries({ queryKey: ['fusion', 'plan'] }),
    ]);
}

export function useCrearReceta() {
  const invalidar = useInvalidarRecetas();
  return useMutation({
    mutationFn: (cuerpo: RecetaRequest) => api<RecetaCompleta>(BASE, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
    meta: { aviso: 'Receta creada' },
  });
}

export function useActualizarReceta(id: number) {
  const invalidar = useInvalidarRecetas();
  return useMutation({
    mutationFn: (cuerpo: RecetaRequest) => api<RecetaCompleta>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
    meta: { aviso: 'Receta guardada' },
  });
}

/** `alBorrar` se llama antes de invalidar, por lo mismo que en useBorrarComida. */
export function useBorrarReceta(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarRecetas();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: clavesReceta.una(id) });
      return invalidar();
    },
    meta: { aviso: 'Receta borrada' },
  });
}

/** Mensaje legible de un fallo de la API de recetas (el backend los escribe sin tildes). */
export function mensajeErrorReceta(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 400 && /plan de comidas/i.test(e.message)) return 'No se puede borrar: la usa algún plan de comidas.';
    if (e.status === 404 && /alimento/i.test(e.message)) return 'Alguno de los alimentos ya no existe.';
    if (e.status === 404) return 'Esa receta ya no existe.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
