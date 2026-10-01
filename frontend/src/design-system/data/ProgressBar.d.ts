export interface ProgressBarProps {
  value?: number;
  max?: number;
  label?: React.ReactNode;
  /** Right-hand text; defaults to "value / max". false hides it */
  valueLabel?: React.ReactNode | false;
  /** Marker line (e.g. where you "should" be today) */
  target?: number;
  color?: string;
  size?: 'md' | 'lg';
  style?: React.CSSProperties;
}
export declare function ProgressBar(props: ProgressBarProps): JSX.Element;
