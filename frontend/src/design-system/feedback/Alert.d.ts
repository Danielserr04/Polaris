export interface AlertProps {
  tone?: 'info' | 'success' | 'warning' | 'danger' | 'accent';
  title?: React.ReactNode;
  children?: React.ReactNode;
  /** Right-aligned action (usually a small secondary Button) */
  action?: React.ReactNode;
  /** Override Lucide icon */
  icon?: string;
  style?: React.CSSProperties;
}
export declare function Alert(props: AlertProps): JSX.Element;
