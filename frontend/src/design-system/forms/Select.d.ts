export interface SelectProps extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'size'> {
  label?: React.ReactNode;
  hint?: React.ReactNode;
  /** Error message — turns the border red */
  error?: React.ReactNode;
  options?: Array<string | { value: string; label: string }>;
  icon?: string;
  size?: 'sm' | 'md';
}
export declare function Select(props: SelectProps): JSX.Element;
