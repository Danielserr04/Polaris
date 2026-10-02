import { useState, type FormEvent } from 'react';
import {
  mensajeErrorCuentas,
  TIPOS_CUENTA,
  useActualizarCuenta,
  useBorrarCuenta,
  useCrearCuenta,
  useCuenta,
  type CuentaForm,
  type CuentaRequest,
  type TipoCuenta,
} from '../../api/kuiperCuentas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Icon, Input, Select, Switch } from '../../design-system';

interface Props {
  /** Sin id: alta. Con id: edicion, archivado y borrado. */
  cuentaId?: number;
  onClose: () => void;
}

// Mismos colores que las categorias: los del propio sistema.
const COLORES = ['#5bb3a0', '#d0799f', '#e0b04a', '#e8845a', '#7d95e0', '#cdbf98', '#8fb97a', '#d36b5e'];
const ICONOS = ['wallet', 'house', 'shopping-basket', 'key-round', 'lock', 'scale', 'star', 'sparkles'];
// Con signo: una tarjeta puede empezar en negativo. Cabe en DECIMAL(12,2).
const SALDO = /^-?\d{1,10}([.,]\d{1,2})?$/;

export function FormularioCuenta({ cuentaId, onClose }: Props) {
  useRestaurarFoco();
  const editando = cuentaId !== undefined;
  const ficha = useCuenta(cuentaId);

  if (editando && (ficha.isPending || ficha.isError)) {
    return (
      <Dialog open onClose={onClose} title="Editar cuenta" footer={<Button variant="ghost" onClick={onClose}>Cerrar</Button>}>
        {ficha.isPending ? <p className="muted" style={{ margin: 0 }}>Cargando…</p> : <Alert tone="danger">{mensajeErrorCuentas(ficha.error)}</Alert>}
      </Dialog>
    );
  }
  return <Cuerpo cuenta={ficha.data} onClose={onClose} />;
}

function Cuerpo({ cuenta, onClose }: { cuenta: CuentaForm | undefined; onClose: () => void }) {
  const editando = cuenta !== undefined;
  const crear = useCrearCuenta();
  const actualizar = useActualizarCuenta();
  const borrar = useBorrarCuenta(onClose);

  const [nombre, setNombre] = useState(cuenta?.nombre ?? '');
  const [tipo, setTipo] = useState<TipoCuenta>(cuenta?.tipo ?? 'CORRIENTE');
  const [saldo, setSaldo] = useState(cuenta ? String(cuenta.saldoInicial).replace('.', ',') : '0');
  const [banco, setBanco] = useState(cuenta?.banco ?? '');
  const [color, setColor] = useState<string | null>(cuenta?.color ?? null);
  const [icono, setIcono] = useState<string | null>(cuenta?.icono ?? null);
  const [archivada, setArchivada] = useState(cuenta?.archivada ?? false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  const saldoNum = SALDO.test(saldo.trim()) ? Number(saldo.trim().replace(',', '.')) : NaN;
  const errorNombre = nombre.trim() === '' ? 'Escribe un nombre.' : null;
  const errorSaldo = Number.isNaN(saldoNum) ? 'Escribe un importe, con hasta 2 decimales (puede ser negativo).' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setErrorEnvio(null);
    if (errorNombre || errorSaldo) return;
    const cuerpo: CuentaRequest = {
      nombre: nombre.trim(),
      tipo,
      saldoInicial: saldoNum,
      color,
      icono,
      banco: banco.trim() || null,
      archivada,
    };
    try {
      if (cuenta) await actualizar.mutateAsync({ id: cuenta.id, cuerpo });
      else await crear.mutateAsync(cuerpo);
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeErrorCuentas(e));
    }
  };

  const borrarCuenta = () => {
    if (!cuenta) return;
    setErrorEnvio(null);
    borrar.mutate(cuenta.id, {
      onError: (e) => {
        setConfirmando(false);
        setErrorEnvio(mensajeErrorCuentas(e));
      },
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      confirmarDescarte
      title={editando ? 'Editar cuenta' : 'Nueva cuenta'}
      footer={
        confirmando ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar esta cuenta?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarCuenta}>
              Sí, borrar
            </Button>
          </>
        ) : (
          <>
            {editando && (
              <Button variant="ghost" type="button" className="kui-form__borrar" disabled={ocupado} onClick={() => setConfirmando(true)}>
                Borrar
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" form="kui-form-cuenta" loading={ocupado}>
              {editando ? 'Guardar' : 'Añadir'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-cuenta" className="kui-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        <Input
          label="Nombre"
          autoFocus
          maxLength={100}
          placeholder="Nómina, Hucha, Cartera…"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          error={errorNombre} validarAlSalir
        />
        <div className="kui-form__row">
          <Select label="Tipo" value={tipo} onChange={(e) => setTipo(e.target.value as TipoCuenta)} options={TIPOS_CUENTA} />
          <Input label="Banco" placeholder="Opcional" maxLength={100} value={banco} onChange={(e) => setBanco(e.target.value)} />
        </div>
        <Input
          label="Saldo inicial (€)"
          inputMode="decimal"
          placeholder="0,00"
          value={saldo}
          onChange={(e) => setSaldo(e.target.value)}
          error={errorSaldo} validarAlSalir
          hint="Lo que había al empezar a apuntar. El saldo actual se calcula con los movimientos y transferencias."
        />
        <div>
          <span className="kui-field__label">Color</span>
          <div className="kui-swatches" role="group" aria-label="Color">
            <button
              type="button"
              className="kui-swatch kui-swatch--none"
              aria-pressed={color === null}
              aria-label="Sin color"
              onClick={() => setColor(null)}
            >
              —
            </button>
            {COLORES.map((c) => (
              <button
                key={c}
                type="button"
                className="kui-swatch"
                style={{ ['--c' as string]: c }}
                aria-pressed={color === c}
                aria-label={`Color ${c}`}
                onClick={() => setColor(c)}
              />
            ))}
          </div>
        </div>
        <div>
          <span className="kui-field__label">Icono</span>
          <div className="kui-icons" role="group" aria-label="Icono">
            {ICONOS.map((n) => (
              <button key={n} type="button" className="kui-icon" aria-pressed={icono === n} aria-label={n} onClick={() => setIcono(icono === n ? null : n)}>
                <Icon name={n} size={16} />
              </button>
            ))}
          </div>
        </div>
        {editando && <Switch label="Archivada (no se ofrece para movimientos nuevos)" checked={archivada} onChange={setArchivada} />}
      </form>
    </Dialog>
  );
}
