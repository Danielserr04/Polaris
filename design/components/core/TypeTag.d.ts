export interface TypeTagProps {
  /** Odisea content type, exactly as the API returns it */
  tipo?: 'PELICULA' | 'SERIE' | 'JUEGO' | 'LIBRO';
  showLabel?: boolean;
  size?: number;
}
export declare function TypeTag(props: TypeTagProps): JSX.Element;
