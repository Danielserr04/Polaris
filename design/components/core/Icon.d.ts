export interface IconProps {
  /** Lucide icon name in kebab-case, e.g. "search", "film", "gamepad-2" */
  name: string;
  size?: number;
  color?: string;
  title?: string;
  className?: string;
  style?: React.CSSProperties;
}
export declare function Icon(props: IconProps): JSX.Element;
