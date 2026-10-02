import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Avisos } from '../components/Avisos';
import { CampanaRecordatorios } from '../components/CampanaRecordatorios';
import { useUsuario } from '../api/auth';
import { Avatar, Logo, NavBar, StarTrails } from '../design-system';
import { useAplicarTema, useTemaAplicado } from '../lib/tema';
import { MobileTabs } from './MobileTabs';
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
  const { data: usuario } = useUsuario();
  const tema = useTemaAplicado();
  useAplicarTema(tema);

  return (
    <div className="app" data-module={modulo} data-theme={tema === 'light' ? 'light' : undefined}>
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
          user={{ name: usuario?.nombre ?? usuario?.username ?? '·', src: usuario?.avatarUrl, onClick: () => navigate('/perfil') }}
        />
        <CampanaRecordatorios className="campana--nav" />
      </div>
      <div className="app__mtop">
        <button type="button" className="app__mbrand" onClick={() => navigate('/')} aria-label="Polaris — inicio">
          <Logo variant="mark" size={24} />
        </button>
        <span className="app__macc">
          <CampanaRecordatorios />
          <button type="button" className="m-av" onClick={() => navigate('/perfil')} aria-label="Perfil">
            <Avatar name={usuario?.nombre ?? usuario?.username ?? '·'} src={usuario?.avatarUrl} size={34} />
          </button>
        </span>
      </div>
      <MobileTabs activo={activo} onChange={(id) => navigate(NAV.find((n) => n.id === id)?.ruta ?? '/')} />
      <main className="app__main" key={pathname}>
        <Outlet />
      </main>
      <Avisos />
    </div>
  );
}
