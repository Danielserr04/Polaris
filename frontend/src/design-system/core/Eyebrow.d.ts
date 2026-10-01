export interface EyebrowProps {
  children?: React.ReactNode;
  /** Prefix a ✦ star in the accent colour */
  star?: boolean;
  /** Trailing coordinate-style note, e.g. "RA 02h 31m" or "SEP 2026" */
  coord?: React.ReactNode;
  style?: React.CSSProperties;
}
export declare function Eyebrow(props: EyebrowProps): JSX.Element;
