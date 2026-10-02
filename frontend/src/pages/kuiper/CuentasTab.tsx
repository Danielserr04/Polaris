import { useMemo, useState } from 'react';
import {
  etiquetaTipoCuenta,
  mensajeErrorCuentas,
  useArchivarCuenta,
  useCuentas,
  usePatrimonio,
  useTransferencias,
  type CuentaList,
  type TransferenciaList,
} from '../../api/kuiperCuentas';
import { Alert, Badge, Button, Card, Icon, Stat, Switch } from '../../design-system';
import { etiquetaMes, eur, rangoMes, relativa } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';
import { FormularioCuenta } from './FormularioCuenta';
import { FormularioTransferencia } from './FormularioTransferencia';
import './cuentas.css';

interface Props {
  /** Periodo YYYY-MM: las transferencias que se listan son las de ese mes. Los saldos son siempre los de hoy. */
  periodo: string;
}

type CuentaAbierta = { id?: number } | null;
type TransferenciaAbierta = { transferencia?: TransferenciaList; origen?: number } | null;

export function CuentasTab({ periodo }: Props) {
  const { desde, hasta } = rangoMes(periodo);
  const [verArchivadas, setVerArchivadas] = useState(false);
  const [cuentaAbierta, setCuentaAbierta] = useState<CuentaAbierta>(null);
  const [trfAbierta, setTrfAbierta] = useState<TransferenciaAbierta>(null);
  const [errorArchivo, setErrorArchivo] = useState<string | null>(null);
  const cuentas = useCuentas();
  const patrimonio = usePatrimonio();
  const transferencias = useTransferencias({ desde, hasta });
  const archivar = useArchivarCuenta();

  const activas = useMemo(() => (cuentas.data ?? []).filter((c) => !c.archivada), [cuentas.data]);
  const archivadas = (cuentas.data ?? []).length - activas.length;
  const visibles = verArchivadas ? (cuentas.data ?? []) : activas;
  const transferido = (transferencias.data ?? []).reduce((s, t) => s + t.importe, 0);

  const cambiarArchivo = (c: CuentaList) => {
    setErrorArchivo(null);
    archivar.mutate({ id: c.id, archivada: !c.archivada }, { onError: (e) => setErrorArchivo(mensajeErrorCuentas(e)) });
  };

  if (cuentas.isError || patrimonio.isError) {
    return (
      <Alert
        tone="danger"
        title="No se han podido cargar las cuentas"
        action={
          <Button size="sm" variant="secondary" onClick={() => { void cuentas.refetch(); void patrimonio.refetch(); }}>
            Reintentar
          </Button>
        }
      >
        {mensajeErrorCuentas(cuentas.error ?? patrimonio.error)}
      </Alert>
    );
  }
  if (!cuentas.data || !patrimonio.data) return <p className="muted">Cargando…</p>;

  return (
    <>
      <div className="stats kui-cuentas__stats pl-rise">
        <Stat label="Patrimonio" value={patrimonio.data.total} decimals={2} unit="€" caption="todas tus cuentas, hoy" />
        <Stat
          label="Cuentas activas"
          value={activas.length}
          caption={archivadas ? `${archivadas} ${archivadas === 1 ? 'archivada' : 'archivadas'}` : undefined}
        />
        <Stat label="Transferido" value={transferido} decimals={2} unit="€" caption={etiquetaMes(periodo).toLowerCase()} />
      </div>

      <div className="kui-toolbar">
        {archivadas > 0 && <Switch label="Ver archivadas" checked={verArchivadas} onChange={setVerArchivadas} />}
        <div className="kui-cuentas__acciones">
          <Button variant="secondary" icon="repeat" disabled={activas.length < 2} onClick={() => setTrfAbierta({})}>
            Transferencia
          </Button>
          <Button variant="secondary" icon="plus" onClick={() => setCuentaAbierta({})}>
            Cuenta
          </Button>
        </div>
      </div>

      {errorArchivo && <Alert tone="danger">{errorArchivo}</Alert>}

      {visibles.length === 0 ? (
        <Card>
          <div className="kui-vacio" style={{ padding: 0 }}>
            Aún no tienes cuentas. Crea una para ver su saldo y asignarle movimientos; los movimientos sin cuenta siguen
            contando en el resumen.
          </div>
        </Card>
      ) : (
        <div className="kui-cuentas">
          {visibles.map((c, i) => (
            <Card key={c.id} delay={Math.min(i, 12) * 40} className={c.archivada ? 'kui-cuenta kui-cuenta--archivada' : 'kui-cuenta'}>
              <div className="kui-cuenta__head">
                <span className="kui-catchip" style={c.color ? ({ ['--c' as string]: c.color }) : undefined}>
                  <Icon name={iconoOr(c.icono)} size={16} />
                </span>
                <div className="kui-cuenta__id">
                  <b>{c.nombre}</b>
                  <span className="muted">
                    {etiquetaTipoCuenta(c.tipo)}
                    {c.banco ? ` · ${c.banco}` : ''}
                  </span>
                </div>
                {c.archivada && <Badge tone="neutral">Archivada</Badge>}
              </div>
              <div className="money kui-cuenta__saldo" style={c.saldoActual < 0 ? { color: 'var(--danger)' } : undefined}>
                {c.saldoActual < 0 ? '−' : ''}
                {eur(Math.abs(c.saldoActual))}
              </div>
              <div className="kui-cuenta__botones">
                <Button size="sm" variant="ghost" onClick={() => setCuentaAbierta({ id: c.id })} aria-label={`Editar ${c.nombre}`}>
                  Editar
                </Button>
                {!c.archivada && activas.length > 1 && (
                  <Button size="sm" variant="ghost" onClick={() => setTrfAbierta({ origen: c.id })} aria-label={`Transferir desde ${c.nombre}`}>
                    Transferir
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="ghost"
                  loading={archivar.isPending && archivar.variables?.id === c.id}
                  onClick={() => cambiarArchivo(c)}
                  aria-label={`${c.archivada ? 'Reactivar' : 'Archivar'} ${c.nombre}`}
                >
                  {c.archivada ? 'Reactivar' : 'Archivar'}
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Card delay={120} eyebrow="Entre cuentas" title={`Transferencias · ${etiquetaMes(periodo)}`} padding="8px 0 0" style={{ marginTop: 16 }}>
        {transferencias.isError ? (
          <div className="kui-vacio">{mensajeErrorCuentas(transferencias.error)}</div>
        ) : transferencias.isPending ? (
          <div className="kui-vacio">Cargando…</div>
        ) : transferencias.data.length === 0 ? (
          <div className="kui-vacio">Sin transferencias este mes.</div>
        ) : (
          <div className="table">
            {transferencias.data.map((t, i) => (
              <button
                key={t.id}
                type="button"
                className="table__r table__r--trf pl-rise"
                style={{ animationDelay: Math.min(i, 12) * 30 + 'ms' }}
                onClick={() => setTrfAbierta({ transferencia: t })}
                aria-label={`Editar transferencia de ${t.cuentaOrigenNombre} a ${t.cuentaDestinoNombre}, ${eur(t.importe)}`}
              >
                <span className="pl-row__num">{relativa(t.fecha)}</span>
                <span className="kui-trf__ruta">
                  <b>{t.cuentaOrigenNombre}</b>
                  <Icon name="arrow-right" size={14} />
                  <b>{t.cuentaDestinoNombre}</b>
                </span>
                <span className="muted kui-trf__concepto">{t.concepto ?? ''}</span>
                <span className="money">{eur(t.importe)}</span>
              </button>
            ))}
          </div>
        )}
      </Card>

      {cuentaAbierta && <FormularioCuenta cuentaId={cuentaAbierta.id} onClose={() => setCuentaAbierta(null)} />}
      {trfAbierta && (
        <FormularioTransferencia
          transferencia={trfAbierta.transferencia}
          origenInicial={trfAbierta.origen}
          onClose={() => setTrfAbierta(null)}
        />
      )}
    </>
  );
}
