import React from 'react';
import { StateGlyph } from '../core/StatusBadge.jsx';
import { StatusBadge } from '../core/StatusBadge.jsx';
import { TypeTag } from '../core/TypeTag.jsx';
import { Rating } from './Rating.jsx';
const unit = (tipo, d) => d == null ? null : tipo === 'LIBRO' ? d + ' pág' : d + ' min';
export function ListRow({ titulo, tituloOriginal, tipo, anio, duracionMin, estado, valoracion, favorito, selected, dense, index = 0, onClick }) {
  const empty = <span className="pl-row__empty">—</span>;
  return (
    <div role="row" className={'pl-row' + (selected ? ' pl-row--selected' : '') + (dense ? ' pl-row--dense' : '')} style={{ animationDelay: index * 35 + 'ms' }} onClick={onClick}>
      <StateGlyph estado={estado} />
      <span className="pl-row__title"><span>{titulo}{tituloOriginal && tituloOriginal !== titulo && <span className="pl-row__sub"> · {tituloOriginal}</span>}</span>{favorito && <span className="pl-row__fav">✦</span>}</span>
      <TypeTag tipo={tipo} />
      <span className="pl-row__num">{anio || empty}</span>
      <span className="pl-row__num">{unit(tipo, duracionMin) || empty}</span>
      {valoracion != null ? <Rating value={valoracion} size={11} showValue={false} /> : empty}
      <StatusBadge estado={estado} variant="dot" />
    </div>
  );
}
export function ListHeader({ columns = ['', 'Título', 'Tipo', 'Año', 'Duración', 'Valoración', 'Estado'] }) {
  return (
    <div className="pl-row" style={{ height: 32, cursor: 'default', animation: 'none', background: 'none' }}>
      {columns.map((c, i) => <span key={i} className="pl-eyebrow" style={{ fontSize: 10 }}>{c}</span>)}
    </div>
  );
}
