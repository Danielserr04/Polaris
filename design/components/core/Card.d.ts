/**
 * Surface container for widgets, detail blocks and forms.
 * @startingPoint section="Core" subtitle="Widget / detail card with eyebrow, title and action" viewport="700x300"
 */
export interface CardProps {
  /** String renders as <Eyebrow star>; pass a node for custom */
  eyebrow?: React.ReactNode;
  title?: React.ReactNode;
  /** Top-right slot (IconButton, Badge, Button) */
  action?: React.ReactNode;
  footer?: React.ReactNode;
  /** Lifts with an accent block-shadow on hover */
  interactive?: boolean;
  variant?: 'default' | 'raised' | 'sunken';
  /** Body padding override */
  padding?: number | string;
  /** Stagger-entrance delay in ms (enables rise animation) */
  delay?: number;
  onClick?: () => void;
  className?: string;
  style?: React.CSSProperties;
  children?: React.ReactNode;
}
export declare function Card(props: CardProps): JSX.Element;
