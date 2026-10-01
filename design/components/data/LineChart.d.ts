export interface LineSeries { name: string; points: number[]; color?: string; /** Dashed reference series (objective) */ dashed?: boolean; }
/**
 * Line chart that draws itself in on mount; first series gets a soft area fill and an end-point dot.
 * @startingPoint section="Data" subtitle="Trend line with objective" viewport="700x280"
 */
export interface LineChartProps {
  series?: LineSeries[];
  /** X-axis labels, spread evenly (need not match point count) */
  labels?: string[];
  height?: number;
  min?: number;
  max?: number;
  area?: boolean;
  format?: (v: number) => string;
  gridLines?: number;
  showAxis?: boolean;
  showLegend?: boolean;
}
export declare function LineChart(props: LineChartProps): JSX.Element;
