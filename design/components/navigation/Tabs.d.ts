export interface TabItem { value: string; label: React.ReactNode; icon?: string; }
export interface TabsProps {
  items?: TabItem[];
  value?: string;
  onChange?: (value: string) => void;
  style?: React.CSSProperties;
}
export declare function Tabs(props: TabsProps): JSX.Element;
