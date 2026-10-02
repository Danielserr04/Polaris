import { useMutation } from '@tanstack/react-query';
import { api, ApiError } from './client';

// Calculo del objetivo a partir del perfil (CalculoObjetivoController). No guarda nada.

export type TipoObjetivo = 'DEFINICION' | 'MANTENIMIENTO' | 'VOLUMEN';
export type NivelActividad = 'SEDENTARIO' | 'LIGERO' | 'MODERADO' | 'ALTO' | 'MUY_ALTO';

export const ETIQUETA_TIPO: Record<TipoObjetivo, string> = {
  DEFINICION: 'Perder grasa',
  MANTENIMIENTO: 'Mantener',
  VOLUMEN: 'Ganar músculo',
};

export const ETIQUETA_ACTIVIDAD: Record<NivelActividad, string> = {
  SEDENTARIO: 'sedentario',
  LIGERO: 'actividad ligera',
  MODERADO: 'actividad moderada',
  ALTO: 'actividad alta',
  MUY_ALTO: 'actividad muy alta',
};

export interface CalculoObjetivo {
  tipo: TipoObjetivo;
  nivelActividad: NivelActividad;
  sexo: 'HOMBRE' | 'MUJER';
  edad: number;
  alturaCm: number;
  pesoKg: number;
  pesoFecha?: string | null;
  tmb: number;
  gastoTotal: number;
  kcalDiarias: number;
  proteinasObj: number;
  carbosObj: number;
  grasasObj: number;
}

/** Es un GET, pero se lanza al pulsar un boton: por eso va como mutacion (sin aviso). */
export function useCalcularObjetivo() {
  return useMutation({
    mutationFn: (tipo: TipoObjetivo) => api<CalculoObjetivo>('/api/fusion/objetivo/calculo', { query: { tipo } }),
  });
}

/** Mensaje del calculo: el 400 trae la lista de lo que falta en el perfil, sin tildes. */
export function mensajeErrorCalculo(e: unknown): string {
  if (e instanceof ApiError) {
    return e.message.replace('Completalo en tu perfil', 'Complétalo en Perfil');
  }
  return 'No se ha podido conectar con el servidor.';
}
