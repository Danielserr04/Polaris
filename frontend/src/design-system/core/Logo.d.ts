export interface LogoProps {
  /** full = mark + wordmark · mark = block star only · wordmark = type only */
  variant?: 'full' | 'mark' | 'wordmark';
  /** Mark size in px (wordmark scales from it) */
  size?: number;
  wordmarkSize?: number;
  /** Override block colour (defaults to current --accent) */
  color?: string;
  style?: React.CSSProperties;
}
export declare function Logo(props: LogoProps): JSX.Element;
