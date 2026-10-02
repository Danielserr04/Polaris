/**
 * Text field with label, hint, error and a "locked" read-only treatment for immutable data (e.g. username).
 * @startingPoint section="Forms" subtitle="Text, search, error & locked fields" viewport="700x300"
 */
export interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: React.ReactNode;
  hint?: React.ReactNode;
  /** Error message — turns the border red */
  error?: React.ReactNode;
  /** Leading Lucide icon name (e.g. "search") */
  icon?: string;
  /** Trailing slot (Kbd, IconButton, unit). With type="password" a show/hide toggle is added automatically. */
  trailing?: React.ReactNode;
  /** Fixed value: dashed border + lock icon, read-only. Not the same as disabled. */
  locked?: boolean;
  size?: 'sm' | 'md';
}
export declare function Input(props: InputProps): JSX.Element;
export declare function Kbd(props: { children?: React.ReactNode }): JSX.Element;
