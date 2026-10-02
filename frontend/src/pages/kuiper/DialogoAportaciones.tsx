import { useMemo, useState, type FormEvent } from 'react';
import {
  mensajeErrorMeta,
  useAportaciones,
  useAportar,
  useBorrarAportacion,
  type MetaAhorro,
} from '../../api/kuiperMetas';
import { useRestaurarFoco } from '../../components/useRestaurarFoco';
import { Alert, Button, Dialog, IconButton, Input, ProgressBar, SegmentedControl } from '../../design-system';
import { eur, iso, relativa } from '../../lib/fechas';

interface Props {
  /** La meta con los datos frescos del listado: tras aportar se ve el total nuevo. */
  meta: MetaAhorro;
  onClose: () => void;
}

type Sentido = 'aportar' | 'retirar';

const IMPORTE = /^\d{1,8}([.,]\d{1,2})?$/;

/** Aportar o retirar dinero de una meta, con su historial (borrable linea a linea). */
export function DialogoAportaciones({ meta, onClose }: Props) {
  useRestaurarFoco();
  const hoy = useMemo(() => iso(new Date()), []);
  const historial = useAportaciones(meta.id);
  const aportar = useAportar(meta.id);
  const borrar = useBorrarAportacion(meta.id);

  const [sentido, setSentido] = useState<Sentido>('aportar');
  const [importe, setImporte] = useState('');
  const [fecha, setFecha] = useState(hoy);
  const [nota, setNota] = useState('');
  const [error, setError] = useState<string | null>(null);

  const importeNum = IMPORTE.test(importe.trim()) ? Number(importe.trim().replace(',', '.')) : NaN;
  const errorImporte = !(importeNum > 0)
    ? 'Escribe un importe mayor que 0, con hasta 2 decimales.'
    : sentido === 'retirar' && importeNum > meta.importeActual
      ? `Solo hay ${eur(meta.importeActual)} ahorrados.`
      : null;
  const errorFecha = fecha === '' ? 'Elige una fecha.' : fecha > hoy ? 'La fecha no puede ser futura.' : null;

  const enviar = async (ev: FormEvent) => {
    ev.preventDefault();
    setError(null);
    if (errorImporte || errorFecha) return;
    try {
      await aportar.mutateAsync({
        importe: sentido === 'retirar' ? -importeNum : importeNum,
        fecha,
        nota: nota.trim() === '' ? null : nota.trim(),
      });
      setImporte('');
      setNota('');
    } catch (e) {
      setError(mensajeErrorMeta(e));
    }
  };

  const borrarLinea = (id: number) => {
    setError(null);
    borrar.mutate(id, { onError: (e) => setError(mensajeErrorMeta(e)) });
  };

  return (
    <Dialog
      open
      onClose={onClose}
      title={meta.nombre}
      width={520}
      footer={
        <Button variant="ghost" type="button" onClick={onClose}>
          Cerrar
        </Button>
      }
    >
      <div className="kui-form">
        <ProgressBar
          label={`${eur(meta.importeActual)} de ${eur(meta.importeObjetivo)}`}
          value={Math.min(meta.importeActual, meta.importeObjetivo)}
          max={meta.importeObjetivo}
          color={meta.color ?? undefined}
          valueLabel={`${meta.porcentaje.toLocaleString('es-ES', { maximumFractionDigits: 1 })} %`}
          size="lg"
        />
        {meta.ahorroMensualNecesario !== null && (
          <p className="kui-pistas">
            Para llegar a tiempo: {eur(meta.ahorroMensualNecesario)} al mes.
          </p>
        )}

        <form className="kui-form" onSubmit={enviar} noValidate>
          {error && <Alert tone="danger">{error}</Alert>}
          <SegmentedControl
            value={sentido}
            onChange={(v) => setSentido(v as Sentido)}
            options={[
              { value: 'aportar', label: 'Aportar', icon: 'plus' },
              { value: 'retirar', label: 'Retirar', icon: 'minus' },
            ]}
          />
          <div className="kui-form__row">
            <Input
              label="Importe (€)"
              inputMode="decimal"
              autoFocus
              placeholder="100"
              value={importe}
              onChange={(e) => setImporte(e.target.value)}
              error={errorImporte} validarAlSalir
            />
            <Input
              label="Fecha"
              type="date"
              max={hoy}
              value={fecha}
              onChange={(e) => setFecha(e.target.value)}
              error={errorFecha} validarAlSalir
            />
          </div>
          <Input label="Nota" maxLength={255} placeholder="Opcional" value={nota} onChange={(e) => setNota(e.target.value)} />
          <Button type="submit" loading={aportar.isPending} variant={sentido === 'retirar' ? 'danger' : 'primary'}>
            {sentido === 'retirar' ? 'Retirar' : 'Aportar'}
          </Button>
        </form>

        <div>
          <span className="kui-field__label">Historial</span>
          {historial.isError ? (
            <Alert tone="danger">{mensajeErrorMeta(historial.error)}</Alert>
          ) : historial.isPending ? (
            <p className="muted">Cargando…</p>
          ) : historial.data.length === 0 ? (
            <p className="kui-pistas">Aún no hay aportaciones.</p>
          ) : (
            <ul className="kui-aportaciones">
              {historial.data.map((a) => (
                <li key={a.id} className="kui-aportacion">
                  <span className="muted kui-aportacion__fecha">{relativa(a.fecha)}</span>
                  <span className="kui-aportacion__nota">{a.nota ?? (a.importe < 0 ? 'Retirada' : 'Aportación')}</span>
                  <span className={'money kui-aportacion__importe' + (a.importe < 0 ? ' kui-aportacion__importe--neg' : '')}>
                    {a.importe > 0 ? '+' : '−'}
                    {eur(Math.abs(a.importe))}
                  </span>
                  <IconButton
                    icon="x"
                    size="sm"
                    label="Borrar esta línea"
                    disabled={borrar.isPending}
                    onClick={() => borrarLinea(a.id)}
                  />
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </Dialog>
  );
}
