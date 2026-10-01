import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Contratos de Fusion (nutricion). Reflejan los DTO de src/main/java/com/polaris/fusion/.

export type MomentoComida = 'DESAYUNO' | 'COMIDA' | 'CENA' | 'SNACK';

export const MOMENTOS: MomentoComida[] = ['DESAYUNO', 'COMIDA', 'CENA', 'SNACK'];

export const ETIQUETA_MOMENTO: Record<MomentoComida, string> = {
  DESAYUNO: 'Desayuno',
  COMIDA: 'Comida',
  CENA: 'Cena',
  SNACK: 'Snack',
};

export interface MacroResumen {
  consumido: number;
  objetivo: number | null;
  restante: number | null;
  porcentaje: number | null;
}

export interface ResumenDiario {
  fecha: string;
  objetivoVigenteDesde: string | null;
  kcal: MacroResumen;
  proteinas: MacroResumen;
  carbohidratos: MacroResumen;
  grasas: MacroResumen;
}

export interface ComidaLinea {
  id: number;
  alimentoId: number;
  alimentoNombre: string;
  alimentoMarca: string | null;
  cantidadG: number;
  kcal: number;
  proteinas: number;
  carbohidratos: number;
  grasas: number;
}

/** Lo que devuelve el listado: solo los totales de cada comida. */
export interface Comida {
  id: number;
  fecha: string;
  momento: MomentoComida;
  kcalTotal: number;
  proteinasTotal: number;
  carbohidratosTotal: number;
  grasasTotal: number;
}

/** Lo que devuelve GET /{id}: la comida con sus lineas. */
export interface ComidaCompleta extends Comida {
  lineas: ComidaLinea[];
}

export interface ComidaRequest {
  fecha: string;
  momento: MomentoComida;
  lineas: { alimentoId: number; cantidadG: number }[];
}

export interface Alimento {
  id: number;
  nombre: string;
  marca: string | null;
  kcal100g: number;
  proteinas100g: number;
  carbohidratos100g: number;
  grasas100g: number;
}

export interface AlimentoRequest {
  nombre: string;
  marca: string | null;
  kcal100g: number;
  proteinas100g: number;
  carbohidratos100g: number;
  grasas100g: number;
}

export interface ObjetivoRequest {
  kcalDiarias: number;
  proteinasObj: number;
  carbosObj: number;
  grasasObj: number;
  vigenteDesde: string;
}

const BASE = '/api/fusion';

export const claves = {
  resumen: (fecha: string) => ['fusion', 'resumen', fecha] as const,
  comidas: ['fusion', 'comidas'] as const,
  comida: (id: number) => ['fusion', 'comida', id] as const,
  alimentos: ['fusion', 'alimentos'] as const,
};

export function useResumenDia(fecha: string) {
  return useQuery({
    queryKey: claves.resumen(fecha),
    queryFn: () => api<ResumenDiario>(`${BASE}/resumen`, { query: { fecha } }),
  });
}

/**
 * Las comidas de un dia, con sus lineas. El listado solo trae totales, asi que se pide cada
 * una completa (como mucho unas pocas al dia) dentro de una sola consulta.
 */
export function useComidasDia(fecha: string) {
  return useQuery({
    queryKey: [...claves.comidas, 'dia', fecha] as const,
    queryFn: async () => {
      const resumen = await api<Comida[]>(`${BASE}/comida`, { query: { fecha } });
      return Promise.all(resumen.map((c) => api<ComidaCompleta>(`${BASE}/comida/${c.id}`)));
    },
  });
}

/** Comidas de un rango, para la tendencia (solo se usan los totales). */
export function useComidasRango(desde: string, hasta: string) {
  return useQuery({
    queryKey: [...claves.comidas, 'rango', desde, hasta] as const,
    queryFn: () => api<Comida[]>(`${BASE}/comida`, { query: { desde, hasta } }),
  });
}

/** La comida completa, con sus lineas (el listado trae solo los totales). */
export function useComida(id: number | undefined) {
  return useQuery({
    queryKey: claves.comida(id ?? 0),
    queryFn: () => api<ComidaCompleta>(`${BASE}/comida/${id}`),
    enabled: id !== undefined,
    gcTime: 0,
  });
}

/** Busqueda en el catalogo de alimentos. Con texto vacio no se pide nada. */
export function useBuscarAlimentos(q: string) {
  const texto = q.trim();
  return useQuery({
    queryKey: [...claves.alimentos, texto] as const,
    queryFn: () => api<Alimento[]>(`${BASE}/alimento`, { query: { q: texto } }),
    enabled: texto.length >= 2,
    staleTime: 30_000,
  });
}

/** Lo que cambia una comida: el listado, los resumenes de varios dias y el Inicio. */
function useInvalidarFusion() {
  const qc = useQueryClient();
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: ['fusion'] }),
      qc.invalidateQueries({ queryKey: ['inicio'] }),
    ]);
}

export function useCrearComida() {
  const invalidar = useInvalidarFusion();
  return useMutation({
    mutationFn: (cuerpo: ComidaRequest) => api<Comida>(`${BASE}/comida`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarComida(id: number) {
  const invalidar = useInvalidarFusion();
  return useMutation({
    mutationFn: (cuerpo: ComidaRequest) => api<Comida>(`${BASE}/comida/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/**
 * `alBorrar` se llama nada mas borrar, antes de invalidar: el dialogo de edicion debe cerrarse
 * antes de que la ficha ya borrada se vuelva a pedir (daria 404 y el dialogo cambiaria a "error").
 * Va en el hook y no en `mutate` porque ese componente se desmonta durante la recarga.
 */
export function useBorrarComida(alBorrar?: () => void) {
  const qc = useQueryClient();
  const invalidar = useInvalidarFusion();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/comida/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: claves.comida(id) });
      return invalidar();
    },
  });
}

export function useCrearAlimento() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cuerpo: AlimentoRequest) => api<Alimento>(`${BASE}/alimento`, { metodo: 'POST', cuerpo }),
    onSuccess: () => qc.invalidateQueries({ queryKey: claves.alimentos }),
  });
}

/** Un objetivo nuevo desde una fecha: el historico no se edita, solo se añaden objetivos. */
export function useCrearObjetivo() {
  const invalidar = useInvalidarFusion();
  return useMutation({
    mutationFn: (cuerpo: ObjetivoRequest) => api(`${BASE}/objetivo`, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

/** Mensaje legible de un fallo de la API (el backend los escribe sin tildes). */
export function mensajeError(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 404 && /alimento/i.test(e.message)) return 'Alguno de los alimentos ya no existe.';
    if (e.status === 404) return 'Esa comida ya no existe.';
    if (e.status === 409) return 'Ya tienes un objetivo que empieza ese día. Elige otra fecha.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}
