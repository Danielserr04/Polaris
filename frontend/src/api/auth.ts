import { useQuery } from '@tanstack/react-query';
import { api } from './client';
import type { TokenDto, UsuarioDto } from './tipos';
import { useHaySesion } from '../auth/sesion';

export function login(usernameOEmail: string, password: string): Promise<TokenDto> {
  return api<TokenDto>('/api/auth/login', { metodo: 'POST', cuerpo: { usernameOEmail, password } });
}

/** El usuario autenticado. Solo se pide si hay sesion. */
export function useUsuario() {
  const haySesion = useHaySesion();
  return useQuery({
    queryKey: ['usuario'],
    queryFn: () => api<UsuarioDto>('/api/auth/usuario'),
    enabled: haySesion,
    staleTime: 60_000,
  });
}

// En desarrollo el backend esta en otro puerto y el login de Google tiene que empezar
// alli (la URI de redireccion registrada en Google es la del backend). En produccion
// van bajo el mismo origen.
const BACKEND = import.meta.env.VITE_BACKEND_URL ?? (import.meta.env.DEV ? 'http://localhost:8080' : '');
export const GOOGLE_LOGIN_URL = `${BACKEND}/oauth2/authorization/google`;
