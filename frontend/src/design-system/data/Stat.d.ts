export interface StatProps {
  label?: React.ReactNode;
  /** Numbers count up on mount */
  value: number | string;
  unit?: string;
  decimals?: number;
  /** e.g. "+0,4 kg" or "-12 %" — sign picks the colour unless deltaTone is set */
  delta?: React.ReactNode;
  deltaTone?: 'up' | 'down' | 'flat';
  caption?: React.ReactNode;
  /** Font size of the number in px */
  size?: number;
  style?: React.CSSProperties;
}
export declare function Stat(props: StatProps): JSX.Element;
