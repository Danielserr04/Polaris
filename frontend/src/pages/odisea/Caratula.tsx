import { useEffect, useState } from 'react';
import { TypeTag } from '../../design-system';
import type { TipoContenido } from '../../api/odisea';

interface Props {
  titulo: string;
  tipo: TipoContenido;
  anio?: number | null;
  imagenUrl?: string | null;
  big?: boolean;
}

/** Carátula real si hay URL y carga; si no, el placeholder tipográfico del diseño. */
export function Caratula({ titulo, tipo, anio, imagenUrl, big }: Props) {
  const [rota, setRota] = useState(false);
  useEffect(() => setRota(false), [imagenUrl]);

  if (imagenUrl && !rota) {
    return (
      <div className={'cover cover--img' + (big ? ' cover--big' : '')}>
        <img src={imagenUrl} alt={`Carátula de ${titulo}`} loading="lazy" onError={() => setRota(true)} />
      </div>
    );
  }
  return (
    <div className={'cover' + (big ? ' cover--big' : '')} role="img" aria-label={`Sin carátula: ${titulo}`}>
      <span className="cover__star">✦</span>
      <TypeTag tipo={tipo} showLabel={false} size={big ? 18 : 12} />
      <b>{titulo}</b>
      <span>{anio || '—'}</span>
    </div>
  );
}
