import { cerrarSesion, getToken } from '../auth/sesion';
import type { ErrorResponse } from './tipos';

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  cuerpo?: unknown;
  query?: Record<string, string | number | boolean | undefined | null>;
  /**
   * Por defecto un 401 con sesion cierra la sesion (token caducado). Los endpoints que
   * responden 401 por una contrasena mala (cambiar contrasena, cambiar email) lo desactivan:
   * ahi el token sigue valiendo y el 401 es un error de formulario.
   */
  cerrarSesionEn401?: boolean;
}

function conQuery(ruta: string, query?: Opciones['query']): string {
  if (!query) return ruta;
  const params = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined && v !== null && v !== '') params.set(k, String(v));
  }
  const qs = params.toString();
  return qs ? `${ruta}?${qs}` : ruta;
}

async function leerError(res: Response): Promise<string> {
  try {
    const cuerpo = (await res.json()) as Partial<ErrorResponse>;
    if (cuerpo.error) return cuerpo.error;
  } catch {
    // cuerpo vacio o no JSON
  }
  return res.statusText || `Error ${res.status}`;
}

/**
 * Llamada a la API. Pone el Bearer si hay sesion; un 401 con sesion activa significa
 * que el token ya no vale (caducado o revocado) y se cierra la sesion: el guardian de
 * rutas devuelve al usuario al login. Un 401 sin sesion (credenciales malas en el
 * login) es un error normal.
 */
export async function api<T>(ruta: string, { metodo = 'GET', cuerpo, query, cerrarSesionEn401 = true }: Opciones = {}): Promise<T> {
  const token = getToken();
  const res = await fetch(conQuery(ruta, query), {
    method: metodo,
    headers: {
      Accept: 'application/json',
      ...(cuerpo !== undefined && { 'Content-Type': 'application/json' }),
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
  });

  if (!res.ok) {
    if (res.status === 401 && token && cerrarSesionEn401) cerrarSesion();
    throw new ApiError(res.status, await leerError(res));
  }
  if (res.status === 204) return undefined as T;
  // Un 201 sin cuerpo (p. ej. el registro) tampoco es JSON.
  const texto = await res.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}
