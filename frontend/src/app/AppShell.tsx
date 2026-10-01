import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { NavBar, StarTrails } from '../design-system';
import { NAV, type ModuloId } from './modulos';

function moduloActual(pathname: string): { activo: string | null; modulo: ModuloId } {
  if (pathname.startsWith('/perfil')) return { activo: null, modulo: 'nucleo' };
  const item = NAV.find((n) => n.ruta !== '/' && pathname.startsWith(n.ruta)) ?? NAV[0];
  return { activo: item.id, modulo: item.modulo };
}

// Cascaron de la app autenticada: isla de navegacion flotante arriba, sin barra lateral.
// El acento de todo el subarbol cambia con data-module.
export function AppShell() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const { activo, modulo } = moduloActual(pathname);

  return (
    <div className="app" data-module={modulo}>
      <div className="app__sky">
        <StarTrails pole={[0.5, -0.08]} speed={0.35} density={0.7} />
      </div>
      <div className="app__veil" />
      <div className="app__glow" />
      <div className="app__nav">
        <NavBar
          items={NAV}
          value={activo}
          onChange={(id: string) => navigate(NAV.find((n) => n.id === id)?.ruta ?? '/')}
          onBrand={() => navigate('/')}
          onSearch={null}
          user={{ name: 'Tú', onClick: () => navigate('/perfil') }}
        />
      </div>
      <main className="app__main" key={pathname}>
        <Outlet />
      </main>
    </div>
  );
}
