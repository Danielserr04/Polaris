export interface RingChartProps {
  value?: number;
  max?: number;
  size?: number;
  thickness?: number;
  color?: string;
  /** Centre text; defaults to the percentage */
  label?: React.ReactNode;
  sublabel?: React.ReactNode;
  children?: React.ReactNode;
}
export declare function RingChart(props: RingChartProps): JSX.Element;
