/**
 * Primary action control. Block-style: lifts with a hard offset shadow on hover.
 * @startingPoint section="Core" subtitle="Primary, secondary, ghost & danger buttons" viewport="700x260"
 */
export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  /** Lucide icon name shown before the label */
  icon?: string;
  iconRight?: string;
  loading?: boolean;
  block?: boolean;
  children?: React.ReactNode;
}
export declare function Button(props: ButtonProps): JSX.Element;
