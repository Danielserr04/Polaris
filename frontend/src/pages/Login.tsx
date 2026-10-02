import { useEffect, useState, type FormEvent } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ApiError } from '../api/client';
import { GOOGLE_LOGIN_URL, login } from '../api/auth';
import { iniciarSesion, useHaySesion } from '../auth/sesion';
import { Button, Eyebrow, Input, Logo, StarTrails } from '../design-system';
import { Registro } from './login/Registro';
import { useTemaOscuro } from '../lib/tema';

const MODULOS: [string, string, string][] = [
  ['Odisea', 'Ocio', 'var(--mod-odisea)'],
  ['Kuiper', 'Gastos', 'var(--mod-kuiper)'],
  ['Fusión', 'Nutrición', 'var(--mod-fusion)'],
  ['Atlas', 'Gym', 'var(--mod-atlas)'],
];

// La "G" de Google en sus colores oficiales (decorativa: el texto del boton ya lo dice).
function GoogleG() {
  return (
    <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true" focusable="false">
      <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
      <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
      <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
      <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
    </svg>
  );
}

// Lo que el backend manda en /login?error= tras un login de Google fallido (ADR 031).
const ERRORES_GOOGLE: Record<string, string> = {
  cancelado: 'Has cancelado el acceso con Google.',
  google: 'No se ha podido completar el acceso con Google.',
};

// El backend manda estos mensajes sin tildes; aqui se muestran como pide el sistema de diseño.
function mensajeLogin(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 401) return 'Usuario o contraseña incorrectos.';
    if (e.status === 403) return 'Verifica tu correo antes de iniciar sesión.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}

// Pantalla de entrada. Las estelas aceleran al entrar (efecto warp) antes de pasar al shell.
export function Login() {
  const haySesion = useHaySesion();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [params] = useSearchParams();
  const [usuario, setUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(ERRORES_GOOGLE[params.get('error') ?? ''] ?? null);
  const [saliendo, setSaliendo] = useState(false);
  const [yendoAGoogle, setYendoAGoogle] = useState(false);
  const [registrando, setRegistrando] = useState(false);
  const [ahora, setAhora] = useState(() => new Date());
  useTemaOscuro();

  useEffect(() => {
    const i = setInterval(() => setAhora(new Date()), 1000);
    return () => clearInterval(i);
  }, []);

  const entrar = useMutation({
    mutationFn: () => login(usuario.trim(), password),
    onSuccess: (t) => {
      queryClient.clear();
      setSaliendo(true);
      setTimeout(() => {
        iniciarSesion(t.token, t.expiraEnSegundos);
        navigate('/', { replace: true });
      }, 900);
    },
    onError: (e) => setError(mensajeLogin(e)),
  });

  if (haySesion && !saliendo) return <Navigate to="/" replace />;

  const enviar = (ev: FormEvent) => {
    ev.preventDefault();
    if (!usuario.trim() || !password) {
      setError('Escribe tu usuario y tu contraseña.');
      return;
    }
    setError(null);
    entrar.mutate();
  };

  const hora = ahora.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const velocidad = saliendo ? 14 : entrar.isPending || yendoAGoogle ? 3 : 1;

  return (
    <div className="lp" data-module="polaris">
      <StarTrails
        anchor={() => {
          const c = document.querySelector('.lp__card');
          const h = document.querySelector('.lp__word');
          if (!c || !h) return null;
          const cr = c.getBoundingClientRect();
          const hr = h.getBoundingClientRect();
          return [(hr.right + cr.left) / 2, Math.max(70, cr.top - 70)];
        }}
        speed={velocidad}
      />
      <div className="lp__veil" />
      <header className="lp__top pl-rise">
        <Logo size={30} />
        <span className="lp__coord">
          α UMi <b>·</b> RA 02h 31m 49s <b>·</b> Dec +89° 15′ 51″ <b>·</b> {hora}
        </span>
      </header>
      <main className={'lp__main' + (saliendo ? ' lp__main--out' : '')}>
        <section className="lp__hero">
          <div className="pl-rise" style={{ animationDelay: '80ms' }}>
            <Eyebrow star>Tu norte, cada día</Eyebrow>
          </div>
          <h1 className="lp__word pl-rise" style={{ animationDelay: '140ms' }}>
            Polaris
          </h1>
          <p className="lp__lead pl-rise" style={{ animationDelay: '220ms' }}>
            Lo que ves, lo que gastas, lo que comes y lo que entrenas. En un solo sitio, sin ruido.
          </p>
          <ul className="lp__mods">
            {MODULOS.map(([n, d, c], i) => (
              <li key={n} className="pl-rise" style={{ animationDelay: 300 + i * 70 + 'ms', ['--c' as string]: c }}>
                <i />
                <b>{n}</b>
                <span>{d}</span>
              </li>
            ))}
          </ul>
        </section>
        <section className="lp__card pl-rise" style={{ animationDelay: '260ms' }}>
          {registrando ? (
            <Registro onVolver={() => setRegistrando(false)} />
          ) : (
            <>
              <div className="lp__cardhead">
                <h2>Entrar</h2>
                <span className="pl-eyebrow">Sesión personal</span>
              </div>
              <form className="lp__form" onSubmit={enviar}>
                <Input
                  label="Usuario o email"
                  icon="user-round"
                  value={usuario}
                  onChange={(e) => setUsuario(e.target.value)}
                  autoComplete="username"
                  autoFocus
                />
                <Input
                  label="Contraseña"
                  icon="key-round"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    setError(null);
                  }}
                  error={error}
                  autoComplete="current-password"
                />
                <Button type="submit" size="lg" block iconRight="arrow-right" loading={entrar.isPending}>
                  Entrar
                </Button>
              </form>
            </>
          )}
          {!registrando && (
            <>
            <div className="lp__or">
              <span>o</span>
            </div>
            <Button
              variant="secondary"
              size="lg"
              block
              loading={yendoAGoogle}
              onClick={() => {
                setYendoAGoogle(true);
                window.location.assign(GOOGLE_LOGIN_URL);
              }}
            >
              <GoogleG />
              Continuar con Google
            </Button>
              <Button variant="ghost" block onClick={() => setRegistrando(true)}>
                Crear cuenta
              </Button>
            </>
          )}
        </section>
      </main>
      <footer className="lp__foot">
        <span>Uso personal · un solo usuario</span>
        <span>Odisea · Kuiper · Fusión · Atlas · Núcleo</span>
      </footer>
    </div>
  );
}
