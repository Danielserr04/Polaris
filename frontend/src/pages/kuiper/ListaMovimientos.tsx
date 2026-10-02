import type { MovimientoList } from '../../api/kuiper';
import { Icon, IconButton } from '../../design-system';
import { eur, relativa } from '../../lib/fechas';
import './papelera.css';

interface Props {
  movimientos: MovimientoList[];
  vacio: string;
  onEditar: (m: MovimientoList) => void;
  /** Con seleccion, pulsar una fila la marca o desmarca en vez de abrir su edicion. */
  seleccion?: ReadonlySet<number>;
  onAlternar?: (id: number) => void;
  /** Si se pasa, cada fila lleva un boton "Duplicar" (fuera del modo seleccion). */
  onDuplicar?: (m: MovimientoList) => void;
}

/** Filas de movimientos: pulsar una abre su edicion (o la selecciona, en modo seleccion). */
export function ListaMovimientos({ movimientos, vacio, onEditar, seleccion, onAlternar, onDuplicar }: Props) {
  if (!movimientos.length) return <div className="kui-vacio">{vacio}</div>;
  const seleccionando = seleccion !== undefined && onAlternar !== undefined;
  const conAcciones = !seleccionando && onDuplicar !== undefined;
  return (
    <div className="table">
      {movimientos.map((m, i) => {
        const nombre = m.concepto || m.categoriaNombre;
        const marcado = seleccionando && seleccion.has(m.id);
        const fila = (
          <button
            key={m.id}
            type="button"
            className={
              'table__r table__r--mov pl-rise' +
              (seleccionando ? ' table__r--sel' : '') +
              (conAcciones ? ' table__r--acc' : '') +
              (marcado ? ' is-marcado' : '')
            }
            style={{ animationDelay: Math.min(i, 12) * 30 + 'ms' }}
            onClick={() => (seleccionando ? onAlternar(m.id) : onEditar(m))}
            aria-label={seleccionando ? `Seleccionar ${nombre}, ${eur(m.importe)}` : `Editar ${nombre}, ${eur(m.importe)}`}
            aria-pressed={seleccionando ? marcado : undefined}
          >
            {seleccionando && <span className="kui-check">{marcado && <Icon name="check" size={12} />}</span>}
            <span className="pl-row__num">{relativa(m.fecha)}</span>
            <b>{nombre}</b>
            <span className="kui-cat" style={m.categoriaColor ? ({ ['--cat' as string]: m.categoriaColor }) : undefined}>
              <i />
              <span>{m.categoriaNombre}</span>
            </span>
            <span className="money" style={{ color: m.tipo === 'INGRESO' ? 'var(--success)' : 'var(--text-1)' }}>
              {m.tipo === 'INGRESO' ? '+' : '−'}
              {eur(m.importe)}
            </span>
            {conAcciones && <span aria-hidden />}
          </button>
        );
        if (!conAcciones) return fila;
        // El boton de duplicar no puede ir dentro del de la fila (botones anidados): va encima de
        // su ultima celda, que la fila deja vacia.
        return (
          <div key={m.id} className="kui-movfila">
            {fila}
            <IconButton icon="copy" label={`Duplicar ${nombre} con fecha de hoy`} size="sm" className="kui-movfila__dup" onClick={() => onDuplicar(m)} />
          </div>
        );
      })}
    </div>
  );
}
