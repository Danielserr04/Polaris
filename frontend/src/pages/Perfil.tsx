import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { ApiError } from '../api/client';
import { GOOGLE_LOGIN_URL, useUsuario } from '../api/auth';
import {
  useActualizarPerfil,
  useCambiarEmail,
  useCambiarPassword,
  useDesvincularGoogle,
  useReenviarVerificacion,
} from '../api/perfil';
import type { UsuarioDto } from '../api/tipos';
import { cerrarSesion } from '../auth/sesion';
import { PageHeader } from '../components/PageHeader';
import { Alert, Avatar, Badge, Button, Card, Dialog, Icon, Input, SegmentedControl, Toast, Tooltip } from '../design-system';
import { guardarTema, useTemaElegido, type Tema } from '../lib/tema';
import './perfil.css';

// ---------------------------------------------------------------- textos de error

const SIN_CONEXION = 'No se ha podido conectar con el servidor.';

function sinTildes(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}

/**
 * El backend escribe sin tildes ("contrasena"). Para los casos conocidos se usa el texto
 * correcto del sistema de diseño; el resto pasa tal cual. `status` y mensaje deciden.
 */
function mensajeError(e: unknown): string {
  if (!(e instanceof ApiError)) return SIN_CONEXION;
  const m = sinTildes(e.message);
  if (e.status === 401) return 'La contraseña actual no es correcta.';
  if (m.includes('ya es tu email')) return 'Ese ya es tu email.';
  if (e.status === 409 || m.includes('ya esta registrado')) return 'Ese email ya está registrado por otra cuenta.';
  if (m.includes('ya esta verificado')) return 'Tu email ya está verificado.';
  if (m.includes('pon una contrasena antes'))
    return 'Pon antes una contraseña: sin ella no te quedaría ninguna forma de entrar.';
  if (m.includes('no esta vinculada')) return 'Esta cuenta no está vinculada con Google.';
  return e.message;
}

/**
 * Una URL de imagen que no carga dejaria al Avatar con el texto alternativo a medio pintar.
 * Se comprueba antes y, si falla, se cae a las iniciales.
 */
function useImagenCargable(url: string | undefined): string | undefined {
  const [cargada, setCargada] = useState<string | undefined>();
  useEffect(() => {
    setCargada(undefined);
    if (!url) return;
    const img = new Image();
    img.onload = () => setCargada(url);
    img.src = url;
    return () => {
      img.onload = null;
    };
  }, [url]);
  return cargada;
}

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// ---------------------------------------------------------------- toast

interface Aviso {
  tono: 'success' | 'danger';
  texto: string;
}

function useAviso() {
  const [aviso, setAviso] = useState<Aviso | null>(null);
  useEffect(() => {
    if (!aviso) return;
    const t = setTimeout(() => setAviso(null), 3200);
    return () => clearTimeout(t);
  }, [aviso]);
  return { aviso, mostrar: setAviso, cerrar: () => setAviso(null) };
}

// ---------------------------------------------------------------- pantalla

export function Perfil() {
  const { data: usuario, isPending, isError, refetch } = useUsuario();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const salir = () => {
    cerrarSesion();
    queryClient.clear();
    navigate('/login', { replace: true });
  };

  const alta = usuario
    ? new Date(usuario.creadoEn).toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' })
    : null;

  return (
    <div className="perfil">
      <PageHeader
        eyebrow="Cuenta"
        coord={alta ? 'DESDE ' + alta.toUpperCase() : undefined}
        title="Perfil"
        actions={
          <Button variant="ghost" icon="log-out" onClick={salir}>
            Cerrar sesión
          </Button>
        }
      />
      {usuario ? (
        <PerfilContenido usuario={usuario} />
      ) : isPending ? (
        <p className="muted" role="status">
          Cargando tu perfil…
        </p>
      ) : isError ? (
        <Alert
          tone="danger"
          title="No se ha podido cargar tu perfil"
          action={
            <Button size="sm" variant="secondary" onClick={() => void refetch()}>
              Reintentar
            </Button>
          }
        >
          Comprueba la conexión e inténtalo de nuevo.
        </Alert>
      ) : null}
    </div>
  );
}

