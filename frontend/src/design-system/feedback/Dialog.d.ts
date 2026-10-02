export interface DialogProps {
  open?: boolean;
  title?: React.ReactNode;
  children?: React.ReactNode;
  footer?: React.ReactNode;
  /** Called on Esc, scrim click and close button */
  onClose?: () => void;
  width?: number;
  /** If something was typed, Esc / scrim / close button ask before discarding it. The footer's own Cancel is not affected. */
  confirmarDescarte?: boolean;
}
export declare function Dialog(props: DialogProps): JSX.Element | null;
