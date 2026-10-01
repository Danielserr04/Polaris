export interface SegmentedOption { value: string; label: React.ReactNode; icon?: string; count?: number; }
export interface SegmentedControlProps {
  options?: SegmentedOption[];
  value?: string;
  onChange?: (value: string) => void;
  style?: React.CSSProperties;
}
export declare function SegmentedControl(props: SegmentedControlProps): JSX.Element;
