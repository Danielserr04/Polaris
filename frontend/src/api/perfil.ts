import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import type { UsuarioDto } from './tipos';

// Los cinco endpoints del perfil de cuenta (docs/modulos/auth.md, "El perfil de cuenta").
// Tras cada exito se invalida ['usuario']: el nav (avatar y nombre) y la pantalla salen de ahi.

export interface ActualizarPerfil {
  nombre: string;
  avatarUrl: string | null;
}

export interface CambiarPassword {
  /** Obligatoria si la cuenta ya tiene contrasena; se omite en cuentas solo-Google. */
  passwordActual?: string;
  passwordNueva: string;
}

export interface CambiarEmail {
  email: string;
  /** Obligatoria si la cuenta ya tiene contrasena; se omite en cuentas solo-Google. */
  password?: string;
}

function useInvalidarUsuario() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['usuario'] });
}

/** PUT /api/auth/usuario */
export function useActualizarPerfil() {
  const invalidar = useInvalidarUsuario();
  return useMutation({
    mutationFn: (cuerpo: ActualizarPerfil) => api<UsuarioDto>('/api/auth/usuario', { metodo: 'PUT', cuerpo }),
    onSuccess: invalidar,
  });
}

/** PUT /api/auth/usuario/password (204). Un 401 es contrasena actual mala, no sesion caducada. */
export function useCambiarPassword() {
  const invalidar = useInvalidarUsuario();
  return useMutation({
    mutationFn: (cuerpo: CambiarPassword) =>
      api<void>('/api/auth/usuario/password', { metodo: 'PUT', cuerpo, cerrarSesionEn401: false }),
    onSuccess: invalidar,
  });
}

/** PUT /api/auth/usuario/email. Deja el email sin verificar y manda el enlace al nuevo. */
export function useCambiarEmail() {
  const invalidar = useInvalidarUsuario();
  return useMutation({
    mutationFn: (cuerpo: CambiarEmail) =>
      api<UsuarioDto>('/api/auth/usuario/email', { metodo: 'PUT', cuerpo, cerrarSesionEn401: false }),
    onSuccess: invalidar,
  });
}

/** POST /api/auth/usuario/verificacion (204). */
export function useReenviarVerificacion() {
  const invalidar = useInvalidarUsuario();
  return useMutation({
    mutationFn: () => api<void>('/api/auth/usuario/verificacion', { metodo: 'POST' }),
    // Si el backend dice que ya estaba verificado, refrescar corrige la pantalla.
    onSettled: invalidar,
  });
}

/** DELETE /api/auth/usuario/google. 400 si la cuenta no tiene contrasena. */
export function useDesvincularGoogle() {
  const invalidar = useInvalidarUsuario();
  return useMutation({
    mutationFn: () => api<UsuarioDto>('/api/auth/usuario/google', { metodo: 'DELETE' }),
    onSettled: invalidar,
  });
}