// ---------------------------------------------------------------- contenido

function PerfilContenido({ usuario: u }: { usuario: UsuarioDto }) {
  const { aviso, mostrar, cerrar } = useAviso();
  return (
    <>
      <div className="stack-16">
        <TusDatos u={u} avisar={mostrar} />
        <EmailCard u={u} avisar={mostrar} />
        <PasswordCard u={u} avisar={mostrar} />
        <GoogleCard u={u} avisar={mostrar} />
        <AparienciaCard />
      </div>
      {aviso && (
        <Toast fixed tone={aviso.tono} onClose={cerrar}>
          {aviso.texto}
        </Toast>
      )}
    </>
  );
}

interface CardProps {
  u: UsuarioDto;
  avisar: (a: Aviso) => void;
}

// ---- 01 Tus datos

function TusDatos({ u, avisar }: CardProps) {
  const nombreGuardado = u.nombre ?? u.username;
  const [nombre, setNombre] = useState(nombreGuardado);
  const [error, setError] = useState<string | null>(null);
  const [cambiandoAvatar, setCambiandoAvatar] = useState(false);
  const guardar = useActualizarPerfil();
  const avatar = useImagenCargable(u.avatarUrl);

  // Si el usuario se refresca desde fuera (otro guardado, nuevo login), el campo lo sigue.
  useEffect(() => setNombre(nombreGuardado), [nombreGuardado]);

  const limpio = nombre.trim();
  const sinCambios = limpio === nombreGuardado;

  const enviar = (ev: FormEvent) => {
    ev.preventDefault();
    if (!limpio) return setError('El nombre no puede estar vacío.');
    if (limpio.length > 255) return setError('Máximo 255 caracteres.');
    setError(null);
    guardar.mutate(
      { nombre: limpio, avatarUrl: u.avatarUrl ?? null },
      {
        onSuccess: () => avisar({ tono: 'success', texto: 'Nombre guardado' }),
        onError: (e) => setError(mensajeError(e)),
      },
    );
  };

  return (
    <Card delay={60} eyebrow="01" title="Tus datos">
      <form className="pf-datos" onSubmit={enviar} noValidate>
        <div className="stack-8" style={{ alignItems: 'center' }}>
          <Avatar name={nombreGuardado} src={avatar} size={88} />
          <Button type="button" size="sm" variant="ghost" icon="image-up" onClick={() => setCambiandoAvatar(true)}>
            Cambiar
          </Button>
        </div>
        <div className="stack-12">
          <div className="pf-2">
            <Input
              label="Nombre"
              value={nombre}
              maxLength={255}
              autoComplete="name"
              onChange={(e) => {
                setNombre(e.target.value);
                setError(null);
              }}
              error={error}
              aria-invalid={error ? true : undefined}
            />
            <Input
              label="Nombre de usuario"
              value={'@' + u.username}
              locked
              hint="Es la mitad de tus credenciales: no se puede cambiar."
            />
          </div>
          <div>
            <Button
              type="submit"
              variant="secondary"
              loading={guardar.isPending}
              disabled={sinCambios}
              title={sinCambios ? 'Cambia el nombre para poder guardarlo' : undefined}
            >
              Guardar nombre
            </Button>
          </div>
        </div>
      </form>
      {cambiandoAvatar && <AvatarDialog u={u} onClose={() => setCambiandoAvatar(false)} avisar={avisar} />}
    </Card>
  );
}

