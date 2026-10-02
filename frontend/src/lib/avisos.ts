import { useSyncExternalStore } from 'react';

// Aviso de exito tras una accion (crear, guardar, borrar). Un solo aviso a la vez: el ultimo manda.
// Lo emite la MutationCache de main.tsx con el `meta.aviso` de cada mutacion; los errores no pasan
// por aqui porque cada formulario ya los muestra en linea.

export interface Aviso {
  id: number;
  texto: string;
}

const oyentes = new Set<() => void>();
let actual: Aviso | null = null;
let siguiente = 1;

function cambiar(a: Aviso | null) {
  actual = a;
  oyentes.forEach((o) => o());
}

export function avisar(texto: string): void {
  cambiar({ id: siguiente++, texto });
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
