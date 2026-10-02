import { useSyncExternalStore } from 'react';

// Aviso flotante tras una accion (crear, guardar, borrar). Un solo aviso a la vez: el ultimo manda.
// Lo emite la MutationCache de main.tsx con el `meta.aviso` de cada mutacion, o cualquiera con
// `avisar()`: vive fuera de React para que lo pueda emitir un componente que se desmonta justo
// despues (un dialogo que se cierra al borrar). Los errores de formulario se muestran en linea.

export interface AccionAviso {
  texto: string;
  icono?: string;
  alPulsar: () => void;
}

export interface Aviso {
  /** Distinto en cada aviso: reinicia el temporizador aunque el texto se repita */
  id: number;
  texto: string;
  /** Por defecto 'success' */
  tono?: 'success' | 'danger' | 'info';
  /** Un boton en el aviso, p. ej. "Deshacer". Con accion el aviso dura mas */
  accion?: AccionAviso;
}

const oyentes = new Set<() => void>();
let actual: Aviso | null = null;
let siguiente = 1;

function cambiar(a: Aviso | null) {
  actual = a;
  oyentes.forEach((o) => o());
}

export function avisar(texto: string, opciones: Pick<Aviso, 'tono' | 'accion'> = {}): void {
  cambiar({ id: siguiente++, texto, ...opciones });
}

export function cerrarAviso(): void {
  cambiar(null);
}

function suscribir(o: () => void) {
  oyentes.add(o);
  return () => oyentes.delete(o);
}

export function useAviso(): Aviso | null {
  return useSyncExternalStore(suscribir, () => actual);
}
