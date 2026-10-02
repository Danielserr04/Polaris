import type { EstadoPresupuesto } from '../../api/kuiper';
import { colorEstado, etiquetaEstado, pctTexto, tonoEstado } from '../../api/kuiperAlertas';
import { Badge, Icon, ProgressBar } from '../../design-system';
import { eur } from '../../lib/fechas';
import { iconoOr } from '../../lib/iconos';

interface Props {
  nombre: string;
  icono: string | null;
  gastado: number;
  limite: number;
  porcentaje: number | null;
  porcentajeAlerta: number | null;
  estado: EstadoPresupuesto;
}

/**
 * Lo gastado frente al limite, coloreado por estado (bien, aviso, excedido) y con una marca
 * donde salta el aviso.
 */
export function BarraPresupuesto({ nombre, icono, gastado, limite, porcentaje, porcentajeAlerta, estado }: Props) {
  const exceso = gastado - limite;
  return (
    <div className="kui-barra" data-estado={estado}>
      <span className="catic">
        <Icon name={iconoOr(icono)} size={15} />
      </span>
      <ProgressBar
        style={{ flex: 1 }}
        color={colorEstado(estado)}
        target={porcentajeAlerta !== null ? (limite * porcentajeAlerta) / 100 : undefined}
        label={
          <>
            {nombre}
            {estado === 'EXCEDIDO' ? (
              <Badge tone="danger" style={{ marginLeft: 8, height: 18 }}>
                +{eur(exceso, 0)}
              </Badge>
            ) : estado === 'AVISO' ? (
              <Badge tone={tonoEstado(estado)} style={{ marginLeft: 8, height: 18 }}>
                {etiquetaEstado(estado)}
              </Badge>
            ) : null}
          </>
        }
        value={gastado}
        max={limite || 1}
        valueLabel={
          <>
            {eur(gastado, 0)} / {eur(limite, 0)} · <span className={`kui-pct kui-pct--${estado.toLowerCase()}`}>{pctTexto(porcentaje)}</span>
          </>
        }
      />
    </div>
  );
}
