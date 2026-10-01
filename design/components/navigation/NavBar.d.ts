export interface NavItem { id: string; label: React.ReactNode; icon?: string; /** Module colour for the active pill */ color?: string; }
/**
 * Floating top "island" navigation — brand mark, module tabs with a spring-sliding pill, ⌘K search and avatar.
 * @startingPoint section="Navigation" subtitle="Floating island navbar" viewport="900x120"
 */
export interface NavBarProps {
  items?: NavItem[];
  value?: string;
  onChange?: (id: string) => void;
  onBrand?: () => void;
  /** Pass null to hide the search trigger */
  onSearch?: (() => void) | null;
  user?: { name: string; src?: string; onClick?: () => void };
  style?: React.CSSProperties;
}
export declare function NavBar(props: NavBarProps): JSX.Element;
