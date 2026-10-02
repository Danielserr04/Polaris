import { useState } from 'react';
import { mensajeError } from '../../api/kuiper';
import {
  DIAS_EN_PAPELERA,
  useBorrarDefinitivo,
  usePapelera,
  useRestaurarMovimientos,
  useVaciarPapelera,
  type MovimientoPapelera,
} from '../../api/kuiperPapelera';
import { avisar } from '../../lib/avisos';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton } from '../../design-system';
import { diasEntre, eur, relativa } from '../../lib/fechas';

interface Props {
  onClose: () => void;
}

/** "Se borra en 12 días": cuenta desde borradoEn (fecha y hora local, sin zona). */
function caducidad(m: MovimientoPapelera): string {
  const quedan = DIAS_EN_PAPELERA - diasEntre(new Date(m.borradoEn), new Date());
  if (quedan <= 0) return 'Se borra hoy';
  return quedan === 1 ? 'Se borra mañana' : `Se borra en ${quedan} días`;
}

/** La papelera: restaurar, borrar para siempre uno a uno o vaciarla entera. */
export function PapeleraDialog({ onClose }: Props) {
  useRestaurarFoco();
  const papelera = usePapelera();
  const restaurar = useRestaurarMovimientos();
  const definitivo = useBorrarDefinitivo();
  const vaciar = useVaciarPapelera();
  const [confirmandoId, setConfirmandoId] = useState<number | null>(null);
  const [confirmandoVaciar, setConfirmandoVaciar] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const filas = papelera.data ?? [];
  const ocupado = restaurar.isPending || definitivo.isPending || vaciar.isPending;

  const alRestaurar = (m: MovimientoPapelera) => {
    setError(null);
    restaurar.mutate([m.id], {
      onSuccess: () => avisar('Movimiento restaurado.'),
      onError: (e) => setError(mensajeError(e)),
    });
  };

  const alBorrar = (m: MovimientoPapelera) => {
    if (confirmandoId !== m.id) {
      setConfirmandoId(m.id);
      return;
    }
    setError(null);
    definitivo.mutate(m.id, {
      onSuccess: () => setConfirmandoId(null),
      onError: (e) => setError(mensajeError(e)),
    });
  };

  const alVaciar = () => {
    setError(null);
    vaciar.mutate(undefined, {
      onSuccess: () => setConfirmandoVaciar(false),
      onError: (e) => setError(mensajeError(e)),
    });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      width={560}
      title="Papelera"
      footer={
        confirmandoVaciar ? (
          <>
            <span className="muted kui-form__borrar">¿Borrar para siempre {filas.length === 1 ? 'el movimiento' : `los ${filas.length} movimientos`}?</span>
            <Button variant="ghost" type="button" autoFocus onClick={() => setConfirmandoVaciar(false)}>
              No
            </Button>
            <Button variant="danger" type="button" loading={vaciar.isPending} onClick={alVaciar}>
              Sí, vaciar
            </Button>
          </>
        ) : (
          <>
            {filas.length > 0 && (
              <Button variant="ghost" type="button" className="kui-form__borrar" disabled={ocupado} onClick={() => setConfirmandoVaciar(true)}>
                Vaciar papelera
              </Button>
            )}
            <Button variant="ghost" type="button" onClick={onClose}>
              Cerrar
            </Button>
          </>
        )
      }
    >
      <p className="kui-pistas" style={{ marginBottom: 12 }}>
        Lo que borras se guarda aquí {DIAS_EN_PAPELERA} días y después desaparece solo. No cuenta en listados ni resúmenes.
      </p>
      {error && <Alert tone="danger">{error}</Alert>}
      {papelera.isError ? (
        <Alert tone="danger">{mensajeError(papelera.error)}</Alert>
      ) : papelera.isPending ? (
        <p className="muted" style={{ margin: 0 }}>Cargando…</p>
      ) : filas.length === 0 ? (
        <div className="kui-vacio" style={{ padding: '12px 0' }}>La papelera está vacía.</div>
      ) : (
        <ul className="kui-pap">
          {filas.map((m) => (
            <li key={m.id} className="kui-pap__r">
              <div className="kui-pap__txt">
                <b>{m.concepto || m.categoriaNombre}</b>
                <span className="muted">
                  {relativa(m.fecha)} · {m.categoriaNombre} · {caducidad(m)}
                </span>
              </div>
              <span className="money" style={{ color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)' }}>
                {m.tipo === 'INGRESO' ? '+' : '−'}
                {eur(m.importe)}
              </span>
              <div className="kui-pap__acc">
                <IconButton icon="rotate-ccw" label="Restaurar" size="sm" disabled={ocupado} onClick={() => alRestaurar(m)} />
                {confirmandoId === m.id ? (
                  <Button size="sm" variant="danger" loading={definitivo.isPending} onClick={() => alBorrar(m)}>
                    Borrar
                  </Button>
                ) : (
                  <IconButton icon="trash-2" label="Borrar para siempre" size="sm" disabled={ocupado} onClick={() => alBorrar(m)} />
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </Dialog>
  );
}
