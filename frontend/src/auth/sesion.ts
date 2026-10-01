import { useSyncExternalStore } from 'react';

// La sesion es el JWT propio de Polaris + cuando caduca. Vive en localStorage para
// sobrevivir a un refresco y se expone como un store externo para que tanto React
// como el cliente de la API (que no es un componente) la lean y la cierren.
// Ver docs/decisiones/031-login-google-redirige-al-frontend.md (consecuencias).

const CLAVE = 'polaris.sesion';

interface Sesion {
  token: string;
  /** Instante de caducidad, en milisegundos desde epoch */
  expiraEn: number;
}

const oyentes = new Set<() => void>();

function leer(): Sesion | null {
  try {
    const bruto = localStorage.getItem(CLAVE);
    if (!bruto) return null;
    const s = JSON.parse(bruto) as Sesion;
    if (typeof s.token !== 'string' || typeof s.expiraEn !== 'number') return null;
    return s.expiraEn > Date.now() ? s : null;
  } catch {
    return null;
  }
}

let actual: Sesion | null = leer();

function avisar() {
  oyentes.forEach((o) => o());
}

export function iniciarSesion(token: string, expiraEnSegundos: number): void {
  actual = { token, expiraEn: Date.now() + expiraEnSegundos * 1000 };
  try {
    localStorage.setItem(CLAVE, JSON.stringify(actual));
  } catch {
    // Sin localStorage la sesion dura lo que dure la pestana: sigue funcionando.
  }
  avisar();
}

export function cerrarSesion(): void {
  actual = null;
  try {
    localStorage.removeItem(CLAVE);
  } catch {
    // nada que limpiar
  }
  avisar();
}

/** El token vigente, o null si no hay sesion o ha caducado (en cuyo caso la cierra). */
export function getToken(): string | null {
  if (actual && actual.expiraEn <= Date.now()) {
    cerrarSesion();
    return null;
  }
  return actual?.token ?? null;
}

export function suscribir(oyente: () => void): () => void {
  oyentes.add(oyente);
  return () => {
    oyentes.delete(oyente);
  };
}

export function useHaySesion(): boolean {
  return useSyncExternalStore(suscribir, () => getToken() !== null);
}
