export interface ToastProps {
  tone?: 'accent' | 'success' | 'danger' | 'info';
  children?: React.ReactNode;
  action?: React.ReactNode;
  onClose?: () => void;
  /** Pin to bottom-centre of the viewport */
  fixed?: boolean;
  icon?: string;
}
export declare function Toast(props: ToastProps): JSX.Element;
