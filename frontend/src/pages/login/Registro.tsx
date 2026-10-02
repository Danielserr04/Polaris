import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { ApiError } from '../../api/client';
import { registrar } from '../../api/auth';
import { Alert, Button, Input } from '../../design-system';

interface Props {
  /** Vuelve a la pantalla de entrar. */
  onVolver: () => void;
}

// El backend manda estos mensajes sin tildes; aqui se muestran como pide el sistema de diseño.
function mensajeRegistro(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 409) return 'Ese usuario o ese email ya están registrados.';
    if (e.status === 400) return 'Revisa los datos: usuario de 3 a 50 caracteres, email válido y contraseña de 8 a 100.';
    return e.message;
  }
  return 'No se ha podido conectar con el servidor.';
}

/** Contenido de la tarjeta del login en modo "crear cuenta": formulario y, despues, el aviso de verificacion. */
export function Registro({ onVolver }: Props) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  const crear = useMutation({
    mutationFn: () => registrar(username.trim(), email.trim(), password),
    onError: (e) => setError(mensajeRegistro(e)),
  });

  const u = username.trim();
  const errUsuario = u.length < 3 || u.length > 50 ? 'De 3 a 50 caracteres.' : null;
  const errEmail = /^\S+@\S+\.\S+$/.test(email.trim()) ? null : 'Escribe un email válido.';
  const errPassword = password.length < 8 || password.length > 100 ? 'De 8 a 100 caracteres.' : null;

  const enviar = (ev: FormEvent) => {
    ev.preventDefault();
    setError(null);
    if (errUsuario || errEmail || errPassword) return;
    crear.mutate();
  };

  if (crear.isSuccess) {
    return (
      <>
        <div className="lp__cardhead">
          <h2>Revisa tu correo</h2>
        </div>
        <Alert tone="success" title="Cuenta creada">
          Hemos enviado un enlace de verificación a {email.trim()}. Ábrelo y después podrás entrar.
        </Alert>
        <Button size="lg" block onClick={onVolver}>
          Ir a entrar
        </Button>
      </>
    );
  }

  return (
    <>
      <div className="lp__cardhead">
        <h2>Crear cuenta</h2>
      </div>
      <form className="lp__form" onSubmit={enviar} noValidate>
        {error && <Alert tone="danger">{error}</Alert>}
        <Input label="Usuario" icon="user-round" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" autoFocus error={errUsuario} validarAlSalir />
        <Input label="Email" icon="mail" type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" error={errEmail} validarAlSalir />
        <Input label="Contraseña" icon="key-round" type="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="new-password" error={errPassword} validarAlSalir hint="Mínimo 8 caracteres." />
        <Button type="submit" size="lg" block iconRight="arrow-right" loading={crear.isPending}>
          Crear cuenta
        </Button>
      </form>
      <Button variant="ghost" block onClick={onVolver}>
        Ya tengo cuenta
      </Button>
    </>
  );
}
