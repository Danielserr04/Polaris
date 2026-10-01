const PERFIL_ESTADOS = {
  completa: { tieneGoogle: true, tienePassword: true, emailVerificado: true },
  google: { tieneGoogle: true, tienePassword: false, emailVerificado: true },
  nativa: { tieneGoogle: false, tienePassword: true, emailVerificado: true },
  sinverificar: { tieneGoogle: true, tienePassword: true, emailVerificado: false },
};
function Perfil() {
  const { Card, Avatar, Input, Button, Alert, Tooltip, Dialog, SegmentedControl, Badge, Icon, Toast } = window.PolarisDesignSystem_b0ab94;
  const [demo, setDemo] = React.useState('completa');
  const u = { ...PD.user, ...PERFIL_ESTADOS[demo] };
  const [email, setEmail] = React.useState(u.email);
  const [emailErr, setEmailErr] = React.useState(null);
  const [actual, setActual] = React.useState('');
  const [nueva, setNueva] = React.useState('');
  const [passErr, setPassErr] = React.useState(null);
  const [unlink, setUnlink] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const flash = t => { setToast(t); setTimeout(() => setToast(null), 2800); };
  const saveEmail = () => {
    if (email === u.email) return setEmailErr('Ese ya es tu email.');
    if (email.startsWith('otro')) return setEmailErr('Ese email ya está registrado por otra cuenta.');
    setEmailErr(null); setDemo('sinverificar'); flash('Te hemos enviado un enlace a ' + email);
  };
  const savePass = () => {
    if (u.tienePassword && actual !== 'polaris') return setPassErr('La contraseña actual no es correcta.');
    setPassErr(null); setActual(''); setNueva(''); flash(u.tienePassword ? 'Contraseña cambiada' : 'Contraseña creada');
  };
  const creado = new Date(u.creadoEn).toLocaleDateString('es-ES', { day: 'numeric', month: 'long', year: 'numeric' });
  return (
    <div className="perfil">
      <PageHeader eyebrow="Cuenta" coord={'DESDE ' + creado.toUpperCase()} title="Perfil" actions={<Button variant="ghost" icon="log-out" onClick={() => window.polarisLogout && window.polarisLogout()}>Cerrar sesión</Button>} />
      <div className="demo pl-rise"><span className="pl-eyebrow">Estado de demo</span>
        <SegmentedControl value={demo} onChange={setDemo} options={[{ value: 'completa', label: 'Cuenta completa' }, { value: 'google', label: 'Solo Google' }, { value: 'nativa', label: 'Solo nativa' }, { value: 'sinverificar', label: 'Email sin verificar' }]} />
      </div>
      <div className="stack-16">
        <Card delay={60} eyebrow="01" title="Tus datos">
          <div className="pf-datos">
            <div className="stack-8" style={{ alignItems: 'center' }}><Avatar name={u.nombre} size={88} /><Button size="sm" variant="ghost" icon="image-up">Cambiar</Button></div>
            <div className="pf-2">
              <Input label="Nombre" defaultValue={u.nombre} />
              <Input label="Nombre de usuario" value={'@' + u.username} locked hint="Es la mitad de tus credenciales: no se puede cambiar." />
            </div>
          </div>
        </Card>
        <Card delay={120} eyebrow="02" title="Email" action={u.emailVerificado ? <Badge tone="success">Verificado</Badge> : <Badge tone="warning">Sin verificar</Badge>}>
          <div className="stack-12">
            {!u.emailVerificado && <Alert tone="warning" title="Falta un paso: verifica tu nuevo email" action={<Button size="sm" variant="secondary" icon="send" onClick={() => flash('Correo reenviado')}>Reenviar correo</Button>}>Te enviamos un enlace. Hasta que lo abras, el email aparece como sin verificar.</Alert>}
            <div className="pf-row"><Input style={{ flex: 1 }} label="Dirección" icon="mail" value={email} onChange={e => { setEmail(e.target.value); setEmailErr(null); }} error={emailErr} hint="Si lo cambias, quedará sin verificar hasta que abras el enlace." /><Button variant="secondary" onClick={saveEmail}>Guardar</Button></div>
          </div>
        </Card>
        <Card delay={180} eyebrow="03" title={u.tienePassword ? 'Cambiar contraseña' : 'Poner una contraseña'}>
          <div className="stack-12">
            {!u.tienePassword && <span className="muted" style={{ fontSize: 13 }}>Entraste con Google y aún no tienes contraseña. Con una podrás entrar también con tu usuario.</span>}
            <div className="pf-3">
              {u.tienePassword && <Input label="Contraseña actual" type="password" value={actual} onChange={e => { setActual(e.target.value); setPassErr(null); }} error={passErr} hint="Pista de la maqueta: polaris" />}
              <Input label="Nueva contraseña" type="password" value={nueva} onChange={e => setNueva(e.target.value)} />
              <Input label="Repite la nueva" type="password" />
            </div>
            <div><Button onClick={savePass}>{u.tienePassword ? 'Cambiar contraseña' : 'Poner contraseña'}</Button></div>
          </div>
        </Card>
        <Card delay={240} eyebrow="04" title="Google">
          {u.tieneGoogle ? (
            <div className="pf-google">
              <span className="gmark"><Icon name="link-2" size={16} /></span>
              <div className="stack-4" style={{ flex: 1 }}><b>Vinculada</b><span className="muted" style={{ fontSize: 13 }}>{PD.user.email}</span></div>
              {u.tienePassword ? <Button variant="danger" icon="unlink" onClick={() => setUnlink(true)}>Desvincular</Button>
                : <Tooltip wrap label="Pon antes una contraseña: sin ella no te quedaría ninguna forma de entrar."><Button variant="danger" icon="unlink" disabled>Desvincular</Button></Tooltip>}
            </div>
          ) : (
            <div className="pf-google">
              <span className="gmark gmark--off"><Icon name="link-2-off" size={16} /></span>
              <div className="stack-4" style={{ flex: 1 }}><b>No vinculada</b><span className="muted" style={{ fontSize: 13 }}>Conecta tu cuenta para entrar con un clic.</span></div>
              <Button variant="secondary" icon="link-2">Conectar con Google</Button>
            </div>
          )}
          {u.tieneGoogle && !u.tienePassword && <div style={{ marginTop: 12 }}><Alert tone="info" title="¿Por qué no puedo desvincular?">Google es ahora tu única forma de entrar. Pon una contraseña en el bloque 03 y podrás hacerlo.</Alert></div>}
        </Card>
      </div>
      <Dialog open={unlink} onClose={() => setUnlink(false)} title="¿Desvincular Google?" footer={<><Button variant="ghost" onClick={() => setUnlink(false)}>Cancelar</Button><Button variant="danger" onClick={() => { setUnlink(false); setDemo('nativa'); flash('Google desvinculado'); }}>Desvincular</Button></>}>
        Seguirás entrando con <b style={{ color: 'var(--text-1)' }}>@{u.username}</b> y tu contraseña. Puedes volver a conectarla cuando quieras.
      </Dialog>
      {toast && <Toast fixed tone="success" onClose={() => setToast(null)}>{toast}</Toast>}
    </div>
  );
}
window.Perfil = Perfil;
