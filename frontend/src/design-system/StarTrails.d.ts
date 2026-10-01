export interface StarTrailsProps {
  /** Polo celeste en coordenadas relativas [x, y] (0..1) */
  pole?: [number, number];
  /** Alternativa a `pole`: devuelve el ancla en pixeles */
  anchor?: () => [number, number];
  speed?: number;
  density?: number;
  /** Color de fondo "r,g,b" con el que se desvanece la estela */
  bg?: string;
}
export declare function StarTrails(props: StarTrailsProps): JSX.Element;
