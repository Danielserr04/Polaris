import type { MovimientoList } from '../../api/kuiper';
import { eur, relativa } from '../../lib/fechas';

interface Props {
  movimientos: MovimientoList[];
  vacio: string;
  onEditar: (m: MovimientoList) => void;
}

/** Filas de movimientos: pulsar una abre su edicion. */
export function ListaMovimientos({ movimientos, vacio, onEditar }: Props) {
  if (!movimientos.length) return <div className="kui-vacio">{vacio}</div>;
  return (
    <div className="table">
      {movimientos.map((m, i) => (
        <button
          key={m.id}
          type="button"
          className="table__r table__r--mov pl-rise"
          style={{ animationDelay: Math.min(i, 12) * 30 + 'ms' }}
          onClick={() => onEditar(m)}
          aria-label={`Editar ${m.concepto || m.categoriaNombre}, ${eur(m.importe)}`}
        >
          <span className="pl-row__num">{relativa(m.fecha)}</span>
          <b>{m.concepto || m.categoriaNombre}</b>
          <span className="kui-cat" style={m.categoriaColor ? ({ ['--cat' as string]: m.categoriaColor }) : undefined}>
            <i />
            <span>{m.categoriaNombre}</span>
          </span>
          <span className="money" style={{ color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)' }}>
            {m.tipo === 'INGRESO' ? '+' : '−'}
            {eur(m.importe)}
          </span>
        </button>
      ))}
    </div>
  );
}
