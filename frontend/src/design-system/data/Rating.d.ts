export interface RatingProps {
  /** 0–10 (the API scale). Rendered as five stars with half steps. null = unrated */
  value?: number | null;
  /** Makes it interactive; returns 1–10 */
  onChange?: (value: number) => void;
  size?: number;
  showValue?: boolean;
}
export declare function Rating(props: RatingProps): JSX.Element;
