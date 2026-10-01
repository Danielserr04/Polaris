export interface BadgeProps {
  tone?: 'neutral' | 'accent' | 'success' | 'warning' | 'danger' | 'info';
  variant?: 'soft' | 'solid' | 'outline' | 'plain';
  /** Custom tone colour (overrides tone) */
  color?: string;
  children?: React.ReactNode;
  style?: React.CSSProperties;
}
export declare function Badge(props: BadgeProps): JSX.Element;