function AvatarDialog({ u, onClose, avisar }: CardProps & { onClose: () => void }) {
  const [url, setUrl] = useState(u.avatarUrl ?? '');
  const [error, setError] = useState<string | null>(null);
  const guardar = useActualizarPerfil();
  const nombre = u.nombre ?? u.username;

  const enviar = (avatarUrl: string | null, texto: string) => {
    guardar.mutate(
      { nombre, avatarUrl },
      {
        onSuccess: () => {
          avisar({ tono: 'success', texto });
          onClose();
        },
        onError: (e) => setError(mensajeError(e)),
      },
    );
  };

  const aceptar = (ev: FormEvent) => {
    ev.preventDefault();
    const limpia = url.trim();
    if (!limpia) return setError('Pega la dirección de una imagen o usa «Quitar foto».');
    if (limpia.length > 255) return setError('Máximo 255 caracteres.');
    if (!/^https?:\/\/\S+$/i.test(limpia)) return setError('Escribe una dirección que empiece por http:// o https://');
    setError(null);
    enviar(limpia, 'Foto actualizada');
  };

  const previa = useImagenCargable(/^https?:\/\/\S+$/i.test(url.trim()) ? url.trim() : undefined);

  return (
    <Dialog
      open
      onClose={onClose}
      title="Cambiar foto"
      footer={
        <>
          {u.avatarUrl && (
            <Button
              type="button"
              variant="ghost"
              disabled={guardar.isPending}
              onClick={() => enviar(null, 'Foto quitada')}
              style={{ marginRight: 'auto' }}
            >
              Quitar foto
            </Button>
          )}
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-avatar" loading={guardar.isPending}>
            Guardar
          </Button>
        </>
      }
    >
      <form id="form-avatar" className="pf-avatar" onSubmit={aceptar} noValidate>
        <Avatar name={nombre} src={previa} size={72} />
        <Input
          label="Dirección de la imagen"
          icon="link-2"
          type="url"
          placeholder="https://…"
          value={url}
          autoFocus
          onChange={(e) => {
            setUrl(e.target.value);
            setError(null);
          }}
          error={error}
          aria-invalid={error ? true : undefined}
          hint="Polaris guarda la dirección, no la imagen."
        />
      </form>
    </Dialog>
  );
}

// ---- 02 Email

function EmailCard({ u, avisar }: CardProps) {
  const [email, setEmail] = useState(u.email);
  const [password, setPassword] = useState('');
  const [emailErr, setEmailErr] = useState<string | null>(null);
  const [passErr, setPassErr] = useState<string | null>(null);
  const cambiar = useCambiarEmail();
  const reenviar = useReenviarVerificacion();

  useEffect(() => setEmail(u.email), [u.email]);

  const cambiado = email.trim().toLowerCase() !== u.email.toLowerCase();

  const enviar = (ev: FormEvent) => {
    ev.preventDefault();
    const nuevo = email.trim();
    if (!nuevo) return setEmailErr('Escribe un email.');
    if (!EMAIL_VALIDO.test(nuevo)) return setEmailErr('Ese email no tiene un formato válido.');
    if (nuevo.toLowerCase() === u.email.toLowerCase()) return setEmailErr('Ese ya es tu email.');
    if (u.tienePassword && !password) return setPassErr('Escribe tu contraseña actual para confirmar el cambio.');
    setEmailErr(null);
    setPassErr(null);
    cambiar.mutate(
      { email: nuevo, password: u.tienePassword ? password : undefined },
      {
        onSuccess: () => {
          setPassword('');
          avisar({ tono: 'success', texto: 'Te hemos enviado un enlace a ' + nuevo });
        },
        onError: (e) => {
          const msg = mensajeError(e);
          if (e instanceof ApiError && e.status === 401) setPassErr(msg);
          else setEmailErr(msg);
        },
      },
    );
  };

  const reenviarCorreo = () =>
    reenviar.mutate(undefined, {
      onSuccess: () => avisar({ tono: 'success', texto: 'Correo reenviado' }),
      onError: (e) => avisar({ tono: 'danger', texto: mensajeError(e) }),
    });

  return (
    <Card
      delay={120}
      eyebrow="02"
      title="Email"
      action={u.emailVerificado ? <Badge tone="success">Verificado</Badge> : <Badge tone="warning">Sin verificar</Badge>}
    >
      <div className="stack-12">
        {!u.emailVerificado && (
          <Alert
            tone="warning"
            title="Falta un paso: verifica tu email"
            action={
              <Button size="sm" variant="secondary" icon="send" loading={reenviar.isPending} onClick={reenviarCorreo}>
                Reenviar correo
              </Button>
            }
          >
            Te enviamos un enlace a {u.email}. Hasta que lo abras, el email aparece como sin verificar.
          </Alert>
        )}
        <form className="stack-12" onSubmit={enviar} noValidate>
          <div className="pf-row">
            <Input
              style={{ flex: 1 }}
              label="Dirección"
              icon="mail"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setEmailErr(null);
              }}
              error={emailErr}
              aria-invalid={emailErr ? true : undefined}
              hint="Si lo cambias, quedará sin verificar hasta que abras el enlace."
            />
            <Button type="submit" variant="secondary" loading={cambiar.isPending}>
              Guardar
            </Button>
          </div>
          {cambiado && u.tienePassword && (
            <div className="pf-confirma">
              <Input
                label="Contraseña actual"
                icon="key-round"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setPassErr(null);
                }}
                error={passErr}
                aria-invalid={passErr ? true : undefined}
                hint="La pedimos para confirmar que eres tú."
              />
            </div>
          )}
        </form>
      </div>
    </Card>
  );
}

