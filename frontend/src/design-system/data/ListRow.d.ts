/**
 * Dense Odisea list row: state glyph · title · type · year · duration · rating · state. Staggered rise-in by index.
 * @startingPoint section="Data" subtitle="Dense Odisea list with header" viewport="900x260"
 */
export interface ListRowProps {
  titulo: string;
  tituloOriginal?: string;
  tipo: 'PELICULA' | 'SERIE' | 'JUEGO' | 'LIBRO';
  anio?: number | null;
  /** Minutes for films/series, pages for books, null for games */
  duracionMin?: number | null;
  estado: 'PENDIENTE' | 'EN_CURSO' | 'TERMINADO' | 'ABANDONADO';
  /** 0–10 or null */
  valoracion?: number | null;
  favorito?: boolean;
  selected?: boolean;
  dense?: boolean;
  /** Position in the list — drives entrance stagger */
  index?: number;
  onClick?: () => void;
}
export declare function ListRow(props: ListRowProps): JSX.Element;
export declare function ListHeader(props: { columns?: string[] }): JSX.Element;
