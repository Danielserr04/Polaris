import { useQueries } from '@tanstack/react-query';
import { api } from './client';

// Logros de todos los modulos (docs/decisiones/044-logros-globales-con-fecha-calculada.md).
// Cada modulo calcula los suyos en /api/<modulo>/logros con la misma forma; aqui se juntan.

export type NivelLogro = 'BRONCE' | 'PLATA' | 'ORO' | 'PLATINO';

export interface Logro {
  codigo: string;
  nombre: string;
  descripcion: string;
  icono: string;
  nivel: NivelLogro;
  /** Como se cuenta el progreso: "sesiones", "t", "días"... */
  unidad: string;
  objetivo: number;
  progreso: number;
  conseguido: boolean;
  /** Dia en que se consiguio (yyyy-mm-dd). Nulo si no se tiene o si el dato no tiene fecha. */
  fechaConseguido?: string | null;
}

export interface LogroDeModulo extends Logro {
  modulo: ModuloLogros;
}

export type ModuloLogros = 'odisea' | 'kuiper' | 'fusion' | 'atlas' | 'nucleo';

export interface SeccionLogros {
  id: ModuloLogros;
  nombre: string;
  icono: string;
  color: string;
}

// Mismo orden que la navegacion; el cuerpo (Nucleo) al final, como el perfil.
export const SECCIONES: SeccionLogros[] = [
  { id: 'odisea', nombre: 'Odisea', icono: 'clapperboard', color: 'var(--mod-odisea)' },
  { id: 'kuiper', nombre: 'Kuiper', icono: 'wallet', color: 'var(--mod-kuiper)' },
  { id: 'fusion', nombre: 'Fusión', icono: 'flame', color: 'var(--mod-fusion)' },
  { id: 'atlas', nombre: 'Atlas', icono: 'dumbbell', color: 'var(--mod-atlas)' },
  { id: 'nucleo', nombre: 'Cuerpo', icono: 'user-round', color: 'var(--mod-nucleo)' },
];

export const NOMBRE_NIVEL: Record<NivelLogro, string> = {
  BRONCE: 'Bronce',
  PLATA: 'Plata',
  ORO: 'Oro',
  PLATINO: 'Platino',
};

export const clavesLogros = {
  modulo: (m: ModuloLogros) => ['logros', m] as const,
};

/**
 * Los logros de los cinco modulos en paralelo. Si uno falla, los demas se ven igual:
 * `fallidos` dice cuales faltan.
 */
export function useLogrosGlobales() {
  return useQueries({
    queries: SECCIONES.map((s) => ({
      queryKey: clavesLogros.modulo(s.id),
      queryFn: () => api<Logro[]>(`/api/${s.id}/logros`),
    })),
    combine: (res) => ({
      logros: res.flatMap((r, i) => (r.data ?? []).map((l): LogroDeModulo => ({ ...l, modulo: SECCIONES[i].id }))),
      cargando: res.some((r) => r.isPending),
      fallidos: SECCIONES.filter((_, i) => res[i].isError),
      reintentar: () => res.forEach((r) => r.isError && void r.refetch()),
    }),
  });
}
