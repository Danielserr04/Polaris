import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { iniciarSesion } from '../auth/sesion';

// Destino del redirect del login de Google (ADR 031): /auth/callback#token=...&expiraEnSegundos=...
// El fragmento se lee una vez y se borra de la URL en el acto para que no quede en el
// historial ni se pueda copiar sin querer.
export function AuthCallback() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  useEffect(() => {
    const params = new URLSearchParams(window.location.hash.replace(/^#/, ''));
    const token = params.get('token');
    const expira = Number(params.get('expiraEnSegundos'));
    window.history.replaceState(null, '', window.location.pathname);

    if (token && Number.isFinite(expira) && expira > 0) {
      queryClient.clear();
      iniciarSesion(token, expira);
      navigate('/', { replace: true });
    } else {
      navigate('/login?error=google', { replace: true });
    }
  }, [navigate, queryClient]);

  return null;
}
