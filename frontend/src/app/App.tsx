import { Route, Routes } from 'react-router-dom';
import { RequireAuth } from '../auth/RequireAuth';
import { AuthCallback } from '../pages/AuthCallback';
import { Login } from '../pages/Login';
import { Inicio } from '../pages/inicio/Inicio';
import { Odisea } from '../pages/Odisea';
import { Kuiper } from '../pages/Kuiper';
import { Fusion } from '../pages/Fusion';
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
          <Route index element={<Inicio />} />
          <Route path="odisea" element={<Odisea />} />
          <Route path="kuiper" element={<Kuiper />} />
          <Route path="fusion" element={<Fusion />} />
          <Route path="atlas" element={<Pendiente eyebrow="Atlas" title="Progresión" />} />
          <Route path="perfil" element={<Perfil />} />
          <Route path="*" element={<Pendiente eyebrow="Polaris" title="No encontrada" />} />
        </Route>
      </Route>
    </Routes>
  );
}
