import { Route, Routes } from 'react-router-dom';
import { RequireAuth } from '../auth/RequireAuth';
import { AuthCallback } from '../pages/AuthCallback';
import { Login } from '../pages/Login';
import { Pendiente } from '../pages/Pendiente';
import { Perfil } from '../pages/Perfil';
import { AppShell } from './AppShell';

export function App() {
  return (
    <Routes>
      <Route path="login" element={<Login />} />
      <Route path="auth/callback" element={<AuthCallback />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppShell />}>
          <Route index element={<Pendiente eyebrow="Inicio" title="Inicio" />} />
          <Route path="odisea" element={<Pendiente eyebrow="Odisea" title="Tu lista" />} />
          <Route path="kuiper" element={<Pendiente eyebrow="Kuiper" title="Gastos" />} />
          <Route path="fusion" element={<Pendiente eyebrow="Fusión" title="Hoy" />} />
          <Route path="atlas" element={<Pendiente eyebrow="Atlas" title="Progresión" />} />
          <Route path="perfil" element={<Perfil />} />
          <Route path="*" element={<Pendiente eyebrow="Polaris" title="No encontrada" />} />
        </Route>
      </Route>
    </Routes>
  );
}
