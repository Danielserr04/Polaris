export interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /** Lucide icon name */
  icon: string;
  /** Accessible label (also used as title) */
  label: string;
  variant?: 'ghost' | 'outline' | 'solid';
  size?: 'sm' | 'md';
  /** Toggle state — tints the icon with the accent */
  pressed?: boolean;
}
export declare function IconButton(props: IconButtonProps): JSX.Element;
