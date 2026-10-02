import { useEffect, useRef, type KeyboardEvent } from 'react';
import { tieneProgreso, type EntradaList, type TipoContenido } from '../../api/odisea';
import { ListHeader, ListRow } from '../../design-system';

interface Props {
  entradas: EntradaList[];
  seleccionada: number | undefined;
  onSeleccionar: (id: number) => void;
  /** Mensaje cuando no hay filas (filtros sin resultados o lista vacía) */
  vacio: string;
}

/**
 * Lista densa. Es un único elemento enfocable: con el foco dentro, las flechas, Inicio y Fin
 * mueven la selección (y con ella la ficha), igual que un listbox de una sola tabulación.
 */
/** En series y libros a medias, por donde vas pesa mas que la duracion. */
function textoProgreso(tipo: TipoContenido, progreso: number | null): string | null {
  if (progreso == null || !tieneProgreso(tipo)) return null;
  return tipo === 'LIBRO' ? `pág. ${progreso.toLocaleString('es-ES')}` : `ep. ${progreso}`;
}

export function ListaEntradas({ entradas, seleccionada, onSeleccionar, vacio }: Props) {
  const caja = useRef<HTMLDivElement>(null);

  // Con el teclado la fila elegida puede quedar fuera de la vista.
  useEffect(() => {
    caja.current?.querySelector('.pl-row--selected')?.scrollIntoView({ block: 'nearest' });
  }, [seleccionada]);

  const alPulsar = (ev: KeyboardEvent<HTMLDivElement>) => {
    if (!entradas.length) return;
    const i = entradas.findIndex((e) => e.id === seleccionada);
    let destino: number | null = null;
    if (ev.key === 'ArrowDown') destino = Math.min(entradas.length - 1, i + 1);
    else if (ev.key === 'ArrowUp') destino = Math.max(0, i - 1);
    else if (ev.key === 'Home') destino = 0;
    else if (ev.key === 'End') destino = entradas.length - 1;
    if (destino === null) return;
    ev.preventDefault();
    onSeleccionar(entradas[destino].id);
  };

  return (
    <div
      ref={caja}
      className="listbox pl-rise"
      style={{ animationDelay: '180ms' }}
      role="grid"
      aria-label="Tu lista"
      tabIndex={0}
      onKeyDown={alPulsar}
    >
      <ListHeader columns={['', 'Título', 'Tipo', 'Año', 'Detalle', 'Valoración', 'Estado']} />
      {entradas.map((e, i) => (
        <ListRow
          key={e.id}
          index={Math.min(i, 14)}
          titulo={e.tituloTitulo}
          tipo={e.tituloTipo}
          tituloOriginal={e.tituloOriginal ?? undefined}
          anio={e.tituloAnio ?? undefined}
          duracionMin={e.tituloDuracionMin ?? undefined}
          progreso={textoProgreso(e.tituloTipo, e.progreso)}
          estado={e.estado}
          valoracion={e.valoracion}
          favorito={e.favorito}
          selected={e.id === seleccionada}
          onClick={() => onSeleccionar(e.id)}
        />
      ))}
      {!entradas.length && (
        <div className="cmd__empty" style={{ padding: 32 }}>
          {vacio}
        </div>
      )}
    </div>
  );
}