// ---- 03 Contraseña

function PasswordCard({ u, avisar }: CardProps) {
  const [actual, setActual] = useState('');
  const [nueva, setNueva] = useState('');
  const [repite, setRepite] = useState('');
  const [actualErr, setActualErr] = useState<string | null>(null);
  const [nuevaErr, setNuevaErr] = useState<string | null>(null);
  const [repiteErr, setRepiteErr] = useState<string | null>(null);
  const cambiar = useCambiarPassword();

  const enviar = (ev: FormEvent) => {
    ev.preventDefault();
    let ok = true;
    if (u.tienePassword && !actual) {
      setActualErr('Escribe tu contraseña actual.');
      ok = false;
    }
    if (nueva.length < 8) {
      setNuevaErr('Mínimo 8 caracteres.');
      ok = false;
    } else if (nueva.length > 100) {
      setNuevaErr('Máximo 100 caracteres.');
      ok = false;
    }
    if (repite !== nueva) {
      setRepiteErr('No coincide con la nueva contraseña.');
      ok = false;
    }
    if (!ok) return;

    cambiar.mutate(
      { passwordActual: u.tienePassword ? actual : undefined, passwordNueva: nueva },
      {
        onSuccess: () => {
          setActual('');
          setNueva('');
          setRepite('');
          avisar({ tono: 'success', texto: u.tienePassword ? 'Contraseña cambiada' : 'Contraseña creada' });
        },
        onError: (e) => {
          const msg = mensajeError(e);
          if (e instanceof ApiError && e.status === 401) setActualErr(msg);
          else setNuevaErr(msg);
        },
      },
    );
  };

  return (
    <Card delay={180} eyebrow="03" title={u.tienePassword ? 'Cambiar contraseña' : 'Poner una contraseña'}>
      <form className="stack-12" onSubmit={enviar} noValidate>
        {!u.tienePassword && (
          <span className="muted" style={{ fontSize: 13 }}>
            Entraste con Google y aún no tienes contraseña. Con una podrás entrar también con tu usuario.
          </span>
        )}
        <div className="pf-3">
          {u.tienePassword && (
            <Input
              label="Contraseña actual"
              type="password"
              autoComplete="current-password"
              value={actual}
              onChange={(e) => {
                setActual(e.target.value);
                setActualErr(null);
              }}
              error={actualErr}
              aria-invalid={actualErr ? true : undefined}
            />
          )}
          <Input
            label="Nueva contraseña"
            type="password"
            autoComplete="new-password"
            value={nueva}
            onChange={(e) => {
              setNueva(e.target.value);
              setNuevaErr(null);
            }}
            error={nuevaErr}
            aria-invalid={nuevaErr ? true : undefined}
            hint={nuevaErr ? undefined : 'Entre 8 y 100 caracteres.'}
          />
          <Input
            label="Repite la nueva"
            type="password"
            autoComplete="new-password"
            value={repite}
            onChange={(e) => {
              setRepite(e.target.value);
              setRepiteErr(null);
            }}
            error={repiteErr}
            aria-invalid={repiteErr ? true : undefined}
          />
        </div>
        <div>
          <Button type="submit" loading={cambiar.isPending}>
            {u.tienePassword ? 'Cambiar contraseña' : 'Poner contraseña'}
          </Button>
        </div>
      </form>
    </Card>
  );
}

