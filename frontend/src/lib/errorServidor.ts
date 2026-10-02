import { useSyncExternalStore } from 'react';

// Ventana centrada que avisa de un fallo del servidor (5xx). La abre el cliente de la API en
// cualquier llamada, sin que cada pantalla tenga que hacer nada. Si ya esta abierta (varias
// peticiones fallando a la vez, o el reintento de una consulta) no se vuelve a abrir.

const oyentes = new Set<() => void>();
let abierto = false;

function cambiar(valor: boolean) {
  if (abierto === valor) return;
  abierto = valor;
  oyentes.forEach((o) => o());
}

export function avisarErrorServidor(): void {
  cambiar(true);
}

export function cerrarErrorServidor(): void {
  cambiar(false);
}

function suscribir(o: () => void) {
  oyentes.add(o);
  return () => oyentes.delete(o);
}

export function useErrorServidor(): boolean {
  return useSyncExternalStore(suscribir, () => abierto);
}
