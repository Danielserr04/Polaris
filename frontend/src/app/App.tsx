import { Route, Routes } from 'react-router-dom';
import { Pendiente } from '../pages/Pendiente';
import { AppShell } from './AppShell';

export function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Pendiente eyebrow="Inicio" title="Inicio" />} />
        <Route path="odisea" element={<Pendiente eyebrow="Odisea" title="Tu lista" />} />
        <Route path="kuiper" element={<Pendiente eyebrow="Kuiper" title="Gastos" />} />
        <Route path="fusion" element={<Pendiente eyebrow="Fusión" title="Hoy" />} />
        <Route path="atlas" element={<Pendiente eyebrow="Atlas" title="Progresión" />} />
        <Route path="perfil" element={<Pendiente eyebrow="Cuenta" title="Perfil" />} />
        <Route path="*" element={<Pendiente eyebrow="Polaris" title="No encontrada" />} />
      </Route>
    </Routes>
  );
}