// ---- 04 Google

function GoogleCard({ u, avisar }: CardProps) {
  const [confirmando, setConfirmando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [yendoAGoogle, setYendoAGoogle] = useState(false);
  const desvincular = useDesvincularGoogle();

  const cerrarDialogo = () => {
    setConfirmando(false);
    setError(null);
  };

  const confirmar = () =>
    desvincular.mutate(undefined, {
      onSuccess: () => {
        cerrarDialogo();
        avisar({ tono: 'success', texto: 'Google desvinculado' });
      },
      onError: (e) => setError(mensajeError(e)),
    });

  const conectar = () => {
    setYendoAGoogle(true);
    window.location.assign(GOOGLE_LOGIN_URL);
  };

  return (
    <Card delay={240} eyebrow="04" title="Google">
      {u.tieneGoogle ? (
        <div className="pf-google">
          <span className="gmark">
            <Icon name="link-2" size={16} />
          </span>
          <div className="stack-4" style={{ flex: 1 }}>
            <b>Vinculada</b>
            <span className="muted" style={{ fontSize: 13 }}>
              {u.email}
            </span>
          </div>
          {u.tienePassword ? (
            <Button variant="danger" icon="unlink" onClick={() => setConfirmando(true)}>
              Desvincular
            </Button>
          ) : (
            <Tooltip wrap label="Pon antes una contraseña: sin ella no te quedaría ninguna forma de entrar.">
              <Button variant="danger" icon="unlink" disabled>
                Desvincular
              </Button>
            </Tooltip>
          )}
        </div>
      ) : (
        <div className="pf-google">
          <span className="gmark gmark--off">
            <Icon name="link-2-off" size={16} />
          </span>
          <div className="stack-4" style={{ flex: 1 }}>
            <b>No vinculada</b>
            <span className="muted" style={{ fontSize: 13 }}>
              Conecta tu cuenta para entrar con un clic.
            </span>
          </div>
          <Button variant="secondary" icon="link-2" loading={yendoAGoogle} onClick={conectar}>
            Conectar con Google
          </Button>
        </div>
      )}
      {u.tieneGoogle && !u.tienePassword && (
        <div style={{ marginTop: 12 }}>
          <Alert tone="info" title="¿Por qué no puedo desvincular?">
            Google es ahora tu única forma de entrar. Pon una contraseña en el bloque 03 y podrás hacerlo.
          </Alert>
        </div>
      )}
      <Dialog
        open={confirmando}
        onClose={cerrarDialogo}
        title="¿Desvincular Google?"
        footer={
          <>
            <Button variant="ghost" onClick={cerrarDialogo}>
              Cancelar
            </Button>
            <Button variant="danger" loading={desvincular.isPending} onClick={confirmar}>
              Desvincular
            </Button>
          </>
        }
      >
        <div className="stack-12">
          <span>
            Seguirás entrando con <b style={{ color: 'var(--text-1)' }}>@{u.username}</b> y tu contraseña. Puedes volver
            a conectarla cuando quieras.
          </span>
          {error && (
            <Alert tone="danger" title="No se ha podido desvincular">
              {error}
            </Alert>
          )}
        </div>
      </Dialog>
    </Card>
  );
}

// ---------------------------------------------------------------- 05 apariencia

function AparienciaCard() {
  const tema = useTemaElegido();
  return (
    <Card delay={300} eyebrow="05" title="Apariencia">
      <div className="stack-12">
        <SegmentedControl
          value={tema}
          onChange={(v) => guardarTema(v as Tema)}
          options={[
            { value: 'oscuro', label: 'Oscuro' },
            { value: 'claro', label: 'Claro' },
            { value: 'sistema', label: 'Sistema' },
          ]}
        />
        <span className="muted" style={{ fontSize: 13 }}>
          Oscuro es el tema de la marca. «Sistema» sigue la preferencia de tu dispositivo. Se guarda en este navegador.
        </span>
      </div>
    </Card>
  );
}
