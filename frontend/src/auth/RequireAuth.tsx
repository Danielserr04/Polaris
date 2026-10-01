import { Navigate, Outlet } from 'react-router-dom';
import { useHaySesion } from './sesion';

// Guardian de las rutas privadas: sin sesion (o con el token caducado) se va al login.
export function RequireAuth() {
  return useHaySesion() ? <Outlet /> : <Navigate to="/login" replace />;
}
