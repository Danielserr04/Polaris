import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Datos corporales de Nucleo que Atlas pinta en la pestana "Cuerpo": medidas
// (docs/decisiones/041-medida-corporal-una-por-dia.md) y la altura del perfil para el IMC.
// El backend omite los campos nulos: un campo opcional llega como `undefined`.

export const CAMPOS_MEDIDA = [
  { campo: 'cuelloCm', nombre: 'Cuello' },
  { campo: 'pechoCm', nombre: 'Pecho' },
  { campo: 'cinturaCm', nombre: 'Cintura' },
  { campo: 'caderaCm', nombre: 'Cadera' },
  { campo: 'brazoIzqCm', nombre: 'Brazo izq.' },
  { campo: 'brazoDchoCm', nombre: 'Brazo dcho.' },
  { campo: 'musloIzqCm', nombre: 'Muslo izq.' },
  { campo: 'musloDchoCm', nombre: 'Muslo dcho.' },
] as const;

export type CampoMedida = (typeof CAMPOS_MEDIDA)[number]['campo'];

export type Medidas = { [K in CampoMedida]?: number | null };

export interface MedidaCorporal extends Medidas {
  id: number;
  fecha: string;
  notas?: string | null;
}

export interface MedidaRequest extends Medidas {
  fecha: string;
  notas: string | null;
}

export interface PerfilCorporal {
  alturaCm?: number | null;
  fechaNacimiento?: string | null;
  sexo?: 'HOMBRE' | 'MUJER' | null;
}

const BASE = '/api/nucleo/medida-corporal';
const claves = { medidas: ['nucleo', 'medidas'] as const, perfil: ['nucleo', 'perfil'] as const };

/** Del mas reciente al mas antiguo, sin notas. */
export function useMedidas() {
  return useQuery({ queryKey: claves.medidas, queryFn: () => api<MedidaCorporal[]>(BASE) });
}

export function useMedida(id: number | undefined) {
  return useQuery({
    queryKey: [...claves.medidas, 'ficha', id] as const,
    queryFn: () => api<MedidaCorporal>(`${BASE}/${id}`),
    enabled: id !== undefined,
  });
}

/** El perfil puede no existir todavia (404): entonces no hay altura y no se calcula el IMC. */
export function usePerfilCorporal() {
  return useQuery({
    queryKey: claves.perfil,
    queryFn: async () => {
      try {
        return await api<PerfilCorporal>('/api/nucleo/perfil');
      } catch (e) {
        if (e instanceof ApiError && e.status === 404) return null;
        throw e;
      }
    },
  });
}

function useInvalidarMedidas() {
  const qc = useQueryClient();
  return () => qc.invalidateQueries({ queryKey: claves.medidas });
}

/** Una medicion por dia: si ya hay una en esa fecha, la reemplaza. */
export function useApuntarMedidas() {
  const invalidar = useInvalidarMedidas();
  return useMutation({
    meta: { aviso: 'Medidas apuntadas' },
    mutationFn: (cuerpo: MedidaRequest) => api<MedidaCorporal>(BASE, { metodo: 'POST', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useActualizarMedidas(id: number) {
  const invalidar = useInvalidarMedidas();
  return useMutation({
    meta: { aviso: 'Medidas guardadas' },
    mutationFn: (cuerpo: MedidaRequest) => api<MedidaCorporal>(`${BASE}/${id}`, { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

export function useBorrarMedidas(alBorrar?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    meta: { aviso: 'Medidas borradas' },
    mutationFn: (id: number) => api<void>(`${BASE}/${id}`, { metodo: 'DELETE' }),
    onSuccess: (_, id) => {
      alBorrar?.();
      qc.removeQueries({ queryKey: [...claves.medidas, 'ficha', id] });
      return qc.invalidateQueries({ queryKey: claves.medidas });
    },
  });
}

/** IMC = kg / m². Sin altura o sin peso, no hay IMC. */
export function imc(pesoKg: number | undefined, alturaCm: number | null | undefined): number | null {
  if (!pesoKg || !alturaCm) return null;
  const m = alturaCm / 100;
  return pesoKg / (m * m);
}

/** Categorias de la OMS para adultos. */
export function categoriaImc(v: number): string {
  if (v < 18.5) return 'Bajo peso';
  if (v < 25) return 'Normal';
  if (v < 30) return 'Sobrepeso';
  return 'Obesidad';
}
