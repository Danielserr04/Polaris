import { useEffect, useSyncExternalStore } from 'react';

// Tema de la app autenticada. Oscuro es el de la marca (y el unico del login); claro y "sistema"
// se eligen en Perfil. Vive en localStorage: es de este navegador, no de la cuenta.

export type Tema = 'oscuro' | 'claro' | 'sistema';
export type TemaAplicado = 'dark' | 'light';

const CLAVE = 'polaris.tema';
const EVENTO = 'polaris:tema';
const CONSULTA = '(prefers-color-scheme: light)';

export function leerTema(): Tema {
  try {
    const v = localStorage.getItem(CLAVE);
    return v === 'claro' || v === 'sistema' ? v : 'oscuro';
  } catch {
    return 'oscuro';
  }
}

export function guardarTema(t: Tema): void {
  try {
    localStorage.setItem(CLAVE, t);
  } catch {
    // Sin localStorage el cambio dura lo que dure la pestana.
  }
  window.dispatchEvent(new Event(EVENTO));
}

function suscribir(avisar: () => void): () => void {
  const mq = window.matchMedia(CONSULTA);
  window.addEventListener(EVENTO, avisar);
  window.addEventListener('storage', avisar);
  mq.addEventListener('change', avisar);
  return () => {
    window.removeEventListener(EVENTO, avisar);
    window.removeEventListener('storage', avisar);
    mq.removeEventListener('change', avisar);
  };
}

/** El tema elegido (para el selector de Perfil). */
export function useTemaElegido(): Tema {
  return useSyncExternalStore(suscribir, leerTema, () => 'oscuro' as Tema);
}

/** El tema que se pinta: "sistema" se resuelve con la preferencia del sistema operativo. */
export function useTemaAplicado(): TemaAplicado {
  const elegido = useTemaElegido();
  const claroSistema = useSyncExternalStore(suscribir, () => window.matchMedia(CONSULTA).matches, () => false);
  return elegido === 'claro' || (elegido === 'sistema' && claroSistema) ? 'light' : 'dark';
}

// Color de la barra del navegador en movil; los mismos fondos que --bg-app de cada tema.
const COLOR_BARRA: Record<TemaAplicado, string> = { dark: '#12110e', light: '#f2eee6' };

function pintar(tema: TemaAplicado): void {
  if (tema === 'light') document.documentElement.dataset.theme = 'light';
  else delete document.documentElement.dataset.theme;
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', COLOR_BARRA[tema]);
}

/**
 * Pone data-theme en <html> mientras haya shell. El script de index.html ya lo deja puesto antes
 * del primer pintado; esto lo mantiene al cambiar de tema en Perfil o en el sistema.
 */
export function useAplicarTema(tema: TemaAplicado): void {
  useEffect(() => {
    pintar(tema);
    return () => pintar('dark');
  }, [tema]);
}

/** Login y callback: siempre oscuros, aunque index.html haya puesto el claro antes de redirigir. */
export function useTemaOscuro(): void {
  useEffect(() => pintar('dark'), []);
}
