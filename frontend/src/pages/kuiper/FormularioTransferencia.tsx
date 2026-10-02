import { useMemo, useState, type FormEvent } from 'react';
import {
  mensajeErrorCuentas,
  useActualizarTransferencia,
  useBorrarTransferencia,
  useCrearTransferencia,
  useCuentas,
  type TransferenciaList,
  type TransferenciaRequest,
} from '../../api/kuiperCuentas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, Input, Select } from '../../design-system';
import { iso } from '../../lib/fechas';

interface Props {
  /** Sin transferencia: alta. Con ella: edicion y borrado (el listado ya trae todos sus campos). */
  transferencia?: TransferenciaList;
  /** Cuenta de origen sugerida al abrir el alta desde una tarjeta. */
  origenInicial?: number;
  onClose: () => void;
}

const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;

export function FormularioTransferencia({ transferencia, origenInicial, onClose }: Props) {
  useRestaurarFoco();
  const editando = transferencia !== undefined;
  const hoy = iso(new Date());
  const cuentas = useCuentas();
  const crear = useCrearTransferencia();
  const actualizar = useActualizarTransferencia(transferencia?.id ?? 0);
  const borrar = useBorrarTransferencia(onClose);

  const [origen, setOrigen] = useState(String(transferencia?.cuentaOrigenId ?? origenInicial ?? ''));
  const [destino, setDestino] = useState(String(transferencia?.cuentaDestinoId ?? ''));
  const [importe, setImporte] = useState(transferencia ? String(transferencia.importe).replace('.', ',') : '');
  const [fecha, setFecha] = useState(transferencia?.fecha ?? hoy);
  const [concepto, setConcepto] = useState(transferencia?.concepto ?? '');
  const [intentado, setIntentado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorEnvio, setErrorEnvio] = useState<string | null>(null);

  // Las archivadas no se ofrecen, salvo la que ya tenga la transferencia que se edita.
  const disponibles = useMemo(
    () =>
      (cuentas.data ?? []).filter(
        (c) => !c.archivada || c.id === transferencia?.cuentaOrigenId || c.id === transferencia?.cuentaDestinoId,
      ),
    [cuentas.data, transferencia],
  );
  const opciones = (vacia: string, excluir: string) => [
    { value: '', label: vacia },
    ...disponibles.filter((c) => String(c.id) !== excluir).map((c) => ({ value: String(c.id), label: c.nombre })),
  ];

  const importeNum = IMPORTE.test(importe.trim()) ? Number(importe.trim().replace(',', '.')) : NaN;
  const errorOrigen = origen === '' ? 'Elige la cuenta de origen.' : null;
  const errorDestino = destino === '' ? 'Elige la cuenta de destino.' : destino === origen ? 'Tiene que ser otra cuenta.' : null;
  const errorImporte = !(importeNum > 0) ? 'Escribe un importe mayor que 0, con hasta 2 decimales.' : null;
  const errorFecha = !fecha ? 'Elige una fecha.' : fecha > hoy ? 'No puede ser una fecha futura.' : null;
  const ocupado = crear.isPending || actualizar.isPending || borrar.isPending;
  const ver = (e: string | null) => (intentado ? e : null);

  const guardar = async (ev: FormEvent) => {
    ev.preventDefault();
    setIntentado(true);
    setErrorEnvio(null);
    if (errorOrigen || errorDestino || errorImporte || errorFecha) return;
    const cuerpo: TransferenciaRequest = {
      cuentaOrigenId: Number(origen),
      cuentaDestinoId: Number(destino),
      importe: importeNum,
      fecha,
      concepto: concepto.trim() || null,
    };
    try {
      if (editando) await actualizar.mutateAsync(cuerpo);
      else await crear.mutateAsync(cuerpo);
      onClose();
    } catch (e) {
      setErrorEnvio(mensajeErrorCuentas(e));
    }
  };

  const borrarTransferencia = () => {
    if (!transferencia) return;
    setErrorEnvio(null);
    borrar.mutate(transferencia.id, {
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
      title={editando ? 'Editar transferencia' : 'Nueva transferencia'}
      footer={
        confirmando ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar esta transferencia?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmando(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={borrar.isPending} onClick={borrarTransferencia}>
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
            <Button type="submit" form="kui-form-transferencia" loading={ocupado}>
              {editando ? 'Guardar' : 'Transferir'}
            </Button>
          </>
        )
      }
    >
      <form id="kui-form-transferencia" className="kui-form" onSubmit={guardar} noValidate>
        {errorEnvio && <Alert tone="danger">{errorEnvio}</Alert>}
        {cuentas.isError && <Alert tone="danger">{mensajeErrorCuentas(cuentas.error)}</Alert>}
        <div className="kui-form__row">
          <Select
            label="Desde"
            value={origen}
            onChange={(e) => setOrigen(e.target.value)}
            options={opciones('Elige una cuenta', '')}
            error={ver(errorOrigen)}
          />
          <Select
            label="Hacia"
            value={destino}
            onChange={(e) => setDestino(e.target.value)}
            options={opciones('Elige una cuenta', origen)}
            error={ver(errorDestino)}
          />
        </div>
        <div className="kui-form__row">
          <Input
            label="Importe (€)"
            inputMode="decimal"
            placeholder="0,00"
            autoFocus
            value={importe}
            onChange={(e) => setImporte(e.target.value)}
            error={errorImporte} validarAlSalir
          />
          <Input label="Fecha" type="date" max={hoy} value={fecha} onChange={(e) => setFecha(e.target.value)} error={errorFecha} validarAlSalir />
        </div>
        <Input label="Concepto" placeholder="Opcional" maxLength={255} value={concepto} onChange={(e) => setConcepto(e.target.value)} />
        <p className="kui-pistas">Una transferencia mueve saldo entre tus cuentas: no cuenta como ingreso ni como gasto.</p>
      </form>
    </Dialog>
  );
}
