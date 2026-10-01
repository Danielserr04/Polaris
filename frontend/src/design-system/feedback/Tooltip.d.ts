export interface TooltipProps {
  label: React.ReactNode;
  children?: React.ReactNode;
  placement?: 'top' | 'bottom';
  /** Allow multi-line (max 260px) — for explanations like why an action is blocked */
  wrap?: boolean;
}
export declare function Tooltip(props: TooltipProps): JSX.Element;
