export interface AvatarProps {
  src?: string;
  /** Used for initials fallback and alt text */
  name?: string;
  size?: number;
  style?: React.CSSProperties;
}
export declare function Avatar(props: AvatarProps): JSX.Element;
