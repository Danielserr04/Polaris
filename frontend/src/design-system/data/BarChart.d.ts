export interface BarDatum { label: string; value: number; color?: string; highlight?: boolean; }
/**
 * Vertical bar chart with staggered grow-in, hover values and an optional dashed target line.
 * @startingPoint section="Data" subtitle="Bar chart with budget line" viewport="700x260"
 */
export interface BarChartProps {
  data?: BarDatum[];
  height?: number;
  max?: number;
  /** Dashed reference line (budget, objective) */
  target?: number;
  targetLabel?: string;
  /** Index or label of the bar to paint in full accent */
  highlight?: number | string;
  format?: (v: number) => string;
  gap?: number;
  showAxis?: boolean;
  gridLines?: number;
}
export declare function BarChart(props: BarChartProps): JSX.Element;
