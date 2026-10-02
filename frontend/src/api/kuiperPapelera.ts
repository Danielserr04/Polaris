import { useSyncExternalStore } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import { claves, type MovimientoForm, type MovimientoList } from './kuiper';

// Papelera y duplicado de movimientos de Kuiper. Ver
// docs/decisiones/038-movimiento-papelera-y-duplicar.md.

const BASE = '/api/kuiper/movimiento';

/** Lo que aguanta un movimiento en la papelera antes de borrarse solo (lo hace un job del backend). */
export const DIAS_EN_PAPELERA = 30;

export interface MovimientoPapelera extends MovimientoList {
  /** Fecha y hora (sin zona) en que se mando a la papelera */
  borradoEn: string;
}

export const clavePapelera = ['kuiper', 'papelera'] as const;

/** Lo mismo que invalida api/kuiper.ts: cualquier cambio toca listados, resumenes, papelera e Inicio. */
function useInvalidar() {
  const qc = useQueryClient();
  return () =>
    Promise.all([qc.invalidateQueries({ queryKey: ['kuiper'] }), qc.invalidateQueries({ queryKey: ['inicio'] })]);
}

export function usePapelera(activa = true) {
  return useQuery({
    queryKey: clavePapelera,
    queryFn: () => api<MovimientoPapelera[]>(`${BASE}/papelera`),
    enabled: activa,
  });
}

/** Restaura uno o varios (no hay restauracion en bloque en la API: una llamada por id). */
export function useRestaurarMovimientos() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (ids: number[]) =>
      Promise.all(ids.map((id) => api<MovimientoForm>(`${BASE}/${id}/restaurar`, { metodo: 'POST' }))),
    onSettled: invalidar,
  });
}

export function useBorrarDefinitivo() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (id: number) => api<void>(`${BASE}/${id}/definitivo`, { metodo: 'DELETE' }),
    onSettled: invalidar,
  });
}

export function useVaciarPapelera() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: () => api<void>(`${BASE}/papelera`, { metodo: 'DELETE' }),
    onSettled: invalidar,
  });
}

/** Manda varios a la papelera: todos o ninguno (404 si alguno ya no esta). */
export function useBorrarMovimientos() {
  const qc = useQueryClient();
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: (ids: number[]) => api<void>(`${BASE}/borrar`, { metodo: 'POST', cuerpo: { ids } }),
    onSuccess: (_, ids) => {
      ids.forEach((id) => qc.removeQueries({ queryKey: claves.movimiento(id) }));
      avisar({ texto: ids.length === 1 ? 'Movimiento en la papelera.' : `${ids.length} movimientos en la papelera.`, restaurar: ids });
    },
    onSettled: invalidar,
  });
}

/** Copia un movimiento con fecha de hoy, o con `fecha` (YYYY-MM-DD) si se pasa. */
export function useDuplicarMovimiento() {
  const invalidar = useInvalidar();
  return useMutation({
    mutationFn: ({ id, fecha }: { id: number; fecha?: string }) =>
      api<MovimientoForm>(`${BASE}/${id}/duplicar`, { metodo: 'POST', cuerpo: fecha ? { fecha } : undefined }),
    onSuccess: invalidar,
  });
}

// ---------------------------------------------------------------------------
// Aviso con "Deshacer". Vive fuera de React porque quien borra (el dialogo de edicion) se desmonta
// justo al borrar; lo pinta <AvisoKuiper />, montado una sola vez en la pagina de Kuiper.

export interface Aviso {
  /** Distinto en cada aviso: reinicia el temporizador aunque el texto se repita */
  id: number;
  texto: string;
  /** Por defecto 'info' */
  tono?: 'success' | 'danger' | 'info';
  /** Ids a restaurar si se pulsa "Deshacer"; sin ellos el aviso no ofrece deshacer */
  restaurar?: number[];
}

let actual: Aviso | null = null;
let siguiente = 1;
const oyentes = new Set<() => void>();

function emitir(a: Aviso | null) {
  actual = a;
  oyentes.forEach((o) => o());
}

export function avisar(a: Omit<Aviso, 'id'>): void {
  emitir({ ...a, id: siguiente++ });
}

export function cerrarAviso(): void {
  emitir(null);
}

export function useAviso(): Aviso | null {
  return useSyncExternalStore(
    (o) => {
      oyentes.add(o);
      return () => oyentes.delete(o);
    },
    () => actual,
  );
}
